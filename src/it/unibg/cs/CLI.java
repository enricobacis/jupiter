package it.unibg.cs;

import net.hydromatic.optiq.tools.Planner;
import net.hydromatic.optiq.tools.RuleSets;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.eigenbase.rel.RelNode;

public class CLI {

	public static void main(String[] args) throws Exception {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("Planner")
			.defaultHelp(true)
			.description("Use Optiq to get a relational plan from SQL statement.");
		parser.addArgument("-n", "--no-optimization")
			.dest("no-opt")
			.action(Arguments.storeTrue())
			.help("Convert SQL to a relational plan but do not optimize");
		parser.addArgument("-j", "--json")
			.dest("json")
			.action(Arguments.storeTrue())
			.help("Output the result as JSON");
		parser.addArgument("DB")
			.help("SQLite database to use as schema source");
		parser.addArgument("SQL")
			.help("SQL query (remember to quote it to make a single string)");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		Planner planner = Planners.getPlanner(ns.getString("DB"),
			ns.getBoolean("no-opt") ? RuleSets.ofList() : Planners.DEFAULT_RULES);

		RelNode rel = Planners.optimize(planner, ns.getString("SQL"));

		if (ns.getBoolean("json"))
			Writers.completeJson(rel);
		else
			Writers.complete(rel);

	}

}
