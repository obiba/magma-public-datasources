package org.obiba.magma.datasource.healthcanada;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;

public class HCDatasource extends AbstractDatasource {

  public static final String TYPE = "healthcanada";

  protected HCDatasource(@Nonnull String name, String type) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {
    return ImmutableSet.of("Drugs");
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new HCDrugsValueTable(this);
  }
}
