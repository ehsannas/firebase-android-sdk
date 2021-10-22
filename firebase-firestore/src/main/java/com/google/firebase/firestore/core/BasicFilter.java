package com.google.firebase.firestore.core;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QueryConstraint;

public class BasicFilter extends QueryConstraint {
  public BasicFilter(FieldPath fieldPath, Filter.Operator operator, Object value) {
    this.fieldPath = fieldPath;
    this.operator = operator;
    this.value = value;
  }

  public FieldPath getFieldPath() {
    return fieldPath;
  }

  public Object getValue() {
    return value;
  }

  public Filter.Operator getOperator() {
    return operator;
  }

  private FieldPath fieldPath;
  private Filter.Operator operator;
  private Object value;
}
