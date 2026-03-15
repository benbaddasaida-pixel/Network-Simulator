import java.util.*;

public class Pc extends EquipementReseau implements InterfaceReseau {
    private String systemeExploitation;
    private ArrayList<String> applications;
    private boolean navigateurOuvert;

    public Pc(String nom, String adresseIP, String os) {
        super(nom, adresseIP);
        this.systemeExploitation = os;
        this.applications = new ArrayList<>();
        this.navigateurOuvert = false;

        applications.add("Chrome");
        applications.add("Firefox");
        applications.add("Terminal");
    }

    public void ouvrirNavigateur() {
        navigateurOuvert = true;
        System.out.println("🌐 " + nom + " ouvre le navigateur");
    }

    public void ping(String destination) {
        System.out.println("📡 " + nom + " ping " + destination + "...");
        System.out.println("  ✅ Réponse de " + destination + ": temps=42ms");
    }

    @Override
    public void envoyerPaquet(Paquet paquet, EquipementReseau destination) {
        if (!actif) {
            System.out.println("❌ PC " + nom + " est éteint!");
            return;
        }

        System.out.println("📤 PC " + nom + " envoie une requête...");

        // Correction: vérifier si le protocole est HTTP sans utiliser getProtocole()
        if (navigateurOuvert && paquet.getDonnees().contains("HTTP")) {
            System.out.println("  🌍 Navigation web: " + paquet.getDonnees());
        }

        destination.recevoirPaquet(paquet);
        historiquePaquets.add(paquet);
    }

    @Override
    public void recevoirPaquet(Paquet paquet) {
        if (!actif)
            return;

        System.out.println("📥 PC " + nom + " reçoit: " + paquet.getDonnees());

        if (paquet.getDonnees().contains("404")) {
            System.out.println("  ⚠️ Page non trouvée!");
        }

        historiquePaquets.add(paquet);
    }

    @Override
    public String getAdresseIP() {
        return adresseIP;
    }

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public int getDebit() {
        return 100;
    }

    @Override
    public String getType() {
        return "PC";
    }
}