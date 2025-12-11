/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.*;
import java.util.Date;

/**
 * Reservasi Service - Layanan untuk mengelola reservasi meja restaurant
 * 
 * Fitur Utama:
 * - Mengecek apakah akun user memiliki reservasi aktif
 * - Validasi ketersediaan meja berdasarkan tanggal dan waktu
 * - Membuat reservasi baru dengan status PENDING
 * - Menghapus reservasi
 * - Mengambil data reservasi untuk admin dan user
 * 
 * Sistem Pengecekan Reservasi (2 Fase):
 * 
 * FASE 1 - USER LOCK:
 * - Mengecek apakah akun user sudah memiliki reservasi aktif
 * - 1 akun hanya boleh memiliki 1 reservasi aktif (PENDING/APPROVED)
 * - Mencegah user membuat multiple booking
 * - Return: nama meja yang sudah direservasi jika user sudah punya reservasi
 * 
 * FASE 2 - MEJA LOCK:
 * - Mengecek apakah meja sudah dipesan user lain pada waktu yang sama
 * - Validasi overlap waktu reservasi
 * - Mencegah double booking pada meja yang sama
 * 
 * Database Integration:
 * - Menggunakan DBConnection untuk koneksi MySQL
 * - Tabel: reservations
 * - Status: PENDING (menunggu approval), APPROVED (disetujui)
 * 
 * @author ASUS
 * @version 1.1
 */
public class ReservasiService {

    /**
     * Mengecek apakah meja tersedia untuk direservasi.
     * 
     * Method ini melakukan 2 fase pengecekan:
     * 
     * FASE 1 - Pengecekan Apakah Akun Memiliki Reservasi:
     * - Query: SELECT meja_name FROM reservations WHERE user_email = ? AND status IN ('PENDING', 'APPROVED')
     * - Mengecek apakah user_email (dari UserSession) sudah memiliki reservasi aktif
     * - Jika user sudah punya reservasi, return nama meja yang sudah direservasi
     * - Sistem 1 akun = 1 reservasi aktif untuk mencegah spam booking
     * 
     * FASE 2 - Pengecekan Ketersediaan Meja:
     * - Jika user belum punya reservasi, cek apakah meja available pada waktu yang diminta
     * - Validasi overlap waktu dengan reservasi lain pada meja yang sama
     * - Cek konflik jadwal dengan user lain
     * 
     * @param mejaName Nama meja yang ingin direservasi (contoh: "A1", "B2", "VIP")
     * @param date Tanggal reservasi
     * @param startTime Waktu mulai (format: "HH:MM:SS")
     * @param endTime Waktu selesai (format: "HH:MM:SS")
     * @return null jika tersedia, nama meja jika konflik, "ERROR_DB" jika ada error database
     */
    public String isMejaAvailable(String mejaName, Date date, String startTime, String endTime) {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Ambil email user yang sedang login dari UserSession
        String userEmail = util.UserSession.getInstance().getUserEmail();

        // ===============================================
        // FASE 1: USER LOCK - PENGECEKAN APAKAH AKUN MEMILIKI RESERVASI
        // ===============================================
        // Mengecek apakah user ini sudah punya reservasi aktif (PENDING atau APPROVED)
        // 1 AKUN = 1 RESERVASI AKTIF untuk mencegah multiple booking
        // ===============================================
        String sqlUserLock = "SELECT meja_name FROM reservations "
                + "WHERE user_email = ? AND status IN ('PENDING', 'APPROVED')";

        try {
            conn = DBConnection.getConnection();

            // Pengecekan 1: Apakah User Sudah Memiliki Reservasi Aktif?
            pstmt = conn.prepareStatement(sqlUserLock);
            pstmt.setString(1, userEmail); // Set parameter user email

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // User sudah punya reservasi aktif!
                // Ambil nama meja yang sudah direservasi
                String conflictingMeja = rs.getString("meja_name");
                return conflictingMeja; // Return nama meja yang sudah dipesan oleh user ini
            }

            // ===============================================
            // FASE 2: MEJA LOCK (Cek apakah meja ini sudah dipesan orang lain pada waktu ini)
            // ===============================================
            String sqlMejaLock = "SELECT COUNT(*) FROM reservations "
                    + "WHERE meja_name = ? AND reservasi_date = ? AND status IN ('PENDING', 'APPROVED') "
                    + "AND (? < end_time) AND (? > start_time)";

            pstmt = conn.prepareStatement(sqlMejaLock);

            pstmt.setString(1, mejaName);
            pstmt.setDate(2, sqlDate);
            pstmt.setString(3, startTime);
            pstmt.setString(4, endTime);

            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Meja dipesan orang lain, kita kembalikan nama meja yang konflik
                return mejaName; // Meja yang konflik adalah meja yang dicoba dipesan
            }

