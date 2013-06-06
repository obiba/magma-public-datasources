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

  private Set<VariableEntity> entities;

  private final HCDrugsValueTable table;

  public HCDrugsVariableEntityProvider(HCDrugsValueTable table) {
    this.table = table;
  }

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
      CSVReader reader = table.getEntryReader("drug.txt");

      ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();

      try {
        String[] nextLine;
        while((nextLine = reader.readNext()) != null) {
          nextLine = table.normalize(nextLine);
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


}
