// A TableModel that supplies ResultSet data to a JTable.
import java.util.Properties;

import javax.swing.table.AbstractTableModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import com.mysql.cj.jdbc.MysqlDataSource;

public class ResultSetTableModel extends AbstractTableModel 
{
   private Statement statement;
   private ResultSet resultSet;
   private ResultSetMetaData metaData;
   private int numberOfRows;
   String queryUser;

   /*********************************
              CONSTRUCTOR
   *********************************/

   // constructor initializes resultSet and obtains its meta data object;
   // determines number of rows
   public ResultSetTableModel( Connection connection, String query ) 
      throws SQLException, ClassNotFoundException
   {         
       //read properties file
	   try {
	
            // create Statement to query database
            statement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
            DatabaseMetaData dbMetaData = connection.getMetaData();
            queryUser = dbMetaData.getUserName();
            // update database connection status

            // set query and execute it
		
		    //set update and execute it
		    //setUpdate (query);
	  } //end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
         System.exit( 1 );
      } // end catch
   } // end constructor ResultSetTableModel





   /**************************************************
                     Get Column CLASS
   ***************************************************/
   // get class that represents column type
   public Class<?> getColumnClass( int column ) throws IllegalStateException
   {
      // ensure database connection is available
      try 
      {
         String className = metaData.getColumnClassName( column + 1 );
         
         // return Class object that represents className
         return Class.forName( className );
      } // end try
      catch ( Exception exception ) 
      {
         exception.printStackTrace();
      } // end catch
      
      return Object.class; // if problems occur above, assume type Object
   } // end method getColumnClass





   /*********************************
            Get Column Count
   *********************************/
   // get number of columns in ResultSet
   public int getColumnCount() throws IllegalStateException
   {   
      // ensure database connection is available
      try 
      {
         return metaData.getColumnCount(); 
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return 0; // if problems occur above, return 0 for number of columns
   } // end method getColumnCount





   /*********************************
            Get Column Name
   *********************************/
   // get name of a particular column in ResultSet
   public String getColumnName( int column ) throws IllegalStateException
   {    
      // determine column name
      try 
      {
         return metaData.getColumnName( column + 1 );  
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string for column name
   } // end method getColumnName





   /*********************************
             Get Row Count
   *********************************/
   // return number of rows in ResultSet
   public int getRowCount() throws IllegalStateException
   {      
      return numberOfRows;
   } // end method getRowCount





   /*********************************
             Get Value At
   *********************************/

   // obtain value in particular row and column
   public Object getValueAt( int row, int column ) 
   {   
      // obtain a value at specified ResultSet row and column
      try 
      {
		   resultSet.next();  /* fixes a bug in MySQL/Java with date format */
         resultSet.absolute( row + 1 );
         return resultSet.getObject( column + 1 );
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string object
   } // end method getValueAt
   



   




   /*********************************
               Set Query
   *********************************/

   // set new database query string
   public void setQuery( String query ) 
      throws SQLException, IllegalStateException 
   {
      // specify query and execute it
      resultSet = statement.executeQuery( query );

      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last();                   // move to last row
      numberOfRows = resultSet.getRow();  // get row number      

      // Connect to the operations log database
      Properties properties = new Properties();
      FileInputStream urlFileIn = null;
      FileInputStream userFileIn = null;
      
      try {
          // Load operations log database connection details
          urlFileIn = new FileInputStream("operationslog.properties");
          userFileIn = new FileInputStream("project3app.properties");
          properties.load(urlFileIn); // Load URL properties first
  
          MysqlDataSource dataSource = new MysqlDataSource();
          dataSource.setURL(properties.getProperty("MYSQL_DB_URL"));
  
          // Reset properties to load user details
          properties.clear();
          properties.load(userFileIn); // Load user properties
          dataSource.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
          dataSource.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
         
          // Connect to operations log database
          try (Connection logDbConnection = dataSource.getConnection()) {
              System.out.println("Operations Log Database connected");
  
              // Check if the user already has an entry
              String checkUserQueries = "SELECT num_queries FROM operationscount WHERE login_username = ?";
              String updateCountQueries = "UPDATE operationscount SET num_queries = num_queries + 1 WHERE login_username = ?";
              String insertNewUserQuery = "INSERT INTO operationscount (login_username, num_queries) VALUES (?, 1)";
  
              if (!queryUser.equalsIgnoreCase("theaccountant@localhost")) { // Check to avoid operation for "theaccountant"
                  try (PreparedStatement checkUserStmt = logDbConnection.prepareStatement(checkUserQueries)) {
                      checkUserStmt.setString(1, queryUser);
                      ResultSet rs = checkUserStmt.executeQuery();
  
                      if (rs.next()) {
                          // User exists, update their count
                          try (PreparedStatement updateCountStmt = logDbConnection.prepareStatement(updateCountQueries)) {
                              updateCountStmt.setString(1, queryUser);
                              updateCountStmt.executeUpdate();
                          }
                      } else {
                          // New user, insert a new row
                          try (PreparedStatement insertNewUserStmt = logDbConnection.prepareStatement(insertNewUserQuery)) {
                              insertNewUserStmt.setString(1, queryUser);
                              insertNewUserStmt.executeUpdate();
                          }
                      }
                  }
              }
          }
      } catch (SQLException sqlException) {
         sqlException.printStackTrace();
      } catch (IOException ioException) {
         ioException.printStackTrace();
      } finally {
         // Close file input streams
         try {
            if (urlFileIn != null) {
                  urlFileIn.close();
            }
            if (userFileIn != null) {
                  userFileIn.close();
            }
         } catch (IOException ioException) {
            ioException.printStackTrace();
         }
      }

      // Notify JTable that model has changed
      fireTableStructureChanged();

   }// end method setQuery








   /*********************************
              Set Update
   *********************************/

   // set new database update-query string
   public int setUpdate( String query ) 
      throws SQLException, IllegalStateException 
   {
      // specify query and execute it
      int res = statement.executeUpdate(query);

      Properties properties = new Properties();
      FileInputStream urlFileIn = null;
      FileInputStream userFileIn = null;
      
      try {
          // Load operations log database connection details
          urlFileIn = new FileInputStream("operationslog.properties");
          userFileIn = new FileInputStream("project3app.properties");
          properties.load(urlFileIn); // Load URL properties first
  
          MysqlDataSource dataSource = new MysqlDataSource();
          dataSource.setURL(properties.getProperty("MYSQL_DB_URL"));
  
          // Reset properties to load user details
          properties.clear();
          properties.load(userFileIn); // Load user properties
          dataSource.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
          dataSource.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
  
          // Connect to operations log database
          try (Connection logDbConnection = dataSource.getConnection()) {
              System.out.println("Operations Log Database connected");
  
              // Check if the user already has an entry
              String checkUserUpdates = "SELECT num_updates FROM operationscount WHERE login_username = ?";
              String updateCountUpdates = "UPDATE operationscount SET num_updates = num_updates + 1 WHERE login_username = ?";
              String insertNewUserUpdate = "INSERT INTO operationscount (login_username, num_updates) VALUES (?, 1)";
  
              if (!queryUser.equalsIgnoreCase("theaccountant@localhost")) { // Check to avoid operation for "theaccountant"
                  try (PreparedStatement checkUserStmt = logDbConnection.prepareStatement(checkUserUpdates)) {
                      checkUserStmt.setString(1, queryUser);
                      ResultSet rs = checkUserStmt.executeQuery();
  
                      if (rs.next()) {
                          // User exists, update their count
                          try (PreparedStatement updateCountStmt = logDbConnection.prepareStatement(updateCountUpdates)) {
                              updateCountStmt.setString(1, queryUser);
                              updateCountStmt.executeUpdate();
                          }
                      } else {
                          // New user, insert a new row
                          try (PreparedStatement insertNewUserStmt = logDbConnection.prepareStatement(insertNewUserUpdate)) {
                              insertNewUserStmt.setString(1, queryUser);
                              insertNewUserStmt.executeUpdate();
                          }
                      }
                  }
              }
          }
      } catch (SQLException sqlException) {
          sqlException.printStackTrace();
      } catch (IOException ioException) {
          ioException.printStackTrace();
      } finally {
          // Close file input streams
          try {
              if (urlFileIn != null) {
                  urlFileIn.close();
              }
              if (userFileIn != null) {
                  userFileIn.close();
              }
          } catch (IOException ioException) {
              ioException.printStackTrace();
          }
      }

      fireTableStructureChanged();

      return res;

   } // end method setUpdate
  





   // close Statement and Connection
   public void closeStatement()
   {
      // close Statement and Connection
      try 
      {
         statement.close();
      } // end try
      catch ( SQLException sqlException )
      {
         sqlException.printStackTrace();
      } // end catch
   } // end method closeStatement
}  // end class ResultSetTableModel