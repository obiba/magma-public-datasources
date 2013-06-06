/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.datasource.healthcanada;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.Values;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import au.com.bytecode.opencsv.CSVReader;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

/**
 * Extract the drugs about human from the DPD files.
 * 
 * @see http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/dpd_bdpp_data_extract-eng.php
 * @see http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/dpd_read_me-bdpp_lisez-moi-eng.php
 */
public class MedicationsBuilder {

  private static final Logger log = LoggerFactory.getLogger(MedicationsBuilder.class);

  private static final String ALL_FILES_ZIP_URL = "http://www.hc-sc.gc.ca/dhp-mps/alt_formats/zip/prodpharma/databasdon/allfiles.zip";

  private static final String DEFAULT_DRUGS_TABLE_NAME = "Drugs";

  private static final Charset WESTERN_EUROPE = Charset.availableCharsets().get("ISO-8859-1");

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

  private Map<String, String[]> drug;

  private Map<String, String[]> comp;

  private Map<String, String[]> form;

  private Map<String, String[]> ingred;

  private Map<String, String[]> pack;

  private Map<String, String[]> pharm;

  private Map<String, String[]> route;

  private Map<String, String[]> schedule;

  private Map<String, String[]> status;

  private Map<String, String[]> ther;

  private Map<String, String[]> indications;

  private List<String> noIndications;

  private View view;

  private java.io.File csvDestinationFile;

  private MedicationsBuilder() {
    super();
  }

  /**
   * Download latest data extract from Health Canada.
   * @return
   * @throws IOException
   */
  public static MedicationsBuilder fromLatest() throws IOException {
    return from(downloadLatestAllFiles());
  }

  /**
   * Use Health Canada data extract from provided file (all files zip).
   * @param source
   * @return
   * @throws IOException
   */
  public static MedicationsBuilder from(java.io.File source) throws IOException {
    MedicationsBuilder builder = new MedicationsBuilder();
    builder.readTables(source);
    return builder;
  }

  /**
   * With mapping between ingredients and indications (csv file).
   * @param indicationSource
   * @return
   * @throws IOException
   */
  public MedicationsBuilder indications(java.io.File indicationSource) throws IOException {
    indications = readIndicationsTable(indicationSource);
    return this;
  }

  public MedicationsBuilder noIndications(java.io.File noIndicationSource) throws IOException {
    noIndications = readNoIndicationsTable(noIndicationSource);
    return this;
  }

  /**
   * Write to datasource file (type is guessed from file suffix) in table with default name.
   * @param destination
   * @return
   * @throws IOException
   */
  public MedicationsBuilder to(java.io.File destination) throws IOException {
    return to(destination, DEFAULT_DRUGS_TABLE_NAME);
  }

  /**
   * Write to datasource file (type is guessed from file suffix) in table with given name.
   * @param destination
   * @param tableName
   * @return
   * @throws IOException
   */
  public MedicationsBuilder to(java.io.File destination, String tableName) throws IOException {
    if(destination.exists()) {
      destination.delete();
    }

    DatasourceFactory factory = null;
    if(destination.getName().toLowerCase().endsWith(".xlsx")) {
      ExcelDatasourceFactory xlFactory = new ExcelDatasourceFactory();
      xlFactory.setFile(destination);
      factory = xlFactory;
    }

    if(factory == null) throw new IllegalArgumentException("Unidentified datasource type from file name: " + destination.getAbsolutePath());

    factory.setName("medications");
    to(factory, tableName);
    log.info("Done: {}", destination.getAbsolutePath());
    return this;
  }

  /**
   * Write to datasource (created from provided factory) in table with given name.
   * @param factory
   * @param tableName
   * @return
   * @throws IOException
   */
  public MedicationsBuilder to(DatasourceFactory factory, String tableName) throws IOException {
    Datasource datasource = factory.create();
    Initialisables.initialise(datasource);
    to(datasource, tableName);
    Disposables.dispose(datasource);

    if(view != null && csvDestinationFile != null) {
      log.info("Persisting view '{}' to {}", view.getName(), csvDestinationFile.getAbsolutePath());
      datasource = factory.create();
      Initialisables.initialise(datasource);
      copyView(datasource, tableName);
      Disposables.dispose(datasource);
    }

    return this;
  }

