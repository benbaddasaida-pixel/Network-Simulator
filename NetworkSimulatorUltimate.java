import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkSimulatorUltimate extends JFrame {

    // ========== COULEURS ==========
    private final Color BG_DARK = new Color(10, 15, 20);
    private final Color BG_CARD = new Color(20, 25, 35);
    private final Color ACCENT_BLUE = new Color(0, 160, 255);
    private final Color ACCENT_GREEN = new Color(0, 220, 150);
    private final Color ACCENT_PURPLE = new Color(170, 100, 255);
    private final Color ACCENT_ORANGE = new Color(255, 140, 0);
    private final Color ACCENT_RED = new Color(255, 60, 60);
    private final Color ACCENT_YELLOW = new Color(255, 220, 0);
    private final Color ACCENT_CYAN = new Color(0, 255, 255);
    private final Color SUCCESS_COLOR = new Color(0, 255, 100);
    private final Color ERROR_COLOR = new Color(255, 60, 60);
    private final Color WARNING_COLOR = new Color(255, 180, 0);
    private final Color TEXT_PRIMARY = new Color(230, 240, 255);
    private final Color TEXT_SECONDARY = new Color(160, 170, 190);
    private final Color CISCO_BLUE = new Color(0, 153, 204);
    private final Color CISCO_DARK = new Color(0, 102, 153);

    // ========== COMPOSANTS RÉSEAU ==========
    private List<EquipementReseau> equipements;
    private List<LiaisonReseau> liaisons;
    private Map<String, Color> couleursEquipements;
    private Random random = new Random();

    // ========== COMPOSANTS GRAPHIQUES ==========
    private JPanel panelTopologie;
    private JPanel panelGraph;
    private JTextArea consoleNetwork;
    private JPanel panelStats;
    private JPanel panelControle;
    private JProgressBar barreBandePassante;
    private JLabel labelPaquets, labelDebit, labelConnexions, labelTauxReussite, labelLatence;
    private JComboBox<String> comboSource, comboDestination, comboProtocole, comboVLAN;
    private JTextField champDonnees;
    private JButton boutonEnvoyer, boutonPing, boutonTraceroute, boutonScan, boutonReset;
    private JButton boutonPanne, boutonTableARP, boutonDDOS, boutonExport, boutonLogin;
    private JButton boutonCLI, boutonSimulation, boutonFirewall;
    private Timer animationTimer, graphTimer;
    private List<AnimationPaquet> animations = new ArrayList<>();
    private List<AnimationConnexion> animationsConnexion = new ArrayList<>();

    // ========== MODE TOPOLOGIE ==========
    private JTabbedPane tabbedPane;
    private JPanel panelConfiguration;
    private JComboBox<String> comboEquipementsConfig;
    private JTextField champIP, champMasque, champVLANConfig;
    private JTextArea zoneInterfaces;
    private Map<String, Point> positionsEquipements = new HashMap<>();
    private EquipementReseau equipementSelectionne = null;
    private boolean modeCablage = false;
    private EquipementReseau sourceCablage = null;
    private boolean modeSimulation = false;

    // ========== STATISTIQUES ==========
    private int totalPaquets = 0;
    private int paquetsPerdus = 0;
    private int connexionsActives = 0;
    private double debitMoyen = 0;
    private int paquetsReussis = 0;
    private List<Integer> historiqueDebit = new ArrayList<>();
    private List<Integer> historiqueLatence = new ArrayList<>();

    // ========== GESTION DES PANNES ==========
    private Map<String, Boolean> etatLiaisons = new HashMap<>();
    private Map<String, Boolean> etatEquipements = new HashMap<>();

    // ========== VLAN ==========
    private Map<String, Integer> vlanEquipements = new HashMap<>();
    private final int VLAN_DEFAULT = 1;
    private final int VLAN_10 = 10;
    private final int VLAN_20 = 20;
    private final int VLAN_30 = 30;

    // ========== SÉCURITÉ ==========
    private boolean modeAdmin = false;
    private int tentativesConnexion = 0;
    private boolean attaqueEnCours = false;

    public NetworkSimulatorUltimate() {
        initialiserReseau();
        initialiserVLAN();
        setupUI();
        demarrerAnimations();
        demarrerReseauAvecMessages();
        demarrerGraphTimer();
    }

    private void initialiserReseau() {
        equipements = new ArrayList<>();
        liaisons = new ArrayList<>();
        couleursEquipements = new HashMap<>();

        // Création des équipements
        Routeur routeur2811 = new Routeur("Cisco 2811", "192.168.1.1");
        Routeur routeur1841 = new Routeur("Cisco 1841", "192.168.2.1");

        Switch switch2960 = new Switch("Cisco 2960-24TT", "192.168.1.2", 24);
        Switch switch3560 = new Switch("Cisco 3560-24PS", "192.168.1.3", 24);
        Switch switch2950 = new Switch("Cisco 2950-24", "192.168.1.4", 12);

        Serveur serveurWeb = new Serveur("Server-WEB", "192.168.1.10", 1000);
        Serveur serveurDNS = new Serveur("Server-DNS", "192.168.1.11", 500);
        Serveur serveurFTP = new Serveur("Server-FTP", "192.168.1.12", 500);

        Pc pcSanae = new Pc("PC-Sanae", "192.168.1.20", "Windows 11");
        Pc pcSaida = new Pc("PC-Saida", "192.168.1.21", "macOS");
        Pc pcRajaa = new Pc("PC-Rajaa", "192.168.1.22", "Ubuntu");

        // Ajout des équipements
        equipements.add(routeur2811);
        equipements.add(routeur1841);
        equipements.add(switch2960);
        equipements.add(switch3560);
        equipements.add(switch2950);
        equipements.add(serveurWeb);
        equipements.add(serveurDNS);
        equipements.add(serveurFTP);
        equipements.add(pcSanae);
        equipements.add(pcSaida);
        equipements.add(pcRajaa);

        // Création des liaisons
        liaisons.add(new LiaisonReseau(routeur2811, switch2960, 1000));
        liaisons.add(new LiaisonReseau(routeur2811, routeur1841, 100));
        liaisons.add(new LiaisonReseau(switch2960, serveurWeb, 1000));
        liaisons.add(new LiaisonReseau(switch2960, serveurDNS, 1000));
        liaisons.add(new LiaisonReseau(switch2960, serveurFTP, 1000));
        liaisons.add(new LiaisonReseau(switch2960, switch3560, 1000));
        liaisons.add(new LiaisonReseau(switch3560, pcSanae, 100));
        liaisons.add(new LiaisonReseau(switch3560, pcSaida, 100));
        liaisons.add(new LiaisonReseau(switch3560, pcRajaa, 100));

        // Initialisation des états
        for (LiaisonReseau l : liaisons) {
            etatLiaisons.put(l.eq1.getNom() + "-" + l.eq2.getNom(), true);
        }
        for (EquipementReseau e : equipements) {
            etatEquipements.put(e.getNom(), true);
        }

        // Couleurs
        for (EquipementReseau e : equipements) {
            if (e.getType().equals("ROUTEUR"))
                couleursEquipements.put(e.getNom(), CISCO_BLUE);
            else if (e.getType().equals("SWITCH"))
                couleursEquipements.put(e.getNom(), CISCO_DARK);
            else if (e.getType().equals("SERVEUR"))
                couleursEquipements.put(e.getNom(), ACCENT_PURPLE);
            else
                couleursEquipements.put(e.getNom(), ACCENT_ORANGE);
        }
    }

    private void initialiserVLAN() {
        for (EquipementReseau e : equipements) {
            if (e.getNom().contains("Sanae"))
                vlanEquipements.put(e.getNom(), VLAN_10);
            else if (e.getNom().contains("Saida"))
                vlanEquipements.put(e.getNom(), VLAN_20);
            else if (e.getNom().contains("Rajaa"))
                vlanEquipements.put(e.getNom(), VLAN_30);
            else
                vlanEquipements.put(e.getNom(), VLAN_DEFAULT);
        }
    }

    private JButton creerBouton(String texte, Color couleur) {
        JButton bouton = new JButton(texte);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bouton.setForeground(Color.WHITE);
        bouton.setBackground(couleur);
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur.brighter(), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return bouton;
    }

    private void logMessage(String message) {
        consoleNetwork.append(message + "\n");
        consoleNetwork.setCaretPosition(consoleNetwork.getDocument().getLength());
    }

    // ========== CLASSES INTERNES ==========

    class LiaisonReseau {
        EquipementReseau eq1, eq2;
        int debit;

        LiaisonReseau(EquipementReseau eq1, EquipementReseau eq2, int debit) {
            this.eq1 = eq1;
            this.eq2 = eq2;
            this.debit = debit;
        }
    }

    class AnimationPaquet {
        double x1, y1, x2, y2, x, y;
        double progress = 0;
        Color couleur;

        AnimationPaquet(EquipementReseau source, EquipementReseau dest) {
            int idx1 = equipements.indexOf(source);
            int idx2 = equipements.indexOf(dest);
            x1 = 100 + (idx1 % 4) * 150 + 35;
            y1 = 100 + (idx1 / 4) * 120 + 35;
            x2 = 100 + (idx2 % 4) * 150 + 35;
            y2 = 100 + (idx2 / 4) * 120 + 35;
            x = x1;
            y = y1;
            couleur = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }

        boolean update() {
            progress += 0.03;
            x = x1 + (x2 - x1) * progress;
            y = y1 + (y2 - y1) * progress;
            return progress >= 1;
        }

        void draw(Graphics2D g2d) {
            g2d.setColor(couleur);
            g2d.fillOval((int) x - 5, (int) y - 5, 10, 10);
        }
    }

    class AnimationConnexion {
        double x1, y1, x2, y2;
        double progress = 0;
        Color couleur;

        AnimationConnexion(EquipementReseau eq1, EquipementReseau eq2, Color couleur) {
            int idx1 = equipements.indexOf(eq1);
            int idx2 = equipements.indexOf(eq2);
            x1 = 100 + (idx1 % 4) * 150 + 35;
            y1 = 100 + (idx1 / 4) * 120 + 35;
            x2 = 100 + (idx2 % 4) * 150 + 35;
            y2 = 100 + (idx2 / 4) * 120 + 35;
            this.couleur = couleur;
        }

        boolean update() {
            progress += 0.05;
            return progress > 1;
        }

        void draw(Graphics2D g2d) {
            int x = (int) (x1 + (x2 - x1) * progress);
            int y = (int) (y1 + (y2 - y1) * progress);
            g2d.setColor(couleur);
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    // ========== MÉTHODES DE DESSIN ==========

    private void dessinerEquipements(Graphics2D g2d) {
        for (int i = 0; i < equipements.size(); i++) {
            EquipementReseau eq = equipements.get(i);
            Point pos = positionsEquipements.get(eq.getNom());
            if (pos == null) {
                pos = new Point(100 + (i % 4) * 150, 100 + (i / 4) * 120);
                positionsEquipements.put(eq.getNom(), pos);
            }

            Color couleur = couleursEquipements.get(eq.getNom());

            // Ombre
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillOval(pos.x + 5, pos.y + 5, 70, 70);

            // Cercle principal
            g2d.setColor(couleur);
            g2d.fillOval(pos.x, pos.y, 70, 70);

            // Bordure
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(pos.x, pos.y, 70, 70);

            // Statut
            if (etatEquipements.get(eq.getNom())) {
                g2d.setColor(SUCCESS_COLOR);
                g2d.fillOval(pos.x + 55, pos.y + 5, 10, 10);
            }

            // Icône
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
            String icone = "";
            if (eq.getType().equals("ROUTEUR"))
                icone = "🖧";
            else if (eq.getType().equals("SWITCH"))
                icone = "🔄";
            else if (eq.getType().equals("SERVEUR"))
                icone = "💾";
            else
                icone = "💻";
            g2d.drawString(icone, pos.x + 22, pos.y + 45);

            // Nom
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2d.setColor(Color.WHITE);
            String nomCourt = eq.getNom();
            if (nomCourt.length() > 12)
                nomCourt = nomCourt.substring(0, 10) + "...";
            g2d.drawString(nomCourt, pos.x + 10, pos.y + 90);

            // IP
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2d.drawString(eq.getAdresseIP(), pos.x + 10, pos.y + 105);
        }
    }

    private void dessinerLiaisons(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(100, 200, 255, 150));

        for (LiaisonReseau liaison : liaisons) {
            Point p1 = positionsEquipements.get(liaison.eq1.getNom());
            Point p2 = positionsEquipements.get(liaison.eq2.getNom());

            if (p1 != null && p2 != null) {
                g2d.drawLine(p1.x + 35, p1.y + 35, p2.x + 35, p2.y + 35);
            }
        }
    }

    private void dessinerAnimations(Graphics2D g2d) {
        synchronized (animations) {
            Iterator<AnimationPaquet> it = animations.iterator();
            while (it.hasNext()) {
                AnimationPaquet anim = it.next();
                if (anim.update()) {
                    it.remove();
                } else {
                    anim.draw(g2d);
                }
            }
        }
    }

    private void dessinerAnimationsConnexion(Graphics2D g2d) {
        synchronized (animationsConnexion) {
            Iterator<AnimationConnexion> it = animationsConnexion.iterator();
            while (it.hasNext()) {
                AnimationConnexion anim = it.next();
                if (anim.update()) {
                    it.remove();
                } else {
                    anim.draw(g2d);
                }
            }
        }
    }

    // ========== MÉTHODES DE GESTION ==========

    private void rendreGlissable(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                equipementSelectionne = trouverEquipementParPosition(e.getX(), e.getY());
                if (equipementSelectionne != null) {
                    if (modeCablage) {
                        if (sourceCablage == null) {
                            sourceCablage = equipementSelectionne;
                            logMessage("🔌 Source: " + sourceCablage.getNom());
                        } else {
                            creerLiaison(sourceCablage, equipementSelectionne);
                            sourceCablage = null;
                            modeCablage = false;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (equipementSelectionne != null && !modeCablage) {
                    positionsEquipements.put(equipementSelectionne.getNom(), new Point(e.getX(), e.getY()));
                    panel.repaint();
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (equipementSelectionne != null && !modeCablage) {
                    positionsEquipements.put(equipementSelectionne.getNom(), new Point(e.getX(), e.getY()));
                    panel.repaint();
                }
            }
        });
    }

    private EquipementReseau trouverEquipementParPosition(int x, int y) {
        for (EquipementReseau eq : equipements) {
            Point pos = positionsEquipements.get(eq.getNom());
            if (pos != null) {
                if (Math.abs(pos.x - x) < 40 && Math.abs(pos.y - y) < 40) {
                    return eq;
                }
            }
        }
        return null;
    }

    private void ajouterEquipement(String type) {
        String nom = JOptionPane.showInputDialog(this, "Nom du " + type + ":");
        if (nom != null && !nom.isEmpty()) {
            String ip = "192.168.1." + (equipements.size() + 10);
            EquipementReseau nouvelEq = null;

            switch (type) {
                case "ROUTEUR":
                    nouvelEq = new Routeur(nom, ip);
                    break;
                case "SWITCH":
                    nouvelEq = new Switch(nom, ip, 24);
                    break;
                case "SERVEUR":
                    nouvelEq = new Serveur(nom, ip, 500);
                    break;
                case "PC":
                    nouvelEq = new Pc(nom, ip, "Windows 11");
                    break;
            }

            if (nouvelEq != null) {
                equipements.add(nouvelEq);
                Color couleur = type.equals("ROUTEUR") ? CISCO_BLUE
                        : type.equals("SWITCH") ? CISCO_DARK : type.equals("SERVEUR") ? ACCENT_PURPLE : ACCENT_ORANGE;
                couleursEquipements.put(nom, couleur);
                vlanEquipements.put(nom, VLAN_DEFAULT);
                etatEquipements.put(nom, true);
                logMessage("✅ Équipement ajouté: " + nom + " (" + ip + ")");
                chargerEquipementsDansCombo();
                panelTopologie.repaint();
            }
        }
    }

    private void creerLiaison(EquipementReseau eq1, EquipementReseau eq2) {
        if (eq1 != null && eq2 != null && eq1 != eq2) {
            boolean existe = false;
            for (LiaisonReseau l : liaisons) {
                if ((l.eq1.equals(eq1) && l.eq2.equals(eq2)) ||
                        (l.eq1.equals(eq2) && l.eq2.equals(eq1))) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                liaisons.add(new LiaisonReseau(eq1, eq2, 1000));
                logMessage("🔗 Liaison: " + eq1.getNom() + " ↔ " + eq2.getNom());
                panelTopologie.repaint();
            }
        }
    }

    private void chargerEquipementsDansCombo() {
        comboEquipementsConfig.removeAllItems();
        for (EquipementReseau eq : equipements) {
            comboEquipementsConfig.addItem(eq.getNom());
        }
    }

    private void chargerConfigEquipement() {
        String nom = (String) comboEquipementsConfig.getSelectedItem();
        if (nom != null) {
            for (EquipementReseau eq : equipements) {
                if (eq.getNom().equals(nom)) {
                    champIP.setText(eq.getAdresseIP());
                    champVLANConfig.setText(String.valueOf(vlanEquipements.getOrDefault(nom, 1)));

                    StringBuilder sb = new StringBuilder();
                    sb.append("Cisco IOS Software\n");
                    sb.append("Hostname: ").append(eq.getNom()).append("\n");
                    sb.append("Adresse MAC: ").append(eq.getAdresseMAC()).append("\n");
                    sb.append("Statut: ").append(etatEquipements.get(nom) ? "🟢 ACTIF" : "🔴 INACTIF").append("\n\n");
                    sb.append("Interfaces:\n");
                    sb.append("  GigabitEthernet0/0: ").append(eq.getAdresseIP()).append("/24\n");
                    sb.append("  GigabitEthernet0/1: unassigned\n");
                    sb.append("  VLAN1: 192.168.1.254/24\n");

                    zoneInterfaces.setText(sb.toString());
                    equipementSelectionne = eq;
                    break;
                }
            }
        }
    }

    private void appliquerConfig() {
        if (equipementSelectionne != null) {
            String ip = champIP.getText();
            int vlan = Integer.parseInt(champVLANConfig.getText());
            vlanEquipements.put(equipementSelectionne.getNom(), vlan);
            logMessage("⚙️ Config: " + equipementSelectionne.getNom() + " IP=" + ip + " VLAN=" + vlan);
        }
    }

    // ========== MÉTHODES PRINCIPALES ==========

    private void demarrerAnimations() {
        animationTimer = new Timer(50, e -> {
            if (panelTopologie != null)
                panelTopologie.repaint();
        });
        animationTimer.start();
    }

    private void demarrerGraphTimer() {
        graphTimer = new Timer(1000, e -> {
            labelPaquets.setText("📦 PAQUETS: " + totalPaquets);
            labelDebit.setText("📊 DÉBIT: " + (int) debitMoyen + " Mbps");
            labelConnexions.setText("🔗 CONNEXIONS: " + connexionsActives);
            int taux = totalPaquets > 0 ? (paquetsReussis * 100 / totalPaquets) : 100;
            labelTauxReussite.setText("✅ RÉUSSITE: " + taux + "%");

            historiqueDebit.add((int) debitMoyen);
            historiqueLatence.add(15 + random.nextInt(30));
            if (historiqueDebit.size() > 20) {
                historiqueDebit.remove(0);
                historiqueLatence.remove(0);
            }
            if (panelGraph != null)
                panelGraph.repaint();
        });
        graphTimer.start();
    }

    private void demarrerReseauAvecMessages() {
        for (LiaisonReseau l : liaisons) {
            logMessage("🔗 CONNEXION: " + l.eq1.getNom() + " ↔ " + l.eq2.getNom() + " | " + l.debit + " Mbps");
        }
        connexionsActives = liaisons.size();
    }

    private EquipementReseau trouverEquipement(String nom) {
        for (EquipementReseau e : equipements) {
            if (e.getNom().equals(nom))
                return e;
        }
        return null;
    }

    // ========== ACTIONS DES BOUTONS ==========

    private void envoyerPaquet() {
        String source = (String) comboSource.getSelectedItem();
        String dest = (String) comboDestination.getSelectedItem();

        if (source.equals(dest)) {
            logMessage("❌ Erreur: Source et destination identiques");
            return;
        }

        EquipementReseau eqSource = trouverEquipement(source);
        EquipementReseau eqDest = trouverEquipement(dest);

        if (!etatEquipements.get(source)) {
            logMessage("❌ " + source + " hors ligne");
            return;
        }

        logMessage("\n📨 ENVOI PAQUET [" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]");
        logMessage("   DE: " + source + " → " + dest);
        logMessage("   PROTOCOLE: " + comboProtocole.getSelectedItem());

        animations.add(new AnimationPaquet(eqSource, eqDest));

        totalPaquets++;
        debitMoyen = 100 + random.nextInt(900);
        barreBandePassante.setValue((int) debitMoyen);

        if (random.nextInt(100) < 95) {
            paquetsReussis++;
            logMessage("   ✅ SUCCÈS");
        } else {
            paquetsPerdus++;
            logMessage("   ❌ PAQUET PERDU");
        }
    }

    private void ping() {
        String source = (String) comboSource.getSelectedItem();
        String dest = (String) comboDestination.getSelectedItem();

        logMessage("\n📡 PING " + source + " → " + dest);
        for (int i = 1; i <= 4; i++) {
            int latence = 10 + random.nextInt(40);
            logMessage("   Réponse " + i + ": temps=" + latence + "ms");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
        }
    }

    private void traceroute() {
        logMessage("\n🔄 TRACEROUTE");
        logMessage("   1  Cisco 2811 (192.168.1.1) 4ms");
        logMessage("   2  Cisco 2960 (192.168.1.2) 8ms");
        logMessage("   3  Destination (192.168.1.10) 12ms");
    }

    private void scanReseau() {
        logMessage("\n🔍 SCAN RÉSEAU");
        for (EquipementReseau eq : equipements) {
            String etat = etatEquipements.get(eq.getNom()) ? "🟢" : "🔴";
            logMessage("   " + etat + " " + eq.getNom() + " (" + eq.getAdresseIP() + ")");
        }
    }

    private void resetReseau() {
        totalPaquets = 0;
        paquetsPerdus = 0;
        paquetsReussis = 0;
        debitMoyen = 500;
        for (String key : etatEquipements.keySet()) {
            etatEquipements.put(key, true);
        }
        animations.clear();
        animationsConnexion.clear();
        logMessage("\n♻️ RÉSEAU RÉINITIALISÉ");
    }

    private void simulerPanne() {
        List<EquipementReseau> actifs = new ArrayList<>();
        for (EquipementReseau e : equipements) {
            if (etatEquipements.get(e.getNom()))
                actifs.add(e);
        }
        if (!actifs.isEmpty()) {
            EquipementReseau panne = actifs.get(random.nextInt(actifs.size()));
            etatEquipements.put(panne.getNom(), false);
            logMessage("⚠️ PANNE: " + panne.getNom() + " hors service");
        }
    }

    private void afficherTableARP() {
        logMessage("\n📋 TABLE ARP");
        for (EquipementReseau eq : equipements) {
            logMessage("   " + eq.getAdresseIP() + " → " + eq.getAdresseMAC());
        }
    }

    private void simulerAttaque() {
        attaqueEnCours = !attaqueEnCours;
        if (attaqueEnCours) {
            logMessage("\n⚠️ ATTAQUE DDOS DÉTECTÉE");
        } else {
            logMessage("\n✅ ATTAQUE CONTENUE");
        }
    }

    private void exporterLogs() {
        try {
            String filename = "logs_" + System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(filename);
            writer.println("=== LOGS RÉSEAU ===");
            writer.println("Date: " + new Date());
            writer.println("Total paquets: " + totalPaquets);
            writer.println("Paquets réussis: " + paquetsReussis);
            writer.println("Paquets perdus: " + paquetsPerdus);
            writer.println("Débit moyen: " + (int) debitMoyen + " Mbps");
            writer.close();
            logMessage("📄 Logs exportés: " + filename);
        } catch (Exception e) {
            logMessage("❌ Erreur d'export");
        }
    }

    private void loginAdmin() {
        String password = JOptionPane.showInputDialog(this, "Mot de passe admin (cisco):");
        if (password != null && password.equals("cisco")) {
            modeAdmin = true;
            logMessage("\n🔐 MODE ADMIN ACTIVÉ");
        } else {
            tentativesConnexion++;
            logMessage("❌ Mot de passe incorrect");
        }
    }

    private void ouvrirCLI() {
        String equipement = (String) comboSource.getSelectedItem();
        logMessage("\n💻 CLI CISCO - " + equipement);
        logMessage("Cisco IOS Software");
        logMessage(equipement + "> enable");
        logMessage("Password: ");
        logMessage(equipement + "# show running-config");
    }

    private void activerModeSimulation() {
        modeSimulation = !modeSimulation;
        logMessage(modeSimulation ? "🎬 Mode simulation ON" : "▶️ Mode simulation OFF");
    }

    private void configurerFirewall() {
        logMessage("\n🔥 FIREWALL ASA");
        logMessage("Règle 1: permit tcp any any eq 80");
        logMessage("Règle 2: permit tcp any any eq 443");
        logMessage("Règle 3: deny ip any any");
    }

    // ========== ACTIONS DU MENU ==========

    private void basculerModeAdmin() {
        if (!modeAdmin)
            loginAdmin();
        else {
            modeAdmin = false;
            logMessage("🔓 Mode utilisateur");
        }
    }

    private void configurerVLAN() {
        if (!modeAdmin) {
            logMessage("❌ Mode admin requis");
            return;
        }
        String[] options = { "VLAN 10", "VLAN 20", "VLAN 30", "VLAN 1" };
        String choix = (String) JOptionPane.showInputDialog(this, "Choisir VLAN:", "VLAN",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choix != null) {
            logMessage("\n🔧 Configuration " + choix);
        }
    }

    private void afficherTablesRoutage() {
        if (!modeAdmin) {
            logMessage("❌ Mode admin requis");
            return;
        }
        logMessage("\n📋 TABLES DE ROUTAGE");
        for (EquipementReseau eq : equipements) {
            if (eq.getType().equals("ROUTEUR")) {
                logMessage("\n" + eq.getNom() + ":");
                logMessage("  192.168.1.0/24 via Gi0/0");
                logMessage("  192.168.2.0/24 via 192.168.1.254");
                logMessage("  0.0.0.0/0 via 192.168.1.254");
            }
        }
    }

    private void afficherJournalBord() {
        logMessage("\n📋 JOURNAL DE BORD");
        logMessage("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        logMessage("Paquets: " + totalPaquets);
        logMessage("Réussite: " + (totalPaquets > 0 ? (paquetsReussis * 100 / totalPaquets) : 100) + "%");
        logMessage("Connexions: " + connexionsActives);
        logMessage("\nÉquipements actifs:");
        for (EquipementReseau eq : equipements) {
            if (etatEquipements.get(eq.getNom())) {
                logMessage("  🟢 " + eq.getNom());
            }
        }
    }

    private void nouvelleSimulation() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Nouvelle simulation? Toutes les données seront perdues.",
                "Nouvelle simulation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            equipements.clear();
            liaisons.clear();
            positionsEquipements.clear();
            initialiserReseau();
            initialiserVLAN();
            resetReseau();
            logMessage("\n🔄 Nouvelle simulation créée");
        }
    }

    // ========== MÉTHODES CORRIGÉES POUR JFileChooser ==========

    private void chargerConfiguration() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fichier = fc.getSelectedFile(); // CORRIGÉ: getSelectedFile() au lieu de getSelectedItem()
            logMessage("📂 Chargement: " + fichier.getName());
        }
    }

    private void sauvegarderConfiguration() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fichier = fc.getSelectedFile(); // CORRIGÉ: getSelectedFile() au lieu de getSelectedItem()
            logMessage("💾 Sauvegarde: " + fichier.getName());
        }
    }

    private void afficherRouteurs() {
        logMessage("\n📋 ROUTEURS CISCO");
        logMessage("Cisco 2811 - IOS 15.2 - 512M RAM - 4 ports");
        logMessage("Cisco 1841 - IOS 15.2 - 384M RAM - 2 ports");
        logMessage("Cisco 2821 - IOS 15.2 - 768M RAM - 4 ports");
    }

    private void afficherSwitches() {
        logMessage("\n📋 SWITCHES CISCO");
        logMessage("Cisco 2960-24TT - 24 ports - 10/100 Mbps");
        logMessage("Cisco 3560-24PS - 24 ports PoE - Gigabit");
        logMessage("Cisco 2950-24 - 24 ports - 10/100 Mbps");
    }

    private void afficherFirewalls() {
        logMessage("\n📋 FIREWALLS CISCO");
        logMessage("ASA 5505 - 150 Mbps - 8 ports - VPN 10");
        logMessage("ASA 5510 - 300 Mbps - 5 ports - VPN 25");
    }

    private void afficherPointsAcces() {
        logMessage("\n📋 POINTS D'ACCÈS");
        logMessage("AIR-AP1242 - 54 Mbps - 2.4GHz");
        logMessage("AIR-AP1252 - 300 Mbps - 2.4/5GHz");
    }

    private void afficherIOSImages() {
        logMessage("\n📋 IMAGES IOS");
        logMessage("c2800nm-advipservicesk9-mz.152-4.M7.bin");
        logMessage("c2960-lanbasek9-mz.152-2.E7.bin");
        logMessage("asa925-ssl-k8.bin");
    }

    private void configurerIDS() {
        logMessage("\n🛡️ IDS/IPS");
        logMessage("Signatures chargées: 2456");
        logMessage("Politiques actives: 3");
    }

    private void afficherJournalAttaques() {
        logMessage("\n⚠️ JOURNAL ATTAQUES");
        logMessage("Aucune attaque détectée");
        logMessage("Dernier scan: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void configurerACL() {
        logMessage("\n📋 ACL LISTS");
        logMessage("access-list 100 permit tcp any any eq 80");
        logMessage("access-list 100 permit tcp any any eq 443");
        logMessage("access-list 100 deny ip any any");
        logMessage("Applied on interface Gi0/0");
    }

    // ========== PAGE TOPOLOGIE ==========

    private JPanel creerPageTopologie() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JPanel zoneDessin = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(30, 35, 45));
                for (int i = 0; i < getWidth(); i += 40) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }

                dessinerLiaisons(g2d);
                dessinerEquipements(g2d);
                dessinerAnimations(g2d);
                dessinerAnimationsConnexion(g2d);
            }
        };
        zoneDessin.setBackground(new Color(15, 20, 25));
        zoneDessin.setPreferredSize(new Dimension(900, 500));

        rendreGlissable(zoneDessin);

        JPanel barreOutils = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barreOutils.setBackground(BG_CARD);

        JButton btnRouteur = new JButton("➕ Routeur");
        btnRouteur.setBackground(CISCO_BLUE);
        btnRouteur.setForeground(Color.WHITE);
        btnRouteur.addActionListener(e -> ajouterEquipement("ROUTEUR"));

        JButton btnSwitch = new JButton("➕ Switch");
        btnSwitch.setBackground(CISCO_DARK);
        btnSwitch.setForeground(Color.WHITE);
        btnSwitch.addActionListener(e -> ajouterEquipement("SWITCH"));

        JButton btnServeur = new JButton("➕ Serveur");
        btnServeur.setBackground(ACCENT_PURPLE);
        btnServeur.setForeground(Color.WHITE);
        btnServeur.addActionListener(e -> ajouterEquipement("SERVEUR"));

        JButton btnPC = new JButton("➕ PC");
        btnPC.setBackground(ACCENT_ORANGE);
        btnPC.setForeground(Color.WHITE);
        btnPC.addActionListener(e -> ajouterEquipement("PC"));

        JButton btnCabler = new JButton("🔌 Câbler");
        btnCabler.setBackground(ACCENT_GREEN);
        btnCabler.setForeground(Color.WHITE);
        btnCabler.addActionListener(e -> {
            modeCablage = !modeCablage;
            logMessage(modeCablage ? "🔌 Mode câblage activé" : "🔌 Mode câblage désactivé");
        });

        barreOutils.add(btnRouteur);
        barreOutils.add(btnSwitch);
        barreOutils.add(btnServeur);
        barreOutils.add(btnPC);
        barreOutils.add(btnCabler);

        panel.add(barreOutils, BorderLayout.NORTH);
        panel.add(new JScrollPane(zoneDessin), BorderLayout.CENTER);

        return panel;
    }

    // ========== PAGE CONFIGURATION ==========

    private JPanel creerPageConfiguration() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSelection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSelection.setBackground(BG_CARD);
        panelSelection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                "🔧 SÉLECTIONNER ÉQUIPEMENT",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                ACCENT_BLUE));

        comboEquipementsConfig = new JComboBox<>();
        comboEquipementsConfig.setPreferredSize(new Dimension(300, 30));
        comboEquipementsConfig.addActionListener(e -> chargerConfigEquipement());

        panelSelection.add(new JLabel("Équipement:"));
        panelSelection.add(comboEquipementsConfig);

        JPanel panelConfig = new JPanel(new GridBagLayout());
        panelConfig.setBackground(BG_CARD);
        panelConfig.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2),
                "⚙️ PARAMÈTRES",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                ACCENT_GREEN));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelConfig.add(new JLabel("Adresse IP:"), gbc);
        gbc.gridx = 1;
        champIP = new JTextField(15);
        panelConfig.add(champIP, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelConfig.add(new JLabel("Masque:"), gbc);
        gbc.gridx = 1;
        champMasque = new JTextField("255.255.255.0");
        panelConfig.add(champMasque, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelConfig.add(new JLabel("VLAN:"), gbc);
        gbc.gridx = 1;
        champVLANConfig = new JTextField("1");
        panelConfig.add(champVLANConfig, gbc);

        JButton btnAppliquer = new JButton("✅ APPLIQUER");
        btnAppliquer.addActionListener(e -> appliquerConfig());

        JPanel panelBoutons = new JPanel(new FlowLayout());
        panelBoutons.setBackground(BG_CARD);
        panelBoutons.add(btnAppliquer);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panelConfig.add(panelBoutons, gbc);

        JPanel panelInterfaces = new JPanel(new BorderLayout());
        panelInterfaces.setBackground(BG_CARD);
        panelInterfaces.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                "🔌 INTERFACES",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                ACCENT_PURPLE));

        zoneInterfaces = new JTextArea();
        zoneInterfaces.setEditable(false);
        zoneInterfaces.setBackground(Color.BLACK);
        zoneInterfaces.setForeground(new Color(0, 255, 0));
        zoneInterfaces.setFont(new Font("Monospaced", Font.PLAIN, 12));
        zoneInterfaces.setRows(8);

        JScrollPane scrollInterfaces = new JScrollPane(zoneInterfaces);
        panelInterfaces.add(scrollInterfaces, BorderLayout.CENTER);

        JPanel panelGauche = new JPanel(new BorderLayout());
        panelGauche.setBackground(BG_DARK);
        panelGauche.add(panelSelection, BorderLayout.NORTH);
        panelGauche.add(panelConfig, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelGauche, panelInterfaces);
        split.setDividerLocation(500);
        split.setBackground(BG_DARK);

        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // ========== SETUP UI ==========

    private void setupUI() {
        setTitle("🌐 CISCO PACKET TRACER SIMULATOR - Sanae · Saida · Rajaa");
        setSize(1600, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_CARD);

        JMenu menuFichier = new JMenu("Fichier");
        menuFichier.setForeground(Color.WHITE);

        JMenuItem menuNouveau = new JMenuItem("Nouvelle simulation");
        menuNouveau.addActionListener(e -> nouvelleSimulation());
        menuFichier.add(menuNouveau);

        JMenuItem menuCharger = new JMenuItem("Charger configuration");
        menuCharger.addActionListener(e -> chargerConfiguration());
        menuFichier.add(menuCharger);

        JMenuItem menuSauver = new JMenuItem("Sauvegarder");
        menuSauver.addActionListener(e -> sauvegarderConfiguration());
        menuFichier.add(menuSauver);

        menuFichier.addSeparator();

        JMenuItem menuQuitter = new JMenuItem("Quitter");
        menuQuitter.addActionListener(e -> System.exit(0));
        menuFichier.add(menuQuitter);

        JMenu menuCisco = new JMenu("Cisco");
        menuCisco.setForeground(Color.WHITE);

        JMenuItem menuRouteurs = new JMenuItem("Routeurs 2800 Series");
        menuRouteurs.addActionListener(e -> afficherRouteurs());
        menuCisco.add(menuRouteurs);

        JMenuItem menuSwitches = new JMenuItem("Switches 2960 Series");
        menuSwitches.addActionListener(e -> afficherSwitches());
        menuCisco.add(menuSwitches);

        JMenuItem menuFirewalls = new JMenuItem("Firewalls ASA 5500");
        menuFirewalls.addActionListener(e -> afficherFirewalls());
        menuCisco.add(menuFirewalls);

        JMenuItem menuAP = new JMenuItem("Points d'accès AIR");
        menuAP.addActionListener(e -> afficherPointsAcces());
        menuCisco.add(menuAP);

        menuCisco.addSeparator();

        JMenuItem menuIOS = new JMenuItem("IOS Images");
        menuIOS.addActionListener(e -> afficherIOSImages());
        menuCisco.add(menuIOS);

        JMenu menuAdmin = new JMenu("Administration");
        menuAdmin.setForeground(Color.WHITE);

        JMenuItem menuModeAdmin = new JMenuItem("Mode Admin");
        menuModeAdmin.addActionListener(e -> basculerModeAdmin());
        menuAdmin.add(menuModeAdmin);

        JMenuItem menuVLAN = new JMenuItem("Configurer VLAN");
        menuVLAN.addActionListener(e -> configurerVLAN());
        menuAdmin.add(menuVLAN);

        JMenuItem menuRoutes = new JMenuItem("Tables de routage");
        menuRoutes.addActionListener(e -> afficherTablesRoutage());
        menuAdmin.add(menuRoutes);

        JMenuItem menuJournal = new JMenuItem("Journal de bord");
        menuJournal.addActionListener(e -> afficherJournalBord());
        menuAdmin.add(menuJournal);

        JMenu menuSecurite = new JMenu("Sécurité");
        menuSecurite.setForeground(Color.WHITE);

        JMenuItem menuFW = new JMenuItem("Pare-feu ASA");
        menuFW.addActionListener(e -> configurerFirewall());
        menuSecurite.add(menuFW);

        JMenuItem menuIDS = new JMenuItem("IDS/IPS");
        menuIDS.addActionListener(e -> configurerIDS());
        menuSecurite.add(menuIDS);

        JMenuItem menuAttaques = new JMenuItem("Journal des attaques");
        menuAttaques.addActionListener(e -> afficherJournalAttaques());
        menuSecurite.add(menuAttaques);

        JMenuItem menuACL = new JMenuItem("ACL Lists");
        menuACL.addActionListener(e -> configurerACL());
        menuSecurite.add(menuACL);

        menuBar.add(menuFichier);
        menuBar.add(menuCisco);
        menuBar.add(menuAdmin);
        menuBar.add(menuSecurite);
        setJMenuBar(menuBar);

        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBackground(BG_DARK);
        panelTitre.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        JLabel labelTitre = new JLabel("🌐 CISCO PACKET TRACER SIMULATOR v5.0");
        labelTitre.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelTitre.setForeground(CISCO_BLUE);

        JLabel labelSousTitre = new JLabel("Sanae · Saida · Rajaa | Mode: " + (modeAdmin ? "ADMIN" : "UTILISATEUR"));
        labelSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelSousTitre.setForeground(TEXT_SECONDARY);

        panelTitre.add(labelTitre, BorderLayout.NORTH);
        panelTitre.add(labelSousTitre, BorderLayout.SOUTH);
        add(panelTitre, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_CARD);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panelTopologie = creerPageTopologie();
        tabbedPane.addTab("📡 TOPOLOGIE", panelTopologie);

        panelConfiguration = creerPageConfiguration();
        tabbedPane.addTab("⚙️ CONFIGURATION", panelConfiguration);

        panelStats = new JPanel(new GridLayout(2, 3, 15, 15));
        panelStats.setBackground(BG_CARD);
        panelStats.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        labelPaquets = new JLabel("📦 PAQUETS: 0");
        labelPaquets.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelPaquets.setForeground(ACCENT_BLUE);

        labelDebit = new JLabel("📊 DÉBIT: 0 Mbps");
        labelDebit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelDebit.setForeground(ACCENT_GREEN);

        labelConnexions = new JLabel("🔗 CONNEXIONS: 0");
        labelConnexions.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelConnexions.setForeground(ACCENT_PURPLE);

        labelTauxReussite = new JLabel("✅ RÉUSSITE: 100%");
        labelTauxReussite.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelTauxReussite.setForeground(SUCCESS_COLOR);

        labelLatence = new JLabel("⏱️ LATENCE: 0 ms");
        labelLatence.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelLatence.setForeground(ACCENT_ORANGE);

        barreBandePassante = new JProgressBar(0, 1000);
        barreBandePassante.setStringPainted(true);
        barreBandePassante.setString("DÉBIT");
        barreBandePassante.setForeground(ACCENT_BLUE);
        barreBandePassante.setBackground(BG_DARK);

        panelStats.add(labelPaquets);
        panelStats.add(labelDebit);
        panelStats.add(labelConnexions);
        panelStats.add(labelTauxReussite);
        panelStats.add(labelLatence);
        panelStats.add(barreBandePassante);

        panelGraph = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2d.setColor(new Color(30, 35, 45));
                g2d.fillRect(0, 0, w, h);

                g2d.setColor(new Color(50, 55, 65));
                for (int i = 0; i < w; i += 30) {
                    g2d.drawLine(i, 0, i, h);
                }
                for (int i = 0; i < h; i += 30) {
                    g2d.drawLine(0, i, w, i);
                }

                if (historiqueDebit.size() > 1) {
                    g2d.setColor(ACCENT_GREEN);
                    g2d.setStroke(new BasicStroke(2));
                    for (int i = 1; i < historiqueDebit.size(); i++) {
                        int x1 = (i - 1) * w / 20;
                        int y1 = h - (historiqueDebit.get(i - 1) * h / 1000);
                        int x2 = i * w / 20;
                        int y2 = h - (historiqueDebit.get(i) * h / 1000);
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                }

                if (historiqueLatence.size() > 1) {
                    g2d.setColor(ACCENT_ORANGE);
                    g2d.setStroke(new BasicStroke(2));
                    for (int i = 1; i < historiqueLatence.size(); i++) {
                        int x1 = (i - 1) * w / 20;
                        int y1 = h - (historiqueLatence.get(i - 1) * h / 100);
                        int x2 = i * w / 20;
                        int y2 = h - (historiqueLatence.get(i) * h / 100);
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        };
        panelGraph.setBackground(BG_CARD);
        panelGraph.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_CYAN, 2),
                " 📈 MONITORING TEMPS RÉEL ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                ACCENT_CYAN));
        panelGraph.setPreferredSize(new Dimension(500, 150));

        panelControle = new JPanel(new GridBagLayout());
        panelControle.setBackground(BG_CARD);
        panelControle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        panelControle.add(new JLabel("📤 SOURCE:"), gbc2);
        gbc2.gridx = 1;
        gbc2.gridwidth = 2;
        comboSource = new JComboBox<>();
        for (EquipementReseau e : equipements) {
            comboSource.addItem(e.getNom());
        }
        panelControle.add(comboSource, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.gridwidth = 1;
        panelControle.add(new JLabel("📥 DESTINATION:"), gbc2);
        gbc2.gridx = 1;
        gbc2.gridwidth = 2;
        comboDestination = new JComboBox<>();
        for (EquipementReseau e : equipements) {
            comboDestination.addItem(e.getNom());
        }
        panelControle.add(comboDestination, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 2;
        panelControle.add(new JLabel("📋 PROTOCOLE:"), gbc2);
        gbc2.gridx = 1;
        comboProtocole = new JComboBox<>(new String[] { "HTTP", "HTTPS", "FTP", "SSH", "DNS", "ICMP" });
        panelControle.add(comboProtocole, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 3;
        panelControle.add(new JLabel("📦 DONNÉES:"), gbc2);
        gbc2.gridx = 1;
        champDonnees = new JTextField("GET /index.html");
        panelControle.add(champDonnees, gbc2);

        JPanel panelBoutons2 = new JPanel(new GridLayout(4, 4, 5, 5));
        panelBoutons2.setBackground(BG_CARD);
        panelBoutons2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        boutonEnvoyer = creerBouton("📨 ENVOYER", ACCENT_BLUE);
        boutonPing = creerBouton("📡 PING", ACCENT_GREEN);
        boutonTraceroute = creerBouton("🔄 TRACEROUTE", ACCENT_PURPLE);
        boutonScan = creerBouton("🔍 SCAN", ACCENT_YELLOW);
        boutonReset = creerBouton("♻️ RESET", ACCENT_ORANGE);
        boutonPanne = creerBouton("💥 PANNE", ACCENT_RED);
        boutonTableARP = creerBouton("📋 ARP", new Color(100, 150, 255));
        boutonDDOS = creerBouton("⚠️ ATTAQUE", WARNING_COLOR);
        boutonExport = creerBouton("📄 EXPORT", new Color(150, 100, 200));
        boutonLogin = creerBouton("🔐 ADMIN", new Color(0, 200, 200));
        boutonCLI = creerBouton("💻 CLI", CISCO_BLUE);
        boutonSimulation = creerBouton("🎬 SIMU", ACCENT_CYAN);
        boutonFirewall = creerBouton("🔥 FW", ACCENT_RED);

        boutonEnvoyer.addActionListener(e -> envoyerPaquet());
        boutonPing.addActionListener(e -> ping());
        boutonTraceroute.addActionListener(e -> traceroute());
        boutonScan.addActionListener(e -> scanReseau());
        boutonReset.addActionListener(e -> resetReseau());
        boutonPanne.addActionListener(e -> simulerPanne());
        boutonTableARP.addActionListener(e -> afficherTableARP());
        boutonDDOS.addActionListener(e -> simulerAttaque());
        boutonExport.addActionListener(e -> exporterLogs());
        boutonLogin.addActionListener(e -> loginAdmin());
        boutonCLI.addActionListener(e -> ouvrirCLI());
        boutonSimulation.addActionListener(e -> activerModeSimulation());
        boutonFirewall.addActionListener(e -> configurerFirewall());

        panelBoutons2.add(boutonEnvoyer);
        panelBoutons2.add(boutonPing);
        panelBoutons2.add(boutonTraceroute);
        panelBoutons2.add(boutonScan);
        panelBoutons2.add(boutonReset);
        panelBoutons2.add(boutonPanne);
        panelBoutons2.add(boutonTableARP);
        panelBoutons2.add(boutonDDOS);
        panelBoutons2.add(boutonExport);
        panelBoutons2.add(boutonLogin);
        panelBoutons2.add(boutonCLI);
        panelBoutons2.add(boutonSimulation);
        panelBoutons2.add(boutonFirewall);

        JPanel panelControlePrincipal = new JPanel();
        panelControlePrincipal.setLayout(new BoxLayout(panelControlePrincipal, BoxLayout.Y_AXIS));
        panelControlePrincipal.setBackground(BG_DARK);
        panelControlePrincipal.add(panelControle);
        panelControlePrincipal.add(panelBoutons2);
        panelControlePrincipal.add(panelStats);
        panelControlePrincipal.add(panelGraph);

        consoleNetwork = new JTextArea();
        consoleNetwork.setEditable(false);
        consoleNetwork.setBackground(Color.BLACK);
        consoleNetwork.setForeground(new Color(0, 255, 100));
        consoleNetwork.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollConsole = new JScrollPane(consoleNetwork);
        scrollConsole.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2),
                " 📋 CONSOLE RÉSEAU ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                ACCENT_GREEN));

        JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tabbedPane, panelControlePrincipal);
        splitHorizontal.setDividerLocation(900);
        splitHorizontal.setBackground(BG_DARK);

        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                splitHorizontal, scrollConsole);
        splitVertical.setDividerLocation(600);
        splitVertical.setBackground(BG_DARK);

        add(splitVertical, BorderLayout.CENTER);

        logMessage("=".repeat(70));
        logMessage("🚀 SIMULATEUR CISCO DÉMARRÉ");
        logMessage("👤 Sanae · Saida · Rajaa");
        logMessage("📡 " + equipements.size() + " équipements");
        logMessage("=".repeat(70));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new NetworkSimulatorUltimate().setVisible(true));
    }
}