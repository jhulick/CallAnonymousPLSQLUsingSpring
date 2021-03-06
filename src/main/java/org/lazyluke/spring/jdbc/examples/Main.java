package org.lazyluke.spring.jdbc.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;

@Component
class App 
{
	@Autowired
	ExampleJdbcDaoSupportDaoUsingPlainSql daoSql;

	@Autowired
	ExampleSpringStoredProcedureCallingPLSQLBlockWithCursorReturn storedProcedureForCursorReturnPLSQL;

	@Autowired
	ExampleSpringStoredProcedureCallingPLSQLBlockWithStringReturn storedProcedureForSimplePLSQL;
	
	@Autowired
	ExampleSpringStoredProcedureCallingComplexPLSQLBlockWithCursorReturn storedProcedureForComplexPLSQL;

	
    public void doSomething() {
        daoSql.doSomethingInTheDatabase();

        printBanner("Call simple plsql block with Spring StoredProcedure");
        printResults(storedProcedureForSimplePLSQL.executePLSQLBlock("123456"));

        printBanner("Call plsql block with string input and string output and cursor output via spring StoredProcedure");
        printResults(storedProcedureForCursorReturnPLSQL.executePLSQLBlock("11111111"));

        printBanner("Call plsql block with typed array input and cursor output via spring StoredProcedure");
        printResults(storedProcedureForComplexPLSQL.executePLSQLBlock("22222"));
    }

    private static void printBanner(String message) {
        System.out.println();
        System.out.println("====================================================");
        System.out.println(message);
        System.out.println("====================================================");
    }


    private static void printResults(Map<String, Object> result1) {
        for (Entry<String, Object> entry : result1.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }
}

@ComponentScan(basePackageClasses=Main.class)
public class Main {
	
	@Bean(name="ExampleDaoDataSource") DataSource createFooDataSource() throws SQLException {
		oracle.jdbc.OracleDriver driver = new oracle.jdbc.OracleDriver();
		DriverManager.registerDriver(driver);
		return new SimpleDriverDataSource(driver,"jdbc:oracle:thin:@localhost:1521:XE","system", "manager");
	}
	
    public static void main( String[] args ) throws Exception
    {
		setupOracleTypes();
		
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        App app = context.getBean(App.class);
		app.doSomething();
    }
    
	public static void setupOracleTypes()
			throws SQLException {
        final Connection c = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "manager");
		String createStudentType = "create or replace type student as object (id integer(4), name varchar2(25), phone_number varchar2(25))";
		String createStudentArrayType = "create or replace type student_array is table of student";
		System.out.println("" +
				"Set up some global Oracle datatypes:" +
				"(Passing array types from Java to Oracle requires Oracle type definitions." +
				"This should NOT be done in Java but is being done here for the sake of the example." +
				"It should be done once in Oracle. "
				);
		Statement st = c.createStatement();
		try {
			st.execute("drop type student_array ");
		} catch (Exception e) {
			// ignore
		}
		st.execute(createStudentType);
		st.execute(createStudentArrayType);
		st.close();
		c.close();
	}

}
