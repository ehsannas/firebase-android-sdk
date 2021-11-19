package com.google.firebase.firestore.core;

import static com.google.firebase.firestore.util.Preconditions.checkNotNull;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.firestore.model.FieldPath;
import com.google.firebase.firestore.util.Assert;

/** Represents an unqualified field filter */
public class UnqualifiedFieldFilter extends Filter {
  public final FieldFilter.Operator operator;
  public final Object value;
  public final FieldPath field;

  public UnqualifiedFieldFilter(FieldPath field, FieldFilter.Operator operator, Object value) {
    checkNotNull(field, "Provided field path must not be null.");
    checkNotNull(operator, "Provided op must not be null.");
    checkNotNull(value, "Provided value must not be null.");
    this.field = field;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public boolean matches(Document doc) {
    throw Assert.fail("UnqualifiedFieldFilters must first be transformed to FieldFilters");
  }

  @Override
  public String getCanonicalId() {
    throw Assert.fail("UnqualifiedFieldFilters must first be transformed to FieldFilters");
  }
}
