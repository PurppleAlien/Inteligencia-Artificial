import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

// ════════════════════════════════════════════════════════════════════════════
//  Búsqueda de Rutas  ·  BFS / UCS / A*  ·  Interfaz renovada con grafo
// ════════════════════════════════════════════════════════════════════════════
public class Rutas_CG_COMPLETO extends JFrame {

    // ── Paleta ──────────────────────────────────────────────────────────────
    static final Color C_BG      = new Color(13, 17, 27);
    static final Color C_PANEL   = new Color(20, 25, 40);
    static final Color C_CARD    = new Color(28, 35, 54);
    static final Color C_BORDER  = new Color(48, 58, 84);
    static final Color C_ACCENT  = new Color(99, 102, 241);
    static final Color C_SUCCESS = new Color(52, 211, 153);
    static final Color C_WARN    = new Color(251, 191, 36);
    static final Color C_DANGER  = new Color(248, 113, 113);
    static final Color C_TEXT    = new Color(226, 232, 240);
    static final Color C_MUTED   = new Color(100, 116, 139);

    // ── Coordenadas de ciudades para el mapa (lat, lon)  ────────────────────
    // Transformadas a píxeles en GraphPanel
    private static final String[] CIUDADES = {
        "Acapulco","Cuernavaca","DF","Huatulco","Pachuca",
        "Poza Rica","Puebla","Queretaro","Toluca","Veracruz"
    };

    // Coordenadas geográficas reales (lat, lon)
    private static final double[][] GEO = {
        {16.85, 99.82}, // Acapulco
        {18.92, 99.22}, // Cuernavaca
        {19.25, 99.13}, // DF
        {15.83, 96.31}, // Huatulco
        {20.10, 98.76}, // Pachuca
        {20.53, 97.46}, // Poza Rica
        {19.04, 98.21}, // Puebla
        {20.56,100.39}, // Queretaro
        {19.28, 99.66}, // Toluca
        {19.17, 96.13}, // Veracruz
    };

    // ── Estado ──────────────────────────────────────────────────────────────
    private List<String>  currentPath     = new ArrayList<>();
    private Set<String>   highlightedPath = new HashSet<>();
    private String        startCity = null, endCity = null;
    private int           animPathIdx = 0;
    private javax.swing.Timer pathAnimTimer;

    // ── Componentes ─────────────────────────────────────────────────────────
    private GraphPanel       graphPanel;
    private JComboBox<String> cityStart, cityEnd;
    private JRadioButton      btnBFS, btnUCS, btnAStar;
    private JTextArea         resultArea;
    private JButton           runBtn, compareBtn;
    private JProgressBar      progBar;

    private Rutas_SG_BFS            buscBFS  = new Rutas_SG_BFS();
    private Rutas_SG_UCS            buscUCS  = new Rutas_SG_UCS();
    private Rutas_SG_UCS_Heuristica buscAStar = new Rutas_SG_UCS_Heuristica();

    // ════════════════════════════════════════════════════════════════════════
    public Rutas_CG_COMPLETO() {
        setTitle("Problema de Rutas · BFS / UCS / A*");
        setSize(1100, 740);
        setMinimumSize(new Dimension(960, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(14, 12));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(16, 18, 14, 18));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);

