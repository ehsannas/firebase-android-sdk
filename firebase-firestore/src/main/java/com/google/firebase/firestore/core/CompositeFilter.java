package com.google.firebase.firestore.core;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.model.Document;

/** Represents a filter that is the conjunction or disjunction of single-field filters. */
public class CompositeFilter extends Filter {
  // List of sub-filters, each of which might be a FieldFilter or a CompositeFilter.
  private final Filter[] filters;
  private final boolean isAnd;

  protected CompositeFilter(Filter[] filters, boolean isAnd) {
    this.filters = filters;
    this.isAnd = isAnd;
  }

  @Override
  public boolean matches(Document doc) {
    // TODO(ehsann): implement.
    return false;
  }

  @Override
  public String getCanonicalId() {
    return "composite filter:...";
  }
}
