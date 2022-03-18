// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.firestore.util;

import static com.google.firebase.firestore.util.Assert.hardAssert;

import androidx.annotation.VisibleForTesting;
import com.google.firebase.firestore.core.CompositeFilter;
import com.google.firebase.firestore.core.FieldFilter;
import com.google.firebase.firestore.core.Filter;
import com.google.firestore.v1.StructuredQuery;
import com.google.firestore.v1.StructuredQuery.CompositeFilter.Operator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides utility functions that help with boolean logic transformations needed for handling
 * complex filters used in queries.
 */
public class LogicUtils {

  /** Asserts that the given filter is a FieldFilter or CompositeFilter. */
  private static void assertFieldFilterOrCompositeFilter(Filter filter) {
    hardAssert(
        filter instanceof FieldFilter || filter instanceof CompositeFilter,
        "Only field filters and composite filters are accepted.");
  }

  /** Returns true if the given filter is a single field filter. e.g. (a == 10). */
  private static boolean isFieldFilter(Filter filter) {
    return filter instanceof FieldFilter;
  }

  /**
   * Returns true if the given filter is the conjunction of one or more field filters. e.g. (a == 10
   * && b == 20)
   */
  private static boolean isFlatConjunction(Filter filter) {
    return filter instanceof CompositeFilter && ((CompositeFilter) filter).isFlatConjunction();
  }

