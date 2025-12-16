package com.example.soccer.db;

import com.example.soccer.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SQLiteConnectionFactory {

    private SQLiteConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + Config.SQLITE_DB_PATH;
        return DriverManager.getConnection(url);
    }
}




