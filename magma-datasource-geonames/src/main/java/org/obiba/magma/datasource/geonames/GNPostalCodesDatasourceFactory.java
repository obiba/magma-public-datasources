package org.obiba.magma.datasource.geonames;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class GNPostalCodesDatasourceFactory extends AbstractDatasourceFactory {

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new GNPostalCodesDatasource(getName());
  }
}
