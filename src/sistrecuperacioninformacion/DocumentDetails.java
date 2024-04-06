package sistrecuperacioninformacion;

import java.util.ArrayList;

/**
 *
 * @author Aida Rosa
 */
public class DocumentDetails {

    private String nombre;
    private ArrayList<String> token;
    private double significacion;

    public DocumentDetails(String nombre, ArrayList<String> token, double significacion) {
        this.nombre = nombre;
        this.token = token;
        this.significacion = significacion;
    }

    public DocumentDetails(String nombre, ArrayList<String> token) {
        this.nombre = nombre;
        this.token = token;
    }

    public double getSignificacion() {
        return significacion;
    }

    public void setSignificacion(double significacion) {
        this.significacion = significacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<String> getToken() {
        return token;
    }

    public void setToken(ArrayList<String> token) {
        this.token = token;
    }
}
