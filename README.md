# Tubes-PBOL - Sistem Reservasi Meja Restaurant "Ten Out of Ten"

## Deskripsi Proyek

Proyek ini adalah aplikasi desktop yang dibuat menggunakan Java Swing untuk mengelola reservasi meja di restaurant "Ten Out of Ten". Aplikasi ini dirancang agar pelanggan dapat dengan mudah melakukan pemesanan meja secara digital, sementara pihak admin restaurant dapat memantau dan mengelola semua reservasi yang masuk melalui dashboard khusus.

Tujuan utama dari aplikasi ini adalah untuk mempermudah proses reservasi yang sebelumnya dilakukan secara manual, sehingga lebih efisien dan terorganisir dengan baik.

## Fitur Utama

### Fitur untuk Pelanggan

Pelanggan yang menggunakan aplikasi ini dapat melakukan beberapa hal berikut:

- **Registrasi dan Login** - Pelanggan harus membuat akun terlebih dahulu sebelum bisa melakukan reservasi. Sistem login menggunakan enkripsi BCrypt untuk menjaga keamanan password.
- **Reservasi Meja Regular** - Tersedia berbagai pilihan meja regular seperti meja A1 sampai A5, B1 sampai B5, C1 sampai C3, dan D1 sampai D3. Biaya reservasi untuk meja regular adalah Rp 10.000.
- **Reservasi Meja VIP** - Bagi pelanggan yang menginginkan pengalaman lebih eksklusif, tersedia ruang VIP dengan biaya reservasi Rp 50.000.
- **Melihat Riwayat Reservasi** - Pelanggan dapat melihat kembali daftar reservasi yang pernah mereka buat sebelumnya.
- **Memilih Waktu Reservasi** - Pelanggan bebas memilih tanggal dan jam reservasi sesuai keinginan, dengan jam operasional dari pukul 09.00 sampai 18.00.

### Fitur untuk Admin

Admin restaurant memiliki akses ke fitur-fitur berikut:

- **Dashboard Admin** - Admin dapat melihat seluruh data reservasi yang masuk dalam bentuk tabel yang rapi dan mudah dibaca.
- **Filter Data** - Admin bisa memfilter data reservasi berdasarkan tanggal atau nama meja tertentu untuk mempermudah pencarian.
- **Mengelola Reservasi** - Admin dapat menyetujui atau menghapus reservasi sesuai kebutuhan.
- **Update Data Real-time** - Data pada dashboard akan diperbarui secara otomatis sehingga admin selalu melihat informasi terbaru.

## Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan beberapa teknologi sebagai berikut:

| Komponen                 | Teknologi                |
| ------------------------ | ------------------------ |
| Bahasa Pemrograman       | Java                     |
| Framework Antarmuka      | Java Swing               |
| Database                 | MySQL                    |
| Enkripsi Password        | BCrypt (jbcrypt-0.4.jar) |
| Komponen Pemilih Tanggal | JCalendar (JDateChooser) |
| IDE Pengembangan         | NetBeans                 |

## Struktur Proyek

Berikut adalah susunan folder dan file dalam proyek ini:

```
src/
├── TubesPBOL.java          # Class utama aplikasi
├── service/
│   ├── AuthService.java     # Menangani proses login dan registrasi
│   ├── DBConnection.java    # Mengelola koneksi ke database MySQL
│   ├── DBSeeder.java        # Membuat data admin default
│   └── ReservasiService.java # Menangani semua operasi reservasi
├── tampilan/
│   ├── AdminReservasi.java  # Halaman dashboard untuk admin
│   ├── home.java            # Halaman utama yang menampilkan denah meja
│   ├── Login.java           # Halaman untuk login
│   ├── LoginAndSignUp.java  # Halaman gabungan login dan daftar
│   ├── reservasi.java       # Form untuk reservasi meja regular
│   ├── RiwayatReservasi.java # Halaman riwayat reservasi pengguna
│   ├── SignUp.java          # Halaman untuk mendaftar akun baru
│   └── vip.java             # Form untuk reservasi meja VIP
└── util/
    └── UserSession.java     # Mengelola sesi pengguna yang sedang login
```