  /**
   * Write to provided datasource in table with given name.
   * @param datasource
   * @param tableName
   * @return
   * @throws IOException
   */
  public MedicationsBuilder to(Datasource datasource, String tableName) throws IOException {
    writeValueSets(datasource, tableName, writeVariables(datasource, tableName));

    // if(indications != null) {
    // // build a unique list of ingredients
    // List<String> ingredientNames = new ArrayList<String>();
    // for(String drug : ingred.keySet()) {
    // String[] line = ingred.get(drug);
    // ValueSequence sequence = getValueSequence(TextType.get(), line[2]).asSequence();
    // for(Value ing : sequence.getValues()) {
    // String ingName = ing.toString().toUpperCase();
    // if(ingredientNames.contains(ingName) == false) {
    // ingredientNames.add(ingName);
    // }
    // }
    // }
    // // then clear indications file
    // System.out.println("==== indications file cleared ====");
    // for(String ingredientIndic : indications.keySet()) {
    // String[] line = indications.get(ingredientIndic);
    // if(ingredientNames.contains(ingredientIndic.toUpperCase())) {
    // System.out.println(ingredientIndic.toUpperCase() + "," + line[1] + ",exact," + line[0].trim());
    // } else {
    // boolean contains = false;
    // for(String ingName : ingredientNames) {
    // if(ingName.contains(ingredientIndic.toUpperCase())) {
    // System.out.println(ingName + "," + line[1] + ",contains," + line[0].trim());
    // contains = true;
    // }
    // }
    // if(contains == false) {
    // System.out.println(line[0].trim() + "," + line[1] + ",unknown," + line[0].trim());
    // }
    // }
    // }
    // }

    return this;
  }

  private void copyView(Datasource datasource, String tableName) throws IOException {
    if(view == null) return;

    View viewSource = new View(view.getName(), datasource.getValueTable(tableName));
    viewSource.setListClause(view.getListClause());
    ViewAwareDatasource viewDs = new ViewAwareDatasource(datasource, ImmutableSet.<View> of(viewSource));
    viewSource.setDatasource(viewDs);

    CsvDatasource destination = new CsvDatasource("csv-" + datasource.getName());
    if(csvDestinationFile.exists()) {
      csvDestinationFile.delete();
    }
    csvDestinationFile.createNewFile();
    destination.addValueTable(tableName, null, csvDestinationFile);
    Initialisables.initialise(viewSource, destination);
    DatasourceCopier.Builder.newCopier().dontCopyMetadata().build().copy(viewSource, destination);
    Disposables.dispose(viewSource, destination);
  }

  private void readTables(java.io.File source) throws IOException {
    File zsource = new File(source);

    drug = readDrugProductTable(zsource);
    comp = readTable(zsource, "comp.txt", drug.keySet());
    form = readTable(zsource, "form.txt", drug.keySet());
    ingred = readTable(zsource, "ingred.txt", drug.keySet());
    pack = readTable(zsource, "package.txt", drug.keySet());
    pharm = readTable(zsource, "pharm.txt", drug.keySet());
    route = readTable(zsource, "route.txt", drug.keySet());
    schedule = readTable(zsource, "schedule.txt", drug.keySet());
    status = readTable(zsource, "status.txt", drug.keySet());
    ther = readTable(zsource, "ther.txt", drug.keySet());
  }

