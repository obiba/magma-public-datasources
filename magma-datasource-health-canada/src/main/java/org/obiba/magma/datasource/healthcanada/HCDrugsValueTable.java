package org.obiba.magma.datasource.healthcanada;

import java.util.Date;

import javax.annotation.Nonnull;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.io.File;

public class HCDrugsValueTable extends AbstractValueTable {

  private static final Logger log = LoggerFactory.getLogger(HCDrugsValueTable.class);

  static final String DRUG_ENTITY_TYPE = "Drug";

  public HCDrugsValueTable(HCDatasource datasource) {
    super(datasource, "Drugs", new HCDrugsVariableEntityProvider());
    addVariableValueSources(new HCDrugsVariableValueSourceFactory());
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;
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
          drugEntry = ((HCDrugsVariableEntityProvider) getVariableEntityProvider()).getFileEntry("drug.txt");
        }
        return drugEntry;
      }
    };
  }

}