### Struktur Tabel Database

Aplikasi ini menggunakan dua tabel utama:

**Tabel users** - Menyimpan data pengguna

| Kolom      | Tipe Data        | Keterangan                         |
| ---------- | ---------------- | ---------------------------------- |
| id         | INT              | Nomor identitas unik (Primary Key) |
| full_name  | VARCHAR          | Nama lengkap pengguna              |
| email      | VARCHAR (UNIQUE) | Alamat email (tidak boleh sama)    |
| password   | VARCHAR          | Password yang sudah dienkripsi     |
| user_level | ENUM             | Level akses: 'user' atau 'admin'   |

**Tabel reservations** - Menyimpan data reservasi

| Kolom          | Tipe Data | Keterangan                                   |
| -------------- | --------- | -------------------------------------------- |
| id             | INT       | Nomor identitas unik (Primary Key)           |
| user_email     | VARCHAR   | Email pengguna yang memesan                  |
| meja_name      | VARCHAR   | Nama meja yang dipesan (contoh: A1, B2, VIP) |
| reservasi_date | DATE      | Tanggal reservasi                            |
| start_time     | TIME      | Jam mulai reservasi                          |
| end_time       | TIME      | Jam selesai reservasi                        |
| status         | ENUM      | Status reservasi: 'PENDING' atau 'APPROVED'  |

## Cara Menjalankan Aplikasi

### Yang Perlu Disiapkan

Sebelum menjalankan aplikasi, pastikan komputer sudah terinstall:

1. Java JDK versi 8 atau lebih baru
2. MySQL Server yang sudah berjalan
3. NetBeans IDE (opsional, tapi direkomendasikan untuk pengembangan)

### Langkah-langkah Instalasi

Pertama, clone atau download repository ini ke komputer:

```bash
git clone <repository-url>
```

Kedua, buat database baru di MySQL dengan nama "tenoutoften":

```sql
CREATE DATABASE tenoutoften;
```

Ketiga, buat tabel-tabel yang diperlukan dengan menjalankan query berikut:

```sql
USE tenoutoften;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_level ENUM('user', 'admin') DEFAULT 'user'
);

CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(100) NOT NULL,
    meja_name VARCHAR(20) NOT NULL,
    reservasi_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'APPROVED') DEFAULT 'PENDING'
);
```

Keempat, tambahkan library berikut ke dalam project:

- `jbcrypt-0.4.jar` untuk enkripsi password
- `jcalendar-1.4.jar` untuk komponen pemilih tanggal
- `mysql-connector-java.jar` untuk koneksi ke database MySQL

Kelima, buka project menggunakan NetBeans, lalu jalankan file `LoginAndSignUp.java` atau lakukan build pada project.

### Akun Admin Bawaan

Setelah menjalankan DBSeeder, sistem akan membuat akun admin default dengan kredensial:

- Email: admin@gmail.com
- Password: Admin123

## Fitur Keamanan

Aplikasi ini dilengkapi dengan beberapa fitur keamanan untuk melindungi data pengguna:

- Password pengguna tidak disimpan dalam bentuk asli, melainkan dienkripsi menggunakan algoritma BCrypt sebelum disimpan ke database.
- Semua query database menggunakan PreparedStatement untuk mencegah serangan SQL Injection.
- Sistem menggunakan Singleton Pattern untuk mengelola sesi pengguna, sehingga hanya satu instance session yang aktif dalam satu waktu.
- Terdapat validasi input baik di sisi aplikasi maupun di sisi database untuk memastikan data yang masuk valid.
- Setiap akun hanya diperbolehkan memiliki satu reservasi aktif dalam satu waktu untuk mencegah penyalahgunaan sistem.

## Tim Pengembang

Proyek ini dibuat sebagai Tugas Besar untuk mata kuliah Pemrograman Berorientasi Objek Lanjut (PBOL).
