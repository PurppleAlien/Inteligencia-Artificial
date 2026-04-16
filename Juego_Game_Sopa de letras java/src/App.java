// Programa Sopa De Letras — Versión Mejorada
// Características: diseño dark, tiempos por palabra, resolver todo animado,
// buscar palabra por palabra y selección manual con el mouse.

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class App extends JFrame {

    // ── CONSTANTES DE DISEÑO ─────────────────────────────────────────────────
    private static final int GRID_SIZE   = 20;
    private static final int CELL_SIZE   = 34;
    private static final int GRID_PAD    = 14;

    // Paleta dark-space
    private static final Color BG_MAIN      = new Color(10,  15,  35);
    private static final Color BG_PANEL     = new Color(18,  25,  55);
    private static final Color BG_CELL      = new Color(22,  32,  68);
    private static final Color COLOR_TEXT   = new Color(200, 220, 255);
    private static final Color COLOR_TITLE  = new Color(80,  180, 255);
    private static final Color COLOR_BORDER = new Color(40,  70,  140);

    // Colores de palabras encontradas (cíclicos)
    private static final Color[] WORD_COLORS = {
        new Color(255,  90,  90, 210),
        new Color( 90, 255, 140, 210),
        new Color(255, 210,  50, 210),
        new Color(160, 100, 255, 210),
        new Color(255, 140,  50, 210),
        new Color( 50, 210, 255, 210),
        new Color(255,  90, 200, 210),
        new Color(140, 255,  50, 210),
        new Color(255, 170, 170, 210),
        new Color( 50, 255, 230, 210),
    };

    private static final Color COLOR_SEL_START  = new Color(255, 235,  50, 200);
    private static final Color COLOR_SEL_LINE   = new Color(255, 200,  50, 110);
    private static final Color COLOR_MANUAL_OK  = new Color( 50, 255, 190, 210);

    // ── ESTADO DEL JUEGO ─────────────────────────────────────────────────────
    private final char[][]  grid       = new char [GRID_SIZE][GRID_SIZE];
    private final Color[][] cellColors = new Color[GRID_SIZE][GRID_SIZE];

    private List<String>         palabras        = new ArrayList<>();
    /** palabra → {fila, col, dFila, dCol} */
    private Map<String, int[]>   posiciones      = new LinkedHashMap<>();
    private Set<String>          resueltas       = new LinkedHashSet<>();
    private int                  colorIndex      = 0;

    // Selección manual
    private int[]       selInicio       = null;
    private List<int[]> selLinea        = new ArrayList<>();

    // Tiempos
    private long tiempoInicioTotal;
    private long tiempoInicioPalabra;

    // ── COMPONENTES UI ───────────────────────────────────────────────────────
    private GridPanel  gridPanel;
    private JTextArea  areaPalabras;
    private DefaultListModel<String> modelEncontradas;
    private JList<String>            listaEncontradas;
    private JTextArea  areaTiempos;
    private JLabel     labelEstado;
    private JLabel     labelTiempo;
    private JButton    btnNueva, btnSiguiente, btnTodo, btnLimpiar;

    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }

    public App() {
        super("Sopa de Letras — IA");
        initUI();
        cargarPalabras();
        actualizarAreaPalabras();
    }

    // ── CONSTRUCCIÓN DE LA UI ────────────────────────────────────────────────

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1230, 850);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        root.add(buildHeader(),     BorderLayout.NORTH);
        root.add(buildGridArea(),   BorderLayout.CENTER);
        root.add(buildSidePanel(),  BorderLayout.EAST);
        root.add(buildButtonBar(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(10, 16, 10, 16)));

        JLabel title = new JLabel("✦  SOPA  DE  LETRAS  ✦", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_TITLE);
        p.add(title, BorderLayout.CENTER);

        labelTiempo = new JLabel("⏱  --", SwingConstants.RIGHT);
        labelTiempo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelTiempo.setForeground(new Color(255, 210, 60));
        p.add(labelTiempo, BorderLayout.EAST);

        labelEstado = new JLabel("  Haz clic en  «Nueva Sopa»  para empezar");
        labelEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        labelEstado.setForeground(new Color(150, 180, 255));
        p.add(labelEstado, BorderLayout.WEST);

        return p;
    }

    private JPanel buildGridArea() {
        gridPanel = new GridPanel();
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        wrap.setBackground(BG_MAIN);
        wrap.add(gridPanel);
        return wrap;
    }

    private JPanel buildSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(10, 8, 10, 8)));
        panel.setPreferredSize(new Dimension(215, 600));

        // — Palabras a buscar —
        panel.add(secLabel("PALABRAS A BUSCAR"));
        areaPalabras = darkTA();
        panel.add(darkScroll(areaPalabras, 195, 185, new Color(50, 80, 150)));
        panel.add(Box.createVerticalStrut(10));

        // — Palabras encontradas —
        panel.add(secLabel("PALABRAS ENCONTRADAS"));
        modelEncontradas = new DefaultListModel<>();
        listaEncontradas = new JList<>(modelEncontradas);
        listaEncontradas.setBackground(new Color(10, 30, 20));
        listaEncontradas.setForeground(new Color(80, 255, 140));
        listaEncontradas.setFont(new Font("Monospaced", Font.BOLD, 12));
        listaEncontradas.setSelectionBackground(new Color(30, 80, 50));
        panel.add(darkScroll(listaEncontradas, 195, 165, new Color(30, 100, 60)));
        panel.add(Box.createVerticalStrut(10));

        // — Log de tiempos —
        panel.add(secLabel("LOG DE TIEMPOS"));
        areaTiempos = darkTA();
        areaTiempos.setForeground(new Color(255, 200, 50));
        areaTiempos.setBackground(new Color(15, 20, 10));
        panel.add(darkScroll(areaTiempos, 195, 160, new Color(100, 80, 20)));

        return panel;
    }

    private JLabel secLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(100, 150, 255));
        l.setBorder(new EmptyBorder(4, 0, 3, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextArea darkTA() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(BG_CELL);
        ta.setForeground(COLOR_TEXT);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setBorder(new EmptyBorder(5, 6, 5, 6));
        ta.setCaretColor(Color.WHITE);
        return ta;
    }

    private JScrollPane darkScroll(Component c, int w, int h, Color border) {
        JScrollPane sp = new JScrollPane(c);
        sp.setPreferredSize(new Dimension(w, h));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        sp.setBorder(new LineBorder(border, 1));
        sp.getViewport().setBackground(BG_CELL);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    private JPanel buildButtonBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        p.setBackground(BG_PANEL);
        p.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)));

        btnNueva     = mkBtn("⟳  Nueva Sopa",       new Color(35, 100, 200));
        btnSiguiente = mkBtn("▶  Siguiente Palabra", new Color(35, 155,  75));
        btnTodo      = mkBtn("⚡  Resolver Todo",     new Color(195,  65,  35));
        btnLimpiar   = mkBtn("↺  Limpiar Colores",   new Color(125,  75,  25));

        btnNueva    .addActionListener(e -> generarNuevaSopa());
        btnSiguiente.addActionListener(e -> resolverSiguiente());
        btnTodo     .addActionListener(e -> resolverTodo());
        btnLimpiar  .addActionListener(e -> limpiarColores());

        p.add(btnNueva);
        p.add(btnSiguiente);
        p.add(btnTodo);
        p.add(btnLimpiar);

        JLabel hint = new JLabel("  Mouse: 1er clic = inicio  |  2do clic = fin  →  selección manual");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(140, 160, 210));
        p.add(hint);
        return p;
    }

    private JButton mkBtn(String text, Color base) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isRollover() ? base.brighter() : base;
                Color bot = getModel().isPressed()  ? base.darker()   : base.darker();
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(base.brighter());
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(185, 38));
        return btn;
    }

    // ── PANEL DE LA CUADRÍCULA (dibujado con paintComponent) ─────────────────

    private class GridPanel extends JPanel {

        GridPanel() {
            int side = GRID_SIZE * CELL_SIZE + GRID_PAD * 2;
            setPreferredSize(new Dimension(side, side));
            setBackground(BG_MAIN);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int[] cell = toCell(e.getX(), e.getY());
                    if (cell == null) return;

                    if (selInicio == null) {
                        // Primer clic → fijar inicio
                        selInicio = cell;
                        selLinea.clear();
                        selLinea.add(cell.clone());
                        labelEstado.setText(
                            "Inicio (" + cell[0] + "," + cell[1] + ")  →  ahora clic en celda final");
                    } else {
                        // Segundo clic → intentar completar la selección
                        intentarSeleccion(selInicio, cell);
                        selInicio = null;
                        selLinea.clear();
                    }
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (selInicio == null) return;
                    int[] cell = toCell(e.getX(), e.getY());
                    if (cell != null) {
                        selLinea = lineaCeldas(selInicio, cell);
                        repaint();
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int[] toCell(int px, int py) {
            int c = (px - GRID_PAD) / CELL_SIZE;
            int r = (py - GRID_PAD) / CELL_SIZE;
            if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE) return null;
            return new int[]{r, c};
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fondo de la cuadrícula
            int gSize = GRID_SIZE * CELL_SIZE;
            g2.setColor(new Color(12, 18, 48));
            g2.fillRoundRect(GRID_PAD - 4, GRID_PAD - 4, gSize + 8, gSize + 8, 12, 12);
            g2.setColor(new Color(50, 80, 160));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(GRID_PAD - 4, GRID_PAD - 4, gSize + 8, gSize + 8, 12, 12);

            // Conjunto de celdas en selección activa
            Set<String> enSel = new HashSet<>();
            for (int[] sc : selLinea) enSel.add(sc[0] + "," + sc[1]);

            Font fLetra = new Font("Consolas", Font.BOLD, 16);
            g2.setFont(fLetra);
            FontMetrics fm = g2.getFontMetrics();

            for (int r = 0; r < GRID_SIZE; r++) {
                for (int c = 0; c < GRID_SIZE; c++) {
                    int x = GRID_PAD + c * CELL_SIZE;
                    int y = GRID_PAD + r * CELL_SIZE;

                    // Color de fondo de la celda
                    Color bg = BG_CELL;
                    if (cellColors[r][c] != null) {
                        bg = cellColors[r][c];
                    }
                    if (selInicio != null && selInicio[0] == r && selInicio[1] == c) {
                        bg = COLOR_SEL_START;
                    } else if (enSel.contains(r + "," + c)) {
                        bg = COLOR_SEL_LINE;
                    }

                    g2.setColor(bg);
                    g2.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);

                    // Borde de celda
                    g2.setColor(COLOR_BORDER);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);

                    // Letra
                    if (grid[r][c] != 0) {
                        String letra = String.valueOf(grid[r][c]);
                        int tx = x + (CELL_SIZE - fm.stringWidth(letra)) / 2;
                        int ty = y + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;

                        // Sombra
                        g2.setColor(new Color(0, 0, 0, 100));
                        g2.drawString(letra, tx + 1, ty + 1);

                        // Texto blanco si está resaltada, tenue si no
                        g2.setColor(cellColors[r][c] != null ? Color.WHITE : COLOR_TEXT);
                        g2.drawString(letra, tx, ty);
                    }
                }
            }
        }
    }

    // ── LÓGICA DEL JUEGO ─────────────────────────────────────────────────────

    private void generarNuevaSopa() {
        // Limpiar estado
        posiciones.clear();
        resueltas.clear();
        modelEncontradas.clear();
        colorIndex = 0;
        selInicio  = null;
        selLinea.clear();
        areaTiempos.setText("");

        for (int i = 0; i < GRID_SIZE; i++)
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j]       = 0;
                cellColors[i][j] = null;
            }

        tiempoInicioTotal = System.currentTimeMillis();
        Random rand = new Random();

        StringBuilder log = new StringBuilder("=== INSERCIÓN DE PALABRAS ===\n");
        for (String p : palabras) {
            long t0  = System.currentTimeMillis();
            boolean ok = insertarPalabra(p, rand);
            long dt  = System.currentTimeMillis() - t0;
            log.append(String.format("%-16s %s  %d ms%n", p, ok ? "OK" : "NO COLOCADA!", dt));
        }

        // Rellenar huecos con letras aleatorias
        for (int i = 0; i < GRID_SIZE; i++)
            for (int j = 0; j < GRID_SIZE; j++)
                if (grid[i][j] == 0)
                    grid[i][j] = (char) ('A' + rand.nextInt(26));

        long total = System.currentTimeMillis() - tiempoInicioTotal;
        log.append(String.format("%n  Tiempo total generación: %d ms%n", total));
        areaTiempos.setText(log.toString());

        labelTiempo.setText("⏱  Generación: " + total + " ms");
        labelEstado.setText("¡Sopa lista!  " + palabras.size() + " palabras escondidas.  ¡Encuéntralas!");

        gridPanel.repaint();
    }

    /** Inserta una palabra en la cuadrícula; devuelve true si lo logró. */
    private boolean insertarPalabra(String palabra, Random rand) {
        int[][] dirs = {{0,1},{1,0},{-1,1},{1,1},{-1,-1},{1,-1},{0,-1},{-1,0}};

        for (int intento = 0; intento < 600; intento++) {
            int d  = rand.nextInt(8);
            int dr = dirs[d][0], dc = dirs[d][1];
            int r  = rand.nextInt(GRID_SIZE), c = rand.nextInt(GRID_SIZE);
            int rF = r + dr * (palabra.length() - 1);
            int cF = c + dc * (palabra.length() - 1);

            if (rF < 0 || rF >= GRID_SIZE || cF < 0 || cF >= GRID_SIZE) continue;

            boolean ok = true;
            for (int i = 0; i < palabra.length(); i++) {
                char ex = grid[r + dr * i][c + dc * i];
                if (ex != 0 && ex != palabra.charAt(i)) { ok = false; break; }
            }

            if (ok) {
                for (int i = 0; i < palabra.length(); i++)
                    grid[r + dr * i][c + dc * i] = palabra.charAt(i);
                posiciones.put(palabra, new int[]{r, c, dr, dc});
                return true;
            }
        }
        return false;
    }

    /** Resalta una palabra con el color indicado y actualiza los paneles. */
    private void resaltarPalabra(String palabra, Color color) {
        int[] pos = posiciones.get(palabra);
        if (pos == null) return;

        tiempoInicioPalabra = System.currentTimeMillis();

        int r = pos[0], c = pos[1], dr = pos[2], dc = pos[3];
        for (int i = 0; i < palabra.length(); i++)
            cellColors[r + dr * i][c + dc * i] = color;

        long dt = System.currentTimeMillis() - tiempoInicioPalabra;
        resueltas.add(palabra);
        modelEncontradas.addElement("✓  " + palabra + "   (" + dt + " ms)");

        areaTiempos.append(String.format("Búsqueda  %-14s  %d ms%n", palabra, dt));
        areaTiempos.setCaretPosition(areaTiempos.getDocument().getLength());

        gridPanel.repaint();
    }

    /** Busca y resalta la siguiente palabra pendiente. */
    private void resolverSiguiente() {
        if (posiciones.isEmpty()) {
            labelEstado.setText("Primero genera una sopa de letras.");
            return;
        }
        List<String> pendientes = getPendientes();
        if (pendientes.isEmpty()) {
            labelEstado.setText("¡Ya encontraste todas las palabras!  :)");
            return;
        }

        String sig = pendientes.get(0);
        Color color = WORD_COLORS[colorIndex % WORD_COLORS.length];
        colorIndex++;

        long t0 = System.currentTimeMillis();
        resaltarPalabra(sig, color);
        long dt = System.currentTimeMillis() - t0;

        labelTiempo.setText("⏱  Última búsq.: " + dt + " ms");
        labelEstado.setText("Encontrada: " + sig + "   —   Quedan " + (pendientes.size() - 1));
    }

    /** Resuelve todas las palabras pendientes de forma animada. */
    private void resolverTodo() {
        if (posiciones.isEmpty()) {
            labelEstado.setText("Primero genera una sopa de letras.");
            return;
        }
        List<String> pendientes = getPendientes();
        if (pendientes.isEmpty()) {
            labelEstado.setText("¡Ya están todas resueltas!");
            return;
        }

        tiempoInicioTotal = System.currentTimeMillis();
        setBotonesHabilitados(false);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (String p : pendientes) {
                    Thread.sleep(380);
                    publish(p);
                }
                return null;
            }
            @Override
            protected void process(List<String> chunks) {
                for (String p : chunks) {
                    Color col = WORD_COLORS[colorIndex % WORD_COLORS.length];
                    colorIndex++;
                    resaltarPalabra(p, col);
                    labelEstado.setText("Buscando:  " + p + "  …");
                }
            }
            @Override
            protected void done() {
                long total = System.currentTimeMillis() - tiempoInicioTotal;
                labelTiempo.setText("⏱  Total resolución: " + total + " ms");
                labelEstado.setText("¡Todas las palabras encontradas!   Tiempo total: " + total + " ms");
                areaTiempos.append(String.format("%n══ TOTAL RESOLUCIÓN:  %d ms ══%n", total));
                areaTiempos.setCaretPosition(areaTiempos.getDocument().getLength());
                setBotonesHabilitados(true);
            }
        };
        worker.execute();
    }

    private void limpiarColores() {
        for (int i = 0; i < GRID_SIZE; i++)
            for (int j = 0; j < GRID_SIZE; j++)
                cellColors[i][j] = null;
        resueltas.clear();
        modelEncontradas.clear();
        colorIndex = 0;
        selInicio  = null;
        selLinea.clear();
        labelEstado.setText("Colores limpiados.  ¡Vuelve a intentarlo!");
        gridPanel.repaint();
    }

    private List<String> getPendientes() {
        List<String> list = new ArrayList<>();
        for (String p : palabras)
            if (!resueltas.contains(p)) list.add(p);
        return list;
    }

    private void setBotonesHabilitados(boolean en) {
        btnNueva.setEnabled(en);
        btnSiguiente.setEnabled(en);
        btnTodo.setEnabled(en);
        btnLimpiar.setEnabled(en);
    }

    // ── SELECCIÓN MANUAL CON EL MOUSE ────────────────────────────────────────

    /**
     * Devuelve la lista de celdas en línea recta entre inicio y fin.
     * Solo acepta horizontal, vertical o diagonal exacta (45°).
     */
    private List<int[]> lineaCeldas(int[] ini, int[] fin) {
        List<int[]> cells = new ArrayList<>();
        int lenR = Math.abs(fin[0] - ini[0]);
        int lenC = Math.abs(fin[1] - ini[1]);
        if (lenR != 0 && lenC != 0 && lenR != lenC) return cells; // no es línea válida

        int dr = Integer.signum(fin[0] - ini[0]);
        int dc = Integer.signum(fin[1] - ini[1]);
        int pasos = Math.max(lenR, lenC);

        for (int i = 0; i <= pasos; i++)
            cells.add(new int[]{ini[0] + dr * i, ini[1] + dc * i});
        return cells;
    }

    private void intentarSeleccion(int[] ini, int[] fin) {
        List<int[]> linea = lineaCeldas(ini, fin);
        if (linea.size() < 2) {
            labelEstado.setText("Línea inválida. Usa horizontal, vertical o diagonal a 45°.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int[] cell : linea) sb.append(grid[cell[0]][cell[1]]);
        String directa  = sb.toString();
        String inversa  = sb.reverse().toString();

        String hallada = null;
        for (String p : palabras) {
            if (p.equals(directa) || p.equals(inversa)) { hallada = p; break; }
        }

        if (hallada == null) {
            labelEstado.setText("«" + directa + "»  no es ninguna palabra. Inténtalo de nuevo.");
        } else if (resueltas.contains(hallada)) {
            labelEstado.setText("«" + hallada + "»  ya fue encontrada anteriormente.");
        } else {
            resaltarPalabra(hallada, COLOR_MANUAL_OK);
            labelEstado.setText("¡Encontraste  «" + hallada + "»!  Excelente ojo.");
            labelTiempo.setText("⏱  Manual: " + hallada);
        }

        selLinea.clear();
        gridPanel.repaint();
    }

    // ── CARGA DE DATOS ───────────────────────────────────────────────────────

    private void cargarPalabras() {
        palabras.clear();
        try {
            InputStream is = App.class.getResourceAsStream("/palabras.txt");
            if (is == null) is = App.class.getResourceAsStream("palabras.txt");
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linea;
                while ((linea = br.readLine()) != null) {
                    String p = linea.trim().toUpperCase();
                    if (!p.isEmpty()) palabras.add(p);
                }
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarAreaPalabras() {
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) sb.append(p).append("\n");
        areaPalabras.setText(sb.toString());
    }
}
