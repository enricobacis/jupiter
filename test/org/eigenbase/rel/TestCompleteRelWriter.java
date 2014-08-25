package org.eigenbase.rel;

import it.unibg.cs.Planners;
import it.unibg.cs.Writers;
import net.hydromatic.optiq.tools.Planner;
import testcases.TestCases;

public class TestCompleteRelWriter {

	public static void main(String[] args) throws Exception {

		Planner smart = Planners.getPlanner("test.db", Planners.DEFAULT_RULES);
		RelNode smartNode = Planners.optimize(smart, TestCases.twoway);
		Writers.complete(smartNode);
	}

}
