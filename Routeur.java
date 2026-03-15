import java.util.*;

public class Routeur extends EquipementReseau implements InterfaceReseau {
    private HashMap<String, String> tableRoutage;
    private ArrayList<String> interfaces;

    public Routeur(String nom, String adresseIP) {
        super(nom, adresseIP);
        this.tableRoutage = new HashMap<>();
        this.interfaces = new ArrayList<>();
    }

    public void ajouterRoute(String destination, String nextHop) {
        tableRoutage.put(destination, nextHop);
    }

    public void ajouterInterface(String interfaceName) {
        interfaces.add(interfaceName);
    }

    @Override
    public void envoyerPaquet(Paquet paquet, EquipementReseau destination) {
        if (!actif) {
            System.out.println("❌ " + nom + " est hors ligne!");
            return;
        }

        System.out.println("🔄 " + nom + " (Routeur) route le paquet...");
        String nextHop = tableRoutage.get(paquet.getDestination());

        if (nextHop != null) {
            System.out.println("  📍 Table de routage: " + paquet.getDestination() + " → " + nextHop);
            // Correction: ne pas créer un nouveau paquet, utiliser celui existant
            destination.recevoirPaquet(paquet);
        } else {
            System.out.println("  ⚠️ Route non trouvée pour " + paquet.getDestination());
        }

        historiquePaquets.add(paquet);
    }

    @Override
    public void recevoirPaquet(Paquet paquet) {
        if (!actif)
            return;

        System.out.println("📥 " + nom + " reçoit un paquet: " + paquet.getDonnees());
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
    }

    @Override
    public String getType() {
        return "ROUTEUR";
    }

    public HashMap<String, String> getTableRoutage() {
        return tableRoutage;
    }
}