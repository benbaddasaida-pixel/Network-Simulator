import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Paquet {
    private String source;
    private String destination;
    private String donnees;
    private String protocole;
    private int taille;
    private String heureEnvoi;
    private boolean estArrive;

    public Paquet(String source, String destination, String donnees, String protocole) {
        this.source = source;
        this.destination = destination;
        this.donnees = donnees;
        this.protocole = protocole;
        this.taille = donnees.length() * 8; // taille en bits
        this.heureEnvoi = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.estArrive = false;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getDonnees() {
        return donnees;
    }

    public String getProtocole() {
        return protocole;
    }

    public int getTaille() {
        return taille;
    }

    public String getHeureEnvoi() {
        return heureEnvoi;
    }

    public boolean isArrive() {
        return estArrive;
    }

    public void setArrive(boolean estArrive) {
        this.estArrive = estArrive;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s | %s | %d bits",
                heureEnvoi, source, destination, protocole, taille);
    }
}