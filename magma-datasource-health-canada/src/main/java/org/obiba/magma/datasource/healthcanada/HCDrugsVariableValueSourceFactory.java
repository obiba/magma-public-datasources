package org.obiba.magma.datasource.healthcanada;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;

public class HCDrugsVariableValueSourceFactory implements VariableValueSourceFactory {

  @Override
  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();

    addDrugVariables(builder);
    addIngredVariables(builder);
    addFormVariables(builder);
    addStatusVariables(builder);
    addPackagingVariables(builder);
    addPharmVariables(builder);
    addRouteVariables(builder);
    addScheduleVariables(builder);
    addTherVariables(builder);
    addCompVariables(builder);

    return builder.build();
  }

  private void addDrugVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // drug
    builder.add(newVariableValueSource(
        newVariable("PRODUCT_CATEGORIZATION").addAttribute("label", "Product Categorization", Locale.ENGLISH),
        "drug.txt", 1));
    builder.add(newVariableValueSource(
        newVariable("CLASS").addAttribute("label", "Class of Drug Product", Locale.ENGLISH)
            .addCategories("Human", "Disinfectant", "Veterinary"), "drug.txt", 2));
    builder.add(newVariableValueSource(
        newVariable("DRUG_IDENTIFICATION_NUMBER").addAttribute("label", "Drug Identification Number", Locale.ENGLISH),
        "drug.txt", 3));
    builder.add(newVariableValueSource(newVariable("BRAND_NAME").addAttribute("label", "Brand Name", Locale.ENGLISH),
        "drug.txt", 4));
    builder.add(newVariableValueSource(newVariable("DESCRIPTOR").addAttribute("label", "Description", Locale.ENGLISH),
        "drug.txt", 5));
    builder.add(newVariableValueSource(
        newVariable("PEDIATRIC_FLAG").addAttribute("label", "Pediatric Flag", Locale.ENGLISH)
            .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "drug.txt", 6));
    builder.add(newVariableValueSource(
        newVariable("ACCESSION_NUMBER").addAttribute("label", "Accession Number", Locale.ENGLISH), "drug.txt", 7));
    builder.add(newVariableValueSource(
        newVariable("NUMBER_OF_AIS").addAttribute("label", "Number of Active Ingredients", Locale.ENGLISH), "drug.txt",
        8));
    builder.add(newVariableValueSource(
        newVariable("LAST_UPDATE_DATE", DateType.get()).addAttribute("label", "Last Update Date", Locale.ENGLISH),
        "drug.txt", 9));
    builder.add(newVariableValueSource(
        newVariable("AI_GROUP_NO").addAttribute("label", "Active Ingredient Group Number", Locale.ENGLISH), "drug.txt",
        10));
  }

  private void addIngredVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // ingred
    builder.add(newVariableValueSource(newVariable("ACTIVE_INGREDIENT_CODE", TextType.get(), "ingred")
        .addAttribute("label", "Active Ingredient Code", Locale.ENGLISH), "ingred.txt", 1));
    builder.add(newVariableValueSource(
        newVariable("INGREDIENT", TextType.get(), "ingred").addAttribute("label", "Ingredient", Locale.ENGLISH),
        "ingred.txt", 2));
    builder.add(newVariableValueSource(newVariable("INGREDIENT_SUPPLIED_IND", TextType.get(), "ingred")
        .addAttribute("label", "Ingredient Supplied Ind", Locale.ENGLISH), "ingred.txt", 3));
    builder.add(newVariableValueSource(
        newVariable("STRENGTH", TextType.get(), "ingred").addAttribute("label", "Strength", Locale.ENGLISH),
        "ingred.txt", 4));
    builder.add(newVariableValueSource(
        newVariable("STRENGTH_UNIT", TextType.get(), "ingred").addAttribute("label", "Strength Unit", Locale.ENGLISH),
        "ingred.txt", 5));
    builder.add(newVariableValueSource(
        newVariable("STRENGTH_TYPE", TextType.get(), "ingred").addAttribute("label", "Strength Type", Locale.ENGLISH),
        "ingred.txt", 6));
    builder.add(newVariableValueSource(
        newVariable("DOSAGE_VALUE", TextType.get(), "ingred").addAttribute("label", "Dosage Value", Locale.ENGLISH),
        "ingred.txt", 7));
    builder.add(newVariableValueSource(
        newVariable("BASE", TextType.get(), "ingred").addAttribute("label", "Base", Locale.ENGLISH)
            .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "ingred.txt", 8));
    builder.add(newVariableValueSource(
        newVariable("DOSAGE_UNIT", TextType.get(), "ingred").addAttribute("label", "Dosage Unit", Locale.ENGLISH),
        "ingred.txt", 9));
    builder.add(newVariableValueSource(
        newVariable("NOTES", TextType.get(), "ingred").addAttribute("label", "Notes", Locale.ENGLISH), "ingred.txt",
        10));
  }

  private void addFormVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // form
    builder.add(newVariableValueSource(newVariable("PHARM_FORM_CODE", TextType.get(), "form")
        .addAttribute("label", "Pharmaceutical Form Code", Locale.ENGLISH), "form.txt", 1));
    builder.add(newVariableValueSource(newVariable("PHARMACEUTICAL_FORM", TextType.get(), "form")
        .addAttribute("label", "Pharmaceutical Form", Locale.ENGLISH), "form.txt", 2));
  }

  private void addStatusVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // status
    builder.add(newVariableValueSource(newVariable("CURRENT_STATUS_FLAG", TextType.get(), "status")
        .addAttribute("label", "Current Status Flag", Locale.ENGLISH)
        .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "status.txt", 1));
    builder.add(newVariableValueSource(
        newVariable("STATUS", TextType.get(), "status").addAttribute("label", "Status", Locale.ENGLISH), "status.txt",
        2));
    builder.add(newVariableValueSource(
        newVariable("HISTORY_DATE", DateType.get(), "status").addAttribute("label", "History Date", Locale.ENGLISH),
        "status.txt", 3));
  }

  private void addPackagingVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // package
    builder.add(newVariableValueSource(
        newVariable("UPC", TextType.get(), "package").addAttribute("label", "Universal Product Code", Locale.ENGLISH),
        "package.txt", 1));
    builder.add(newVariableValueSource(newVariable("PACKAGE_SIZE_UNIT", TextType.get(), "package")
        .addAttribute("label", "Package Size Unit", Locale.ENGLISH), "package.txt", 2));
    builder.add(newVariableValueSource(
        newVariable("PACKAGE_TYPE", TextType.get(), "package").addAttribute("label", "Package Type", Locale.ENGLISH),
        "package.txt", 3));
    builder.add(newVariableValueSource(
        newVariable("PACKAGE_SIZE", TextType.get(), "package").addAttribute("label", "Package Type", Locale.ENGLISH),
        "package.txt", 4));
    builder.add(newVariableValueSource(newVariable("PRODUCT_INFORMATION", TextType.get(), "package")
        .addAttribute("label", "Product Information", Locale.ENGLISH), "package.txt", 5));
  }

  private void addPharmVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // pharm
    builder.add(newVariableValueSource(newVariable("PHARMACEUTICAL_STD", TextType.get(), "pharm")
        .addAttribute("label", "Pharmaceutical STD", Locale.ENGLISH), "pharm.txt", 1));
  }

  private void addRouteVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // route
    builder.add(newVariableValueSource(newVariable("ROUTE_OF_ADMINISTRATION_CODE", TextType.get(), "route")
        .addAttribute("label", "Route of Administration Code", Locale.ENGLISH), "route.txt", 1));
    builder.add(newVariableValueSource(newVariable("ROUTE_OF_ADMINISTRATION", TextType.get(), "route")
        .addAttribute("label", "Route of Administration", Locale.ENGLISH), "route.txt", 2));
  }

  private void addScheduleVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // schedule
    builder.add(newVariableValueSource(
        newVariable("SCHEDULE", TextType.get(), "schedule").addAttribute("label", "Schedule", Locale.ENGLISH)
            .addCategories("Schedule F", "Schedule F Recommended", "Schedule G", "Schedule G (CDSA III)",
                "Schedule G (CDSA IV)", "Schedule D", "Narcotic", "Narcotic (CDSA I)", "Narcotic (CDSA II)",
                "CDSA Recommended", "Targeted (CDSA IV)", "Homeopathic", "OTC", "Ethical"), "schedule.txt", 1));

  }

  private void addTherVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // ther
    builder.add(newVariableValueSource(
        newVariable("TC_ATC_NUMBER").addAttribute("label", "Anatomical Therapeutical Chemical Number", Locale.ENGLISH),
        "ther.txt", 1));
    builder.add(newVariableValueSource(
        newVariable("TC_ATC").addAttribute("label", "Anatomical Therapeutical Chemical", Locale.ENGLISH), "ther.txt",
        2));
    builder.add(newVariableValueSource(newVariable("TC_AHFS_NUMBER", TextType.get(), "ther")
        .addAttribute("label", "American Hospital Formulary Service Number", Locale.ENGLISH), "ther.txt", 3));
    builder.add(newVariableValueSource(newVariable("TC_AHFS", TextType.get(), "ther")
        .addAttribute("label", "American Hospital Formulary Service", Locale.ENGLISH), "ther.txt", 4));
  }

  private void addCompVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    // comp
    builder.add(
        newVariableValueSource(newVariable("MFR_CODE").addAttribute("label", "Manufacturer Code", Locale.ENGLISH),
            "comp.txt", 1));
    builder.add(
        newVariableValueSource(newVariable("COMPANY_CODE").addAttribute("label", "Company Code", Locale.ENGLISH),
            "comp.txt", 2));
    builder.add(
        newVariableValueSource(newVariable("COMPANY_NAME").addAttribute("label", "Company Name", Locale.ENGLISH),
            "comp.txt", 3));
    builder.add(
        newVariableValueSource(newVariable("COMPANY_TYPE").addAttribute("label", "Company Type", Locale.ENGLISH),
            "comp.txt", 4));
    builder.add(newVariableValueSource(
        newVariable("ADDRESS_MAILING_FLAG").addAttribute("label", "Address Mailing Flag", Locale.ENGLISH)
            .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "comp.txt", 5));
    builder.add(newVariableValueSource(
        newVariable("ADDRESS_BILLING_FLAG").addAttribute("label", "Address Billing Flag", Locale.ENGLISH)
            .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "comp.txt", 6));
    builder.add(newVariableValueSource(
        newVariable("ADDRESS_NOTIFICATION_FLAG").addAttribute("label", "Address Notification Flag", Locale.ENGLISH)
            .addCategories(Arrays.asList(newCategory("Y", "Yes"), newCategory("N", "No"))), "comp.txt", 7));
    builder.add(
        newVariableValueSource(newVariable("ADDRESS_OTHER").addAttribute("label", "Other Address", Locale.ENGLISH),
            "comp.txt", 8));
    builder.add(
        newVariableValueSource(newVariable("SUITE_NUMBER").addAttribute("label", "Suite Number", Locale.ENGLISH),
            "comp.txt", 9));
    builder.add(
        newVariableValueSource(newVariable("STREET_NAME").addAttribute("label", "Street", Locale.ENGLISH), "comp.txt",
            10));
    builder.add(
        newVariableValueSource(newVariable("CITY_NAME").addAttribute("label", "City", Locale.ENGLISH), "comp.txt", 11));
    builder.add(
        newVariableValueSource(newVariable("PROVINCE").addAttribute("label", "Province", Locale.ENGLISH), "comp.txt",
            12));
    builder.add(
        newVariableValueSource(newVariable("COUNTRY").addAttribute("label", "Country", Locale.ENGLISH), "comp.txt",
            13));
    builder.add(newVariableValueSource(newVariable("POSTAL_CODE").addAttribute("label", "Postal Code", Locale.ENGLISH),
        "comp.txt", 14));
    builder.add(
        newVariableValueSource(newVariable("POST_OFFICE_BOX").addAttribute("label", "Post Office Box", Locale.ENGLISH),
            "comp.txt", 15));
  }

  private VariableValueSource newVariableValueSource(Variable.Builder builder, String sourceFile, int column) {
    return new HCDrugsVariableValueSource(
        builder.addAttribute("file", sourceFile).addAttribute("column", Integer.toString(column)));
  }

  private Variable.Builder newVariable(String name) {
    return newVariable(name, TextType.get(), null);
  }

  private Variable.Builder newVariable(String name, ValueType type) {
    return newVariable(name, type, null);
  }

  private Variable.Builder newVariable(String name, ValueType type, String occGroup) {
    Variable.Builder builder = Variable.Builder.newVariable(name, type, HCDrugsValueTable.DRUG_ENTITY_TYPE);
    if(occGroup != null) {
      builder.repeatable();
      if(!occGroup.isEmpty()) {
        builder.occurrenceGroup(occGroup);
      }
    }
    builder.addAttribute("info", "http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/index-eng.php");
    builder.addAttribute("source", HCDrugsValueTable.ALL_FILES_ZIP_URL);

    return builder;
  }

  private Category newCategory(String name, String label) {
    Category.Builder builder = Category.Builder.newCategory(name);
    builder.addAttribute("label", label, Locale.ENGLISH);
    return builder.build();
  }

}
