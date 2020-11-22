package ch.entwicklung.roos.tobias.databaseapi;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    private static Database instance;

    private final String hostname;
    private final int port;
    private final String database;
    private final String passwd;
    private final String username;
    private Connection connection;

    private Database() {
        hostname   = "hostname";
        port       = 3306;
        database   = "database";
        passwd     = "password";
        username   = "username";

        try {
            openConnection();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Get current Database instance
     * @return Returns current instance of Database class
     */
    public static Database getInstance() {
        if (Database.instance == null) {
            Database.instance = new Database();
        }
        return Database.instance;
    }

    /**
     * Opens Connection
     * @throws SQLException Falls die Connection nicht aufgebaut werden kann.
     * @throws ClassNotFoundException Falls die Klasse "com.mysql.jdbc.Driver" nicht gefunden werden kann.
     */
    private void openConnection() throws SQLException, ClassNotFoundException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (this.connection != null && !this.connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.username, this.passwd);
        }
    }

    /**
     * Get Connection to Database
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Builds select statement
     * @param was
     * @param tabellen
     * @param einschraenkungen
     * @param zusatzangaben
     * @return SQL Query
     */
    protected String build_select_statement(String was, String tabellen, String einschraenkungen, String zusatzangaben) {
        if (einschraenkungen == null) einschraenkungen = "";
        if (zusatzangaben == null) zusatzangaben = "";

        String sql_query = "SELECT " + was + " FROM " + tabellen;
        if (einschraenkungen != "") {
            sql_query += " WHERE " + einschraenkungen;
        }
        if (zusatzangaben != "") {
            sql_query += " " + zusatzangaben;
        }
        return sql_query;
    }

    /**
     * Builds delete statement
     * @param tabellen
     * @param einschraenkungen
     * @param zusatzangaben
     * @return SQL Query
     */
    protected String build_delete_statement(String tabellen, String einschraenkungen) {
        if (einschraenkungen == null) einschraenkungen = "";

        String sql_query = "DELETE FROM " + tabellen;
        if (einschraenkungen != "") {
            sql_query += " WHERE " + einschraenkungen;
        }
        return sql_query;
    }

    /**
     * Builds insert statement
     * @param was
     * @param tabellen
     * @return SQL Query
     */
    protected String build_insert_statement(String was, String tabellen) {
        int count = was.split(",").length;
        String questionmarks = " ";
        for (int i = 0; i < count; i++) {
            questionmarks += "?";
            if(i<count-1) questionmarks += ", ";
        }
        String sql_query = "INSERT INTO " + tabellen + " ( " + was + " ) VALUES (" + questionmarks + ")" ;
        System.out.println(sql_query);
        return sql_query;
    }

    /**
     * Builds update statement
     * @param was
     * @param tabellen
     * @param einschraenkungen
     * @param zusatzangaben
     * @return SQL Query
     */
    protected String build_update_statement(String was, String tabellen, String einschraenkungen) {
        if (einschraenkungen == null) einschraenkungen = "";

        String sql_query = "UPDATE " + tabellen + " SET " + was + " ";
        if (einschraenkungen != "") {
            sql_query += " WHERE " + einschraenkungen;
        }
        return sql_query;
    }

    /**
     * Runs database delete
     * @param tabellen
     * @param einschraenkungen
     * @param vars
     * @param zusatzangaben
     * @return boolean success
     * @throws SQLException
     */
    public boolean delete(String tabellen, String einschraenkungen, Object[] vars) throws SQLException {
        if (vars == null) {
            vars = new Object[]{};
        }
        Database db = Database.getInstance();
        String query = db.build_delete_statement(tabellen, einschraenkungen);
        PreparedStatement statement = db.preparePreparedStatement(db.getConnection().prepareStatement(query), vars);
        int rowCount = statement.executeUpdate();
        return rowCount > 0;
    }

    /**
     * Runs database update
     * @param was
     * @param tabellen
     * @param einschraenkungen
     * @param vars
     * @param zusatzangaben
     * @return boolean success
     * @throws SQLException
     */
    public boolean update(String was, String tabellen, String einschraenkungen, Object[] vars) throws SQLException {
        if (vars == null) {
            vars = new Object[]{};
        }
        Database db = Database.getInstance();
        String query = db.build_update_statement(was, tabellen, einschraenkungen);
        PreparedStatement statement = db.preparePreparedStatement(db.getConnection().prepareStatement(query), vars);
        int rowCount = statement.executeUpdate();
        return rowCount > 0;
    }

    /**
     * Runs database insert
     * @param was
     * @param tabellen
     * @param vars
     * @return boolean success
     * @throws SQLException
     */
    public boolean insert(String was, String tabellen, Object[] vars) throws SQLException {
        if (vars == null) {
            vars = new Object[]{};
        }
        Database db = Database.getInstance();
        String query = db.build_insert_statement(was, tabellen);
        PreparedStatement statement = db.preparePreparedStatement(db.getConnection().prepareStatement(query), vars);
        int rowCount = statement.executeUpdate();
        return rowCount > 0;
    }

    /**
     * Runs insert
     * @param was
     * @param tabellen
     * @param einschraenkungen
     * @param vars
     * @param zusatzangaben
     * @return Hashmap with key rownumber and value as ArrayList
     * @throws SQLException
     */
    public HashMap<Integer, ArrayList> select(String was, String tabellen, String einschraenkungen, Object[] vars, String zusatzangaben) throws SQLException {
        if (vars == null) {
            vars = new Object[]{};
        }
        HashMap<Integer,ArrayList> result = new HashMap<>();
        Database db = Database.getInstance();
        String query = db.build_select_statement(was, tabellen, einschraenkungen, zusatzangaben);
        PreparedStatement statement = db.preparePreparedStatement(db.getConnection().prepareStatement(query), vars);

        ResultSet rs = statement.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int index = 0;
        while (rs.next()) {
            int j = rsmd.getColumnCount();
            ArrayList<Object> row = new ArrayList<>();
            for (int i = 0; i < j; i++) {
                row.add(rs.getObject(i+1));
            }
            result.put(index, row);
            index++;
        }

        return result;
    }

    /**
     * Prepares the PreparedStatement
     * @param statement
     * @param vars
     * @return Prepared PreparedStatement
     * @throws SQLException
     */
    protected PreparedStatement preparePreparedStatement(PreparedStatement statement, Object[] vars) throws SQLException {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] instanceof String) {
                statement.setString(i + 1, (String) vars[i]);
            } else if (vars[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) vars[i]);
            } else if (vars[i] instanceof Double) {
                statement.setDouble(i + 1, (Double) vars[i]);
            } else if (vars[i] instanceof Float) {
                statement.setFloat(i + 1, (Float) vars[i]);
            } else {
                statement.setObject(i + 1, vars[i]);
            }
        }
        return statement;
    }

    /**
     * Counts occurences of a string in haystack
     * @param needle
     * @param haystack
     * @return occurences
     */
    protected int get_occurences(String needle, String haystack) {
        int numberOfOccurences = 0;
        int index = haystack.indexOf(needle);
        while (index != -1)
        {
            numberOfOccurences++;
            haystack = haystack.substring(index+needle.length());
            index = haystack.indexOf(needle);
        }
        return numberOfOccurences;
    }

}
