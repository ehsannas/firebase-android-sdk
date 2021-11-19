package com.google.firebase.firestore;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.model.Document;

public class Filter {
  @NonNull
  public static Filter equalTo(@NonNull FieldPath fieldPath, @NonNull Object value) {
    return null;
  }

  public boolean matches(Document doc) { return false; }
  public String getCanonicalId() { return ""; }
}
