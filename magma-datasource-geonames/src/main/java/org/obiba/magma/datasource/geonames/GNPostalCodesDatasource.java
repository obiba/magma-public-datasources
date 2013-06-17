package org.obiba.magma.datasource.geonames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;

public class GNPostalCodesDatasource extends AbstractDatasource {

  public static final String TYPE = "geonames-postalcodes";

  protected GNPostalCodesDatasource(@Nonnull String name) {
    super(name, TYPE);
  }

  @Override
  protected Set<String> getValueTableNames() {

    Collection<String> countries = new ArrayList<String>();
    for(COUNTRIES c : COUNTRIES.values()) {
      countries.add(c.toString());
    }
    return ImmutableSet.copyOf(countries);
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new GNPostalCodesValueTable(this, tableName);
  }

  private enum COUNTRIES {
    AD, AR, AS, AT, AU, AX, BD, BE, BG, BR, CA, CH, CZ, DE, DK, DO, DZ, ES, FI, FO, FR, GB, GF, GG, GL, GP, GT, GU, GY,
    HR, HU, IM, IN, IS, IT, JE, JP, LI, LK, LT, LU, MC, MD, MH, MK, MP, MQ, MX, MY, NL, NO, NZ, PH, PK, PL, PM, PR, PT,
    RE, RU, SE, SI, SJ, SK, SM, TH, TR, US, VA, VI, YT, ZA
  }

}