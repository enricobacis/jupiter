package it.unibg.cs;

import net.hydromatic.optiq.config.Lex;
import net.hydromatic.optiq.rules.java.EnumerableConvention;
import net.hydromatic.optiq.tools.Frameworks;
import net.hydromatic.optiq.tools.Planner;
import net.hydromatic.optiq.tools.RuleSet;
import net.hydromatic.optiq.tools.RuleSets;

import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.rules.MergeProjectRule;
import org.eigenbase.rel.rules.PushFilterPastJoinRule;
import org.eigenbase.rel.rules.PushFilterPastProjectRule;
import org.eigenbase.rel.rules.PushJoinThroughJoinRule;
import org.eigenbase.rel.rules.PushSortPastProjectRule;
import org.eigenbase.rel.rules.ReduceAggregatesRule;
import org.eigenbase.rel.rules.RemoveDistinctAggregateRule;
import org.eigenbase.rel.rules.SwapJoinRule;
import org.eigenbase.rel.rules.TableAccessRule;
import org.eigenbase.relopt.RelTraitSet;
import org.eigenbase.sql.SqlNode;
import org.eigenbase.sql.fun.SqlStdOperatorTable;



public abstract class Planners {

	public static final RuleSet DEFAULT_RULES =
		RuleSets.ofList(
			TableAccessRule.INSTANCE,
			MergeProjectRule.INSTANCE,
			PushFilterPastProjectRule.INSTANCE,
			PushFilterPastJoinRule.FILTER_ON_JOIN,
			RemoveDistinctAggregateRule.INSTANCE,
			ReduceAggregatesRule.INSTANCE,
			SwapJoinRule.INSTANCE,
			PushJoinThroughJoinRule.RIGHT,
			PushJoinThroughJoinRule.LEFT,
			PushSortPastProjectRule.INSTANCE);

	public static Planner getPlanner(String db, RuleSet rules) throws ClassNotFoundException {
		return Frameworks.getPlanner(Lex.JAVA, Schemas.fromSqlite(db, "main"), SqlStdOperatorTable.instance(), rules);
	}

	public static Planner getPlanner(String db) throws ClassNotFoundException {
		return getPlanner(db, DEFAULT_RULES);
	}
	
	public static RelNode optimize(Planner planner, String sql) throws Exception {

		SqlNode sqlNode = planner.parse(sql);
		SqlNode validSqlNode = planner.validate(sqlNode);
		RelNode relNode = planner.convert(validSqlNode);
		
		RelTraitSet traitSet = planner.getEmptyTraitSet()
				.replace(EnumerableConvention.INSTANCE);
		
		return planner.transform(0, traitSet, relNode);
	}

}
