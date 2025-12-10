package util;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ASUS
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
