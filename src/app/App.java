package app;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import com.github.cliftonlabs.json_simple.*;
import org.apache.commons.cli.*;


/* 
 * Returns result of SQL query to as MS SQL Server. 
 * Use with jtds connector. To use a newer Version, simply replace the jar in the app's lib folder.
 * 
 * Version: 1.0.0
 * 
 * @param    username
 * @param    password
 * @param    domain
 * @param    instance
 * @param    host
 * @param    port
 * @param    db
 */

public class App {    
    private static String username;
	private static String password;
	private static String domain;
	private static String instance;
	private static String host;
	private static String port;
	private static String db;
	private static String filename;
	private static Boolean silent;
	
	private static String conn_str;
	private static String sql;
	
	public static void print(Object obj){
	        System.out.println(obj);
	}
	
	private static void validateSql(String sql) {
		if(sql.indexOf(";") <= 0) {
			throw new IllegalArgumentException("You need to escape SQL statement with ;");
		} 
	}
  
    public static void main(String[] args) throws Exception {

    	/* 
    	 * Section: ArgParser for cli args 
    	 */
        Options options = new Options();
        
        Option opt_username = Option.builder("u")
        		.required(true).longOpt("username")
        		.hasArg().desc("(required) username to connect to db").build();    
        options.addOption(opt_username);
        
        Option opt_domain = Option.builder("d")
        		.longOpt("domain")
        		.required(false)
        		.hasArg().desc("(optional) domain of the user, like WINDOMAIN\\username").build();
        options.addOption(opt_domain);
        
        Option opt_password = Option.builder("pw")
        		.required(true).hasArg()
        		.longOpt("password").desc("(required) user password, special characters like ! don't "
        				+ "need to be escaped with '!', without tics esapce ").build();
        options.addOption(opt_password);
        
        Option opt_instance = Option.builder("i")
        		.required(true).hasArg()
        		.longOpt("instance").desc("(required) sql server instance").build();
        options.addOption(opt_instance);  
        
        Option opt_host = Option.builder("h")
        		.required(false).hasArg()
        		.longOpt("hostname").desc("(optional) databse host, defaults to localhost").build();
        options.addOption(opt_host);
        
        Option opt_port = Option.builder("p")
        		.required(false).hasArg()
        		.longOpt("port").desc("(optional) remote port, defaults to 1433").build();
        options.addOption(opt_port);
        
        Option opt_db = Option.builder("db")
        		.required(false).hasArg()
        		.longOpt("database").desc("(optional) remote port, defaults to master").build();
        options.addOption(opt_db);
        
        Option opt_sql = Option.builder("q")
        		.required(true).hasArg()
        		.longOpt("sql").desc("(required) SQL Query").build();
        options.addOption(opt_sql);
        
        Option opt_silent = Option.builder("s")
        		.required(false)
        		.longOpt("silent").desc("(optional) results not printed to stdout").build();
        options.addOption(opt_silent);
        
        Option opt_file = Option.builder("f")
        		.required(false)
        		.hasArg()
        		.longOpt("filename").desc("(optional) save result to file").build();
        options.addOption(opt_file);
        
        try {
        	CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            //print(Arrays.toString(cmd.getOptions()));
            
            username = cmd.getOptionValue("u");
            password = cmd.getOptionValue("pw");
            domain = cmd.getOptionValue("d","");
            host = cmd.getOptionValue("h","localhost");
            instance = cmd.getOptionValue("i");
            db = cmd.getOptionValue("db","master");
            port= cmd.getOptionValue("p","1433");
            sql = cmd.getOptionValue("q");
            silent = cmd.hasOption("s");
            if(cmd.hasOption("f")) {
            	filename = cmd.getOptionValue("f");
            } else {
            	filename = null;
            }
            validateSql(sql);
            
            conn_str = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + db + ";"
            		+ "instance=" + instance + ";"
            		+ "user=" + username + ";"
            		+ "domain=" + domain + ";"
            		+ "password=" + password + ";"
            		+ "useNTLMv2=true;";
            		
            
        } catch (ParseException ex) {
        	print(ex.getMessage());
        	HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("app", options);
            System.exit(1);
        } catch (IllegalArgumentException ex) {
        	print(ex.getMessage());
            print("SQL Statement: " + sql);
        	System.exit(2);
        }
       
        
        /* 
    	 * Section: Trying to connect to the SQL server 
    	 */
        
         Connection conn = null;
         try {
             conn = DriverManager.getConnection(conn_str);
         } catch (SQLException ex) {
        	 print(ex.getMessage());
             print("Connection String: " + conn_str);
             System.exit(1);
         }
         
         
         /* 
     	 * Section: running the SQL query against the server 
     	 */
         
         Statement stmt = null;
         ResultSet rs = null;
         
         try{
             stmt = conn.createStatement();
             if(stmt.execute(sql)){
                 rs = stmt.getResultSet();
                
             }
             int columns = 0;

             String[] header;
             if(rs != null){
                 columns = rs.getMetaData().getColumnCount();
                 header =  new String[columns];
                 for(int i=0; i<header.length; i++){
                     header[i] = rs.getMetaData().getColumnName(i+1);
                 }
             } else {
             	throw new SQLException("ResultSet ist empty");
             }

             JsonArray jentries = new JsonArray();
             while (rs.next()){
                 JsonObject obj = new JsonObject();
                 for(int i=0; i<columns; i++){
                     obj.put(rs.getMetaData().getColumnName(i+1), rs.getString(i+1));
                 }
                 jentries.add(obj);
             }
             if(!silent) {
            	 System.out.println(jentries.toJson());
             }

             if(filename != null) {

                 try(FileWriter file = new FileWriter(filename)){
                     file.write(jentries.toJson());
                 } catch(IOException ex){
                	 print(ex.getMessage());
                 } 
             }
         } catch (SQLException ex){
        	 print(ex.getMessage());
        	 print("Your are connected to " + host + ":" + port + "/" 
        			 + instance + ".\n Using database " + db + " as user" + username + ".");
         } finally {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (SQLException sqlEx) {}
                 rs = null;
             }

             if(stmt != null){
                 try{
                     stmt.close();
                 } catch (SQLException sqlEx) {}
                 stmt = null;
             }
        }
    }
};