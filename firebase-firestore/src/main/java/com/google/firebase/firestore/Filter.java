package com.google.firebase.firestore;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.core.CompositeFilter;
import com.google.firebase.firestore.core.FieldFilter;
import com.google.firebase.firestore.core.UnqualifiedFieldFilter;
import com.google.firebase.firestore.model.Document;
import java.util.Arrays;

public abstract class Filter {
  @NonNull
  public static Filter equalTo(@NonNull String field, @NonNull Object value) {
    return new UnqualifiedFieldFilter(
        FieldPath.fromDotSeparatedPath(field).getInternalPath(), FieldFilter.Operator.EQUAL, value);
  }

  @NonNull
  public static Filter equalTo(@NonNull FieldPath fieldPath, @NonNull Object value) {
    return new UnqualifiedFieldFilter(
        fieldPath.getInternalPath(), FieldFilter.Operator.EQUAL, value);
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
}
