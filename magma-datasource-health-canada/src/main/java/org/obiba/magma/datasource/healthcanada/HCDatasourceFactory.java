package org.obiba.magma.datasource.healthcanada;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class HCDatasourceFactory extends AbstractDatasourceFactory{

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new HCDatasource(getName());
  }
}
