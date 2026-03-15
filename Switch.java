import java.util.*;

public class Switch extends EquipementReseau implements InterfaceReseau {
    private HashMap<String, Integer> tableMAC;
    private int nombrePorts;

    public Switch(String nom, String adresseIP, int nombrePorts) {
        super(nom, adresseIP);
        this.tableMAC = new HashMap<>();
        this.nombrePorts = nombrePorts;
    }

    public void apprendreMAC(String adresseMAC, int port) {
        tableMAC.put(adresseMAC, port);
        System.out.println("  📚 Switch " + nom + " a appris MAC " + adresseMAC + " sur port " + port);
    }

    @Override
    public void envoyerPaquet(Paquet paquet, EquipementReseau destination) {
        if (!actif) {
            System.out.println("❌ Switch " + nom + " est hors ligne!");
            return;
        }

        System.out.println("🔀 Switch " + nom + " commute le paquet...");

        // Simuler la commutation
        if (tableMAC.containsKey(destination.getAdresseMAC())) {
            int port = tableMAC.get(destination.getAdresseMAC());
            System.out.println("  📍 Envoi sur port " + port);
            destination.recevoirPaquet(paquet);
        } else {
            System.out.println("  📢 Diffusion sur tous les ports (MAC inconnue)");
            destination.recevoirPaquet(paquet);
        }

        historiquePaquets.add(paquet);
    }

    @Override
    public void recevoirPaquet(Paquet paquet) {
        if (!actif)
            return;

        System.out.println("📥 Switch " + nom + " reçoit: " + paquet.getDonnees());
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
    } // 100 Mbps

    @Override
    public String getType() {
        return "SWITCH";
    }
}