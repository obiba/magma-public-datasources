package org.obiba.magma.datasource.healthcanada;

import java.io.IOException;
import java.util.Set;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.ImmutableSet;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Get the drugs from Health Canada for Humans only.
 */
public class HCDrugsVariableEntityProvider implements VariableEntityProvider {

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
          if("Human".equals(nextLine[2])) {
            builder.add(new VariableEntityBean(HCDrugsValueTable.DRUG_ENTITY_TYPE, nextLine[0]));
          }
        }
      } catch(IOException e) {
        throw new MagmaRuntimeException("Unable to read Drug identifiers", e);
      } finally {
        try {
          reader.close();
        } catch(IOException ignored) {
        }
      }

      entities = builder.build();
    }

    return entities;
  }

}
