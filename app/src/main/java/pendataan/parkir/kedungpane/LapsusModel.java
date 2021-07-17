package pendataan.parkir.kedungpane;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class LapsusModel {
    @DocumentId
    private String documentId;
    private String foto;
    private GeoPoint geo;
    private String instruksipim;
    private String isilaporan;
    private String namapelapor;
    private Date tgllaporan;


    public LapsusModel() {
    }

    public LapsusModel(String documentId, String foto, GeoPoint geo, String isilaporan, String instruksipim, String namapelapor, Date tgllaporan) {
        this.documentId = documentId;
        this.foto = foto;
        this.geo = geo;
        this.instruksipim = instruksipim;
        this.isilaporan = isilaporan;
        this.namapelapor = namapelapor;
        this.tgllaporan = tgllaporan;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFoto() {
        return foto;
    }

    public GeoPoint getGeo() {
        return geo;
    }

    public String getInstruksiPim() {
        return instruksipim;
    }

    public String getIsiLaporan() {
        return isilaporan;
    }

    public String getNamaPelapor() {
        return namapelapor;
    }

    public Date getTglLaporan() {
        return tgllaporan;
    }
}
