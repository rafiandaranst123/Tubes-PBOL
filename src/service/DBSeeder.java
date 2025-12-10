/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import service.DBConnection;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author ASUS
 */
public class DBSeeder {    
    public static void seedAdminUser() {
        // Data admin yang ingin dimasukkan
        String email = "admin@gmail.com";
        String plainPassword = "Admin123"; // Password default yang aman
        String fullName = "Admin Utama";

        // Cek apakah admin sudah ada
        if (isAdminExists(email)) {
            System.out.println("Seeder: Admin sudah ada. Tidak ada data yang dimasukkan.");
            return;
        }

        // Hashing Password
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        String sql = "INSERT INTO users (full_name, email, password, user_level) VALUES (?, ?, ?, 'admin')";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);

            pstmt.executeUpdate();
            System.out.println("Seeder: Akun admin berhasil dimasukkan.");

        } catch (SQLException e) {
            System.err.println("Seeder Gagal memasukkan Admin: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            DBConnection.closeConnection(conn);
        }
    }

    // Metode helper untuk menghindari duplikasi
    private static boolean isAdminExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saat cek Admin: " + e.getMessage());
        }
        return false;
    }
}

