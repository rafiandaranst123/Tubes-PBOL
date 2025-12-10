package tampilan;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import service.ReservasiService;
import com.toedter.calendar.JDateChooser;
import javax.swing.GroupLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.sql.Time;
import java.util.Calendar;

/**
 *
 * @author Acer
 */
public class AdminReservasi extends JFrame {   // DI SINI DITAMBAH extends JFrame

    private JLabel lblTitle;
    private JLabel lblTanggal;
    private JLabel lblMeja;
    private JDateChooser dateChooser;
    private JComboBox<String> comboMeja;
    private JButton btnTampilkan;
    private JTable tableReservasi;
    private JScrollPane scrollTable;

    // Panel detail (opsional)
    private JLabel lblDetail;
    private JLabel lblNama;
    private JLabel lblJamMulai;
    private JLabel lblJamSelesai;
    private JTextField txtNama;
    private JTextField txtJamMulai;
    private JTextField txtJamSelesai;
    private JButton btnSimpan;
    private JButton btnHapus;

    private int idReservasiTerpilih = -1;
    private Object[][] dataReservasiMentah;
    private JLabel lblCountdown; // <-- Label baru untuk menampilkan countdown
    private Timer countdownTimer;

    private void loadReservasiData() {
        ReservasiService rs = new ReservasiService();

        // 1. Ambil nilai filter dari komponen
        String meja = (String) comboMeja.getSelectedItem(); // Nilai: "A1" atau "Semua Meja"

        // JDateChooser mengembalikan java.util.Date atau null. TIDAK PERLU DIUBAH,
        // langsung kirimkan nilai yang didapat.
        Date tanggal = dateChooser.getDate();

        // 2. Panggil Service dengan filter
        Object[][] rowData = rs.getFilteredReservations(meja, tanggal); // <-- LURUSKAN NILAI NULL
        dataReservasiMentah = rowData;

        // 3. Update Model Tabel (Logika pembersihan dan penambahan baris sama seperti sebelumnya)
        DefaultTableModel model = (DefaultTableModel) tableReservasi.getModel();
        model.setRowCount(0);

        String[] columnNames = {"Meja", "Tanggal", "Jam Mulai", "Jam Selesai", "Nama Pemesan", "Status"};
        model.setColumnIdentifiers(columnNames);

        for (Object[] row : rowData) {
            Object[] displayRow = new Object[6];
            System.arraycopy(row, 0, displayRow, 0, 6);
            model.addRow(displayRow);
        }
    }

    private void tableReservasiMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = tableReservasi.getSelectedRow();

        if (selectedRow != -1) {
            // Ambil data lengkap dari dataReservasiMentah
            Object[] dataLengkap = dataReservasiMentah[selectedRow];
            idReservasiTerpilih = (int) dataLengkap[6];

            // Ambil data penting
            String status = dataLengkap[5].toString();
            java.util.Date reservasiDate = (java.util.Date) dataLengkap[1]; // Tanggal
            java.sql.Time startTime = (java.sql.Time) dataLengkap[2]; // Jam Mulai
            java.sql.Time endTime = (java.sql.Time) dataLengkap[3];

            // Isi field detail (sama seperti sebelumnya)
            txtNama.setText(dataLengkap[4].toString());
            txtJamMulai.setText(startTime.toString());
            txtJamSelesai.setText(dataLengkap[3].toString());

            // Cek apakah timer yang lama berjalan, jika ya, hentikan
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }

