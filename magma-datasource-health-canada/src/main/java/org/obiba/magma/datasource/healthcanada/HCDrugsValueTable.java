package org.obiba.magma.datasource.healthcanada;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import au.com.bytecode.opencsv.CSVReader;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

public class HCDrugsValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(HCDrugsValueTable.class);

  static final String DRUG_ENTITY_TYPE = "Drug";

  static final String ALL_FILES_ZIP_URL
      = "http://www.hc-sc.gc.ca/dhp-mps/alt_formats/zip/prodpharma/databasdon/allfiles.zip";

  private static final Charset WESTERN_EUROPE = Charset.availableCharsets().get("ISO-8859-1");

  private File zsource;

  public HCDrugsValueTable(HCDatasource datasource) {
    super(datasource, "Drugs");
    setVariableEntityProvider(new HCDrugsVariableEntityProvider(this));
    addVariableValueSources(new HCDrugsVariableValueSourceFactory());
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new HCDrugsValueSet(entity);
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      private File drugEntry;

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(new Date(getDrugEntry().lastModified()));
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().nullValue();
      }

      private File getDrugEntry() {
        if(drugEntry == null) {
          drugEntry = getFileEntry("drug.txt");
        }
        return drugEntry;
      }
    };
  }

  private void downloadLatestAllFiles() throws IOException {
    log.info("Download from: {} ...", ALL_FILES_ZIP_URL);

    // Get a connection to the URL and start up a buffered reader.
    URL url = new URL(ALL_FILES_ZIP_URL);
    url.openConnection();
    InputStream reader = url.openStream();

    // Setup a buffered file writer to write out what we read from the website.
    java.io.File allfiles = java.io.File.createTempFile("healthcanada-drugs-", ".zip");
    allfiles.deleteOnExit();
    FileOutputStream writer = new FileOutputStream(allfiles);
    byte[] buffer = new byte[153600];
    int bytesRead = 0;

    while((bytesRead = reader.read(buffer)) > 0) {
      writer.write(buffer, 0, bytesRead);
      buffer = new byte[153600];
    }
    writer.close();
    reader.close();

    zsource = new File(allfiles);
  }

  public File getFileEntry(String name) {
    if(zsource == null) {
      try {
        downloadLatestAllFiles();
      } catch(IOException e) {
        throw new MagmaRuntimeException("Unable to download Health Canada Drugs from: " + ALL_FILES_ZIP_URL, e);
      }
    }
    return new File(zsource, name);
  }

  CSVReader getEntryReader(String name) {
    try {
      return new CSVReader(new InputStreamReader(new FileInputStream(getFileEntry(name)), WESTERN_EUROPE));
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException("Unable to read Health Canada Drug file: " + name, e);
    }
  }

  String[] normalize(String[] line) {
    for(int i = 0; i < line.length; i++) {
      line[i] = normalize(line[i]);
    }
    return line;
  }

  private String normalize(String str) {
    return str.trim().replaceAll("\\s{2,}", " ");
  }

  class HCDrugsValueSet implements ValueSet {

    private final VariableEntity entity;

    private Map<String,String[]> values = Maps.newHashMap();

    private HCDrugsValueSet(VariableEntity entity) {
      this.entity = entity;
    }

    Value getValue(Variable variable) {
      String sourceFile = variable.getAttributeStringValue("file");
      int column = Integer.parseInt(variable.getAttributeStringValue("column"));

      String[] vals = values.get(sourceFile);
      if (vals == null) {
        // TODO fetch data from file
        //values.put(sourceFile,);
      }

      String strVal = vals[column];
      return variable.isRepeatable() ? variable.getValueType().sequenceOf(strVal) : variable.getValueType().valueOf(strVal);
    }

    @Override
    public ValueTable getValueTable() {
      return HCDrugsValueTable.this;
    }

    @Override
    public VariableEntity getVariableEntity() {
      return entity;
    }

    @Override
    public Timestamps getTimestamps() {
      return null;
    }
  }
}
