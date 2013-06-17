package org.obiba.magma.datasource.geonames;

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
import org.obiba.magma.support.VariableEntityBean;

import com.google.common.collect.Lists;

import junit.framework.Assert;

public class GNPostalCodesDatasourceTest {

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
    GNPostalCodesDatasource ds = new GNPostalCodesDatasource("foo");
    Initialisables.initialise(ds);
    Assert.assertEquals(72, ds.getValueTableNames().size());
    Assert.assertTrue(ds.getValueTableNames().contains("CA"));

    ValueTable postalCodes = ds.getValueTable("CA");
    Set<VariableEntity> entities = postalCodes.getVariableEntities();
    Assert.assertTrue(entities.size() > 1040);
    List<Variable> variables = Lists.newArrayList(postalCodes.getVariables());
    Assert.assertEquals(8, variables.size());

    Variable coordinate = postalCodes.getVariable("COORDINATE");
    ValueSet vs = postalCodes.getValueSet(new VariableEntityBean(GNPostalCodesValueTable.ENTITY_TYPE, "CA-H2W"));
    Value val = postalCodes.getValue(coordinate, vs);
    Assert.assertEquals("[-73.5804,45.5176]", val.toString());

    Variable place = postalCodes.getVariable("PLACE_NAME");
    val = postalCodes.getValue(place, vs);
    Assert.assertEquals("Plateau Mont-Royal South Central", val.toString());

    Variable province = postalCodes.getVariable("STATE");
    val = postalCodes.getValue(province, vs);
    Assert.assertEquals("Quebec", val.toString());
  }

}