            return null; // <-- Mengembalikan NULL, artinya Lulus pengecekan (Tersedia)

        } catch (SQLException e) {
            System.err.println("Error saat cek ketersediaan: " + e.getMessage());
            return "ERROR_DB"; // Kembalikan string error jika ada masalah DB
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            DBConnection.closeConnection(conn);
        }
    }

    public boolean createReservation(String userEmail, String mejaName, Date date, String startTime, String endTime) {

        // 1. Convert Date ke java.sql.Date untuk SQL
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        String sql = "INSERT INTO reservations (user_email, meja_name, reservasi_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, userEmail);
            pstmt.setString(2, mejaName);
            pstmt.setDate(3, sqlDate);
            pstmt.setString(4, startTime); // Format harus 'HH:MM:SS' atau 'HH:MM'
            pstmt.setString(5, endTime);   // Format harus 'HH:MM:SS' atau 'HH:MM'

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error saat membuat reservasi: " + e.getMessage());
            // Di sini Anda bisa menambahkan logic untuk cek konflik waktu
            return false;
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

    public Object[][] getFilteredReservations(String filterMeja, Date filterTanggal) {

        String sql = "SELECT meja_name, reservasi_date, start_time, end_time, user_email, status, id_reservasi "
                + "FROM reservations WHERE 1=1 ";

        java.util.List<Object> params = new java.util.ArrayList<>();

        // 1. Tambah Filter Meja (DULUAN)
        if (filterMeja != null && !filterMeja.equals("Semua Meja")) {
            sql += " AND meja_name = ? ";
            params.add(filterMeja);
        }

        // 2. Tambah Filter Tanggal (KEDUA)
        if (filterTanggal != null) {
            sql += " AND reservasi_date = ? ";
            params.add(new java.sql.Date(filterTanggal.getTime()));
        }

        sql += " ORDER BY reservasi_date DESC, start_time ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<Object[]> data = new java.util.ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            // ===============================================
            // BINDING PARAMETER KRITIS: Urutan params harus cocok dengan urutan '?' di SQL
            // ===============================================
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                int paramIndex = i + 1; // Index dimulai dari 1

                if (param instanceof String) {
                    // Ini pasti filterMeja
                    pstmt.setString(paramIndex, (String) param);
                } else if (param instanceof java.sql.Date) {
                    // Ini pasti filterTanggal
                    pstmt.setDate(paramIndex, (java.sql.Date) param);
                }
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("meja_name"),
                    rs.getDate("reservasi_date"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("user_email"),
                    rs.getString("status"),
                    rs.getInt("id_reservasi")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil data reservasi: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            DBConnection.closeConnection(conn);
        }

        return data.toArray(new Object[data.size()][]);
    }

    public boolean updateReservationStatus(int idReservasi, String newStatus) {
        String sql = "UPDATE reservations SET status = ? WHERE id_reservasi = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, idReservasi);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error saat update status reservasi: " + e.getMessage());
            return false;
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

    public Object[][] getReservationsByUser(String userEmail) {
        // Hanya ambil kolom yang dibutuhkan: Meja, Tgl, Jam Mulai, Jam Selesai, Status
        String sql = "SELECT meja_name, reservasi_date, start_time, end_time, status "
                + "FROM reservations WHERE user_email = ? "
                + "ORDER BY reservasi_date DESC, start_time DESC"; // Urutkan dari yang terbaru

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<Object[]> data = new java.util.ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                data.add(new Object[]{
                    rs.getString("meja_name"),
                    rs.getDate("reservasi_date"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error saat mengambil riwayat reservasi: " + e.getMessage());
            return new Object[0][0]; // Kembalikan array kosong jika gagal
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            DBConnection.closeConnection(conn);
        }

        return data.toArray(new Object[data.size()][]);
    }

    /**
     * Mengecek dan mengubah status reservasi yang sudah melewati waktu selesai
     * menjadi 'FINISHED'. Dipanggil secara berkala oleh scheduler.
     */
    public void checkAndFinishReservations() {

        // Query SQL: UPDATE status menjadi 'FINISHED'
        // Kriteria: 
        // 1. Status saat ini masih PENDING atau APPROVED.
        // 2. Tanggal reservasi = Hari ini (CURRENT_DATE()).
        // 3. Waktu selesai (end_time) sudah lewat dari Jam Sekarang (CURRENT_TIME()).
        String sql = "UPDATE reservations SET status = 'FINISHED' "
                + "WHERE status IN ('PENDING', 'APPROVED') "
                + "AND reservasi_date <= CURRENT_DATE() " // Gunakan <= untuk mencakup hari-hari sebelumnya (jika scheduler terlambat jalan)
                + "AND end_time <= CURRENT_TIME()";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Status reservasi otomatis selesai: " + rowsUpdated + " baris diperbarui.");

        } catch (SQLException e) {
            System.err.println("Gagal otomatis menyelesaikan reservasi: " + e.getMessage());
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

    public String getReservationStatus(int idReservasi) {
        String sql = "SELECT status FROM reservations WHERE id_reservasi = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idReservasi);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
            return null; // ID tidak ditemukan

        } catch (SQLException e) {
            System.err.println("Error saat mengambil status reservasi: " + e.getMessage());
            return "ERROR_DB"; // Kembalikan string error jika ada masalah DB
        } finally {
            // Tutup resource
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            DBConnection.closeConnection(conn);
        }
    }

}
