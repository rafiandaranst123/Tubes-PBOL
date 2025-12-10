/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import service.DBConnection; 

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
     * Melakukan verifikasi login.
     * @return user_level ('user' atau 'admin') jika berhasil, null jika gagal.
     */
    public String loginUser(String email, String password) {
        String userLevel = null;
        // Ambil password HASH dan user_level dari kolom 'password'
        String sql = "SELECT password, user_level FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password"); // Ambil hash dari DB
                String level = rs.getString("user_level"); // Ambil level
                
                // Verifikasi Password dengan BCrypt
                if (BCrypt.checkpw(password, storedHash)) {
                    userLevel = level;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saat login: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            DBConnection.closeConnection(conn);
        }
        
        return userLevel;
    }
}