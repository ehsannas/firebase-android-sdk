package com.google.firebase.firestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.core.CompositeFilter;
import com.google.firebase.firestore.core.FieldFilter;
import com.google.firebase.firestore.core.UnqualifiedFieldFilter;
import com.google.firebase.firestore.model.Document;
import java.util.Arrays;

public abstract class Filter {
  @NonNull
  public static Filter equalTo(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.EQUAL, value);
  }

  @NonNull
  public static Filter equalTo(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.EQUAL, value);
  }

  @NonNull
  public static Filter notEqualTo(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.NOT_EQUAL, value);
  }

  @NonNull
  public static Filter notEqualTo(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.NOT_EQUAL, value);
  }

  @NonNull
  public static Filter greaterThan(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.GREATER_THAN, value);
  }

  @NonNull
  public static Filter greaterThan(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.GREATER_THAN, value);
  }

  @NonNull
  public static Filter greaterThanOrEqualTo(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.GREATER_THAN_OR_EQUAL, value);
  }

  @NonNull
  public static Filter greaterThanOrEqualTo(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.GREATER_THAN_OR_EQUAL, value);
  }

  @NonNull
  public static Filter lessThan(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.LESS_THAN, value);
  }

  @NonNull
  public static Filter lessThan(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.LESS_THAN, value);
  }

  @NonNull
  public static Filter lessThanOrEqualTo(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.LESS_THAN_OR_EQUAL, value);
  }

  @NonNull
  public static Filter lessThanOrEqualTo(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.LESS_THAN_OR_EQUAL, value);
  }

  @NonNull
  public static Filter arrayContains(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.ARRAY_CONTAINS, value);
  }

  @NonNull
  public static Filter arrayContains(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.ARRAY_CONTAINS, value);
  }

  @NonNull
  public static Filter arrayContainsAny(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.ARRAY_CONTAINS_ANY, value);
  }

  @NonNull
  public static Filter arrayContainsAny(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.ARRAY_CONTAINS_ANY, value);
  }

  @NonNull
  public static Filter in(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.IN, value);
  }

  @NonNull
  public static Filter in(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.IN, value);
  }

  @NonNull
  public static Filter notIn(@NonNull String field, @Nullable Object value) {
    return createUnqualifiedFilter(
        FieldPath.fromDotSeparatedPath(field), FieldFilter.Operator.NOT_IN, value);
  }

  @NonNull
  public static Filter notIn(@NonNull FieldPath fieldPath, @Nullable Object value) {
    return createUnqualifiedFilter(fieldPath, FieldFilter.Operator.NOT_IN, value);
  }

  @NonNull
  public static Filter or(Filter... filters) {
    return new CompositeFilter(Arrays.asList(filters), /*isAnd*/ false);
  }

  @NonNull
  public static Filter and(Filter... filters) {
    return new CompositeFilter(Arrays.asList(filters), /*isAnd*/ true);
  }

  public abstract boolean matches(Document doc);

  public abstract String getCanonicalId();

  @NonNull
  private static Filter createUnqualifiedFilter(
      @NonNull FieldPath fieldPath, @NonNull FieldFilter.Operator op, @Nullable Object value) {
    return new UnqualifiedFieldFilter(fieldPath.getInternalPath(), op, value);
  }
}
