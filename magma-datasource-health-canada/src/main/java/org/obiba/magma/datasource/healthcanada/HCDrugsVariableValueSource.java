package org.obiba.magma.datasource.healthcanada;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

class HCDrugsVariableValueSource implements VariableValueSource {

  private Variable variable;

  HCDrugsVariableValueSource(Variable.Builder builder) {
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
    // TODO
    return null;
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return null;
  }
}
