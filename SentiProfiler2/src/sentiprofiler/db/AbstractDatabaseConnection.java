package sentiprofiler.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;
import sentiprofiler.Constants;

/**
 * Handles database connections.
 * @author Tuomo Kakkonen
 *
 */
abstract class AbstractDatabaseConnection {
	/** Connection to JDBC database. */
    protected Connection con;
    private String dbUrl, user, password;
    private String dbDriver;

    /**
     * Creates a new instance of DBConnection.
     */
    public AbstractDatabaseConnection(String dbUrl, String user, String password,
            String dbDriver) {
        this.dbUrl = dbUrl;
        this.dbDriver = dbDriver;
        this.user = user;
        this.password = password;
    }
    
    public AbstractDatabaseConnection() {
    	readSettings();
    }

    /**
     * Reads the database settings from a properties file.
     */
    public void readSettings() {
        Properties props = new Properties();
        FileInputStream inputStream;
        try {
	        inputStream = new FileInputStream(Constants.DATABASE_SETTING_FILE);
	        props.load(inputStream);        
	        dbUrl = props.get("database").toString();
	        user = props.get("user").toString();
	        password = props.get("password").toString();
	        dbDriver = props.get("driver").toString();
	        inputStream.close();
        }
        catch(FileNotFoundException e) {
        	e.printStackTrace();
        }
        catch(IOException e) {
        	e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        try {
            if (con == null || con.isClosed())
                connect();
            else
                con.getWarnings();
        } catch (SQLException e) {
            connect();
        } catch (Exception e) {
            connect();
        }
        return this.con;
    }

    public void closeConnection() {
        Connection connection = getConnection();
        if (dbDriver.startsWith("jdbc:hsqldb")) {
            Statement s;
            try {
                s = connection.createStatement();
                s.execute("SHUTDOWN COMPACT");
                s.close();
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        }
    }

    /**
     * Checks for warnings that occurred when processing query or update.
     * @param stmt The statement to check.
     */
    protected void checkWarnings(Statement stmt) {
        try {
            SQLWarning warning = stmt.getWarnings();
            if (warning != null) {
            	System.out.println("---Warning---");
                while (warning != null) {
                	System.out.println("Message: " + warning.getMessage());
                	System.out.println("SQLState: " + warning.getSQLState());
                	System.out.println("Vendor error code: " + warning.getErrorCode());
               
                    warning = warning.getNextWarning();
                }
            }
        } catch (SQLException ex) {
        	System.out.println("AbstractDBConnection:checkWarnings - error\n" + ex);
        }
    }

    /**
     * Returns the maximum id number used in the table indicated by the parameter.
     * @param table Name of the table.
     * @return The highest id in the given table.
     */
    protected int getMaxId(String table) {
        int max = -1;
        try {
            Statement stmt = getConnection().createStatement();
            String query = "SELECT MAX(ID) FROM " + table;
            ResultSet rs = stmt.executeQuery(query);
            if (rs.wasNull())
                max = 0;
            else {
                rs.next();
                max = rs.getInt(1);
                this.checkWarnings(stmt);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
        	ex.printStackTrace();
            max = -1;
        }
        return max;
    }
    
    /**
     * Opens connection to JDBC database.
     * @param dbUrl URL of the database to open.
     * @param user Database username.
     * @param password Database password.
     * @param dbDriver Database drivers 
     * 		(ODBC: sun.jdbc.odbc.JdbcOdbcDriver
     * 		 MySQL: org.gjt.mm.mysql.Driver)
     * @return If true, connection was opened successfully.
     */
    public boolean connect() {
        try {
            Class.forName(dbDriver);
            con = DriverManager.getConnection(dbUrl, user, password);
            con.createStatement();
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Removes items from database.
     * @param statement SQL statement for remove.
     * @param id Id of the item to remove.
     * @return If true, removing was successful.
     */
    protected boolean remove(String statement, int id) {
        try {
            PreparedStatement stm;
            stm = getConnection().prepareStatement(statement);
            stm.setInt(1, id);
            stm.executeUpdate();
            this.checkWarnings(stm);
            stm.close();
        } catch (SQLException ex) {
            System.err.println("SQLException in remove: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /*//**
     * Removes items from database.
     * @param statement SQL statement for remove.
     * @return If true, removing was successful.
     *//*
    protected boolean remove(String statement) {
        try {
            PreparedStatement stm;
            stm = getConnection().prepareStatement(statement);
            stm.executeUpdate();
            this.checkWarnings(stm);
            stm.close();
        } catch (SQLException ex) {
            System.err.println("SQLException in remove: " + ex.getMessage());
            return false;
        }
        return true;
    }*/
    
    
}
