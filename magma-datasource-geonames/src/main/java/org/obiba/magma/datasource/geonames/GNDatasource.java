package org.obiba.magma.datasource.geonames;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;

public class GNDatasource extends AbstractDatasource {

  public static final String TYPE = "geonames";

  protected GNDatasource(@Nonnull String name) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {
    return null;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return null;
  }
}