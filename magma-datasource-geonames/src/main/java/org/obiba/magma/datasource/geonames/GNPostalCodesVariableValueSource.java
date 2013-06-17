package org.obiba.magma.datasource.geonames;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

public class GNPostalCodesVariableValueSource implements VariableValueSource {

  private final Variable variable;

  protected GNPostalCodesVariableValueSource(Variable.Builder builder) {
    variable = builder.build();
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Nonnull
  @Override
  public Value getValue(ValueSet valueSet) {
    return ((GNPostalCodesValueTable.GNPostalCodesValueSet) valueSet).getValue(variable);
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return null;
  }
}
