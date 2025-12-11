package util;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * User Session Manager - Mengelola sesi user yang sedang login
 * 
 * Implementasi Singleton Pattern untuk menyimpan data user yang sedang aktif:
 * - Email user yang login
 * - Level user (admin/user)
 * 
 * Metode penting:
 * - createSession(email, level): Membuat sesi baru saat login berhasil
 * - clearSession(): Menghapus sesi saat logout
 * - getInstance(): Mendapatkan instance sesi yang aktif
 * 
 * Penggunaan:
 * - Login: UserSession.createSession(email, level)
 * - Logout: UserSession.clearSession()
 * - Cek user: UserSession.getInstance().getUserEmail()
 * 
 * @author ASUS
 * @version 1.1
 */
public class UserSession {
    // 1. Variabel statis untuk menyimpan instance tunggal
    private static UserSession instance; 
    
    // 2. Data yang perlu disimpan
    private String userEmail; 
    private String userLevel;

    // 3. Private Constructor (mencegah pembuatan instance baru)
    private UserSession(String email, String level) {
        this.userEmail = email;
        this.userLevel = level;
    }

    // 4. Metode untuk membuat/mendapatkan session saat login
    public static void createSession(String email, String level) {
        instance = new UserSession(email, level);
    }
    
    // 5. Metode untuk membersihkan session saat logout
    public static void clearSession() {
        instance = null;
    }

    // 6. Metode untuk mendapatkan instance session
    public static UserSession getInstance() {
        return instance;
    }

    // 7. Getters untuk data
    public String getUserEmail() {
        return userEmail;
    }
    
    public String getUserLevel() {
        return userLevel;
    }
}
