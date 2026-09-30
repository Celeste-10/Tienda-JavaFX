package com.uam.tiendajavafx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexión centralizada Java -> PostgreSQL mediante JDBC. */
public final class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/tienda_javafx";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
