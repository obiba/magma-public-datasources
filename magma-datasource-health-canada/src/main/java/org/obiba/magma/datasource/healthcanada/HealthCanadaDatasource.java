package org.obiba.magma.datasource.healthcanada;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.StaticValueTable;

import com.google.common.collect.ImmutableSet;

public class HealthCanadaDatasource extends AbstractDatasource {

  public static final String TYPE = "healthcanada";

  protected HealthCanadaDatasource(@Nonnull String name, String type) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {
    return ImmutableSet.of("Drugs");
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new StaticValueTable(this, tableName, new ArrayList<String>(), "Drug");
  }
}
