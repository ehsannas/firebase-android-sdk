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

import static com.google.firebase.firestore.util.Preconditions.checkNotNull;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.core.BasicFilter;
import com.google.firebase.firestore.core.CompositeFilter;

import java.util.Arrays;
import java.util.List;

/** Represents a filter to be applied to query. */
public class Filter {
    public enum Operator {
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        ARRAY_CONTAINS("array_contains"),
        ARRAY_CONTAINS_ANY("array_contains_any"),
        IN("in"),
        NOT_IN("not_in");

        private final String text;

        Operator(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @NonNull
    public static Filter equalTo(@NonNull FieldPath fieldPath, @NonNull Object value) {
        return new BasicFilter(fieldPath, com.google.firebase.firestore.core.Filter.Operator.EQUAL, value);
    }

    @NonNull
    public static Filter equalTo(@NonNull String field, @NonNull Object value) {
        // call the other one.
        return equalTo(FieldPath.fromDotSeparatedPath(field), value);
    }

    @NonNull
    public static Filter in(@NonNull FieldPath field, @NonNull List<? extends Object> values) {
        return new BasicFilter(field, com.google.firebase.firestore.core.Filter.Operator.IN, values);
    }

    @NonNull
    public static Filter in(@NonNull String field, @NonNull List<? extends Object> values) {
        // call the other one.
        return equalTo(FieldPath.fromDotSeparatedPath(field), values);
    }

    //
    // ... Add all the other basic filters.
    //

    @NonNull
    public static Filter and(Filter... filters) {
        return new CompositeFilter(Arrays.asList(filters), true);
    }

    @NonNull
    public static Filter or(Filter... filters) {
        return new CompositeFilter(Arrays.asList(filters), false);
    }
}
