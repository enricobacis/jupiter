package testcases;


public class TestCases {

	public final static String threeway =
			"SELECT C.state, AVG(E.grade)" +
			" FROM root.student AS S" +
			" JOIN root.exam AS E ON S.id = E.sid" +
			" JOIN root.city AS C ON C.name = S.city" +
			" WHERE E.id BETWEEN 0 AND 100" +
			" GROUP BY C.state";
	
	public final static String twoway =
			"SELECT DISTINCT S.id" +
			" FROM main.student AS S" +
			" JOIN main.exam AS E ON S.id = E.sid" +
			" WHERE S.id  BETWEEN 0 AND 100" +
			" GROUP BY S.id" +
			" HAVING MIN(E.grade) > 25";
	
}
