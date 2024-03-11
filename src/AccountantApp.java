/*
Name: Ryan Monahan
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Specialized Accountant Application 
Date: March 10, 2024
Class: Enterprise Computing
*/

import javax.swing.table.*;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.*;

import javax.swing.*;


public class AccountantApp extends JFrame {
    
    /**********************************
                VARIABLES
    *********************************/

    //define JButtons       -- five buttons
    JButton connectButton;
    JButton disconnectButton;
    JButton clearQueryButton;
    JButton executeCommandButton;
    JButton clearResultButton;

    //define JLabels        -- eight labels
    JLabel connectionDetailsTitle;
    JLabel urlPropertiesLabel;
    JLabel userPropertiesLabel;
    JLabel usernameLabel;
    JLabel passwordLabel;
    JLabel queryAreaTitle;
    JLabel connectionInfoLabel;
    JLabel resultWindowTitle;

    //define JTextAreas     -- one for command input
    JTextArea queryArea;

    //define JComboBoxes    -- two boxes
    JComboBox<String> urlPropertiesList;
    JComboBox<String> userPropertiesList;

    //define JTextField     -- one for username
    JTextField username;

    //define JPasswordField -- one for user password
    JPasswordField password;

    //define connection object and JTable
    private Connection connection;
    JTable resultTable;

    DefaultTableModel empty;

    //constuctor method
    public AccountantApp() {
        /**********************************
             Construct GUI Instance
        *********************************/

        //Initialize the drop down menus
        String[] PropertiesItems1 = {"operationslog.properties"};
        String[] PropertiesItems2 = {"theaccountant.properties"};

        //Construct GUI components

        //Define Buttons (five total)
        connectButton = new JButton ("Connect to Database");
        disconnectButton = new JButton("Disconnect from Database");
        clearQueryButton = new JButton("Clear SQL Command");
        executeCommandButton = new JButton("Execute SQL Command");
        clearResultButton = new JButton("Clear Result Window");

        //Define Labels (eight total)
        connectionDetailsTitle = new JLabel("Connection Details");
        urlPropertiesLabel = new JLabel("DB URL Properties");
        userPropertiesLabel = new JLabel("User Properties");
        usernameLabel = new JLabel("Username");
        passwordLabel = new JLabel("Password");
        queryAreaTitle = new JLabel("Enter an SQL Command");
        connectionInfoLabel = new JLabel("NO CONNECTION ESTABLISHED");
        resultWindowTitle = new JLabel("SQL Execution Window");

        //Define user entry areas and result return areas - examples shown not complete
        queryArea = new JTextArea(5, 5);
        username = new JTextField(50);
        password = new JPasswordField(50);
        urlPropertiesList = new JComboBox<>(PropertiesItems1);
        userPropertiesList = new JComboBox<>(PropertiesItems2);
        String[] columnNames = {""};
        empty = new DefaultTableModel(columnNames, 6);
        resultTable = new JTable(empty);

        //Set up the frame
        setTitle("Accountant App - (RCM - CNT4714 - Spring 2024 - Project 3)");
        setSize(1024, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        /**********************************
                    Top Panel
        *********************************/

        // Set up the top panel with a horizontal BoxLayout to contain the connection panel and command panel side by side
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        // Connection Panel that will contain the connection details and buttons
        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));

        // Add the title directly to the connectionPanel for compactness
        connectionDetailsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectionPanel.add(connectionDetailsTitle);

        // Connection Details Panel with labels and fields
        JPanel connectionDetailsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        connectionDetailsPanel.add(urlPropertiesLabel);
        connectionDetailsPanel.add(urlPropertiesList);
        connectionDetailsPanel.add(userPropertiesLabel);
        connectionDetailsPanel.add(userPropertiesList);
        connectionDetailsPanel.add(usernameLabel);
        connectionDetailsPanel.add(username);
        connectionDetailsPanel.add(passwordLabel);
        connectionDetailsPanel.add(password);

        // Ensure this panel doesn't take unnecessary vertical space
        connectionDetailsPanel.setMaximumSize(connectionDetailsPanel.getPreferredSize());
        connectionPanel.add(connectionDetailsPanel);

        // Buttons Panel configured for compactness and centering
        JPanel connectionDetailsButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        connectionDetailsButtons.add(connectButton);
        connectionDetailsButtons.add(disconnectButton);

        // Ensure buttons are small and centered by setting their maximum size
        connectButton.setPreferredSize(new Dimension(300, 25)); // Adjust size as needed
        disconnectButton.setPreferredSize(new Dimension(300, 25)); // Adjust size as needed
        connectionDetailsButtons.setMaximumSize(connectionDetailsButtons.getPreferredSize());
        connectionPanel.add(connectionDetailsButtons);

        // SQL Command Panel setup for more space
        JPanel commandPanel = new JPanel(new BorderLayout(5, 5));
        commandPanel.add(queryAreaTitle, BorderLayout.NORTH);

