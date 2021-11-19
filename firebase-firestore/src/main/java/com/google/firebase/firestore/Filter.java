package com.google.firebase.firestore;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.model.Document;

public abstract class Filter {
  @NonNull
  public static Filter equalTo(@NonNull FieldPath fieldPath, @NonNull Object value) {
    return null;
  }

  public abstract boolean matches(Document doc);

  public abstract String getCanonicalId();
}
