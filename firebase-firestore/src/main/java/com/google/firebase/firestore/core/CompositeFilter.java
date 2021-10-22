package com.google.firebase.firestore.core;

import com.google.firebase.firestore.QueryConstraint;

import java.util.List;

public class CompositeFilter extends QueryConstraint {

  public CompositeFilter(List<QueryConstraint> filters, Boolean isAnd) {
    this.filters = filters;
    this.isAnd = isAnd;
  }

  private List<? extends QueryConstraint> filters;
  private Boolean isAnd;
}
