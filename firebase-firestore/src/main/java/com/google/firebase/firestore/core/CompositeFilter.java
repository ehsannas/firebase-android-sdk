package com.google.firebase.firestore.core;

import com.google.firebase.firestore.Filter;

import java.util.List;

public class CompositeFilter extends Filter {

  public CompositeFilter(List<Filter> filters, Boolean isAnd) {
    this.filters = filters;
    this.isAnd = isAnd;
  }

  private List<? extends Filter> filters;
  private Boolean isAnd;
}
