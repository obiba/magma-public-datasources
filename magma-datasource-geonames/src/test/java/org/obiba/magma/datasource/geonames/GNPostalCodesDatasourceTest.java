package org.obiba.magma.datasource.geonames;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import static org.fest.assertions.api.Assertions.assertThat;

public class GNPostalCodesDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(GNPostalCodesDatasourceTest.class);

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void createDatasource() {
    GNPostalCodesDatasource datasource = new GNPostalCodesDatasource("foo");
    Initialisables.initialise(datasource);
    Set<String> tableNames = datasource.getValueTableNames();
    assertThat(tableNames).hasSize(GNPostalCodesDatasource.COUNTRIES.values().length);
    assertThat(tableNames).contains("CA");

    testCanada(datasource);

    for(ValueTable table : datasource.getValueTables()) {
      log.info("Check {}", table.getName());
      assertThat(table.getValueSetCount()).isNotZero();
    }
  }

  private void testCanada(Datasource datasource) {

    ValueTable postalCodes = datasource.getValueTable("CA");
    Set<VariableEntity> entities = postalCodes.getVariableEntities();
    assertThat(entities.size()).isGreaterThan(1040);
    List<Variable> variables = Lists.newArrayList(postalCodes.getVariables());
    assertThat(variables).hasSize(8);

    Variable coordinate = postalCodes.getVariable("COORDINATE");
    ValueSet vs = postalCodes.getValueSet(new VariableEntityBean(GNPostalCodesValueTable.ENTITY_TYPE, "CA-H2W"));
    assertThat(postalCodes.getValue(coordinate, vs).toString()).isEqualTo("[-73.5804,45.5176]");

    Variable place = postalCodes.getVariable("PLACE_NAME");
    assertThat(postalCodes.getValue(place, vs).toString()).isEqualTo("Plateau Mont-Royal South Central");

    Variable province = postalCodes.getVariable("STATE");
    assertThat(postalCodes.getValue(province, vs).toString()).isEqualTo("Quebec");
  }

}
