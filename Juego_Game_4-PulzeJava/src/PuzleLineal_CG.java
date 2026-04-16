import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * 4-Puzzle Lineal — interfaz gráfica con BFS y DFS
 * Archivo único auto-contenido (no requiere clases externas).
 */
public class PuzleLineal_CG extends JFrame {

    // ── Paleta oscura ──────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(10,  15,  30);
    private static final Color BG_PANEL   = new Color(18,  28,  48);
    private static final Color BG_CARD    = new Color(24,  36,  58);
    private static final Color TILE_BLUE_T= new Color(59, 130, 246);
    private static final Color TILE_BLUE_B= new Color(29,  78, 216);
    private static final Color TILE_GRN_T = new Color(16, 185, 129);
    private static final Color TILE_GRN_B = new Color(5,  150, 105);
    private static final Color TILE_AMB_T = new Color(245,158, 11);
    private static final Color TILE_AMB_B = new Color(180,110,  0);
    private static final Color SUCCESS    = new Color(16, 185, 129);
    private static final Color DANGER     = new Color(239, 68,  68);
    private static final Color TEXT_PRI   = new Color(226,232,240);
    private static final Color TEXT_SEC   = new Color(148,163,184);
    private static final Color BORDER_COL = new Color(40,  55,  82);
    private static final Color BTN_PRI    = new Color(59, 130, 246);
    private static final Color BTN_PUR    = new Color(124, 58, 237);
    private static final Color BTN_SEC    = new Color(30,  45,  72);

    // ── Estado del puzzle ──────────────────────────────────────────────────────
    private int[] estadoActual  = {3, 1, 4, 2};
    private int[] estadoInicial = {3, 1, 4, 2};
    private int   movimientos   = 0;

    // ── Widgets ────────────────────────────────────────────────────────────────
    private JPanel   panelTiles;
    private JTextArea txtLog;
    private JLabel   lblMov, lblEstado, lblNodos, lblTiempo;
    private JButton  btnPausar;
    private JSlider  sldVelocidad;

    // ── Animación ──────────────────────────────────────────────────────────────
    private java.util.List<int[]> animPasos;
    private int   animIdx   = 0;
    private boolean animando = false;
    private javax.swing.Timer animTimer;

    // ══════════════════════════════════════════════════════════════════════════
    public PuzleLineal_CG() {
        setTitle("4-Puzzle Lineal — BFS & DFS");
        setSize(1120, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 540));
        getContentPane().setBackground(BG_DARK);
        construirUI();
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(crearHeader(),  BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout(14, 0));
        main.setBackground(BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        main.add(crearIzquierdo(), BorderLayout.WEST);
        main.add(crearDerecho(),   BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        add(crearStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(23,37,84), getWidth(), 0, BG_PANEL));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(0, 78));
        h.setBorder(BorderFactory.createEmptyBorder(13, 24, 13, 24));

        JLabel t  = lbl("4-Puzzle Lineal", Color.WHITE, 26, Font.BOLD);
        JLabel st = lbl("Búsquedas No Informadas: BFS (Amplitud) y DFS (Profundidad)", new Color(147,197,253), 13, Font.PLAIN);
        JPanel tl = new JPanel(); tl.setLayout(new BoxLayout(tl, BoxLayout.Y_AXIS));
        tl.setOpaque(false); tl.add(t); tl.add(st);

