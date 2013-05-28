package org.obiba.magma.datasource.healthcanada;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import au.com.bytecode.opencsv.CSVReader;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;

/**
 * Get the drugs from Health Canada for Humans only.
 */
public class HCDrugsVariableEntityProvider implements VariableEntityProvider {

  private static final Logger log = LoggerFactory.getLogger(HCDrugsVariableEntityProvider.class);

  static final String ALL_FILES_ZIP_URL
      = "http://www.hc-sc.gc.ca/dhp-mps/alt_formats/zip/prodpharma/databasdon/allfiles.zip";

  private static final Charset WESTERN_EUROPE = Charset.availableCharsets().get("ISO-8859-1");

  private File zsource;

  private Set<VariableEntity> entities;

  @Override
  public String getEntityType() {
    return HCDrugsValueTable.DRUG_ENTITY_TYPE;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return HCDrugsValueTable.DRUG_ENTITY_TYPE.equals(entityType);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    if(entities == null) {
      CSVReader reader = getEntryReader("drug.txt");

      ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();

      try {
        String[] nextLine;
        while((nextLine = reader.readNext()) != null) {
          nextLine = normalize(nextLine);
          if(nextLine[2].equals("Human")) {
            builder.add(new VariableEntityBean(HCDrugsValueTable.DRUG_ENTITY_TYPE, nextLine[0]));
          }
        }
      } catch(IOException e) {
        throw new MagmaRuntimeException("Unable to read Drug identifiers", e);
      } finally {
        try {
          reader.close();
        } catch(IOException e) {
          e.printStackTrace();
        }
      }

      entities = builder.build();
    }

    return entities;
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

  public CSVReader getEntryReader(String name) {
    try {
      return new CSVReader(new InputStreamReader(new FileInputStream(getFileEntry(name)), WESTERN_EUROPE));
    } catch(FileNotFoundException e) {
      throw new MagmaRuntimeException("Unable to read Health Canada Drug file: " + name, e);
    }
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
}
