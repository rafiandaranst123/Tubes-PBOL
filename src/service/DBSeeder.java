/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import service.DBConnection;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Database Seeder - Tool untuk mengisi data awal (seed data) ke database
 * 
 * Fungsi utama:
 * - Membuat akun admin default untuk sistem
 * - Mencegah duplikasi data admin
 * - Menggunakan password hashing dengan BCrypt untuk keamanan
 * 
 * Data Admin Default:
 * - Email: admin@gmail.com
 * - Password: Admin123 (di-hash dengan BCrypt)
 * - Full Name: Admin Utama
 * - User Level: admin
 * 
 * Cara penggunaan:
 * - Panggil DBSeeder.seedAdminUser() saat aplikasi pertama kali dijalankan
 * - Seeder akan otomatis mengecek apakah admin sudah ada
 * - Jika sudah ada, tidak akan membuat duplikat
 * 
 * Keamanan:
 * - Password tidak disimpan dalam plain text
 * - Menggunakan BCrypt.hashpw() untuk hashing
 * - Password default harus diganti setelah login pertama
 * 
 * @author ASUS
 * @version 1.1
 */
public class DBSeeder {    
    /**
     * Seed akun admin default ke database.
     * 
     * Method ini akan:
     * 1. Mengecek apakah admin dengan email tersebut sudah ada
     * 2. Jika belum ada, membuat akun admin baru
     * 3. Hash password menggunakan BCrypt
     * 4. Insert data admin ke tabel users dengan level 'admin'
     * 
     * Koneksi Database:
     * - Menggunakan DBConnection.getConnection()
     * - PreparedStatement untuk keamanan SQL injection
     * - Auto-close connection setelah selesai
     */
    public static void seedAdminUser() {
        // Data admin yang ingin dimasukkan
        String email = "admin@gmail.com";
        String plainPassword = "Admin123"; // Password default yang aman
        String fullName = "Admin Utama";

        // Cek apakah admin sudah ada untuk mencegah duplikasi
        if (isAdminExists(email)) {
            System.out.println("Seeder: Admin sudah ada. Tidak ada data yang dimasukkan.");
            return;
        }

        // Hashing Password menggunakan BCrypt untuk keamanan
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        String sql = "INSERT INTO users (full_name, email, password, user_level) VALUES (?, ?, ?, 'admin')";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword); // Insert password yang sudah di-hash

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

    /**
     * Helper method untuk mengecek apakah admin sudah ada di database.
     * 
     * @param email Email admin yang akan dicek
     * @return true jika admin dengan email tersebut sudah ada, false jika belum
     */
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

