/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.rel;

import java.util.*;

import org.eigenbase.rel.metadata.RelMetadataQuery;
import org.eigenbase.relopt.RelOptCost;
import org.eigenbase.sql.SqlExplainLevel;
import org.eigenbase.util.*;

import com.google.common.collect.ImmutableList;

/**
 * Callback for a relational expression to dump itself as JSON.
 *
 * @see org.eigenbase.rel.RelJsonReader
 */
public class CompleteJsonWriter implements RelWriter {
  //~ Instance fields ----------------------------------------------------------

  private final JsonBuilder jsonBuilder;
  private final RelJson relJson;
  private final Map<RelNode, String> relIdMap =
      new IdentityHashMap<RelNode, String>();
  private final List<Object> relList;
  private final List<Pair<String, Object>> values =
      new ArrayList<Pair<String, Object>>();
  private String previousId;

  //~ Constructors -------------------------------------------------------------

  public CompleteJsonWriter() {
    jsonBuilder = new JsonBuilder();
    relList = jsonBuilder.list();
    relJson = new RelJson(jsonBuilder);
  }

  //~ Methods ------------------------------------------------------------------

  protected void explain_(RelNode rel, List<Pair<String, Object>> values) {
    final Map<String, Object> map = jsonBuilder.map();

    map.put("id", null); // ensure that id is the first attribute
    map.put("relOp", relJson.classToTypeName(rel.getClass()));
    for (Pair<String, Object> value : values) {
      if (value.right instanceof RelNode) {
        continue;
      }
      put(map, value.left, value.right);
    }
    // omit 'inputs: ["3"]' if "3" is the preceding rel
    final List<Object> list = explainInputs(rel.getInputs());
    if (list.size() != 1 || !list.get(0).equals(previousId)) {
      map.put("inputs", list);
    }
    
    map.put("rowCount", RelMetadataQuery.getRowCount(rel));
    put(map, "cost", RelMetadataQuery.getNonCumulativeCost(rel));
    put(map, "cumulativeCost", RelMetadataQuery.getCumulativeCost(rel));

    final String id = Integer.toString(relIdMap.size());
    relIdMap.put(rel, id);
    map.put("id", id);

    relList.add(map);
    previousId = id;
  }
  
  private boolean fullPut(Map<String, Object> map, String name, Object value) {
    if (value instanceof RelOptCost) {
      RelOptCost cost = (RelOptCost) value;
      final Map<String, Object> costMap = jsonBuilder.map();
      costMap.put("cpu", cost.getCpu());
      costMap.put("rows", cost.getRows());
      costMap.put("io", cost.getIo());
      map.put(name, costMap);
      return true;
    }
    return false;
  }

  private void put(Map<String, Object> map, String name, Object value) {
    if (!fullPut(map, name, value))
      map.put(name, relJson.toJson(value));
  }

  private List<Object> explainInputs(List<RelNode> inputs) {
    final List<Object> list = jsonBuilder.list();
    for (RelNode input : inputs) {
      String id = relIdMap.get(input);
      if (id == null) {
        input.explain(this);
        id = previousId;
      }
      list.add(id);
    }
    return list;
  }

  public final void explain(RelNode rel, List<Pair<String, Object>> valueList) {
    explain_(rel, valueList);
  }

  public SqlExplainLevel getDetailLevel() {
    return SqlExplainLevel.ALL_ATTRIBUTES;
  }

  public RelWriter input(String term, RelNode input) {
    return this;
  }

  public RelWriter item(String term, Object value) {
    values.add(Pair.of(term, value));
    return this;
  }

  public RelWriter itemIf(String term, Object value, boolean condition) {
    if (condition) {
      item(term, value);
    }
    return this;
  }

  public RelWriter done(RelNode node) {
    final List<Pair<String, Object>> valuesCopy =
        ImmutableList.copyOf(values);
    values.clear();
    explain_(node, valuesCopy);
    return this;
  }

  public boolean nest() {
    return true;
  }

  /**
   * Returns a JSON string describing the relational expressions that were just
   * explained.
   */
  public String asString() {
    final Map<String, Object> map = jsonBuilder.map();
    map.put("rels", relList);
    return jsonBuilder.toJsonString(map);
  }
}

// End RelJsonWriter.java
