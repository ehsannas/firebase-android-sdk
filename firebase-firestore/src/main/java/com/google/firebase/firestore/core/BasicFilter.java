package com.google.firebase.firestore.core;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Filter;

public class BasicFilter extends Filter {
  public BasicFilter(FieldPath fieldPath, Operator operator, Object value) {
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

  public com.google.firebase.firestore.core.Filter.Operator getOperator() {
    return operator;
  }

  private FieldPath fieldPath;
  private com.google.firebase.firestore.core.Filter.Operator operator;
  private Object value;
}
