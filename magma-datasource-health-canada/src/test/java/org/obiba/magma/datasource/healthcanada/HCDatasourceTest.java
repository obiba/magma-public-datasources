package org.obiba.magma.datasource.healthcanada;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Initialisables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.api.Assertions.assertThat;

public class HCDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(HCDatasourceTest.class);

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
    HCDatasource ds = new HCDatasource("foo");
    Initialisables.initialise(ds);

    assertThat(ds.getValueTableNames()).hasSize(1);
    assertThat(ds.getValueTableNames().iterator().next()).isEqualTo("Drugs");

    ValueTable drugs = ds.getValueTable("Drugs");
    assertThat(drugs.getVariableEntities().size()).isGreaterThan(13000);
    assertThat(drugs.getVariables()).hasSize(53);
  }

  @Test
  public void readDatasource() {
    Datasource ds = new HCDatasource("bar");
    Initialisables.initialise(ds);
    ValueTable drugs = ds.getValueTable("Drugs");
    Set<VariableEntity> entities = drugs.getVariableEntities();
    ValueSet valueSet = drugs.getValueSet(entities.iterator().next());
    for(Variable variable : drugs.getVariables()) {
      log.info("{} = {}", variable.getName(), drugs.getValue(variable, valueSet));
    }
    Variable currentStatusFlag = drugs.getVariable("CURRENT_STATUS_FLAG");
    Value currentStatusFlagValue = drugs.getValue(currentStatusFlag, valueSet);

    assertThat(currentStatusFlagValue.isSequence()).isTrue();
    assertThat(currentStatusFlagValue.toString()).isEqualTo("\"N\",\"Y\",\"N\",\"N\",\"N\",\"N\",\"N\",\"N\"");
    assertThat(valueSet.getTimestamps().getLastUpdate().toString()).isEqualTo("2012-06-27");
    assertThat(valueSet.getTimestamps().getCreated().toString()).isEqualTo("1996-09-23");

    for(ValueTable table : ds.getValueTables()) {
      log.info("Check {}", table.getName());
      assertThat(table.getValueSetCount()).isNotZero();
    }
  }

}
