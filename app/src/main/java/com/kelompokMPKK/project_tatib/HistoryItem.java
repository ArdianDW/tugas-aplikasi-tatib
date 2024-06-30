package com.kelompokMPKK.project_tatib;

public class HistoryItem {
    private int kode;
    private int poin;
    private long timestamp;
    private String pelanggaran;

    public HistoryItem(int kode, int poin, long timestamp, String pelanggaran) {
        this.kode = kode;
        this.poin = poin;
        this.timestamp = timestamp;
        this.pelanggaran = pelanggaran;
    }

    // Getter dan setter
    public int getKode() { return kode; }
    public void setKode(int kode) { this.kode = kode; }
    public int getPoin() { return poin; }
    public void setPoin(int poin) { this.poin = poin; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getPelanggaran() { return pelanggaran; }
    public void setPelanggaran(String pelanggaran) { this.pelanggaran = pelanggaran; }
}
