package org.obiba.magma.datasource.geonames;

import java.util.Locale;
import java.util.Set;

import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;

public class GNPostalCodesVariableValueSourceFactory implements VariableValueSourceFactory {

  @Override
  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> builder = ImmutableSet.builder();
    addVariables(builder);
    return builder.build();
  }

  private void addVariables(ImmutableSet.Builder<VariableValueSource> builder) {
    builder.add(newVariableValueSource(
        newVariable("PLACE_NAME", TextType.get()).addAttribute("label", "Place name", Locale.ENGLISH), 0));
    builder.add(newVariableValueSource(
        newVariable("STATE", TextType.get()).addAttribute("label", "Order subdivision (State)", Locale.ENGLISH), 1));
    builder.add(newVariableValueSource(newVariable("STATE_CODE", TextType.get())
        .addAttribute("label", "Order Subdivision (State) code", Locale.ENGLISH), 2));
    builder.add(newVariableValueSource(newVariable("COUNTRY_PROVINCE", TextType.get())
        .addAttribute("label", "Order Subdivision (Country/province)", Locale.ENGLISH), 3));
    builder.add(newVariableValueSource(newVariable("COUNTRY_PROVINCE_CODE", TextType.get())
        .addAttribute("label", "Order Subdivision (Country/province) code", Locale.ENGLISH), 4));
    builder.add(newVariableValueSource(
        newVariable("COMMUNITY", TextType.get()).addAttribute("label", "Order Subdivision (Community)", Locale.ENGLISH),
        5));
    builder.add(newVariableValueSource(newVariable("COMMUNITY_CODE", TextType.get())
        .addAttribute("label", "Order Subdivision (Community) code", Locale.ENGLISH), 6));
    builder.add(newVariableValueSource(newVariable("COORDINATE", PointType.get())
        .addAttribute("label", " Coordinate point [Longitude , Latitude]", Locale.ENGLISH), 7));
  }

  private VariableValueSource newVariableValueSource(Variable.Builder builder, int index) {
    return new GNPostalCodesVariableValueSource(builder.addAttribute("index", Integer.toString(index)));
  }

  private Variable.Builder newVariable(String name, ValueType type) {
    Variable.Builder builder = Variable.Builder.newVariable(name, type, GNPostalCodesValueTable.ENTITY_TYPE);

    builder.addAttribute("info", "http://download.geonames.org/export/zip/readme.txt");
    builder.addAttribute("source", GNPostalCodesValueTable.POSTAL_CODES_URL);
    return builder;
  }
}
