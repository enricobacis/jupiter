package org.eigenbase.rel;

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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.hydromatic.linq4j.Ord;
import net.hydromatic.optiq.runtime.Spacer;

import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.RelWriter;
import org.eigenbase.rel.metadata.RelMetadataQuery;
import org.eigenbase.rex.RexNode;
import org.eigenbase.sql.SqlExplainLevel;
import org.eigenbase.util.Pair;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link org.eigenbase.rel.RelWriter}.
 */
public class CompleteRelWriter implements RelWriter {
  //~ Instance fields --------------------------------------------------------

  protected final PrintWriter pw;
  private final SqlExplainLevel detailLevel;
  private final boolean withIdPrefix;
  protected final Spacer spacer = new Spacer();
  private final List<Pair<String, Object>> values =
      new ArrayList<Pair<String, Object>>();

  //~ Constructors -----------------------------------------------------------

  public CompleteRelWriter(PrintWriter pw) {
    this(pw, SqlExplainLevel.ALL_ATTRIBUTES, false);
  }

  public CompleteRelWriter(
      PrintWriter pw, SqlExplainLevel detailLevel,
      boolean withIdPrefix) {
    this.pw = pw;
    this.detailLevel = detailLevel;
    this.withIdPrefix = withIdPrefix;
  }

  //~ Methods ----------------------------------------------------------------

  protected void explain_(
      RelNode rel,
      List<Pair<String, Object>> values) {
    List<RelNode> inputs = rel.getInputs();

    if (!RelMetadataQuery.isVisibleInExplain(
        rel,
        detailLevel)) {
      // render children in place of this, at same level
      explainInputs(inputs);
      return;
    }

    StringBuilder s = new StringBuilder();
    spacer.spaces(s);
    if (withIdPrefix) {
      s.append(rel.getId()).append(":");
    }
    s.append(rel.getRelTypeName());
    if (detailLevel != SqlExplainLevel.NO_ATTRIBUTES) {
      int j = 0;
      for (Pair<String, Object> value : values) {
        if (value.right instanceof RelNode) {
          continue;
        }
        if (j++ == 0) {
          s.append("(");
        } else {
          s.append(", ");
        }
        s.append(value.left)
            .append("=[")
            .append(value.right)
            .append("]");
      }
      if (j > 0) {
        s.append(")");
      }
    }
    if (detailLevel == SqlExplainLevel.ALL_ATTRIBUTES) {
      s.append(": rowcount = ")
          .append(RelMetadataQuery.getRowCount(rel))
          .append(", cost = ")
          .append(RelMetadataQuery.getNonCumulativeCost(rel))
          .append(", cumulative cost = ")
          .append(RelMetadataQuery.getCumulativeCost(rel));
      if (!withIdPrefix) {
        // If we didn't print the rel id at the start of the line, print
        // it at the end.
        s.append(", id = ").append(rel.getId());
      }
    }
    pw.println(s);
    spacer.add(2);
    explainInputs(inputs);
    spacer.subtract(2);
  }

  private void explainInputs(List<RelNode> inputs) {
    for (RelNode input : inputs) {
      input.explain(this);
    }
  }

  public final void explain(RelNode rel, List<Pair<String, Object>> valueList) {
    explain_(rel, valueList);
  }

  public SqlExplainLevel getDetailLevel() {
    return detailLevel;
  }

  public RelWriter input(String term, RelNode input) {
    values.add(Pair.of(term, (Object) input));
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
    int i = 0;
    if (values.size() > 0 && values.get(0).left.equals("subset")) {
      ++i;
    }
    for (RelNode input : node.getInputs()) {
      assert values.get(i).right == input;
      ++i;
    }
    for (RexNode expr : node.getChildExps()) {
      assert values.get(i).right == expr;
      ++i;
    }
    final List<Pair<String, Object>> valuesCopy =
        ImmutableList.copyOf(values);
    values.clear();
    explain_(node, valuesCopy);
    pw.flush();
    return this;
  }

  public boolean nest() {
    return false;
  }

  /**
   * Converts the collected terms and values to a string. Does not write to
   * the parent writer.
   */
  public String simple() {
    final StringBuilder buf = new StringBuilder("(");
    for (Ord<Pair<String, Object>> ord : Ord.zip(values)) {
      if (ord.i > 0) {
        buf.append(", ");
      }
      buf.append(ord.e.left).append("=[").append(ord.e.right).append("]");
    }
    buf.append(")");
    return buf.toString();
  }
}

// End RelWriterImpl.java
