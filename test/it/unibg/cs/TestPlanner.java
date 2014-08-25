package it.unibg.cs;

import net.hydromatic.optiq.tools.Planner;
import net.hydromatic.optiq.tools.RuleSets;

import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.metadata.RelMetadataQuery;

import testcases.TestCases;

/**
 * Use this arguments to print logger data:
 * -Djava.util.logging.config.file=logging.properties -Doptiq.debug=true
 */

public class TestPlanner {
	
	public static void main(String[] args) throws Exception {
		
		Planner dumb = Planners.getPlanner("test.db", RuleSets.ofList());
		RelNode dumbNode = Planners.optimize(dumb, TestCases.twoway);
		System.out.println("The dumb: " + RelMetadataQuery.getCumulativeCost(dumbNode));
		Writers.basic(dumbNode);
		
		Planner smart = Planners.getPlanner("test.db", Planners.DEFAULT_RULES);
		RelNode smartNode = Planners.optimize(smart, TestCases.twoway);
		System.out.println("\nThe smart: " + RelMetadataQuery.getCumulativeCost(smartNode));
		Writers.basic(smartNode);
	}
	
}
