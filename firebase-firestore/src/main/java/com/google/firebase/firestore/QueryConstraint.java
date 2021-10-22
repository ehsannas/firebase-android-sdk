// Copyright 2021 Google LLC
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

package com.google.firebase.firestore;

import static com.google.firebase.firestore.util.Assert.hardAssert;
import static com.google.firebase.firestore.util.Preconditions.checkNotNull;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.core.BasicFilter;
import com.google.firebase.firestore.core.CompositeFilter;
import com.google.firebase.firestore.core.FieldFilter;
import com.google.firebase.firestore.core.Filter;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.firestore.model.DocumentKey;
import com.google.firebase.firestore.model.ResourcePath;
import com.google.firebase.firestore.model.Values;
import com.google.firebase.firestore.util.Assert;
import com.google.firebase.firestore.util.Util;
import com.google.firestore.v1.ArrayValue;
import com.google.firestore.v1.Value;
import java.util.Arrays;
import java.util.List;

/** Represents a filter to be applied to query. */
public class QueryConstraint {
    @NonNull
    public static QueryConstraint equalTo(@NonNull FieldPath fieldPath, @NonNull Object value) {
        return new BasicFilter(fieldPath, Filter.Operator.EQUAL, value);
    }

    @NonNull
    public static QueryConstraint equalTo(@NonNull String field, @NonNull Object value) {
        // call the other one.
        return equalTo(FieldPath.fromDotSeparatedPath(field), value);
    }

    @NonNull
    public static QueryConstraint in(@NonNull FieldPath field, @NonNull List<? extends Object> values) {
        return new BasicFilter(field, Filter.Operator.IN, values);
    }

    @NonNull
    public static QueryConstraint in(@NonNull String field, @NonNull List<? extends Object> values) {
        // call the other one.
        return equalTo(FieldPath.fromDotSeparatedPath(field), values);
    }

    //
    // ... Add all the other basic filters.
    //

    @NonNull
    public static QueryConstraint and(List<QueryConstraint> filters) {
        return new CompositeFilter(filters, true);
    }

    @NonNull
    public static QueryConstraint or(List<QueryConstraint> filters) {
        return new CompositeFilter(filters, false);
    }
}