        // Query Area - make it larger and give it more space
        queryArea = new JTextArea(10, 30); // Adjust rows and columns to increase size
        JScrollPane queryScrollPane = new JScrollPane(queryArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        commandPanel.add(queryScrollPane, BorderLayout.CENTER);

        // Buttons at the bottom of the SQL Command Panel, small and aligned
        JPanel commandButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        clearQueryButton.setPreferredSize(new Dimension(300, 25)); // Adjust size as needed
        executeCommandButton.setPreferredSize(new Dimension(300, 25)); // Adjust size as needed
        commandButtonPanel.add(clearQueryButton);
        commandButtonPanel.add(executeCommandButton);
        commandPanel.add(commandButtonPanel, BorderLayout.SOUTH);

        // Adjust proportions between connectionPanel and commandPanel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, connectionPanel, commandPanel);
        splitPane.setResizeWeight(0.5); // This can be adjusted to change the initial division
        topPanel.add(splitPane);

        /*********************************
                   Bottom Panel
        *********************************/
       // Bottom panel setup
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(connectionInfoLabel);

        // Connection Info Label at the top
        bottomPanel.add(statusPanel, BorderLayout.NORTH);

        // Result Window Title directly under the Connection Info Label, aligned to the left
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Ensures left alignment
        titlePanel.add(resultWindowTitle);
        bottomPanel.add(titlePanel, BorderLayout.CENTER); // Adding to the CENTER to ensure it's at the top within its area

        // Result Table with ScrollPane directly under the title
        JScrollPane resultTableScrollPane = new JScrollPane(resultTable);
        resultTableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bottomPanel.add(resultTableScrollPane, BorderLayout.CENTER); // Adding to CENTER replaces the titlePanel, need adjustment

        // Clear Result Button at the bottom left corner
        JPanel clearResultButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Aligns the button to the left
        clearResultButtonPanel.add(clearResultButton);
        bottomPanel.add(clearResultButtonPanel, BorderLayout.SOUTH);

        // Adjusting to add both title and result table properly
        // Wrapping title and result table inside another panel since BorderLayout.CENTER can hold only one component
        JPanel tableWithTitlePanel = new JPanel(new BorderLayout());
        tableWithTitlePanel.add(titlePanel, BorderLayout.NORTH);
        tableWithTitlePanel.add(resultTableScrollPane, BorderLayout.CENTER); // Add scroll pane here

        // Adjust bottomPanel to include the new tableWithTitlePanel
        bottomPanel.add(tableWithTitlePanel, BorderLayout.CENTER); // Now the title and table are both correctly placed

        //Add everything to frame
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        /*******************************************************************
            Register Action Listeners and Event Handlers for Each Button
        *******************************************************************/

        /**********************************
                "Connect" Button
        *********************************/

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                boolean userCredentialsOK = false;
                

                MysqlDataSource dataSource = null;

                try {
                    //set details for Properties objects and file objects
                    //set MysqlDataSource object
                    Properties urlProperties = new Properties();
                    Properties userProperties = new Properties();
                    FileInputStream urlPropertyFileInput = null;
                    FileInputStream userPropertyFileInput = null;

                    //read the properties files
                    try {
                        //open DB properties file
                        //load properties from file for connection details
                        //load MysqlDataSource object
                        //open user properties file
                        //load properties from file for user details
                        urlPropertyFileInput = new FileInputStream(String.valueOf(urlPropertiesList.getSelectedItem()));
                        userPropertyFileInput = new FileInputStream(String.valueOf(userPropertiesList.getSelectedItem()));
                        urlProperties.load(urlPropertyFileInput);
                        dataSource = new MysqlDataSource();
                        dataSource.setURL(urlProperties.getProperty("MYSQL_DB_URL"));
                        userProperties.load(userPropertyFileInput);
                        dataSource.setUser(userProperties.getProperty("MYSQL_DB_USERNAME"));
                        dataSource.setPassword(userProperties.getProperty("MYSQL_DB_PASSWORD"));

                        //match username and password with properties file values
                        if(username.getText().equalsIgnoreCase(dataSource.getUser()) && String.valueOf(password.getPassword()).equalsIgnoreCase(dataSource.getPassword())) userCredentialsOK = true;

                        if(userCredentialsOK) {
                            //load user credentials from properties file
                            //get connection to DB
                            System.out.println("awesome");
                            connection = dataSource.getConnection();
                            connectionInfoLabel.setText("CONNECTED TO:" + dataSource.getURL());
                            //return connection info
                        }
                        else {
                            //indicate no connection
                            connectionInfoLabel.setText("NO CONNECTION ESTABLISHED");
                        }
                    } //end try
                    catch (SQLException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    } //end catch
                } //end try
                catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Properties File Error", JOptionPane.ERROR_MESSAGE);
                }
                // catch (SQLException e) {
                //     JOptionPane.showMessageDialog(null, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                // } //end catch
            } //end actionPerformed()
            } //end actionListener()
        ); // end inner class ConnectButton

        /*********************************
           "Disconnect from DB" Button
        *********************************/
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    connection.close();
                } // end try
                catch ( SQLException sqlException )
                {
                    sqlException.printStackTrace();
                } // end catch
                queryArea.setText("");
                resultTable.clearSelection();
                connectionInfoLabel.setText("NO CONNECTION ESTABLISHED");
                //Clear the results displayed in the window
                //Clear the input command area
                //Indicate connection was terminated and no connection currently exists
            } // end try
        }
        );

        /*********************************
           "Clear Result Window" Button
        *********************************/

        clearResultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                //Clear the results displayed in the window
                resultTable.setModel(empty);
            }
        }
        );

        /*********************************
               "Clear Query" Button
        *********************************/

        clearQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                //Clear the text displayed in the query/command window
                queryArea.setText("");
            }
        }
        );

        /*********************************
               "Execute" Button
        *********************************/

        executeCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    ResultSetTableModel tableModel = new ResultSetTableModel(connection, queryArea.getText());
                    //If select statement is used, use executeQuery() from ResultSetTableModel class
                    //All other command types will use executeUpdate() from the ResultSetTableModel class
                    //New window will pop up with message for user that does not have permission
                    tableModel.setQuery( queryArea.getText());
                    resultTable.setModel(tableModel);
                }
                //catch database error
                catch(SQLException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
                //catch driver error
                catch(ClassNotFoundException NotFound) {
                    JOptionPane.showMessageDialog(null, "MySQL driver not found", "Driver not found", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }

    /*****************************************
                      Main
    *****************************************/
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AccountantApp();
            }
        });
    }

}

