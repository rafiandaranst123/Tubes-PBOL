/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tampilan;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import service.ReservasiService;
import util.UserSession;

public class RiwayatReservasi extends JFrame {

    private JTable tableRiwayat;
    private DefaultTableModel tableModel;

    public RiwayatReservasi() {
        initComponents();
        loadRiwayatData();
    }

    private void initComponents() {
        setTitle("Riwayat Reservasi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null); 

        // Panel judul
        JPanel panelJudul = new JPanel();
        JLabel labelJudul = new JLabel("Riwayat Reservasi", SwingConstants.CENTER);
        labelJudul.setFont(labelJudul.getFont().deriveFont(18f));
        panelJudul.setLayout(new BorderLayout());
        panelJudul.add(labelJudul, BorderLayout.CENTER);

        // Tabel dan model
        String[] namaKolom = {
            "Meja",
            "Tanggal",
            "Waktu Mulai",
            "Waktu Selesai",
            "Status"
        };

        tableModel = new DefaultTableModel(null, namaKolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabel tidak bisa diedit
            }
        };

        tableRiwayat = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableRiwayat);

        // Layout frame
        setLayout(new BorderLayout());
        add(panelJudul, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadRiwayatData() {
        // Bersihkan isi tabel dulu
        tableModel.setRowCount(0);

        // Cek sesi user
        if (UserSession.getInstance() == null || UserSession.getInstance().getUserEmail() == null) {
            tableModel.addRow(new Object[]{"-", "Silakan login ulang.", "-", "-"});
            return;
        }

        String userEmail = UserSession.getInstance().getUserEmail();
        ReservasiService rs = new ReservasiService();

        // Ambil data dari database
        Object[][] rowData = rs.getReservationsByUser(userEmail);

        // Jika tidak ada data / null
        if (rowData == null || rowData.length == 0) {
            tableModel.addRow(new Object[]{"-", "Anda belum memiliki riwayat reservasi.", "-", "-"});
            return;
        }

        // Tambahkan data ke tabel
        for (Object[] row : rowData) {
            // Antisipasi jika kolom kurang / null
            String meja = (row.length > 0 && row[0] != null) ? row[0].toString() : "-";
            String tanggal = (row.length > 1 && row[1] != null) ? row[1].toString() : "-";
            String waktuMulai = (row.length > 2 && row[2] != null) ? row[2].toString() : "-";
            String waktuSelesai = (row.length > 3 && row[3] != null) ? row[3].toString() : "-";
            String status = (row.length > 4 && row[4] != null) ? row[4].toString() : "-";

            // Kolom tabel: Meja, Waktu Mulai (gabung tanggal + jam), Waktu Selesai, Status
            tableModel.addRow(new Object[]{
                meja,
                tanggal,
                waktuMulai,
                waktuSelesai,
                status
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RiwayatReservasi().setVisible(true);
        });
    }
}
