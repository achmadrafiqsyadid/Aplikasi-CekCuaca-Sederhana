package Tugas6.logika;

import java.io.File;
import java.awt.Image;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;

public class AplikasiCekCuacaHelper {

    private static final String API_KEY = "a3ff1f481cacc5657b9d1035dd71a101";

    // === Mengambil Data dari API ===
    public static String ambilDataCuaca(String kota) {
        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + kota + "&appid=" + API_KEY + "&units=metric";
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            return content.toString();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mengambil data cuaca! Pastikan nama kota benar atau koneksi aktif.");
            return null;
        }
    }

    // === Menampilkan Data Cuaca dan Gambar ===
    public static void tampilkanCuaca(String json, JLabel lblKondisi, JLabel lblSuhu, JPanel panel) {
    try {
        JSONObject obj = new JSONObject(json);
        JSONObject main = obj.getJSONObject("main");
        String kondisi = obj.getJSONArray("weather").getJSONObject(0).getString("main");
        double suhu = main.getDouble("temp");

        // 🟢 Update teks label kondisi dan suhu
        lblKondisi.setText("Kondisi : " + kondisi);
        lblSuhu.setText("Suhu : " + suhu + " °C");

        // === Tentukan gambar sesuai kondisi ===
        String fileGambar = switch (kondisi.toLowerCase()) {
            case "clear" -> "/Tugas6/images/cerah.jpg";
            case "clouds" -> "/Tugas6/images/berawan.jpg";
            case "rain" -> "/Tugas6/images/hujan.jpg";
            case "storm", "thunderstorm" -> "/Tugas6/images/badai.jpg";
            case "drizzle" -> "/Tugas6/images/gerimis.jpg";
            case "fog", "mist", "haze" -> "/Tugas6/images/kabut.jpg";
            default -> null;
        };

        if (fileGambar != null) {
            java.net.URL imgURL = AplikasiCekCuacaHelper.class.getResource(fileGambar);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);

                // 🔹 Ambil ukuran panel untuk resize gambar
                int panelWidth = panel.getWidth();
                int panelHeight = panel.getHeight() - 50; // beri ruang untuk label di atas

                Image scaledImage = icon.getImage().getScaledInstance(panelWidth, panelHeight, java.awt.Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                // 🔹 Tambahkan gambar ke bawah label
                panel.removeAll();

                // Buat layout vertikal agar label dan gambar tidak saling tumpuk
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JPanel infoPanel = new JPanel();
                infoPanel.setBackground(panel.getBackground());
                infoPanel.add(lblKondisi);
                infoPanel.add(new JLabel("   ")); // spasi antar label
                infoPanel.add(lblSuhu);

                JLabel lblGambar = new JLabel(scaledIcon);
                lblGambar.setAlignmentX(JLabel.CENTER_ALIGNMENT);

                panel.add(infoPanel);
                panel.add(lblGambar);

                panel.revalidate();
                panel.repaint();
            } else {
                JOptionPane.showMessageDialog(null, "Gambar tidak ditemukan: " + fileGambar);
            }
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal menampilkan data cuaca!");
        e.printStackTrace();
    }
}


    // === Tambah ke Tabel ===
    public static void tambahKeTabel(DefaultTableModel model, String kota, String json) {
        JSONObject obj = new JSONObject(json);
        JSONObject main = obj.getJSONObject("main");
        String kondisi = obj.getJSONArray("weather").getJSONObject(0).getString("main");
        double suhu = main.getDouble("temp");
        String cuaca = kondisi + " (" + suhu + "°C)";

    // cek apakah kota sudah ada di tabel
    boolean ditemukan = false;
        for (int i = 0; i < model.getRowCount(); i++) {
        Object val = model.getValueAt(i, 0);
        if (val != null && val.toString().equalsIgnoreCase(kota)) {
            model.setValueAt(cuaca, i, 1); // update data lama
            ditemukan = true;
            break;
        }
    }

    // kalau belum ada, tambahkan
    if (!ditemukan) {
        model.addRow(new Object[]{kota, cuaca});
    }
}

    // === Simpan dan Muat CSV ===
    // === Simpan CSV dengan File Chooser ===
public static void simpanCSV(DefaultTableModel model) {
    try {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Data Cuaca ke CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Tambahkan ekstensi .csv jika belum ada
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(fileToSave)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object kota = model.getValueAt(i, 0);
                    Object kondisi = model.getValueAt(i, 1);
                    if (kota != null && kondisi != null) {
                        pw.println(kota + "," + kondisi);
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Data berhasil disimpan ke: " + fileToSave.getAbsolutePath());
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal menyimpan data ke CSV!");
    }
}


    // === Muat CSV dengan File Chooser ===
public static void muatCSV(DefaultTableModel model) {
    try {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih File CSV untuk Dimuat");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(fileToOpen))) {
                model.setRowCount(0);
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 2) {
                        model.addRow(data);
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Data berhasil dimuat dari: " + fileToOpen.getAbsolutePath());
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Gagal memuat data dari CSV!");
    }
    }
}
