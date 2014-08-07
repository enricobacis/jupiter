package it.unibg.cs;

import javax.sql.DataSource;

import net.hydromatic.optiq.Schema;
import net.hydromatic.optiq.SchemaPlus;
import net.hydromatic.optiq.impl.jdbc.JdbcSchema;
import net.hydromatic.optiq.tools.Frameworks;

public class Schemas {
	
	public static SchemaPlus fromJdbc(String driverClassName, String url, String schema, String user, String pass) throws ClassNotFoundException {
		Class.forName(driverClassName);
		
		SchemaPlus rootSchema = Frameworks.createRootSchema(false);
		DataSource dataScource = JdbcSchema.dataSource(url, driverClassName, user, pass);
		Schema child = JdbcSchema.create(rootSchema, schema, dataScource, null, null);
		return rootSchema.add(schema, child);
	}
	
	public static SchemaPlus fromJdbc(String driverClassName, String url, String schema) throws ClassNotFoundException {
		return fromJdbc(driverClassName, url, schema, "admin", "admin");
	}
	
	public static SchemaPlus fromSqlite(String url, String schema, String user, String pass) throws ClassNotFoundException {
		return fromJdbc("org.sqlite.JDBC", "jdbc:sqlite:" + url, schema, user, pass);
	}
	
	public static SchemaPlus fromSqlite(String url, String schema) throws ClassNotFoundException {
		return fromSqlite(url, schema, "", "");
	}

}
