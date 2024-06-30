package com.kelompokMPKK.project_tatib;

public class PointItem {
    private int kode;
    private String pelanggaran;
    private int poin;

    public PointItem() {
        // Default constructor required for calls to DataSnapshot.getValue(PointItem.class)
    }

    public PointItem(int kode, String pelanggaran, int poin) {
        this.kode = kode;
        this.pelanggaran = pelanggaran;
        this.poin = poin;
    }

    public int getKode() {
        return kode;
    }

    public String getPelanggaran() {
        return pelanggaran;
    }

    public int getPoin() {
        return poin;
    }
}
