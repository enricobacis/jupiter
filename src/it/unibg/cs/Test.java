package it.unibg.cs;

import net.hydromatic.optiq.tools.Planner;
import net.hydromatic.optiq.tools.RuleSets;

import org.eigenbase.rel.RelNode;

/**
 * Use this arguments to print logger data:
 * -Djava.util.logging.config.file=logging.properties -Doptiq.debug=true
 */

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		Planner dumb = Planners.getPlanner("test.db", RuleSets.ofList());
		Planner smart = Planners.getPlanner("test.db", Planners.DEFAULT_RULES);
		
		System.out.println("The dumb:");
		RelNode dumbNode = Planners.optimize(dumb, TestCases.twoway);
		Planners.print(dumbNode);
		
		System.out.println("\nThe smart:");
		RelNode smartNode = Planners.optimize(smart, TestCases.twoway);
		Planners.print(smartNode);
		
	}
	
}
