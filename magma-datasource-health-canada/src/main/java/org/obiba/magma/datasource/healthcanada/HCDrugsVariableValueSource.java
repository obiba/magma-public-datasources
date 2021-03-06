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

  private final Variable variable;

  HCDrugsVariableValueSource(Variable.Builder builder) {
    variable = builder.build();
  }

  @Override
  public String getName() {
    return variable.getName();
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
    return ((HCDrugsValueTable.HCDrugsValueSet) valueSet).getValue(variable);
  }

  @Override
  public boolean supportVectorSource() {
    return false;
  }

  @Nullable
  @Override
  public VectorSource asVectorSource() {
    return null;
  }
}
