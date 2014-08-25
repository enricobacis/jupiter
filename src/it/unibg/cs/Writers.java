package it.unibg.cs;

import java.io.PrintWriter;

import org.eigenbase.rel.CompleteJsonWriter;
import org.eigenbase.rel.CompleteRelWriter;
import org.eigenbase.rel.RelJsonWriter;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.RelWriterImpl;

public class Writers {

	public static void basic(RelNode rel) {
		final RelWriterImpl writer = new RelWriterImpl(new PrintWriter(System.out));
		rel.explain(writer);
	}

	public static void complete(RelNode rel) {
		final CompleteRelWriter writer = new CompleteRelWriter(new PrintWriter(System.out));
		rel.explain(writer);
	}

	public static void basicJson(RelNode rel) {
		final RelJsonWriter writer = new RelJsonWriter();
		rel.explain(writer);
		System.out.println(writer.asString());
	}

	public static void completeJson(RelNode rel) {
		final CompleteJsonWriter writer = new CompleteJsonWriter();
		rel.explain(writer);
		System.out.println(writer.asString());
	}
	
	

}
