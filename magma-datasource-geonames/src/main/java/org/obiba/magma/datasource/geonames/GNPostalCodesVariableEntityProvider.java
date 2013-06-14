package org.obiba.magma.datasource.geonames;

import java.io.IOException;
import java.util.Set;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.ImmutableSet;

import au.com.bytecode.opencsv.CSVReader;

public class GNPostalCodesVariableEntityProvider implements VariableEntityProvider {

  private Set<VariableEntity> entities;

  private final GNPostalCodesValueTable table;

  private final String COUNTRY_NAME;

  public GNPostalCodesVariableEntityProvider(GNPostalCodesValueTable table, String countryName) {
    this.table = table;
    this.COUNTRY_NAME = countryName;
  }

  @Override
  public String getEntityType() {
    return GNPostalCodesValueTable.ENTITY_TYPE;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return GNPostalCodesValueTable.ENTITY_TYPE.equals(entityType);
  }

  @SuppressWarnings("CallToPrintStackTrace")
  @Override
  public Set<VariableEntity> getVariableEntities() {

    if(entities == null) {
      CSVReader reader = table.getEntryReader();

      ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();

      try {
        String[] nextLine;
        while((nextLine = reader.readNext()) != null) {

          builder.add(new VariableEntityBean(GNPostalCodesValueTable.ENTITY_TYPE, nextLine[0] + "-" + nextLine[1]));
        }
      } catch(IOException e) {
        throw new MagmaRuntimeException("Unable to read Postal Codes for Country: " + COUNTRY_NAME + ".", e);
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
