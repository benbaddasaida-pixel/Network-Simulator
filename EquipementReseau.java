import java.util.*;

public abstract class EquipementReseau {
    protected String nom;
    protected String adresseIP;
    protected String adresseMAC;
    protected boolean actif;
    protected ArrayList<Paquet> historiquePaquets;

    public EquipementReseau(String nom, String adresseIP) {
        this.nom = nom;
        this.adresseIP = adresseIP;
        this.adresseMAC = genererMAC();
        this.actif = true;
        this.historiquePaquets = new ArrayList<>();
    }

    private String genererMAC() {
        Random rand = new Random();
        byte[] mac = new byte[6];
        rand.nextBytes(mac);
        StringBuilder sb = new StringBuilder(18);
        for (byte b : mac) {
            if (sb.length() > 0)
                sb.append(":");
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean isActif() {
        return actif;
    }

    public String getAdresseMAC() {
        return adresseMAC;
    }

    public ArrayList<Paquet> getHistorique() {
        return historiquePaquets;
    }

    // === MÉTHODES MANQUANTES À AJOUTER ===
    public String getNom() {
        return nom;
    }

    public String getAdresseIP() {
        return adresseIP;
    }

    // Méthode abstraite pour recevoir les paquets
    public abstract void recevoirPaquet(Paquet paquet);
    // =====================================

    public abstract String getType();
}