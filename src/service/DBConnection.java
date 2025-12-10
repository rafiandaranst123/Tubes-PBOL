/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author ASUS
 */
public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tenoutoften"; // Nama DB Anda
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Menyediakan objek koneksi ke database.
     * Harus 'static' agar bisa dipanggil tanpa membuat objek DBConnection.
     * @return Connection objek koneksi
     * @throws SQLException jika koneksi gagal
     */
    
    public static Connection getConnection() throws SQLException {
        // Metode ini yang akan dipanggil oleh DBSeeder
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Menutup objek koneksi.
     * Harus 'static'.
     * @param conn Objek Connection yang akan ditutup
     */
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}