  private void writeValueSets(Datasource datasource, String tableName, Map<String, Variable> variables) throws IOException {
    ValueTableWriter vtWriter = datasource.createWriter(tableName, "Drug");

    try {
      log.info("Writing values ...");
      List<String> ingredients = new ArrayList<String>();
      for(String drugCode : drug.keySet()) {
        String[] line = drug.get(drugCode);
        String din = line[3];
        ValueSetWriter vsWriter = vtWriter.writeValueSet(new VariableEntityBean("Drug", din));
        try {
          writeDrugValues(vsWriter, variables, line);
          writeIngredValues(vsWriter, variables, ingred.get(drugCode), din, ingredients);
          writeCompanyValues(vsWriter, variables, comp.get(drugCode));
          writeFormValues(vsWriter, variables, form.get(drugCode));
          writeStatusValues(vsWriter, variables, status.get(drugCode));
          writePackagingValues(vsWriter, variables, pack.get(drugCode));
          writePharmValues(vsWriter, variables, pharm.get(drugCode));
          writeRouteValues(vsWriter, variables, route.get(drugCode));
          writeScheduleValues(vsWriter, variables, schedule.get(drugCode));
          writeTherValues(vsWriter, variables, ther.get(drugCode));

        } finally {
          vsWriter.close();
        }
      }

      // reports not found ingredient names
      List<String> ingredientsNotFound = new ArrayList<String>();
      for(String ing : indications.keySet()) {
        if(ingredients.contains(ing) == false) {
          ingredientsNotFound.add(indications.get(ing)[0].trim());
        }
      }
      if(ingredientsNotFound.size() > 0) {
        log.error("Invalid ingredient names from indications database:");
        StringBuffer buffer = new StringBuffer("\n");
        for(String ingIndic : ingredientsNotFound) {
          buffer.append(ingIndic).append(",");
          boolean first = true;
          for(String ing : ingredients) {
            if(ing.contains(ingIndic.toUpperCase())) {
              if(first == false) {
                buffer.append(" / ");
              } else {
                first = false;
              }
              buffer.append(ing);
            }
          }
          buffer.append("\n");
        }
        log.error(buffer.toString());
      }

    } finally {
      vtWriter.close();
    }
  }

