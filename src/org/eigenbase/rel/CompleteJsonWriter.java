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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eigenbase.rel.metadata.RelMetadataQuery;
import org.eigenbase.relopt.RelOptCost;
import org.eigenbase.sql.SqlExplainLevel;
import org.eigenbase.util.JsonBuilder;
import org.eigenbase.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Callback for a relational expression to dump itself as JSON.
 *
 * @see org.eigenbase.rel.RelJsonReader
 */
public class CompleteJsonWriter implements RelWriter {
  //~ Instance fields ----------------------------------------------------------

  private final Gson gson;
  private final RelJson relJson;
  private final Map<RelNode, String> relIdMap =
      new IdentityHashMap<RelNode, String>();
  private final List<Object> relList;
  private final List<Pair<String, Object>> values =
      new ArrayList<Pair<String, Object>>();
  private String previousId;

  //~ Constructors -------------------------------------------------------------

  public CompleteJsonWriter() {
    gson = new GsonBuilder().setPrettyPrinting().create();
    relList = new ArrayList<Object>();
    relJson = new RelJson(new JsonBuilder());
  }

  //~ Methods ------------------------------------------------------------------

  protected void explain_(RelNode rel, List<Pair<String, Object>> values) {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();

    map.put("id", null); // ensure that id is the first attribute
    map.put("relOp", rel.getRelTypeName());
    for (Pair<String, Object> value : values)
      if (!(value.right instanceof RelNode))
        put(map, value.left, value.right);
    
    map.put("inputs", explainInputs(rel.getInputs()));
    
    map.put("rowCount", RelMetadataQuery.getRowCount(rel));
    put(map, "cost", RelMetadataQuery.getNonCumulativeCost(rel));
    put(map, "cumulativeCost", RelMetadataQuery.getCumulativeCost(rel));

    final String id = Integer.toString(relIdMap.size());
    relIdMap.put(rel, id);
    map.put("id", id);

    relList.add(map);
    previousId = id;
  }
  
  private boolean completePut(Map<String, Object> map, String name, Object value) {
    if (value instanceof RelOptCost) {
      RelOptCost cost = (RelOptCost) value;
      final Map<String, Object> costMap = new LinkedHashMap<String, Object>();
      costMap.put("cpu", cost.getCpu());
      costMap.put("rows", cost.getRows());
      costMap.put("io", cost.getIo());
      map.put(name, costMap);
      return true;
    }
    return false;
  }

  private void put(Map<String, Object> map, String name, Object value) {
    if (!completePut(map, name, value))
      map.put(name, relJson.toJson(value));
  }

  private List<Object> explainInputs(List<RelNode> inputs) {
    final List<Object> list = new ArrayList<Object>();
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
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("root", previousId);
    map.put("rels", relList);
    return gson.toJson(map);
  }
}

// End RelJsonWriter.java
