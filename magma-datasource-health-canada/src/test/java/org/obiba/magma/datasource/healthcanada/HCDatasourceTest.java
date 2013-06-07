package org.obiba.magma.datasource.healthcanada;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Initialisables;

import com.google.common.collect.Lists;

import junit.framework.Assert;

public class HCDatasourceTest {
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
    Assert.assertEquals(1, ds.getValueTableNames().size());
    Assert.assertEquals("Drugs", ds.getValueTableNames().iterator().next());

    ValueTable drugs = ds.getValueTable("Drugs");
    Set<VariableEntity> entities = drugs.getVariableEntities();
    Assert.assertTrue(entities.size()>13000);
    List<Variable> variables = Lists.newArrayList(drugs.getVariables());
    Assert.assertEquals(53, variables.size());
  }

  @Test
  public void readDatasource() {
    HCDatasource ds = new HCDatasource("bar");
    Initialisables.initialise(ds);
    ValueTable drugs = ds.getValueTable("Drugs");
    Set<VariableEntity> entities = drugs.getVariableEntities();
    ValueSet valueSet = drugs.getValueSet(entities.iterator().next());
    for (Variable variable : drugs.getVariables()) {
      System.out.println(variable.getName() + "=" + drugs.getValue(variable,valueSet));
    }
    Variable currentStatusFlag = drugs.getVariable("CURRENT_STATUS_FLAG");
    Value currentStatusFlagValue = drugs.getValue(currentStatusFlag, valueSet);
    Assert.assertTrue(currentStatusFlagValue.isSequence());
    Assert.assertEquals("\"N\",\"Y\",\"N\",\"N\",\"N\",\"N\",\"N\",\"N\"",currentStatusFlagValue.toString());
    Assert.assertEquals("2012-06-27", valueSet.getTimestamps().getLastUpdate().toString());
    Assert.assertEquals("1996-09-23", valueSet.getTimestamps().getCreated().toString());
  }

}