  private void writeDrugValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    writeValue(vsWriter, variables, "PRODUCT_CATEGORIZATION", line[1]);
    writeValue(vsWriter, variables, "CLASS", line[2]);
    writeValue(vsWriter, variables, "DRUG_IDENTIFICATION_NUMBER", line[3]);
    writeValue(vsWriter, variables, "BRAND_NAME", line[4]);
    writeValue(vsWriter, variables, "DESCRIPTOR", line[5]);
    writeValue(vsWriter, variables, "PEDIATRIC_FLAG", line[6]);
    writeValue(vsWriter, variables, "ACCESSION_NUMBER", line[7]);
    writeValue(vsWriter, variables, "NUMBER_OF_AIS", line[8]);
    writeValue(vsWriter, variables, "LAST_UPDATE_DATE", line[9]);
    writeValue(vsWriter, variables, "AI_GROUP_NO", line[10]);
  }

  private void writeIngredValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line, String din, List<String> ingredients) {
    if(line == null) return;

    writeValue(vsWriter, variables, "ACTIVE_INGREDIENT_CODE", line[1]);
    writeValue(vsWriter, variables, "INGREDIENT", line[2]);
    writeValue(vsWriter, variables, "INGREDIENT_SUPPLIED_IND", line[3]);
    writeValue(vsWriter, variables, "STRENGTH", line[4]);
    writeValue(vsWriter, variables, "STRENGTH_UNIT", line[5]);
    writeValue(vsWriter, variables, "STRENGTH_TYPE", line[6]);
    writeValue(vsWriter, variables, "DOSAGE_VALUE", line[7]);
    writeValue(vsWriter, variables, "BASE", line[8]);
    writeValue(vsWriter, variables, "DOSAGE_UNIT", line[9]);
    writeValue(vsWriter, variables, "NOTES", line[10]);

    // indications
    if(indications != null && line[2] != null && line[2].isEmpty() == false //
        && (noIndications == null || noIndications.contains(din) == false)) {
      ValueSequence sequence = getValueSequence(TextType.get(), line[2]).asSequence();
      List<String> indics = new ArrayList<String>();
      for(Value ing : sequence.getValues()) {
        String ingredientName = ing.toString().toUpperCase();
        // for reporting
        if(ingredients.contains(ingredientName) == false) {
          ingredients.add(ingredientName);
        }
        // add indication to drug with this ingredient
        if(indications.containsKey(ingredientName) && indics.contains(indications.get(ingredientName)[1]) == false) {
          String ingredientIndications = indications.get(ingredientName)[1];
          for(String val : ingredientIndications.split("\\|")) {
            if(indics.contains(val) == false) {
              indics.add(val);
            }
          }
        }
      }
      if(indics.size() > 0) {
        vsWriter.writeValue(variables.get("INDICATIONS"), Values.asSequence(TextType.get(), indics.toArray()));
      }
    }
  }

  private void writeCompanyValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;

    writeValue(vsWriter, variables, "MFR_CODE", line[1]);
    writeValue(vsWriter, variables, "COMPANY_CODE", line[2]);
    writeValue(vsWriter, variables, "COMPANY_NAME", line[3]);
    writeValue(vsWriter, variables, "COMPANY_TYPE", line[4]);
    writeValue(vsWriter, variables, "ADDRESS_MAILING_FLAG", line[5]);
    writeValue(vsWriter, variables, "ADDRESS_BILLING_FLAG", line[6]);
    writeValue(vsWriter, variables, "ADDRESS_NOTIFICATION_FLAG", line[7]);
    writeValue(vsWriter, variables, "ADDRESS_OTHER", line[8]);
    writeValue(vsWriter, variables, "SUITE_NUMBER", line[9]);
    writeValue(vsWriter, variables, "STREET_NAME", line[10]);
    writeValue(vsWriter, variables, "CITY_NAME", line[11]);
    writeValue(vsWriter, variables, "PROVINCE", line[12]);
    writeValue(vsWriter, variables, "COUNTRY", line[13]);
    writeValue(vsWriter, variables, "POSTAL_CODE", line[14]);
    writeValue(vsWriter, variables, "POST_OFFICE_BOX", line[15]);
  }

  private void writeFormValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;

    writeValue(vsWriter, variables, "PHARM_FORM_CODE", line[1]);
    writeValue(vsWriter, variables, "PHARMACEUTICAL_FORM", line[2]);
  }

  private void writeStatusValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;

    writeValue(vsWriter, variables, "CURRENT_STATUS_FLAG", line[1]);
    writeValue(vsWriter, variables, "STATUS", line[2]);
    writeValue(vsWriter, variables, "HISTORY_DATE", line[3]);
  }

  private void writePackagingValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;

    writeValue(vsWriter, variables, "UPC", line[1]);
    writeValue(vsWriter, variables, "PACKAGE_SIZE_UNIT", line[2]);
    writeValue(vsWriter, variables, "PACKAGE_TYPE", line[3]);
    writeValue(vsWriter, variables, "PACKAGE_SIZE", line[4]);
    writeValue(vsWriter, variables, "PRODUCT_INFORMATION", line[5]);
  }

  private void writePharmValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;
    writeValue(vsWriter, variables, "PHARMACEUTICAL_STD", line[1]);
  }

  private void writeRouteValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;
    writeValue(vsWriter, variables, "ROUTE_OF_ADMINISTRATION_CODE", line[1]);
    writeValue(vsWriter, variables, "ROUTE_OF_ADMINISTRATION", line[2]);
  }

  private void writeScheduleValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;
    writeValue(vsWriter, variables, "SCHEDULE", line[1]);
  }

  private void writeTherValues(ValueSetWriter vsWriter, Map<String, Variable> variables, String[] line) {
    if(line == null) return;
    writeValue(vsWriter, variables, "TC_ATC_NUMBER", line[1]);
    writeValue(vsWriter, variables, "TC_ATC", line[2]);
    writeValue(vsWriter, variables, "TC_AHFS_NUMBER", line[3]);
    writeValue(vsWriter, variables, "TC_AHFS", line[4]);
  }

  private void writeValue(ValueSetWriter vsWriter, Map<String, Variable> variables, String name, String value) {
    if(value == null || value.isEmpty()) return;

    Variable var = variables.get(name);
    if(var.isRepeatable()) {
      vsWriter.writeValue(var, getValueSequence(var.getValueType(), value));
    } else {
      vsWriter.writeValue(var, getValue(var.getValueType(), value));
    }
  }

  private Value getValue(ValueType type, String value) {
    if(value == null || value.isEmpty()) return type.nullValue();

    if(type.equals(DateType.get())) {
      try {
        return DateType.get().valueOf(dateFormat.parse(value));
      } catch(ParseException e) {
        e.printStackTrace();
        return DateType.get().nullValue();
      }

    } else {
      // case of abusive merging of lines
      if(value.indexOf('|') != -1) {
        String[] values = value.split("\\|");
        if(values.length > 0) {
          return getValue(type, values[0]);
        } else {
          return type.nullValue();
        }
      }
      return type.valueOf(value);
    }

  }

  private Value getValueSequence(ValueType type, final String value) {
    List<Value> values = new ArrayList<Value>();
    for(String val : value.split("\\|")) {
      values.add(getValue(type, val));
    }
    return type.sequenceOf(values);
  }

  private Map<String, Variable> writeVariables(Datasource datasource, String tableName) throws IOException {
    Map<String, Variable> variables = new HashMap<String, Variable>();
    ValueTableWriter vtWriter = datasource.createWriter(tableName, "Drug");
    VariableWriter varWriter = vtWriter.writeVariables();
    try {
      // drug
      writeVariable(varWriter, variables, newVariable("PRODUCT_CATEGORIZATION").addAttribute("label", "Product Categorization", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("CLASS").addAttribute("label", "Class of Drug Product", Locale.ENGLISH).addCategories("Human", "Disinfectant", "Veterinary"));
      writeVariable(varWriter, variables, newVariable("DRUG_IDENTIFICATION_NUMBER").addAttribute("label", "Drug Identification Number", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("BRAND_NAME").addAttribute("label", "Brand Name", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("DESCRIPTOR").addAttribute("label", "Description", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PEDIATRIC_FLAG").addAttribute("label", "Pediatric Flag", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("ACCESSION_NUMBER").addAttribute("label", "Accession Number", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("NUMBER_OF_AIS").addAttribute("label", "Number of Active Ingredients", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("LAST_UPDATE_DATE", DateType.get()).addAttribute("label", "Last Update Date", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("AI_GROUP_NO").addAttribute("label", "Active Ingredient Group Number", Locale.ENGLISH));
      // ingred
      writeVariable(varWriter, variables, newVariable("ACTIVE_INGREDIENT_CODE", TextType.get(), "ingred").addAttribute("label", "Active Ingredient Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("INGREDIENT", TextType.get(), "ingred").addAttribute("label", "Ingredient", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("INGREDIENT_SUPPLIED_IND", TextType.get(), "ingred").addAttribute("label", "Ingredient Supplied Ind", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("STRENGTH", TextType.get(), "ingred").addAttribute("label", "Strength", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("STRENGTH_UNIT", TextType.get(), "ingred").addAttribute("label", "Strength Unit", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("STRENGTH_TYPE", TextType.get(), "ingred").addAttribute("label", "Strength Type", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("DOSAGE_VALUE", TextType.get(), "ingred").addAttribute("label", "Dosage Value", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("BASE", TextType.get(), "ingred").addAttribute("label", "Base", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("DOSAGE_UNIT", TextType.get(), "ingred").addAttribute("label", "Dosage Unit", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("NOTES", TextType.get(), "ingred").addAttribute("label", "Notes", Locale.ENGLISH));
      // indications
      if(indications != null) {
        writeVariable(varWriter, variables, newVariable("INDICATIONS", TextType.get(), "").addAttribute("label", "Indications", Locale.ENGLISH));
      }
      // form
      writeVariable(varWriter, variables, newVariable("PHARM_FORM_CODE", TextType.get(), "form").addAttribute("label", "Pharmaceutical Form Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PHARMACEUTICAL_FORM", TextType.get(), "form").addAttribute("label", "Pharmaceutical Form", Locale.ENGLISH));
      // status
      writeVariable(varWriter, variables, newVariable("CURRENT_STATUS_FLAG", TextType.get(), "status").addAttribute("label", "Current Status Flag", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("STATUS", TextType.get(), "status").addAttribute("label", "Status", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("HISTORY_DATE", DateType.get(), "status").addAttribute("label", "History Date", Locale.ENGLISH));
      // packaging
      writeVariable(varWriter, variables, newVariable("UPC", TextType.get(), "package").addAttribute("label", "Universal Product Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PACKAGE_SIZE_UNIT", TextType.get(), "package").addAttribute("label", "Package Size Unit", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PACKAGE_TYPE", TextType.get(), "package").addAttribute("label", "Package Type", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PACKAGE_SIZE", TextType.get(), "package").addAttribute("label", "Package Type", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PRODUCT_INFORMATION", TextType.get(), "package").addAttribute("label", "Product Information", Locale.ENGLISH));
      // pharm
      writeVariable(varWriter, variables, newVariable("PHARMACEUTICAL_STD", TextType.get(), "pharm").addAttribute("label", "Pharmaceutical STD", Locale.ENGLISH));
      // route
      writeVariable(varWriter, variables, newVariable("ROUTE_OF_ADMINISTRATION_CODE", TextType.get(), "route").addAttribute("label", "Route of Administration Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("ROUTE_OF_ADMINISTRATION", TextType.get(), "route").addAttribute("label", "Route of Administration", Locale.ENGLISH));
      // schedule
      writeVariable(varWriter, variables, newVariable("SCHEDULE", TextType.get(), "schedule").addAttribute("label", "Schedule", Locale.ENGLISH).addCategories("Schedule F", "Schedule F Recommended", "Schedule G", "Schedule G (CDSA III)", "Schedule G (CDSA IV)", "Schedule D", "Narcotic", "Narcotic (CDSA I)", "Narcotic (CDSA II)", "CDSA Recommended", "Targeted (CDSA IV)", "Homeopathic", "OTC", "Ethical"));
      // ther
      writeVariable(varWriter, variables, newVariable("TC_ATC_NUMBER").addAttribute("label", "Anatomical Therapeutical Chemical Number", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("TC_ATC").addAttribute("label", "Anatomical Therapeutical Chemical", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("TC_AHFS_NUMBER", TextType.get(), "ther").addAttribute("label", "American Hospital Formulary Service Number", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("TC_AHFS", TextType.get(), "ther").addAttribute("label", "American Hospital Formulary Service", Locale.ENGLISH));
      // comp
      writeVariable(varWriter, variables, newVariable("MFR_CODE").addAttribute("label", "Manufacturer Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("COMPANY_CODE").addAttribute("label", "Company Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("COMPANY_NAME").addAttribute("label", "Company Name", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("COMPANY_TYPE").addAttribute("label", "Company Type", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("ADDRESS_MAILING_FLAG").addAttribute("label", "Address Mailing Flag", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("ADDRESS_BILLING_FLAG").addAttribute("label", "Address Billing Flag", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("ADDRESS_NOTIFICATION_FLAG").addAttribute("label", "Address Notification Flag", Locale.ENGLISH).addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))));
      writeVariable(varWriter, variables, newVariable("ADDRESS_OTHER").addAttribute("label", "Other Address", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("SUITE_NUMBER").addAttribute("label", "Suite Number", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("STREET_NAME").addAttribute("label", "Street", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("CITY_NAME").addAttribute("label", "City", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("PROVINCE").addAttribute("label", "Province", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("COUNTRY").addAttribute("label", "Country", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("POSTAL_CODE").addAttribute("label", "Postal Code", Locale.ENGLISH));
      writeVariable(varWriter, variables, newVariable("POST_OFFICE_BOX").addAttribute("label", "Post Office Box", Locale.ENGLISH));

    } finally {
      varWriter.close();
      vtWriter.close();
    }
    return variables;
  }

  private void writeVariable(VariableWriter varWriter, Map<String, Variable> variables, Variable.Builder varBuilder) throws IOException {
    Variable var = varBuilder.build();
    variables.put(var.getName(), var);
    varWriter.writeVariable(var);
  }

  private Variable.Builder newVariable(String name) {
    return newVariable(name, TextType.get(), null);
  }

  private Variable.Builder newVariable(String name, ValueType type) {
    return newVariable(name, type, null);
  }

  private Variable.Builder newVariable(String name, ValueType type, String occGroup) {
    Variable.Builder builder = Variable.Builder.newVariable(name, type, "Drug");
    if(occGroup != null) {
      builder.repeatable();
      if(occGroup.isEmpty() == false) {
        builder.occurrenceGroup(occGroup);
      }
    }
    builder.addAttribute("info", "http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/index-eng.php");
    builder.addAttribute("source", ALL_FILES_ZIP_URL);
    // builder.addAttribute("extraction_date", DateType.get().now().toString());

    return builder;
  }

  private Category newCategory(String name, String label) {
    Category.Builder builder = Category.Builder.newCategory(name);
    builder.addAttribute("label", label, Locale.ENGLISH);
    return builder.build();
  }

  private Map<String, String[]> readDrugProductTable(File source) throws IOException {
    log.info("Reading drug.txt...");
    CSVReader reader = getEntryReader(source, "drug.txt");

    Map<String, String[]> map = new LinkedHashMap<String, String[]>();

    String[] nextLine;
    while((nextLine = reader.readNext()) != null) {
      nextLine = normalize(nextLine);
      if(nextLine[2].equals("Human")) {
        map.put(nextLine[0], nextLine);
      }
    }

    reader.close();

    return map;
  }

  private Map<String, String[]> readIndicationsTable(java.io.File indicationSource) throws IOException {
    log.info("Reading {} ...", indicationSource.getName());
    CSVReader reader = new CSVReader(new FileReader(indicationSource));

    Map<String, String[]> map = new LinkedHashMap<String, String[]>();

    String[] nextLine;
    reader.readNext();
    while((nextLine = reader.readNext()) != null) {
      nextLine = normalize(nextLine);
      String key = nextLine[0].toUpperCase();
      if(map.containsKey(key) == false) {
        map.put(key, nextLine);
      } else {
        map.put(key, merge(map.get(key), nextLine));
      }
    }

    reader.close();

    return map;
  }

  private List<String> readNoIndicationsTable(java.io.File noIndicationSource) throws IOException {
    log.info("Reading {} ...", noIndicationSource.getName());
    CSVReader reader = new CSVReader(new FileReader(noIndicationSource));

    List<String> noList = new ArrayList<String>();

    String[] nextLine;
    reader.readNext();
    while((nextLine = reader.readNext()) != null) {
      nextLine = normalize(nextLine);
      String din = nextLine[0].toUpperCase();
      // case leading 0s were removed by spreadsheet application
      while(din.length() < 8) {
        din = "0" + din;
      }
      if(noList.contains(din) == false) {
        noList.add(din);
      }
    }

    reader.close();

    return noList;
  }

  private Map<String, String[]> readTable(File source, String name, Set<String> drugCodes) throws IOException {
    log.info("Reading {} ...", name);
    CSVReader reader = getEntryReader(source, name);
    Map<String, String[]> map = new LinkedHashMap<String, String[]>();

    String[] nextLine;
    while((nextLine = reader.readNext()) != null) {
      nextLine = normalize(nextLine);
      if(drugCodes.contains(nextLine[0])) {
        if(map.containsKey(nextLine[0]) == false) {
          map.put(nextLine[0], nextLine);
        } else {
          // merge lines
          map.put(nextLine[0], merge(map.get(nextLine[0]), nextLine));
        }
      }
    }

    reader.close();

    return map;
  }

  private String[] merge(String[] previousLine, String[] nextLine) {
    for(int i = 0; i < nextLine.length; i++) {
      previousLine[i] = previousLine[i] + "|" + nextLine[i];
    }
    return previousLine;
  }

  private String[] normalize(String[] line) {
    for(int i = 0; i < line.length; i++) {
      line[i] = normalize(line[i]);
    }
    return line;
  }

  private String normalize(String str) {
    return str.trim().replaceAll("\\s{2,}", " ");
  }

  private CSVReader getEntryReader(File source, String name) throws FileNotFoundException {
    return new CSVReader(new InputStreamReader(new FileInputStream(new File(source, name)), WESTERN_EUROPE));
  }

  private static java.io.File downloadLatestAllFiles() throws IOException {
    log.info("Download from: {} ...", ALL_FILES_ZIP_URL);

    // Get a connection to the URL and start up a buffered reader.
    URL url = new URL(ALL_FILES_ZIP_URL);
    url.openConnection();
    InputStream reader = url.openStream();

    // Setup a buffered file writer to write out what we read from the website.
    java.io.File allfiles = java.io.File.createTempFile("medications-", ".zip");
    allfiles.deleteOnExit();
    FileOutputStream writer = new FileOutputStream(allfiles);
    byte[] buffer = new byte[153600];
    int totalBytesRead = 0;
    int bytesRead = 0;

    while((bytesRead = reader.read(buffer)) > 0) {
      writer.write(buffer, 0, bytesRead);
      buffer = new byte[153600];
      totalBytesRead += bytesRead;
    }
    writer.close();
    reader.close();

    return allfiles;
  }

}
