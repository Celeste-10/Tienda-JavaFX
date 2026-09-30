package com.uam.tiendajavafx.util;

/** Prueba independiente para el Paso 8 de la guía. */
public final class ConnectionTest {
    private ConnectionTest() {}

    public static void main(String[] args) {
        if (DatabaseConnection.testConnection()) {
            System.out.println("Conexión exitosa");
        } else {
            System.out.println("Error de conexión");
        }
    }
}