  /**
   * Returns true if the given filter is the disjunction of one or more "flat conjunctions" and
   * field filters. e.g. (a == 10) || (b==20 && c==30)
   */
  private static boolean isDisjunctionOfFieldFiltersAndFlatConjunctions(Filter filter) {
    if (filter instanceof CompositeFilter) {
      CompositeFilter compositeFilter = (CompositeFilter) filter;
      if (compositeFilter.isDisjunction()) {
        for (Filter subfilter : compositeFilter.getFilters()) {
          if (!isFieldFilter(subfilter) && !isFlatConjunction(subfilter)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether or not the given filter is in disjunctive normal form (DNF).
   *
   * <p>In boolean logic, a disjunctive normal form (DNF) is a canonical normal form of a logical
   * formula consisting of a disjunction of conjunctions; it can also be described as an OR of ANDs.
   *
   * <p>For more info, visit: https://en.wikipedia.org/wiki/Disjunctive_normal_form
   */
  private static boolean isDisjunctiveNormalForm(Filter filter) {
    // A single field filter is always in DNF form.
    // An AND of several field filters ("Flat AND") is in DNF form. e.g (A && B).
    // An OR of field filters and "Flat AND"s is in DNF form. e.g. (A || (B && C) || (D && F)).
    // Everything else is not in DNF form.
    return isFieldFilter(filter)
        || isFlatConjunction(filter)
        || isDisjunctionOfFieldFiltersAndFlatConjunctions(filter);
  }

  /**
   * Applies the associativity property to the given filter and returns the resulting filter.
   *
   * <p>A | (B | C) == (A | B) | C == (A | B | C)
   *
   * <p>A & (B & C) == (A & B) & C == (A & B & C)
   *
   * <p>For more info, visit: https://en.wikipedia.org/wiki/Associative_property#Propositional_logic
   */
  @VisibleForTesting
  public static Filter applyAssociation(Filter filter) {
    assertFieldFilterOrCompositeFilter(filter);

    if (isFieldFilter(filter)) {
      return filter;
    }

    CompositeFilter compositeFilter = (CompositeFilter) filter;

    // Example: (A | (((B)) | (C | D) | (E & F & (G | H)) --> (A | B | C | D | (E & F & (G | H))
    List<Filter> filters = compositeFilter.getFilters();

    // If the composite filter only contains 1 filter, apply associativity to it.
    if (filters.size() == 1) {
      return applyAssociation(filters.get(0));
    }

    // Associativity applied to a flat composite filter results in itself.
    if (compositeFilter.isFlat()) {
      return compositeFilter;
    }

    // First apply associativity to all subfilters. This will in turn recursively apply
    // associativity to all nested composite filters and field filters.
    List<Filter> updatedFilters = new ArrayList<>();
    for (Filter subfilter : filters) {
      updatedFilters.add(applyAssociation(subfilter));
    }

    // For composite subfilters that perform the same kind of logical operation as `compositeFilter`
    // take out their filters and add them to `compositeFilter`. For example:
    // compositeFilter = (A | (B | C | D))
    // compositeSubfilter = (B | C | D)
    // Result: (A | B | C | D)
    // Note that the `compositeSubfilter` has been eliminated, and its filters (B, C, D) have been
    // added to the top-level "compositeFilter".
    List<Filter> newSubfilters = new ArrayList<>();
    for (Filter subfilter : updatedFilters) {
      if (subfilter instanceof FieldFilter) {
        newSubfilters.add(subfilter);
      } else if (subfilter instanceof CompositeFilter) {
        CompositeFilter compositeSubfilter = (CompositeFilter) subfilter;
        if (compositeSubfilter.getOperator() == compositeFilter.getOperator()) {
          // compositeFilter: (A | (B | C))
          // compositeSubfilter: (B | C)
          // Result: (A | B | C)
          newSubfilters.addAll(compositeSubfilter.getFilters());
        } else {
          // compositeFilter: (A | (B & C))
          // compositeSubfilter: (B & C)
          // Result: (A | (B & C))
          newSubfilters.add(compositeSubfilter);
        }
      }
    }
    if (newSubfilters.size() == 1) {
      return newSubfilters.get(0);
    }
    return new CompositeFilter(newSubfilters, compositeFilter.getOperator());
  }

  /**
   * Performs conjunction distribution for the given filters.
   *
   * <p>There are generally four types of distributions:
   *
   * <p>Distribution of conjunction over disjunction: P & (Q | R) == (P & Q) | (P & R)
   *
   * <p>Distribution of disjunction over conjunction: P | (Q & R) == (P | Q) & (P | R)
   *
   * <p>Distribution of conjunction over conjunction: P & (Q & R) == (P & Q) & (P & R)
   *
   * <p>Distribution of disjunction over disjunction: P | (Q | R) == (P | Q) | (P | R)
   *
   * <p>This function ONLY performs the first type (distributing conjunction over disjunction) as it
   * is meant to be used towards arriving at a DNF form.
   *
   * <p>For more info, visit:
   * https://en.wikipedia.org/wiki/Distributive_property#Propositional_logic
   */
  @VisibleForTesting
  public static Filter applyDistribution(Filter lhs, Filter rhs) {
    assertFieldFilterOrCompositeFilter(lhs);
    assertFieldFilterOrCompositeFilter(rhs);
    Filter result;
    if (lhs instanceof FieldFilter && rhs instanceof FieldFilter) {
      result = applyDistribution((FieldFilter) lhs, (FieldFilter) rhs);
    }  else if (lhs instanceof FieldFilter && rhs instanceof CompositeFilter) {
      result = applyDistribution((FieldFilter) lhs, (CompositeFilter) rhs);
    } else if (lhs instanceof CompositeFilter && rhs instanceof FieldFilter) {
      result = applyDistribution((FieldFilter) rhs, (CompositeFilter) lhs);
    } else {
      result = applyDistribution((CompositeFilter) lhs, (CompositeFilter) rhs);
    }
    // Since `applyDistribution` is recursive, we must apply association at the end of each
    // distribution in order to ensure the result is as flat as possible for the next round of
    // distributions.
    return applyAssociation(result);
  }

  private static Filter applyDistribution(FieldFilter lhs, FieldFilter rhs) {
    // Conjunction distribution for two field filters is the conjunction of them.
    return new CompositeFilter(Arrays.asList(lhs, rhs), Operator.AND);
  }

  private static Filter applyDistribution(
      FieldFilter fieldFilter, CompositeFilter compositeFilter) {
    // There are two cases:
    // A & (B & C) --> (A & B & C)
    // A & (B | C) --> (A & B) | (A & C)
    if (compositeFilter.isConjunction()) {
      // Case 1
      return compositeFilter.withAddedFilter(fieldFilter);
    } else {
      // Case 2
      List<Filter> newFilters = new ArrayList<>();
      for (Filter subfilter : compositeFilter.getFilters()) {
        newFilters.add(applyDistribution(fieldFilter, subfilter));
      }
      // TODO(orquery): Use OPERATOR_OR.
      return new CompositeFilter(
          newFilters, StructuredQuery.CompositeFilter.Operator.OPERATOR_UNSPECIFIED);
    }
  }

  private static Filter applyDistribution(CompositeFilter lhs, CompositeFilter rhs) {
    hardAssert(
        lhs.getFilters().size() > 0 && rhs.getFilters().size() > 0,
        "Found an empty composite filter");
    // There are four cases:
    // (A & B) & (C & D) --> (A & B & C & D)
    // (A & B) & (C | D) --> (A & B & C) | (A & B & D)
    // (A | B) & (C & D) --> (C & D & A) | (C & D & B)
    // (A | B) & (C | D) --> (A & C) | (A & D) | (B & C) | (B & D)

    // Case 1 is a merge.
    if (lhs.isConjunction() && rhs.isConjunction()) {
      return lhs.withAddedFilters(rhs.getFilters());
    }

    // Case 2,3,4 all have at least one side (lhs or rhs) that is a disjunction. In all three cases
    // we should take each element of the disjunction and distribute it over the other side, and
    // return the disjunction of the distribution results.
    CompositeFilter disjunctionSide = lhs.isDisjunction() ? lhs : rhs;
    CompositeFilter otherSide = lhs.isDisjunction() ? rhs : lhs;
    List<Filter> results = new ArrayList<>();
    for (Filter subfilter : disjunctionSide.getFilters()) {
      results.add(applyDistribution(subfilter, otherSide));
    }
    // TODO(orquery): Use OPERATOR_OR.
    return new CompositeFilter(results, Operator.OPERATOR_UNSPECIFIED);
  }

  @VisibleForTesting
  public static Filter computeDnf(Filter filter) {
    assertFieldFilterOrCompositeFilter(filter);

    if (filter instanceof FieldFilter) {
      return filter;
    }

    CompositeFilter compositeFilter = (CompositeFilter) filter;

    if (compositeFilter.getFilters().size() == 1) {
      return computeDnf(filter.getFilters().get(0));
    }

    // Compute the DNF for each of the subfilters first.
    List<Filter> result = new ArrayList<>();
    for (Filter subfilter : compositeFilter.getFilters()) {
      result.add(computeDnf(subfilter));
    }
    Filter newFilter = new CompositeFilter(result, compositeFilter.getOperator());
    newFilter = applyAssociation(newFilter);

    if (isDisjunctiveNormalForm(newFilter)) {
      return newFilter;
    }

    hardAssert(newFilter instanceof CompositeFilter, "field filters are already in DNF form.");
    CompositeFilter newCompositeFilter = (CompositeFilter) newFilter;
    hardAssert(
        newCompositeFilter.isConjunction(),
        "Disjunction of filters all of which are already in DNF form is itself in DNF form.");
    hardAssert(
        newCompositeFilter.getFilters().size() > 1,
        "Single-filter composite filters are already in DNF form.");
    Filter runningResult = newCompositeFilter.getFilters().get(0);
    for (int i = 1; i < newCompositeFilter.getFilters().size(); ++i) {
      runningResult = applyDistribution(runningResult, newCompositeFilter.getFilters().get(i));
    }
    return runningResult;
  }

  /**
   * Given a composite filter, returns the list of terms in its disjunctive normal form.
   *
   * <p>Each element in the return value is one term of the resulting DNF. For instance: For the
   * input: (A || B) && C, the DNF form is: (A && C) || (B && C), and the return value is a list
   * with two elements: a composite filter that performs (A && C), and a composite filter that
   * performs (B && C).
   *
   * @param filter the composite filter to calculate DNF transform for.
   * @return the terms in the DNF transform.
   */
  public static List<Filter> DnfTransform(CompositeFilter filter) {
    // TODO(orquery): write the DNF transform algorithm here.
    // For now, assume all inputs are of the form AND(A, B, ...). Therefore the resulting DNF form
    // is the same as the input.
    if (filter.getFilters().isEmpty()) {
      return Collections.emptyList();
    }

    Filter result = computeDnf(filter);

    hardAssert(
        isDisjunctiveNormalForm(result), "computeDNF did not result in disjunctive normal form");

    if (isFieldFilter(result) || isFlatConjunction(result)) {
      return Collections.singletonList(result);
    }

    return result.getFilters();
  }
}
