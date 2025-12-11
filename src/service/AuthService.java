/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import service.DBConnection; 

/**
 * Authentication Service - Layanan autentikasi untuk login dan registrasi
 * 
 * Fitur utama:
 * - Registrasi user baru dengan password hashing (BCrypt)
 * - Login user dengan verifikasi password
 * - Validasi kredensial dari database
 * - Menggunakan DBConnection untuk koneksi database
 * - Integrasi dengan UserSession untuk menyimpan data login
 * 
 * Keamanan:
 * - Password di-hash menggunakan BCrypt sebelum disimpan
 * - Validasi input untuk mencegah SQL injection
 * - Penanganan error untuk duplikasi email
 * 
 * @author ASUS
 * @version 1.1
 */
public class AuthService {

    public boolean registerUser(String fullName, String email, String password) {
        // Cek input dasar
        if (fullName == null || email == null || password == null || password.length() < 6) {
             return false;
        }
        
        // Hashing Password
        // Pastikan jbcrypt-0.4.jar sudah terpasang!
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users (full_name, email, password, user_level) VALUES (?, ?, ?, 'user')";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword); // Masukkan hash ke kolom 'password'
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Error 1062 adalah kode MySQL untuk Duplicate Entry (Email UNIQUE)
            if (e.getErrorCode() == 1062) { 
                System.err.println("Registrasi Gagal: Email sudah terdaftar.");
            } else {
                System.err.println("Error saat registrasi: " + e.getMessage());
            }
            return false;
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Melakukan verifikasi login user dan mendeteksi level akses (user/admin).
     * 
     * Proses verifikasi:
     * 1. Mengambil data user dari database berdasarkan email
     * 2. Membandingkan password yang diinput dengan hash di database
     * 3. Mendeteksi level user (user atau admin) dari kolom user_level
     * 4. Mengembalikan user_level jika login berhasil
     * 
     * Koneksi database:
     * - Menggunakan DBConnection.getConnection() untuk koneksi ke MySQL
     * - Query: SELECT password, user_level FROM users WHERE email = ?
     * - PreparedStatement untuk mencegah SQL injection
     * 
     * Deteksi User dan Admin:
     * - Level 'user': User biasa dengan akses terbatas
     * - Level 'admin': Admin dengan akses penuh ke dashboard admin
     * 
     * Security:
     * - Password diverifikasi dengan BCrypt.checkpw()
     * - Password tidak pernah disimpan dalam bentuk plain text
     * 
     * @param email Email user yang akan login
     * @param password Password user dalam plain text
     * @return String 'user' jika user biasa, 'admin' jika admin, null jika login gagal
     */
    public String loginUser(String email, String password) {
        String userLevel = null;
        // Ambil password HASH dan user_level dari kolom 'password'
        String sql = "SELECT password, user_level FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Koneksi ke database menggunakan DBConnection
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password"); // Ambil hash dari DB
                String level = rs.getString("user_level"); // Ambil level (user/admin)
                
                // Verifikasi Password dengan BCrypt
                // Jika password cocok, simpan level untuk deteksi user/admin
                if (BCrypt.checkpw(password, storedHash)) {
                    userLevel = level; // Return level untuk menentukan akses
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saat login: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            DBConnection.closeConnection(conn);
        }
        
        return userLevel; // Return 'user', 'admin', atau null
    }
}