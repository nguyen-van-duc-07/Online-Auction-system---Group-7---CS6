package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://auction-database-tienhoi2007nguyen-b1fd.c.aivencloud.com:28772/defaultdb?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASS = "AVNS_yIEIvG3JqN_2tiF3H6D";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}