// Copyright 2018 Google LLC
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

package com.google.firebase.firestore.local;

import static com.google.firebase.firestore.model.FieldIndex.*;
import static com.google.firebase.firestore.model.FieldIndex.Segment.*;
import static com.google.firebase.firestore.testutil.TestUtil.andFilters;
import static com.google.firebase.firestore.testutil.TestUtil.doc;
import static com.google.firebase.firestore.testutil.TestUtil.docMap;
import static com.google.firebase.firestore.testutil.TestUtil.docSet;
import static com.google.firebase.firestore.testutil.TestUtil.fieldIndex;
import static com.google.firebase.firestore.testutil.TestUtil.filter;
import static com.google.firebase.firestore.testutil.TestUtil.map;
import static com.google.firebase.firestore.testutil.TestUtil.orFilters;
import static com.google.firebase.firestore.testutil.TestUtil.orderBy;
import static com.google.firebase.firestore.testutil.TestUtil.query;
import static com.google.firebase.firestore.testutil.TestUtil.setMutation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.firebase.database.collection.ImmutableSortedMap;
import com.google.firebase.firestore.core.Query;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.firestore.model.DocumentKey;
import com.google.firebase.firestore.model.DocumentSet;
import com.google.firebase.firestore.model.MutableDocument;
import com.google.firebase.firestore.model.SnapshotVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SQLiteQueryEngineTest extends QueryEngineTestCase {

  @Override
  Persistence getPersistence() {
    return PersistenceTestHelpers.createSQLitePersistence();
  }

  @Test
  public void combinesIndexedWithNonIndexedResults() throws Exception {
    MutableDocument doc1 = doc("coll/a", 1, map("foo", true));
    MutableDocument doc2 = doc("coll/b", 2, map("foo", true));
    MutableDocument doc3 = doc("coll/c", 3, map("foo", true));
    MutableDocument doc4 = doc("coll/d", 3, map("foo", true));

    indexManager.addFieldIndex(fieldIndex("coll", "foo", Kind.ASCENDING));

    addDocument(doc1);
    addDocument(doc2);
    indexManager.updateIndexEntries(docMap(doc1, doc2));
    indexManager.updateCollectionGroup("coll", IndexOffset.fromDocument(doc2));

    addDocument(doc3);
    addMutation(setMutation("coll/d", map("foo", true)));

    Query queryWithFilter = query("coll").filter(filter("foo", "==", true));
    ImmutableSortedMap<DocumentKey, Document> results =
        expectOptimizedCollectionScan(
            () ->
                queryEngine.getDocumentsMatchingQuery(
                    queryWithFilter, SnapshotVersion.NONE, DocumentKey.emptyKeySet()));

    assertTrue(results.containsKey(doc1.getKey()));
    assertTrue(results.containsKey(doc2.getKey()));
    assertTrue(results.containsKey(doc3.getKey()));
    assertTrue(results.containsKey(doc4.getKey()));
  }

  @Test
  public void canPerformOrQueriesUsingIndexes() throws Exception {
    MutableDocument doc1 = doc("coll/1", 1, map("a", 1, "b", 0));
    MutableDocument doc2 = doc("coll/2", 1, map("a", 2, "b", 1));
    MutableDocument doc3 = doc("coll/3", 1, map("a", 3, "b", 2));
    MutableDocument doc4 = doc("coll/4", 1, map("a", 1, "b", 3));
    MutableDocument doc5 = doc("coll/5", 1, map("a", 1, "b", 1));
    addDocument(doc1, doc2, doc3, doc4, doc5);
    indexManager.addFieldIndex(fieldIndex("coll", "a", Kind.ASCENDING));
    indexManager.addFieldIndex(fieldIndex("coll", "b", Kind.ASCENDING));
    indexManager.addFieldIndex(fieldIndex("coll", "b", Kind.DESCENDING));
    int a = backfiller.backfill();

//    Query query7 = query("coll").filter(filter("b", ">", 0)).orderBy(orderBy("b", "desc"));
//    DocumentSet result7 = expectOptimizedCollectionScan(() -> runQuery(query7, SnapshotVersion.NONE));
//    assertEquals(docSet(query7.comparator(), doc1), result7);

    // Two equalities: a==1 || b==1.
//    Query query1 = query("coll").filter(orFilters(filter("a", "==", 1), filter("b", "==", 1)));
//    DocumentSet result1 =
//        expectOptimizedCollectionScan(() -> runQuery(query1, SnapshotVersion.NONE));
//    assertEquals(docSet(query1.comparator(), doc1, doc2, doc4, doc5), result1);

    // with one inequality: a>2 || b==1.
    Query query2 = query("coll").filter(orFilters(filter("a", ">", 2), filter("b", "==", 1)));
    DocumentSet result2 =
        expectOptimizedCollectionScan(() -> runQuery(query2, SnapshotVersion.NONE));
    assertEquals(docSet(query2.comparator(), doc2, doc3, doc5), result2);

    // (a==1 && b==0) || (a==3 && b==2)
    Query query3 =
        query("coll")
            .filter(
                orFilters(
                    andFilters(filter("a", "==", 1), filter("b", "==", 0)),
                    andFilters(filter("a", "==", 3), filter("b", "==", 2))));
    DocumentSet result3 =
        expectOptimizedCollectionScan(() -> runQuery(query3, SnapshotVersion.NONE));
    assertEquals(docSet(query3.comparator(), doc1, doc3), result3);

    // a==1 && (b==0 || b==3).
    Query query4 =
        query("coll")
            .filter(
                andFilters(
                    filter("a", "==", 1), orFilters(filter("b", "==", 0), filter("b", "==", 3))));
    DocumentSet result4 =
        expectOptimizedCollectionScan(() -> runQuery(query4, SnapshotVersion.NONE));
    assertEquals(docSet(query4.comparator(), doc1, doc4), result4);

    // (a==2 || b==2) && (a==3 || b==3)
    Query query5 =
        query("coll")
            .filter(
                andFilters(
                    orFilters(filter("a", "==", 2), filter("b", "==", 2)),
                    orFilters(filter("a", "==", 3), filter("b", "==", 3))));
    DocumentSet result5 =
        expectOptimizedCollectionScan(() -> runQuery(query5, SnapshotVersion.NONE));
    assertEquals(docSet(query5.comparator(), doc3), result5);

    /*
    // Test with limits: (a==1) || (b > 0) LIMIT 2
    // (a==1) results in 3 docs (doc1, doc4, doc5) --> after limit: (doc1, doc4)
    // (b>0)  results in 4 docs (doc2, doc3, doc4, doc5) --> after limit: (doc2, doc3)
    // Union of the results: (doc1, doc4, doc2, doc3) --> after ORDER BY and LIMIT: (doc1, doc2)
    Query query6 = query("coll").filter(orFilters(filter("a", "==", 1), filter("b", ">", 0))).limitToFirst(2);
    DocumentSet result6 =
            expectOptimizedCollectionScan(() -> runQuery(query6, SnapshotVersion.NONE));
    assertEquals(docSet(query6.comparator(), doc1, doc2), result6);

    // Test with limits: (a==1) || (b > 0) ORDER BY b LIMIT 2
    // (a==1 order by b desc) --> (doc4, doc5, doc1) --> after limit: (doc4, doc5)
    // (b>0  order by b desc) --> (doc4, doc3, doc2, doc5) --> after limit: (doc4, doc3)
    // Union of the results: (doc1, doc2) --> after ORDER BY and LIMIT: (doc1)
    Query query7 = query("coll").filter(orFilters(filter("a", "==", 1), filter("b", ">", 0)))
            //.orderBy(orderBy("b", "asc"))
            .limitToFirst(4);
//    Query query7 = query("coll").filter(filter("b", ">", 0))
//            .orderBy(orderBy("b", "asc"))
//            .limitToFirst(4);
    DocumentSet result7 =
            expectOptimizedCollectionScan(() -> runQuery(query7, SnapshotVersion.NONE));
    assertEquals(docSet(query7.comparator(), doc1), result7);
     */
  }
}