        pathAnimTimer = new javax.swing.Timer(380, e -> tickPathAnim());
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JLabel t1 = lbl("BÚSQUEDA DE RUTAS", 24, Font.BOLD, C_TEXT);
        JLabel t2 = lbl("Comparativa BFS · UCS · A*  —  10 ciudades de México", 13, Font.PLAIN, C_MUTED);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(C_BG);
        left.add(t1); left.add(t2);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(C_BG);
        wrap.add(left, BorderLayout.WEST);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(C_BG);
        p.add(wrap, BorderLayout.CENTER);
        p.add(sep,  BorderLayout.SOUTH);
        return p;
    }

    // ── Centro principal ────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setBackground(C_BG);

        graphPanel = new GraphPanel();
        p.add(graphPanel,     BorderLayout.CENTER);
        p.add(buildSidebar(), BorderLayout.EAST);

        return p;
    }

    // ── Sidebar derecho ─────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(C_BG);
        p.setPreferredSize(new Dimension(320, 0));

        p.add(buildControlCard(), BorderLayout.NORTH);
        p.add(buildResultCard(),  BorderLayout.CENTER);

        return p;
    }

    private JPanel buildControlCard() {
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Ciudad origen
        addSec(p, "CIUDAD ORIGEN");
        cityStart = combo(CIUDADES);
        p.add(cityStart);
        p.add(gap(10));

        // Ciudad destino
        addSec(p, "CIUDAD DESTINO");
        cityEnd = combo(CIUDADES);
        p.add(cityEnd);
        p.add(gap(14));

        // Algoritmo
        addSec(p, "ALGORITMO");
        ButtonGroup bg = new ButtonGroup();
        btnBFS   = radio("Búsqueda en Amplitud (BFS)");
        btnUCS   = radio("Costo Uniforme (UCS)");
        btnAStar = radio("A* — Heurística geodésica");
        bg.add(btnBFS); bg.add(btnUCS); bg.add(btnAStar);
        btnAStar.setSelected(true);

        for (JRadioButton r : new JRadioButton[]{btnBFS, btnUCS, btnAStar}) {
            r.setAlignmentX(LEFT_ALIGNMENT);
            p.add(r);
            p.add(gap(3));
        }
        p.add(gap(14));

        // Botones
        runBtn     = bigBtn("▶  Ejecutar búsqueda",     C_ACCENT);
        compareBtn = bigBtn("≡  Comparar los 3 algos",  C_SUCCESS);

        runBtn    .addActionListener(e -> runSearch());
        compareBtn.addActionListener(e -> compareAll());

        for (JButton b : new JButton[]{runBtn, compareBtn}) {
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            p.add(b);
            p.add(gap(6));
        }

        return p;
    }

    private JPanel buildResultCard() {
        JPanel p = card();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel t = lbl("RESULTADOS", 10, Font.BOLD, C_MUTED);
        t.setBorder(new EmptyBorder(0, 0, 6, 0));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(10, 14, 22));
        resultArea.setForeground(C_TEXT);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultArea.setMargin(new Insets(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createLineBorder(C_BORDER));

        p.add(t,      BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFooter() {
        progBar = new JProgressBar();
        progBar.setForeground(C_ACCENT);
        progBar.setBackground(C_CARD);
        progBar.setBorderPainted(false);
        progBar.setStringPainted(true);
        progBar.setString("Selecciona ciudades y algoritmo");
        progBar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        progBar.setPreferredSize(new Dimension(0, 20));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(4, 0, 0, 0));
        p.add(progBar);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Acciones
    // ════════════════════════════════════════════════════════════════════════

    private void runSearch() {
        String ini = (String) cityStart.getSelectedItem();
        String fin = (String) cityEnd  .getSelectedItem();

        if (ini == null || fin == null || ini.equals(fin)) {
            JOptionPane.showMessageDialog(this,
                "Selecciona dos ciudades diferentes.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!btnBFS.isSelected() && !btnUCS.isSelected() && !btnAStar.isSelected()) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un algoritmo.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        startCity = ini; endCity = fin;
        pathAnimTimer.stop();
        clearPath();

        runBtn    .setEnabled(false);
        compareBtn.setEnabled(false);
        progBar.setIndeterminate(true);
        progBar.setString("Buscando...");
        resultArea.setText("");

        String algo = btnBFS.isSelected() ? "BFS" : btnUCS.isSelected() ? "UCS" : "A*";
        log("=== " + algo + " : " + ini + " → " + fin + " ===\n");

        new Thread(() -> {
            List<String> path = null;
            int cost = 0;
            long ms  = 0;
            int steps = 0;

            if (btnBFS.isSelected()) {
                long t0 = System.currentTimeMillis();
                Nodo_BFS sol = buscBFS.resolver(ini, fin);
                ms = System.currentTimeMillis() - t0;
                if (sol != null) { path = traceBack(sol); }
            } else if (btnUCS.isSelected()) {
                long t0 = System.currentTimeMillis();
                Nodo_BFS sol = buscUCS.resolver(ini, fin);
                ms = System.currentTimeMillis() - t0;
                if (sol != null) { path = traceBack(sol); cost = sol.getCoste(); }
            } else {
                ResultadoBusqueda res = buscAStar.buscarSolucionAStar(ini, fin);
                path = res.getRuta().isEmpty() ? null : res.getRuta();
                cost = res.getCosto();
                ms   = res.getTiempoEjecucion();
            }

            final List<String> finalPath = path;
            final int finalCost = cost;
            final long finalMs  = ms;

            SwingUtilities.invokeLater(() -> {
                progBar.setIndeterminate(false);
                runBtn    .setEnabled(true);
                compareBtn.setEnabled(true);

                if (finalPath == null || finalPath.isEmpty()) {
                    log("No se encontró ruta.");
                    progBar.setString("Sin ruta");
                    return;
                }

                progBar.setString(algo + " · " + (finalPath.size()-1) + " saltos · " + finalCost + " km");
                logPath(algo, ini, fin, finalPath, finalCost, finalMs);
                animatePath(finalPath);
            });
        }).start();
    }

    private void compareAll() {
        String ini = (String) cityStart.getSelectedItem();
        String fin = (String) cityEnd  .getSelectedItem();

        if (ini == null || fin == null || ini.equals(fin)) {
            JOptionPane.showMessageDialog(this,
                "Selecciona dos ciudades diferentes.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        pathAnimTimer.stop();
        clearPath();
        runBtn    .setEnabled(false);
        compareBtn.setEnabled(false);
        progBar.setIndeterminate(true);
        progBar.setString("Comparando algoritmos...");
        resultArea.setText("");

        log("╔══════════════════════════════════════════╗");
        log("║     COMPARACIÓN DE ALGORITMOS           ║");
        log("╠══════════════════════════════════════════╣");
        log("  Ruta: " + ini + " → " + fin);
        log("╚══════════════════════════════════════════╝\n");

        new Thread(() -> {
            // BFS
            long t0 = System.currentTimeMillis();
            Nodo_BFS solBFS = buscBFS.resolver(ini, fin);
            long msBFS = System.currentTimeMillis() - t0;

            // UCS
            t0 = System.currentTimeMillis();
            Nodo_BFS solUCS = buscUCS.resolver(ini, fin);
            long msUCS = System.currentTimeMillis() - t0;

            // A*
            ResultadoBusqueda resA = buscAStar.buscarSolucionAStar(ini, fin);

            List<String> pathBFS = solBFS != null ? traceBack(solBFS) : null;
            List<String> pathUCS = solUCS != null ? traceBack(solUCS) : null;
            List<String> pathA   = !resA.getRuta().isEmpty() ? resA.getRuta() : null;

            SwingUtilities.invokeLater(() -> {
                progBar.setIndeterminate(false);
                runBtn    .setEnabled(true);
                compareBtn.setEnabled(true);
                progBar.setString("Comparación completa");

                StringBuilder sb = new StringBuilder();
                sb.append(String.format(" %-6s %-6s %-8s %-10s %s%n", "Algo", "Saltos","Costo","Tiempo","Ruta"));
                sb.append(" " + "─".repeat(65) + "\n");

                appendRow(sb, "BFS",  pathBFS, solBFS != null ? 0  : 0, msBFS);
                appendRow(sb, "UCS",  pathUCS, solUCS != null ? solUCS.getCoste() : 0, msUCS);
                appendRow(sb, "A*",   pathA,   resA.getCosto(), resA.getTiempoEjecucion());

                log(sb.toString());

                // Animar el mejor camino (A* por defecto si existe)
                List<String> best = pathA != null ? pathA : pathUCS != null ? pathUCS : pathBFS;
                if (best != null) animatePath(best);
            });
        }).start();
    }

    private void appendRow(StringBuilder sb, String algo, List<String> path, int cost, long ms) {
        if (path == null) {
            sb.append(String.format(" %-6s SIN RUTA%n", algo));
        } else {
            String ruta = String.join("→", path);
            sb.append(String.format(" %-6s %-6d %-8s %-10s %s%n",
                algo, path.size()-1,
                cost > 0 ? cost + "km" : "—",
                ms + "ms",
                ruta));
        }
    }

    // ── Animación de ruta ───────────────────────────────────────────────────

    private void animatePath(List<String> path) {
        currentPath = new ArrayList<>(path);
        highlightedPath.clear();
        animPathIdx = 0;
        graphPanel.repaint();
        pathAnimTimer.setDelay(500);
        pathAnimTimer.start();
    }

    private void tickPathAnim() {
        if (animPathIdx >= currentPath.size()) {
            pathAnimTimer.stop();
            return;
        }
        highlightedPath.add(currentPath.get(animPathIdx++));
        graphPanel.repaint();
    }

    private void clearPath() {
        currentPath.clear();
        highlightedPath.clear();
        graphPanel.repaint();
    }

    // ── Utilidades ──────────────────────────────────────────────────────────

    private List<String> traceBack(Nodo_BFS nodo) {
        LinkedList<String> c = new LinkedList<>();
        while (nodo != null) { c.addFirst(nodo.getDatos()); nodo = nodo.getPadre(); }
        return c;
    }

    private void log(String s) {
        resultArea.append(s + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    private void logPath(String algo, String ini, String fin,
                          List<String> path, int cost, long ms) {
        log("Algoritmo: " + algo);
        log("Origen: " + ini + "   Destino: " + fin);
        log("Ruta: " + String.join(" → ", path));
        log("Ciudades: " + path.size() + "   Saltos: " + (path.size()-1));
        if (cost > 0) log("Costo total: " + cost + " km");
        log("Tiempo: " + ms + " ms\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Panel del grafo (Java2D)
    // ════════════════════════════════════════════════════════════════════════
    class GraphPanel extends JPanel {

        // Aristas del grafo con distancias
        private final int[][] EDGES = {
            // índices de CIUDADES[] : {desde, hasta, km}
            {0, 8, 388}, // Acapulco - Toluca
            {0, 2, 382}, // Acapulco - DF
            {0, 1, 291}, // Acapulco - Cuernavaca
            {1, 2,  49}, // Cuernavaca - DF
            {2, 7, 218}, // DF - Queretaro
            {2, 8,  60}, // DF - Toluca
            {2, 4,  90}, // DF - Pachuca
            {2, 5, 276}, // DF - Poza Rica
            {2, 6, 134}, // DF - Puebla
            {2, 9, 397}, // DF - Veracruz
            {2, 3, 695}, // DF - Huatulco
            {4, 5, 183}, // Pachuca - Poza Rica
            {6, 9, 273}, // Puebla - Veracruz
            {6, 3, 596}, // Puebla - Huatulco
            {7, 8, 196}, // Queretaro - Toluca
        };

        private int[] px, py; // coordenadas pixel de cada ciudad

        GraphPanel() {
            setBackground(C_BG);
            setPreferredSize(new Dimension(720, 460));
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) { calcPositions(); repaint(); }
            });
        }

        // Convierte coordenadas geográficas a píxeles
        private void calcPositions() {
            int W = getWidth()  == 0 ? 720 : getWidth();
            int H = getHeight() == 0 ? 460 : getHeight();
            int PAD = 55;

            double latMin =  15.83, latMax = 20.56;
            double lonMin =  96.13, lonMax = 100.39;

            px = new int[CIUDADES.length];
            py = new int[CIUDADES.length];

            for (int i = 0; i < CIUDADES.length; i++) {
                double lat = GEO[i][0], lon = GEO[i][1];
                // lon más alto = más al oeste (izquierda del mapa)
                px[i] = PAD + (int) ((lonMax - lon) / (lonMax - lonMin) * (W - 2*PAD));
                // lat más alto = más al norte (arriba del mapa)
                py[i] = PAD + (int) ((latMax - lat) / (latMax - latMin) * (H - 2*PAD));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (px == null) calcPositions();

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            // Título del panel
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(C_MUTED);
            g2.drawString("MAPA — Red de ciudades de México  (distancias en km)", 10, 18);

            // ── Leyenda ──────────────────────────────────────────────────────
            drawLegend(g2);

            // ── Aristas ──────────────────────────────────────────────────────
            for (int[] e : EDGES) {
                int a = e[0], b = e[1]; int km = e[2];
                boolean inPath  = currentPath.contains(CIUDADES[a]) && currentPath.contains(CIUDADES[b])
                               && pathConnected(CIUDADES[a], CIUDADES[b]);
                boolean animated = highlightedPath.contains(CIUDADES[a]) && highlightedPath.contains(CIUDADES[b])
                                && pathConnected(CIUDADES[a], CIUDADES[b]);

                if (animated) {
                    g2.setColor(C_SUCCESS);
                    g2.setStroke(new BasicStroke(3.5f));
                } else if (inPath) {
                    g2.setColor(new Color(99, 102, 241, 120));
                    g2.setStroke(new BasicStroke(2.5f));
                } else {
                    g2.setColor(C_BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                }
                g2.drawLine(px[a], py[a], px[b], py[b]);
                g2.setStroke(new BasicStroke(1));

                // Etiqueta de distancia sobre la arista activa
                if (animated || inPath) {
                    int mx = (px[a] + px[b]) / 2;
                    int my = (py[a] + py[b]) / 2;
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g2.setColor(C_WARN);
                    String kml = km + " km";
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(kml);
                    g2.setColor(new Color(13, 17, 27, 180));
                    g2.fillRoundRect(mx - tw/2 - 3, my - 9, tw + 6, 14, 4, 4);
                    g2.setColor(C_WARN);
                    g2.drawString(kml, mx - tw/2, my + 1);
                } else {
                    // Etiqueta pequeña siempre visible
                    int mx = (px[a] + px[b]) / 2;
                    int my = (py[a] + py[b]) / 2;
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.setColor(new Color(80, 95, 130));
                    g2.drawString(km + "", mx - 8, my - 2);
                }
            }

            // ── Nodos ────────────────────────────────────────────────────────
            for (int i = 0; i < CIUDADES.length; i++) {
                String city = CIUDADES[i];
                boolean isStart  = city.equals(startCity);
                boolean isEnd    = city.equals(endCity);
                boolean onPath   = currentPath.contains(city);
                boolean animated = highlightedPath.contains(city);

                int r = 14; // radio del nodo

                // Sombra
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillOval(px[i]-r+2, py[i]-r+3, r*2, r*2);

                // Relleno
                if (isStart) {
                    g2.setColor(C_SUCCESS);
                } else if (isEnd) {
                    g2.setColor(C_DANGER);
                } else if (animated) {
                    g2.setColor(C_ACCENT);
                } else if (onPath) {
                    g2.setColor(new Color(99, 102, 241, 160));
                } else {
                    g2.setColor(C_CARD);
                }
                g2.fillOval(px[i]-r, py[i]-r, r*2, r*2);

                // Borde
                if (isStart || isEnd) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f));
                } else if (animated) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(C_BORDER);
                    g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawOval(px[i]-r, py[i]-r, r*2, r*2);
                g2.setStroke(new BasicStroke(1));

                // Etiqueta
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String lbl = city;
                int lw = fm.stringWidth(lbl);
                int lx = px[i] - lw/2;
                int ly = py[i] + r + 13;

                // Fondo etiqueta
                g2.setColor(new Color(13, 17, 27, 200));
                g2.fillRoundRect(lx - 3, ly - 11, lw + 6, 14, 4, 4);

                g2.setColor(isStart ? C_SUCCESS : isEnd ? C_DANGER : animated ? C_ACCENT : C_TEXT);
                g2.drawString(lbl, lx, ly);

                // Marcador S/E
                if (isStart || isEnd) {
                    String tag = isStart ? "S" : "E";
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2.drawString(tag, px[i] - 4, py[i] + 4);
                }
            }
        }

        private boolean pathConnected(String a, String b) {
            if (currentPath.size() < 2) return false;
            for (int i = 0; i < currentPath.size()-1; i++) {
                String x = currentPath.get(i), y = currentPath.get(i+1);
                if ((x.equals(a) && y.equals(b)) || (x.equals(b) && y.equals(a))) return true;
            }
            return false;
        }

        private void drawLegend(Graphics2D g2) {
            int lx = 12, ly = getHeight() - 70;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

            Object[][] items = {
                {C_SUCCESS,         "Origen (S)"},
                {C_DANGER,          "Destino (E)"},
                {C_ACCENT,          "Ruta encontrada"},
                {C_BORDER,          "Conexión disponible"},
            };

            for (int i = 0; i < items.length; i++) {
                Color c = (Color) items[i][0];
                String txt = (String) items[i][1];
                g2.setColor(c);
                g2.fillOval(lx, ly + i*16 - 7, 10, 10);
                g2.setColor(C_MUTED);
                g2.drawString(txt, lx + 14, ly + i*16 + 2);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers UI
    // ════════════════════════════════════════════════════════════════════════

    private static JLabel lbl(String t, int sz, int style, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", style, sz));
        l.setForeground(c);
        return l;
    }

    private static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(C_CARD);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER));
        return p;
    }

    private static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(C_PANEL);
        cb.setForeground(C_TEXT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(C_BORDER));
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return cb;
    }

    private static JRadioButton radio(String txt) {
        JRadioButton r = new JRadioButton(txt);
        r.setBackground(C_CARD);
        r.setForeground(C_TEXT);
        r.setFont(new Font("SansSerif", Font.PLAIN, 12));
        r.setFocusPainted(false);
        return r;
    }

    private static JButton bigBtn(String txt, Color accent) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? accent.darker()
                         : getModel().isRollover() ? accent.brighter()
                         : C_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth()-1.5f, getHeight()-1.5f, 8, 8));
                super.paintComponent(g);
            }
        };
        b.setForeground(C_TEXT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void addSec(JPanel p, String title) {
        JLabel l = lbl(title, 10, Font.BOLD, C_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(l);
    }

    private static Component gap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Rutas_CG_COMPLETO().setVisible(true));
    }
}