            if (status.equals("APPROVED")) {
                java.util.Date startDateTime = combineDateAndTime(reservasiDate, startTime);
                java.util.Date endDateTime = combineDateAndTime(reservasiDate, endTime); // <-- Gabungkan Tanggal & Jam Selesai

                if (startDateTime.after(new java.util.Date())) {
                    // KASUS 1: BELUM MULAI -> Count Down to Start
                    startCountdownTimer(startDateTime, null); // Kirim waktu mulai sebagai target
                } else if (endDateTime.after(new java.util.Date())) {
                    // KASUS 2: SUDAH MULAI, TAPI BELUM SELESAI -> Count Down to End
                    startCountdownTimer(endDateTime, "RUNNING"); // Kirim waktu selesai sebagai target + status RUNNING
                } else {
                    // KASUS 3: SUDAH SELESAI
                    lblCountdown.setText("Reservasi Sudah Selesai");
                }
            } else {
                lblCountdown.setText("Status Reservasi: " + status);
            }
        }
    }

    private void startCountdownTimer(final java.util.Date targetTime, final String mode) {
        // Cek apakah timer lama berjalan, jika ya, hentikan
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        // Timer akan berjalan setiap 1000 milidetik (1 detik)
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long remainingTimeMillis = targetTime.getTime() - new java.util.Date().getTime();
                String prefix;

                if (remainingTimeMillis <= 0) {
                    // Countdown selesai
                    countdownTimer.stop();

                    if ("RUNNING".equals(mode)) {
                        // Jika mode RUNNING, berarti waktu selesai sudah tiba
                        lblCountdown.setText("RESERVASI TELAH BERAKHIR!");
                    } else {
                        // Jika mode lain, berarti reservasi baru saja dimulai
                        lblCountdown.setText("RESERVASI TELAH DIMULAI!");
                    }

                    loadReservasiData();
                    return;
                }

                // Atur prefix pesan
                if ("RUNNING".equals(mode)) {
                    prefix = "Reservasi berakhir dalam: ";
                } else {
                    prefix = "Waktu menuju reservasi: ";
                }

                // Hitung sisa waktu
                long diffSeconds = remainingTimeMillis / 1000 % 60;
                long diffMinutes = remainingTimeMillis / (60 * 1000) % 60;
                long diffHours = remainingTimeMillis / (60 * 60 * 1000); // Tidak perlu % 24 jika durasi bisa > 24 jam

                // Format output countdown
                String countdownText = String.format("%02d Jam, %02d Menit, %02d Detik",
                        diffHours, diffMinutes, diffSeconds);

                lblCountdown.setText(prefix + countdownText);
            }
        });
        countdownTimer.start();
    }

    private java.util.Date combineDateAndTime(java.util.Date date, java.sql.Time time) { // <-- Pastikan tipe datanya benar
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Set jam, menit, detik dari java.sql.Time
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private void startCountdownTimer(final java.util.Date targetTime) {
        // Timer akan berjalan setiap 1000 milidetik (1 detik)
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long remainingTimeMillis = targetTime.getTime() - new java.util.Date().getTime();

                if (remainingTimeMillis <= 0) {
                    // Countdown selesai, hentikan timer
                    countdownTimer.stop();
                    lblCountdown.setText("RESERVASI TELAH DIMULAI!");
                    loadReservasiData(); // Muat ulang tabel untuk melihat status FINISHED (jika scheduler berjalan)
                    return;
                }

                // Hitung sisa waktu (Hari, Jam, Menit, Detik)
                long diffSeconds = remainingTimeMillis / 1000 % 60;
                long diffMinutes = remainingTimeMillis / (60 * 1000) % 60;
                long diffHours = remainingTimeMillis / (60 * 60 * 1000) % 24;
                long diffDays = remainingTimeMillis / (24 * 60 * 60 * 1000);

                // Format output countdown
                String countdownText = String.format("%d Hari, %02d Jam, %02d Menit, %02d Detik",
                        diffDays, diffHours, diffMinutes, diffSeconds);

                lblCountdown.setText("Waktu Menuju Reservasi: " + countdownText);
            }
        });
        countdownTimer.start();
    }

    private void btnTolakReservasiActionPerformed(java.awt.event.ActionEvent evt) {
        if (idReservasiTerpilih == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih baris reservasi yang ingin ditolak.");
            return;
        }

        ReservasiService rs = new ReservasiService();
        String currentStatus = rs.getReservationStatus(idReservasiTerpilih); // <-- Ambil status saat ini

        // --- LOGIKA PEMBATASAN PENOLAKAN ---
        if (currentStatus == null || currentStatus.equals("ERROR_DB")) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil status reservasi.");
            return;
        }

        // Hanya PENDING yang boleh ditolak
        if (!currentStatus.equals("PENDING")) {
            JOptionPane.showMessageDialog(this,
                    "Reservasi sudah berada di status '" + currentStatus + "'. Tidak dapat ditolak lagi.",
                    "Aksi Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // --- AKHIR LOGIKA PEMBATASAN ---

        int confirm = JOptionPane.showConfirmDialog(this,
                "Anda yakin ingin menolak reservasi ID " + idReservasiTerpilih + "?",
                "Konfirmasi Penolakan", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Jika status PENDING dan dikonfirmasi untuk ditolak:
            boolean success = rs.updateReservationStatus(idReservasiTerpilih, "NOT APPROVED");

            if (success) {
                JOptionPane.showMessageDialog(this, "Reservasi ID " + idReservasiTerpilih + " berhasil ditolak (NOT APPROVED).");
                loadReservasiData();
                idReservasiTerpilih = -1;
                txtNama.setText("");
                txtJamMulai.setText("");
                txtJamSelesai.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menolak reservasi.");
            }
        }
    }

    public AdminReservasi() {
        initComponents();
        setTitle("Halaman Admin Reservasi");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        loadReservasiData(); // <--- Panggil di sini
    }

    private void initComponents() {
        // Inisialisasi komponen
        lblTitle = new JLabel("Halaman Admin Reservasi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        lblTanggal = new JLabel("Tanggal:");
        lblMeja = new JLabel("Meja:");

        dateChooser = new JDateChooser();
        comboMeja = new JComboBox<>(new String[]{
            "Semua Meja", "A1", "A4", "A5", "B1", "B3", "B5", "C1", "C2", "C3", "D1", "D2", "VIP"
        });
        btnTampilkan = new JButton("Tampilkan");
        btnTampilkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTampilkanActionPerformed(evt); // <--- Memanggil metode yang sudah ada
            }
        });

        // Tabel
        tableReservasi = new JTable();
        tableReservasi.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Meja", "Tanggal", "Jam Mulai", "Jam Selesai", "Nama Pemesan", "Status"
                }
        ));
        scrollTable = new JScrollPane(tableReservasi);

        tableReservasi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableReservasiMouseClicked(evt);
            }
        });

        // Panel detail
        lblDetail = new JLabel("Detail Reservasi");
        lblDetail.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblNama = new JLabel("Nama Pemesan:");
        lblJamMulai = new JLabel("Jam Mulai:");
        lblJamSelesai = new JLabel("Jam Selesai:");

        txtNama = new JTextField();
        txtJamMulai = new JTextField();
        txtJamSelesai = new JTextField();

        btnSimpan = new JButton("Konfirmasi Reservasi");
        btnHapus = new JButton("Tolak Reservasi");
        lblCountdown = new JLabel("Waktu Menuju Reservasi: --:--:--"); // <-- Inisialisasi Label

        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (idReservasiTerpilih == -1) {
                    JOptionPane.showMessageDialog(AdminReservasi.this, "Silakan pilih baris reservasi.");
                    return;
                }

                ReservasiService rs = new ReservasiService();
                String currentStatus = rs.getReservationStatus(idReservasiTerpilih); // <-- Ambil status saat ini

                // --- LOGIKA PEMBATASAN KONFIRMASI ---
                if (currentStatus == null || currentStatus.equals("ERROR_DB")) {
                    JOptionPane.showMessageDialog(AdminReservasi.this, "Gagal mengambil status reservasi.");
                    return;
                }

                // Hanya PENDING yang boleh dikonfirmasi
                if (!currentStatus.equals("PENDING")) {
                    JOptionPane.showMessageDialog(AdminReservasi.this,
                            "Reservasi sudah berada di status '" + currentStatus + "'. Tidak dapat dikonfirmasi lagi.",
                            "Aksi Ditolak", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // --- AKHIR LOGIKA PEMBATASAN ---

                // Jika status adalah PENDING, lanjutkan proses konfirmasi:
                int confirm = JOptionPane.showConfirmDialog(AdminReservasi.this, "Anda yakin ingin mengkonfirmasi reservasi ID " + idReservasiTerpilih + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = rs.updateReservationStatus(idReservasiTerpilih, "APPROVED");

                    if (success) {
                        JOptionPane.showMessageDialog(AdminReservasi.this, "Reservasi ID " + idReservasiTerpilih + " berhasil dikonfirmasi!");
                        loadReservasiData();
                        idReservasiTerpilih = -1;
                        txtNama.setText("");
                        txtJamMulai.setText("");
                        txtJamSelesai.setText("");
                    } else {
                        JOptionPane.showMessageDialog(AdminReservasi.this, "Gagal mengkonfirmasi reservasi.");
                    }
                }
            }
        });

        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTolakReservasiActionPerformed(evt);
            }
        });

        // Layout filter
        JPanel panelFilter = new JPanel();
        GroupLayout filterLayout = new GroupLayout(panelFilter);
        panelFilter.setLayout(filterLayout);
        filterLayout.setAutoCreateGaps(true);
        filterLayout.setAutoCreateContainerGaps(true);

        filterLayout.setHorizontalGroup(
                filterLayout.createSequentialGroup()
                        .addComponent(lblTanggal)
                        .addComponent(dateChooser, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(lblMeja)
                        .addComponent(comboMeja, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(btnTampilkan)
        );

        filterLayout.setVerticalGroup(
                filterLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(lblTanggal)
                        .addComponent(dateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblMeja)
                        .addComponent(comboMeja, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnTampilkan)
        );

        // Layout detail
        JPanel panelDetail = new JPanel();
        GroupLayout detailLayout = new GroupLayout(panelDetail);
        panelDetail.setLayout(detailLayout);
        detailLayout.setAutoCreateGaps(true);
        detailLayout.setAutoCreateContainerGaps(true);

        detailLayout.setHorizontalGroup(
                detailLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblDetail)
                        .addGroup(detailLayout.createSequentialGroup()
                                .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblNama)
                                        .addComponent(lblJamMulai)
                                        .addComponent(lblJamSelesai)
                                        .addComponent(lblCountdown))
                                .addGap(10)
                                .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(txtNama)
                                        .addComponent(txtJamMulai)
                                        .addComponent(txtJamSelesai)))
                        .addGroup(detailLayout.createSequentialGroup()
                                .addComponent(btnSimpan)
                                .addGap(18)
                                .addComponent(btnHapus))
        );

        detailLayout.setVerticalGroup(
                detailLayout.createSequentialGroup()
                        .addComponent(lblDetail)
                        .addGap(5)
                        .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblNama)
                                .addComponent(txtNama, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblJamMulai)
                                .addComponent(txtJamMulai, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblJamSelesai)
                                .addComponent(txtJamSelesai, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(10)
                        .addComponent(lblCountdown)
                        .addGap(10)
                        .addGroup(detailLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSimpan)
                                .addComponent(btnHapus))
        );

        // Layout utama
        JPanel mainPanel = new JPanel();
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);
        mainLayout.setAutoCreateGaps(true);
        mainLayout.setAutoCreateContainerGaps(true);

        mainLayout.setHorizontalGroup(
                mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelFilter)
                        .addComponent(scrollTable)
                        .addComponent(panelDetail)
        );

        mainLayout.setVerticalGroup(
                mainLayout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(10)
                        .addComponent(panelFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(10)
                        .addComponent(scrollTable, 200, 200, Short.MAX_VALUE)
                        .addGap(10)
                        .addComponent(panelDetail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        pack();
    }

    private void btnTampilkanActionPerformed(java.awt.event.ActionEvent evt) {
        // Untuk sekarang, tombol ini akan memuat SEMUA data
        loadReservasiData();
    }

    public static void main(String[] args) {
        // Opsional: set Look and Feel ke Nimbus kalau ada
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Abaikan, gunakan look & feel default
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminReservasi().setVisible(true);
            }
        });
    }
}
