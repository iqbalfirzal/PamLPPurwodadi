package pendataan.parkir.kedungpane;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class ParkirModel {
    @DocumentId
    private String documentId;
    private String jeniskendaraan;
    private Date keluarjam;
    private String keperluan;
    private Date masukjam;
    private String platnomor;
    private Boolean sudahkeluar;


    public ParkirModel() {
    }

    public ParkirModel(String documentId, String jeniskendaraan, Date keluarjam, String keperluan, Date masukjam, String platnomor, Boolean sudahkeluar) {
        this.documentId = documentId;
        this.jeniskendaraan = jeniskendaraan;
        this.keluarjam = keluarjam;
        this.keperluan = keperluan;
        this.masukjam = masukjam;
        this.platnomor = platnomor;
        this.sudahkeluar = sudahkeluar;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getJenisKendaraan() {
        return jeniskendaraan;
    }

    public Date getKeluarJam() {
        return keluarjam;
    }

    public String getKeperluan() {
        return keperluan;
    }

    public Date getMasukJam() {
        return masukjam;
    }


    public String getPlatNomor() {
        return platnomor;
    }

    public Boolean getSudahKeluar() {
        return sudahkeluar;
    }

}
