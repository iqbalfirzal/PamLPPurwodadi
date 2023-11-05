package pendataan.pengamanan.rtpwd;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class LapsusModel {
    @DocumentId
    private String documentId;
    private String foto;
    private String fotopelapor;
    private GeoPoint geo;
    private String instruksipim;
    private String isilaporan;
    private String namapelapor;
    private String nippelapor;
    private Date tgllaporan;


    public LapsusModel() {
    }

    public LapsusModel(String documentId, String foto, String fotopelapor, GeoPoint geo, String isilaporan, String instruksipim, String namapelapor, String nippelapor, Date tgllaporan) {
        this.documentId = documentId;
        this.foto = foto;
        this.fotopelapor = fotopelapor;
        this.geo = geo;
        this.instruksipim = instruksipim;
        this.isilaporan = isilaporan;
        this.namapelapor = namapelapor;
        this.nippelapor = nippelapor;
        this.tgllaporan = tgllaporan;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFoto() {
        return foto;
    }

    public String getFotoPelapor() {
        return fotopelapor;
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

    public String getNipPelapor() {
        return nippelapor;
    }

    public Date getTglLaporan() {
        return tgllaporan;
    }
}
