package com.google.firebase.firestore.core;

import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.model.Document;
import java.util.ArrayList;
import java.util.List;

/** Represents a filter that is the conjunction or disjunction of single-field filters. */
public class CompositeFilter extends Filter {
  // List of sub-filters, each of which might be a FieldFilter or a CompositeFilter.
  private final List<Filter> filters;
  private final boolean isAnd;

  public CompositeFilter(List<Filter> filters, boolean isAnd) {
    this.filters = filters;
    this.isAnd = isAnd;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public boolean isAnd() {
    return isAnd;
  }

  // Returns a flattened list of all the filters within this composite filter.
  // For example: For `or(A, B, and(C, D, or(E, F)))`, returns [A, B, C, D, E, F].
  public List<Filter> getAllFlattenedFilters() {
    List<Filter> result = new ArrayList<>();
    for (Filter filter : filters) {
      if (filter instanceof CompositeFilter) {
        result.addAll(((CompositeFilter) filter).getAllFlattenedFilters());
      } else {
        result.add(filter);
      }
    }
    return result;
  }

  // Returns true if all the filters withing this composite filter are FieldFilters.
  // Returns false otherwise.
  public boolean isFullyQualified() {
    for (Filter filter : getAllFlattenedFilters()) {
      if (!(filter instanceof FieldFilter)) {
        return false;
      }
    }
    return true;
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
