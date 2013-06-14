package org.obiba.magma.datasource.geonames;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityBean;

import com.google.common.collect.Maps;

import au.com.bytecode.opencsv.CSVReader;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

//import java.io.InputStreamReader;

public class GNPostalCodesValueTable extends AbstractValueTable {

  protected static final String POSTAL_CODES_URL = "http://download.geonames.org/export/zip/";

  protected static final String ENTITY_TYPE = "PostalCode";

  private Map<VariableEntity, String[]> valueSets = Maps.newHashMap();

  private String country;

  private final String countryFile;

  private static final Charset WESTERN_EUROPE = Charset.availableCharsets().get("ISO-8859-1");

  private File zipFile;

  public GNPostalCodesValueTable(Datasource datasource, String country) {
    super(datasource, country);
    this.country = country;
    countryFile = country + ".txt";
    setVariableEntityProvider(new GNPostalCodesVariableEntityProvider(this, country));
    addVariableValueSources(new GNPostalCodesVariableValueSourceFactory());
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new GNPostalCodesValueSet(entity);
  }

  @Override
  public Timestamps getTimestamps() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  private void downloadFile() throws IOException {

    // Get a connection to the URL and start up a buffered reader.
    URL url = new URL(POSTAL_CODES_URL + country + ".zip");
    url.openConnection();
    InputStream reader = url.openStream();

    // Setup a buffered file writer to write out what we read from the website.
    java.io.File file = java.io.File.createTempFile("postal-codes", ".zip");
    file.deleteOnExit();
    FileOutputStream writer = new FileOutputStream(file);
    byte[] buffer = new byte[153600];
    int bytesRead = 0;

    while((bytesRead = reader.read(buffer)) > 0) {
      writer.write(buffer, 0, bytesRead);
      buffer = new byte[153600];
    }
    writer.close();
    reader.close();

    zipFile = new File(file);
  }

  public CSVReader getEntryReader() {
    try {
      if(zipFile == null) {
        downloadFile();
      }
      File file = new File(zipFile, countryFile);
      return new CSVReader(new InputStreamReader(new FileInputStream(file), WESTERN_EUROPE), '\t');
    } catch(IOException e) {
      throw new MagmaRuntimeException("Unable to download GeoNames file: " + countryFile, e);
    }
  }

  class GNPostalCodesValueSet implements ValueSet {

    private VariableEntity entity;

    GNPostalCodesValueSet(VariableEntity entity) {
      this.entity = entity;
    }

    @Override
    public ValueTable getValueTable() {
      return GNPostalCodesValueTable.this;
    }

    @Override
    public VariableEntity getVariableEntity() {
      return entity;
    }

    Value getValue(Variable variable) {
      String[] valueSet = getValueSet();
      if(valueSet == null) {
        return variable.isRepeatable() ? variable.getValueType().nullSequence() : variable.getValueType().nullValue();
      }
      return variable.getValueType().valueOf(valueSet[Integer.parseInt(variable.getAttributeStringValue("index"))]);
    }

    /**
     * Load the data in memory if not already done.
     *
     * @param countryFile
     * @return
     */
    private String[] getValueSet() {
      try {
        if(valueSets == null) {
          CSVReader reader = getEntryReader();
          String[] line;
          while((line = reader.readNext()) != null) {
            VariableEntity entityIdentifier = new VariableEntityBean(ENTITY_TYPE, line[0] + "-" + line[1]);
            if(getVariableEntityProvider().getVariableEntities().contains(entityIdentifier)) {
              valueSets.put(entityIdentifier, getReformedLine(entityIdentifier, line));
            }
          }
          reader.close();
        }
      } catch(IOException e) {
        throw new MagmaRuntimeException("Unable to read file for country: " + countryFile, e);
      }
      return valueSets.get(entity);
    }

    private String[] getReformedLine(VariableEntity name, String[] line) {
      String[] newline = new String[line.length - 2];
      String coordinate = "[" + line[10] + "," + line[9] + "]";

      System.arraycopy(line, 2, newline, 0, 7);
      newline[7] = coordinate;
      return newline;
    }

    @Override
    public Timestamps getTimestamps() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
  }
}



