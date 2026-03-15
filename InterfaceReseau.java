public interface InterfaceReseau {
    void envoyerPaquet(Paquet paquet, EquipementReseau destination);

    void recevoirPaquet(Paquet paquet);

    String getAdresseIP();

    String getNom();

    int getDebit();
}