        JLabel icon = lbl("[ 1 ][ 2 ][ 3 ][ 4 ]", new Color(99,102,241), 18, Font.BOLD);
        h.add(tl, BorderLayout.WEST); h.add(icon, BorderLayout.EAST);
        return h;
    }

    /** Columna izquierda fija (430 px) */
    private JPanel crearIzquierdo() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(BG_DARK);
        col.setPreferredSize(new Dimension(435, 0));

        // ── Labels de estadísticas (deben crearse ANTES de actualizarTiles) ──
        lblMov    = statLabel("Movimientos: 0");
        lblEstado = statLabel("Estado: Inicial");
        lblNodos  = statLabel("Nodos:  —");
        lblTiempo = statLabel("Tiempo: —");

        // ── Visualizador ──────────────────────────────────────────────────
        col.add(tituloSeccion("Estado del Puzzle"));
        panelTiles = new JPanel(new GridLayout(1, 4, 10, 0));
        panelTiles.setBackground(BG_CARD);
        panelTiles.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelTiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        actualizarTiles(estadoActual, TileMode.NORMAL);
        col.add(envuelve(panelTiles));
        col.add(Box.createVerticalStrut(10));

        // ── Stats ─────────────────────────────────────────────────────────
        col.add(tituloSeccion("Estadísticas"));
        JPanel stats = new JPanel(new GridLayout(2, 2, 8, 6));
        stats.setOpaque(false);
        stats.add(lblMov); stats.add(lblEstado); stats.add(lblNodos); stats.add(lblTiempo);
        col.add(envuelve(stats));
        col.add(Box.createVerticalStrut(10));

        // ── Operadores manuales ───────────────────────────────────────────
        col.add(tituloSeccion("Operadores Manuales"));
        JPanel ops = new JPanel(new GridLayout(1, 3, 7, 0)); ops.setOpaque(false);
        ops.add(btn("⇄  L (1↔2)", BTN_SEC, TEXT_PRI, e -> aplicarOp(0)));
        ops.add(btn("⇄  C (2↔3)", BTN_SEC, TEXT_PRI, e -> aplicarOp(1)));
        ops.add(btn("⇄  R (3↔4)", BTN_SEC, TEXT_PRI, e -> aplicarOp(2)));
        col.add(envuelve(ops));
        col.add(Box.createVerticalStrut(10));

        // ── Configuración ─────────────────────────────────────────────────
        col.add(tituloSeccion("Configuración"));
        JPanel cfg = new JPanel(new GridLayout(1, 3, 7, 0)); cfg.setOpaque(false);
        cfg.add(btn("⌨ Ingresar",  BTN_SEC, TEXT_PRI, e -> generarConfiguracion()));
        cfg.add(btn("⚄ Aleatorio", BTN_SEC, TEXT_PRI, e -> generarAleatorio()));
        cfg.add(btn("↺ Reiniciar", BTN_SEC, TEXT_PRI, e -> reiniciar()));
        col.add(envuelve(cfg));
        col.add(Box.createVerticalStrut(10));

        // ── Resolver ──────────────────────────────────────────────────────
        col.add(tituloSeccion("Resolver Automáticamente"));
        JPanel res = new JPanel(new GridLayout(1, 2, 8, 0)); res.setOpaque(false);
        res.add(btn("▶  BFS  (Amplitud)",   BTN_PRI, Color.WHITE, e -> resolver("BFS")));
        res.add(btn("▶  DFS  (Profundidad)", BTN_PUR, Color.WHITE, e -> resolver("DFS")));
        col.add(envuelve(res));
        col.add(Box.createVerticalStrut(10));

        // ── Animación ─────────────────────────────────────────────────────
        col.add(tituloSeccion("Control de Animación"));
        JPanel anim = new JPanel(new BorderLayout(0, 8)); anim.setOpaque(false);
        JPanel sldRow = new JPanel(new BorderLayout(6, 0)); sldRow.setOpaque(false);
        JLabel lblV = lbl("Velocidad:", TEXT_SEC, 12, Font.PLAIN);
        sldVelocidad = new JSlider(150, 2000, 900); sldVelocidad.setInverted(true);
        sldVelocidad.setBackground(BG_CARD); sldVelocidad.setForeground(TEXT_PRI);
        sldRow.add(lblV, BorderLayout.WEST); sldRow.add(sldVelocidad, BorderLayout.CENTER);
        btnPausar = btn("⏸ Pausar", BTN_SEC, TEXT_PRI, e -> toggleAnim());
        btnPausar.setEnabled(false);
        anim.add(sldRow, BorderLayout.NORTH); anim.add(btnPausar, BorderLayout.SOUTH);
        col.add(envuelve(anim));

        return col;
    }

    private JPanel crearDerecho() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        topBar.add(lbl("  Registro de Ejecución", TEXT_SEC, 11, Font.BOLD), BorderLayout.WEST);
        JButton limLog = new JButton("Limpiar");
        limLog.setFont(new Font("Segoe UI", Font.PLAIN, 11)); limLog.setForeground(TEXT_SEC);
        limLog.setBackground(BTN_SEC); limLog.setFocusPainted(false); limLog.setBorderPainted(false);
        limLog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        limLog.addActionListener(e -> txtLog.setText(""));
        topBar.add(limLog, BorderLayout.EAST);

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(7, 11, 22));
        txtLog.setForeground(TEXT_PRI);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLog.setCaretColor(Color.WHITE);
        JScrollPane sc = new JScrollPane(txtLog);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        sc.getViewport().setBackground(new Color(7, 11, 22));

        p.add(topBar, BorderLayout.NORTH);
        p.add(sc,     BorderLayout.CENTER);
        return p;
    }

    private JPanel crearStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(12, 18, 35));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        bar.setPreferredSize(new Dimension(0, 26));
        bar.add(lbl("  4-Puzzle Lineal — BFS & DFS  |  Inteligencia Artificial", TEXT_SEC, 11, Font.PLAIN), BorderLayout.WEST);
        bar.add(lbl("v2.0  ", new Color(60, 80, 110), 11, Font.PLAIN), BorderLayout.EAST);
        return bar;
    }

    // ── Helpers de layout ─────────────────────────────────────────────────────

    private enum TileMode { NORMAL, ANIMANDO, RESUELTO }

    private JLabel tituloSeccion(String t) {
        JLabel l = lbl("  " + t, TEXT_SEC, 11, Font.BOLD);
        l.setOpaque(true); l.setBackground(BG_PANEL);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));
        return l;
    }

    private JPanel envuelve(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 36));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JLabel statLabel(String t) {
        JLabel l = lbl(t, TEXT_PRI, 12, Font.PLAIN);
        l.setOpaque(true); l.setBackground(BG_PANEL);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return l;
    }

    private JLabel lbl(String t, Color c, int sz, int st) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", st, sz)); l.setForeground(c); return l;
    }

    private JButton btn(String t, Color bg, Color fg, ActionListener al) {
        final Color bgC = bg, fgC = fg;
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bgC.darker()
                        : getModel().isRollover() ? bgC.brighter() : bgC;
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(fgC); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(120, 34));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al); return b;
    }

    // ── Tiles ─────────────────────────────────────────────────────────────────

    private void actualizarTiles(int[] estado, TileMode modo) {
        panelTiles.removeAll();
        boolean resuelto = estaResuelto(estado);
        for (int i = 0; i < estado.length; i++) {
            final int val = estado[i];
            final Color top, bot;
            if (resuelto)              { top = TILE_GRN_T; bot = TILE_GRN_B; }
            else if (modo == TileMode.ANIMANDO) { top = TILE_AMB_T; bot = TILE_AMB_B; }
            else                       { top = TILE_BLUE_T; bot = TILE_BLUE_B; }
            JPanel tile = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
                    g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, 14, 14);
                    g2.setColor(new Color(255,255,255,35));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(3, 3, getWidth()-6, getHeight()-6, 14, 14);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    String s = String.valueOf(val);
                    g2.drawString(s, (getWidth() - fm.stringWidth(s))/2,
                        (getHeight() + fm.getAscent() - fm.getDescent())/2);
                    g2.dispose();
                }
            };
            tile.setOpaque(false);
            tile.setPreferredSize(new Dimension(85, 85));
            panelTiles.add(tile);
        }
        panelTiles.revalidate();
        panelTiles.repaint();

        if (resuelto) { lblEstado.setText("Estado: ✓ RESUELTO"); lblEstado.setForeground(SUCCESS); }
        else          { lblEstado.setText("Estado: En progreso"); lblEstado.setForeground(TEXT_SEC); }
    }

    // ── Operadores ────────────────────────────────────────────────────────────

    private void aplicarOp(int op) {
        int[] s = estadoActual.clone();
        intercambiar(s, op);
        estadoActual = s;
        movimientos++;
        lblMov.setText("Movimientos: " + movimientos);
        actualizarTiles(estadoActual, TileMode.NORMAL);
        String[] n = {"Izquierdo (1↔2)", "Central (2↔3)", "Derecho (3↔4)"};
        log("Mov #" + movimientos + " [" + n[op] + "]: " + Arrays.toString(estadoActual));
        if (estaResuelto(estadoActual)) log("✓ ¡RESUELTO en " + movimientos + " movimientos!");
    }

    private void intercambiar(int[] s, int op) {
        int i = op, j = op + 1;
        int t = s[i]; s[i] = s[j]; s[j] = t;
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    private void generarConfiguracion() {
        String in = JOptionPane.showInputDialog(this,
            "Ingrese 4 números separados por comas\n(ej: 4,3,1,2):", "Configurar", JOptionPane.QUESTION_MESSAGE);
        if (in == null || in.trim().isEmpty()) return;
        try {
            String[] p = in.split(",");
            if (p.length != 4) throw new IllegalArgumentException("Necesita exactamente 4 números");
            estadoActual  = new int[4];
            estadoInicial = new int[4];
            for (int i = 0; i < 4; i++) {
                estadoActual[i] = Integer.parseInt(p[i].trim());
                estadoInicial[i] = estadoActual[i];
            }
            resetContadores(); actualizarTiles(estadoActual, TileMode.NORMAL);
            log("Configuración manual: " + Arrays.toString(estadoActual));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarAleatorio() {
        ArrayList<Integer> nums = new ArrayList<>(Arrays.asList(1,2,3,4));
        Collections.shuffle(nums);
        estadoActual  = new int[]{nums.get(0), nums.get(1), nums.get(2), nums.get(3)};
        estadoInicial = estadoActual.clone();
        resetContadores(); actualizarTiles(estadoActual, TileMode.NORMAL);
        log("Configuración aleatoria: " + Arrays.toString(estadoActual));
    }

    private void reiniciar() {
        if (animTimer != null) animTimer.stop();
        animando = false; btnPausar.setEnabled(false);
        estadoActual = estadoInicial.clone();
        resetContadores(); actualizarTiles(estadoActual, TileMode.NORMAL);
        lblNodos.setText("Nodos:  —"); lblTiempo.setText("Tiempo: —");
        log("↺ Reiniciado al estado inicial: " + Arrays.toString(estadoActual));
    }

    private void resetContadores() {
        movimientos = 0; lblMov.setText("Movimientos: 0");
    }

    // ── Algoritmos de búsqueda ────────────────────────────────────────────────

    private static class Nodo {
        final int[] estado;
        final Nodo  padre;
        final int   profundidad;
        Nodo(int[] e, Nodo p, int d) { estado = e.clone(); padre = p; profundidad = d; }
    }

    private LinkedList<int[]> reconstruir(Nodo nodo) {
        LinkedList<int[]> c = new LinkedList<>();
        for (Nodo n = nodo; n != null; n = n.padre) c.addFirst(n.estado);
        return c;
    }

    private int[] aplicarOpA(int[] est, int op) {
        int[] s = est.clone(); intercambiar(s, op); return s;
    }

    private boolean estaResuelto(int[] s) {
        for (int i = 0; i < s.length - 1; i++) if (s[i] > s[i+1]) return false; return true;
    }

    /** BFS — Búsqueda por Amplitud (garantiza camino más corto) */
    private LinkedList<int[]> bfs(int[] ini, int[] nodosRef) {
        LinkedList<Nodo> cola = new LinkedList<>();
        Set<String> vis = new HashSet<>();
        cola.add(new Nodo(ini, null, 0));
        vis.add(Arrays.toString(ini));
        nodosRef[0] = 0;
        while (!cola.isEmpty()) {
            Nodo cur = cola.poll(); nodosRef[0]++;
            if (estaResuelto(cur.estado)) return reconstruir(cur);
            for (int op = 0; op < 3; op++) {
                int[] sig = aplicarOpA(cur.estado, op);
                String k = Arrays.toString(sig);
                if (!vis.contains(k)) { vis.add(k); cola.add(new Nodo(sig, cur, cur.profundidad+1)); }
            }
        }
        return null;
    }

    /** DFS — Búsqueda por Profundidad (con límite para evitar ciclos) */
    private LinkedList<int[]> dfs(int[] ini, int[] nodosRef) {
        final int LIMITE = 24;
        Stack<Nodo> pila = new Stack<>();
        Set<String>  vis  = new HashSet<>();
        pila.push(new Nodo(ini, null, 0));
        vis.add(Arrays.toString(ini));
        nodosRef[0] = 0;
        while (!pila.isEmpty()) {
            Nodo cur = pila.pop(); nodosRef[0]++;
            if (estaResuelto(cur.estado)) return reconstruir(cur);
            if (cur.profundidad < LIMITE) {
                for (int op = 2; op >= 0; op--) {   // inverso para explorar izquierda primero
                    int[] sig = aplicarOpA(cur.estado, op);
                    String k = Arrays.toString(sig);
                    if (!vis.contains(k)) { vis.add(k); pila.push(new Nodo(sig, cur, cur.profundidad+1)); }
                }
            }
        }
        return null;
    }

    // ── Controlador de resolución ─────────────────────────────────────────────

    private void resolver(String alg) {
        if (animTimer != null) animTimer.stop();
        animando = false;
        log("\n══════  " + alg + "  ══════");
        log("Estado inicial: " + Arrays.toString(estadoActual));

        int[] nRef = {0};
        long t0 = System.currentTimeMillis();
        LinkedList<int[]> sol = "BFS".equals(alg) ? bfs(estadoActual, nRef) : dfs(estadoActual, nRef);
        long ms = System.currentTimeMillis() - t0;

        lblNodos.setText("Nodos: " + nRef[0]);
        lblTiempo.setText("Tiempo: " + ms + " ms");

        if (sol != null) {
            log("✓ Solución: " + (sol.size()-1) + " movs  |  Nodos: " + nRef[0] + "  |  Tiempo: " + ms + " ms");
            log("Camino:");
            int step = 0;
            for (int[] e : sol) log("  [" + step++ + "] " + Arrays.toString(e));
            animPasos = new ArrayList<>(sol);
            iniciarAnimacion();
        } else {
            log("✗ No se encontró solución");
        }
    }

    // ── Animación ─────────────────────────────────────────────────────────────

    private void iniciarAnimacion() {
        if (animPasos == null || animPasos.isEmpty()) return;
        animIdx = 0; animando = true;
        btnPausar.setText("⏸ Pausar"); btnPausar.setEnabled(true);
        animTimer = new javax.swing.Timer(sldVelocidad.getValue(), e -> {
            if (animIdx < animPasos.size()) {
                estadoActual = animPasos.get(animIdx).clone();
                TileMode m = (animIdx > 0 && animIdx < animPasos.size()-1) ? TileMode.ANIMANDO : TileMode.NORMAL;
                actualizarTiles(estadoActual, m);
                lblMov.setText("Movimientos: " + animIdx);
                animIdx++;
            } else {
                ((javax.swing.Timer)e.getSource()).stop(); animando = false;
                btnPausar.setEnabled(false);
                actualizarTiles(estadoActual, TileMode.NORMAL);
                log("✓ Animación completada.");
            }
        });
        animTimer.start();
    }

    private void toggleAnim() {
        if (animTimer == null) return;
        if (animando) { animTimer.stop(); animando = false; btnPausar.setText("▶ Continuar"); }
        else          { animTimer.setDelay(sldVelocidad.getValue()); animTimer.start(); animando = true; btnPausar.setText("⏸ Pausar"); }
    }

    // ── Log ───────────────────────────────────────────────────────────────────

    private void log(String msg) {
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PuzleLineal_CG().setVisible(true));
    }
}
