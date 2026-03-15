import java.util.*;

public class Serveur extends EquipementReseau implements InterfaceReseau {
    private HashMap<String, String> services;
    private int capaciteStockage;
    private ArrayList<String> fichiers;

    public Serveur(String nom, String adresseIP, int capaciteStockage) {
        super(nom, adresseIP);
        this.services = new HashMap<>();
        this.capaciteStockage = capaciteStockage;
        this.fichiers = new ArrayList<>();

        // Services par défaut
        services.put("HTTP", "80");
        services.put("HTTPS", "443");
        services.put("FTP", "21");
        services.put("SSH", "22");
    }

    public void ajouterFichier(String fichier) {
        fichiers.add(fichier);
    }

    public ArrayList<String> getFichiers() {
        return fichiers;
    }

    @Override
    public void envoyerPaquet(Paquet paquet, EquipementReseau destination) {
        if (!actif) {
            System.out.println("❌ Serveur " + nom + " est hors ligne!");
            return;
        }

        System.out.println("📤 Serveur " + nom + " envoie des données...");

        // Ajouter des données serveur
        String donneesServeur = "[SERVEUR " + nom + "] " + paquet.getDonnees();
        Paquet nouveauPaquet = new Paquet(this.adresseIP, destination.getAdresseIP(),
                donneesServeur, paquet.getProtocole());

        destination.recevoirPaquet(nouveauPaquet);
        historiquePaquets.add(nouveauPaquet);
    }

    @Override
    public void recevoirPaquet(Paquet paquet) {
        if (!actif)
            return;

        System.out.println("📥 Serveur " + nom + " traite la requête: " + paquet.getDonnees());

        // Simuler le traitement
        if (paquet.getDonnees().contains("GET")) {
            String fichier = paquet.getDonnees().replace("GET ", "");
            if (fichiers.contains(fichier)) {
                System.out.println("  ✅ Fichier '" + fichier + "' trouvé, envoi en cours...");
            } else {
                System.out.println("  ❌ Fichier '" + fichier + "' non trouvé (404)");
            }
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
        return 1000;
    } // 1 Gbps

    @Override
    public String getType() {
        return "SERVEUR";
    }

    public HashMap<String, String> getServices() {
        return services;
    }
}