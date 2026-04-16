import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;

// ════════════════════════════════════════════════════════════════════════════
//  8-Puzzle  ·  A* Heuristic Search  ·  Interfaz renovada
// ════════════════════════════════════════════════════════════════════════════
public class PuzzleGUI extends JFrame {

    // ── Paleta de colores ───────────────────────────────────────────────────
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

    static final Color[] TILE_CLR = {
        new Color(28, 38, 60),    // 0 – vacío
        new Color(220,  38,  38), // 1 – rojo
        new Color(234,  88,  12), // 2 – naranja
        new Color(180, 130,   4), // 3 – ámbar
        new Color( 22, 163,  74), // 4 – verde
        new Color(  8, 145, 178), // 5 – cian
        new Color( 79,  70, 229), // 6 – índigo
        new Color(147,  51, 234), // 7 – violeta
        new Color(219,  39, 119), // 8 – rosa
    };

    // ── Estado ─────────────────────────────────────────────────────────────
    private int[] board = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    private LinkedList<int[]> solSteps  = new LinkedList<>();
    private int animIdx   = 0;
    private boolean animOn = false;

    // ── Componentes UI ─────────────────────────────────────────────────────
    private PuzzleBoard puzzleBoard;
    private JComboBox<String> hCombo;
    private JTextArea logArea;
    private JLabel stepLbl, nodesLbl, timeLbl, titleStep;
    private JButton solveBtn, shuffleBtn, resetBtn, compareBtn;
    private JButton playBtn, prevBtn, nextBtn;
    private JSlider speedSlider;
    private JProgressBar progBar;
    private javax.swing.Timer animTimer;

    // ═══════════════════════════════════════════════════════════════════════
    public PuzzleGUI() {
        setTitle("8-Puzzle · A* Heuristic Search");
        setSize(940, 680);
        setMinimumSize(new Dimension(880, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_BG);

        JPanel root = new JPanel(new BorderLayout(14, 12));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(16, 18, 14, 18));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        setContentPane(root);

        animTimer = new javax.swing.Timer(350, e -> tickAnim());
        updateBoard();
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JLabel t1 = label("8-PUZZLE", 26, Font.BOLD, C_TEXT);
        JLabel t2 = label("Búsqueda informada con A* · Tres heurísticas", 13, Font.PLAIN, C_MUTED);

        titleStep = label("", 13, Font.PLAIN, C_ACCENT);

        JPanel left = vbox(C_BG, t1, t2);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(C_BG);
        wrap.add(left,      BorderLayout.WEST);
        wrap.add(titleStep, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);

        JPanel p = vbox(C_BG, wrap, Box.createRigidArea(new Dimension(0, 8)), sep);
        return p;
    }

    // ── Centro ─────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(C_BG);

        // Izquierda: tablero + controles animación
        JPanel leftCol = new JPanel(new BorderLayout(0, 10));
        leftCol.setBackground(C_BG);
        puzzleBoard = new PuzzleBoard();
        leftCol.add(puzzleBoard,      BorderLayout.CENTER);
        leftCol.add(buildAnimBar(),   BorderLayout.SOUTH);

        // Derecha: controles + log
        JPanel rightCol = new JPanel(new BorderLayout(0, 10));
        rightCol.setBackground(C_BG);
        rightCol.setPreferredSize(new Dimension(315, 0));
        rightCol.add(buildControls(), BorderLayout.NORTH);
        rightCol.add(buildLog(),      BorderLayout.CENTER);

        p.add(leftCol,  BorderLayout.CENTER);
        p.add(rightCol, BorderLayout.EAST);
        return p;
    }

    // ── Barra de animación ─────────────────────────────────────────────────
    private JPanel buildAnimBar() {
        JPanel p = card();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));

        prevBtn  = iconBtn("⏮");
        playBtn  = iconBtn("▶");
        nextBtn  = iconBtn("⏭");

        prevBtn.addActionListener(e -> prevStep());
        playBtn.addActionListener(e -> toggleAnim());
        nextBtn.addActionListener(e -> nextStep());

        stepLbl = label("—", 12, Font.PLAIN, C_MUTED);

        p.add(prevBtn); p.add(playBtn); p.add(nextBtn);
        p.add(Box.createHorizontalStrut(12));
        p.add(stepLbl);

        setAnimBtnsEnabled(false);
        return p;
    }

    // ── Panel de controles derecho ──────────────────────────────────────────
    private JPanel buildControls() {
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        addSection(p, "HEURÍSTICA");
        hCombo = new JComboBox<>(new String[]{
            "h₁  —  Piezas mal colocadas",
            "h₂  —  Distancia Manhattan",
            "h₃  —  Combinada  h₂ + 3·S(n)"});
        styleCombo(hCombo);
        p.add(hCombo);
        p.add(gap(10));

        addSection(p, "VELOCIDAD DE ANIMACIÓN");
        JPanel sliderRow = new JPanel(new BorderLayout(4, 0));
        sliderRow.setBackground(C_CARD);
        speedSlider = new JSlider(60, 900, 350);
        speedSlider.setBackground(C_CARD);
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(0, 26));
        speedSlider.addChangeListener(e -> { if (animTimer != null) animTimer.setDelay(speedSlider.getValue()); });
        sliderRow.add(label("⚡", 11, Font.PLAIN, C_MUTED), BorderLayout.WEST);
        sliderRow.add(speedSlider, BorderLayout.CENTER);
        sliderRow.add(label("🐢", 11, Font.PLAIN, C_MUTED), BorderLayout.EAST);
        sliderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        sliderRow.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sliderRow);
        p.add(gap(14));

        addSection(p, "ACCIONES");

        solveBtn   = bigBtn("▶  Resolver",             C_ACCENT);
        shuffleBtn = bigBtn("⟳  Mezclar puzzle",        C_WARN);
        resetBtn   = bigBtn("↺  Reiniciar",             C_MUTED);
        compareBtn = bigBtn("≡  Comparar heurísticas",  C_SUCCESS);

        solveBtn  .addActionListener(e -> solve());
        shuffleBtn.addActionListener(e -> shuffle());
        resetBtn  .addActionListener(e -> reset());
        compareBtn.addActionListener(e -> compare());

        for (JButton b : new JButton[]{solveBtn, shuffleBtn, resetBtn, compareBtn}) {
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            p.add(b);
            p.add(gap(5));
        }

        // Mini stats row
        p.add(gap(8));
        nodesLbl = label("Nodos: —", 11, Font.PLAIN, C_MUTED);
        timeLbl  = label("Tiempo: —", 11, Font.PLAIN, C_MUTED);
        nodesLbl.setAlignmentX(LEFT_ALIGNMENT);
        timeLbl .setAlignmentX(LEFT_ALIGNMENT);
        p.add(nodesLbl);
        p.add(gap(3));
        p.add(timeLbl);

        return p;
    }

    // ── Panel de log ───────────────────────────────────────────────────────
    private JPanel buildLog() {
        JPanel p = card();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel t = label("LOG DE SOLUCIÓN", 10, Font.BOLD, C_MUTED);
        t.setBorder(new EmptyBorder(0, 0, 6, 0));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 14, 22));
        logArea.setForeground(C_TEXT);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setMargin(new Insets(6, 6, 6, 6));
        logArea.setCaretColor(C_ACCENT);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        scroll.setBackground(C_CARD);

        p.add(t,      BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Footer / barra de progreso ─────────────────────────────────────────
    private JPanel buildFooter() {
        progBar = new JProgressBar();
        progBar.setForeground(C_ACCENT);
        progBar.setBackground(C_CARD);
        progBar.setBorderPainted(false);
        progBar.setStringPainted(true);
        progBar.setString("Listo");
        progBar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        progBar.setPreferredSize(new Dimension(0, 20));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(4, 0, 0, 0));
        p.add(progBar);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Acciones
    // ═══════════════════════════════════════════════════════════════════════

    private void shuffle() {
        stopAnim();
        List<Integer> nums = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8));
        do { Collections.shuffle(nums); } while (!solvable(nums));
        for (int i = 0; i < 9; i++) board[i] = nums.get(i);
        solSteps.clear();
        updateBoard();
        resetStats();
        log("Puzzle mezclado. ¡Listo para resolver!");
    }

    private void reset() {
        stopAnim();
        board = new int[]{1,2,3,4,5,6,7,8,0};
        solSteps.clear();
        updateBoard();
        resetStats();
        log("Puzzle reiniciado al estado meta.");
    }

    private void solve() {
        if (Arrays.equals(board, new int[]{1,2,3,4,5,6,7,8,0})) {
            log("¡El puzzle ya está en el estado meta!"); return;
        }
        stopAnim();
        int h = hCombo.getSelectedIndex() + 1;
        solveBtn  .setEnabled(false);
        shuffleBtn.setEnabled(false);
        progBar.setIndeterminate(true);
        progBar.setString("Buscando solución con h" + h + "...");
        log("\n=== A* con heurística h" + h + " ===");

        int[] snap = Arrays.copyOf(board, 9);
        new Thread(() -> {
            long t0 = System.currentTimeMillis();
            PuzzleSolver solver = new PuzzleSolver(snap, h);
            LinkedList<int[]> sol = solver.solve();
            long ms = System.currentTimeMillis() - t0;

            SwingUtilities.invokeLater(() -> {
                progBar.setIndeterminate(false);
                solveBtn  .setEnabled(true);
                shuffleBtn.setEnabled(true);

                if (sol == null) {
                    log("No se encontró solución.");
                    progBar.setString("Sin solución");
                    return;
                }
                solSteps = sol;
                animIdx  = 0;
                int moves = sol.size() - 1;

                nodesLbl.setText("Nodos explorados: " + solver.getExploredNodes());
                timeLbl .setText(String.format("Tiempo: %.3f s", ms / 1000.0));
                progBar.setMaximum(moves);
                progBar.setValue(0);
                progBar.setString("Solución: " + moves + " movimientos");

                log("Movimientos: " + moves);
                log("Nodos explorados: " + solver.getExploredNodes());
                log(String.format("Tiempo de ejecución: %.3f s", ms / 1000.0));
                log("Reproduciendo animación...");

                setAnimBtnsEnabled(true);
                startAnim();
            });
        }).start();
    }

    private void compare() {
        if (Arrays.equals(board, new int[]{1,2,3,4,5,6,7,8,0})) {
            log("¡El puzzle ya está en el estado meta!"); return;
        }
        stopAnim();
        solveBtn  .setEnabled(false);
        compareBtn.setEnabled(false);
        progBar.setIndeterminate(true);
        progBar.setString("Comparando heurísticas...");
        log("\n╔══════════════════════════════════════╗");
        log("║    COMPARACIÓN DE HEURÍSTICAS       ║");
        log("╚══════════════════════════════════════╝");

        int[] snap = Arrays.copyOf(board, 9);
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(" %-6s %-8s %-12s %-9s%n", "Heur.", "Pasos", "Nodos", "Tiempo"));
            sb.append(" " + "─".repeat(38) + "\n");
            for (int h = 1; h <= 3; h++) {
                long t0 = System.currentTimeMillis();
                PuzzleSolver s = new PuzzleSolver(snap, h);
                LinkedList<int[]> sol = s.solve();
                long ms = System.currentTimeMillis() - t0;
                if (sol != null)
                    sb.append(String.format(" h%-5d %-8d %-12d %.3f s%n", h, sol.size()-1, s.getExploredNodes(), ms/1000.0));
                else
                    sb.append(String.format(" h%d     SIN SOLUCIÓN%n", h));
            }
            final String out = sb.toString();
            SwingUtilities.invokeLater(() -> {
                progBar.setIndeterminate(false);
                solveBtn  .setEnabled(true);
                compareBtn.setEnabled(true);
                progBar.setString("Comparación completa");
                log(out);
            });
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Animación
    // ═══════════════════════════════════════════════════════════════════════

    private void tickAnim() {
        if (animIdx >= solSteps.size()) { stopAnim(); return; }
        applyStep(animIdx++);
    }

    private void startAnim() {
        animTimer.setDelay(speedSlider.getValue());
        animTimer.start();
        animOn = true;
        playBtn.setText("⏸");
    }

    private void stopAnim() {
        animTimer.stop();
        animOn = false;
        playBtn.setText("▶");
    }

    private void toggleAnim() {
        if (solSteps.isEmpty()) return;
        if (animOn) stopAnim(); else startAnim();
    }

    private void prevStep() {
        if (solSteps.isEmpty()) return;
        stopAnim();
        if (animIdx > 1) applyStep(--animIdx - 1);
    }

    private void nextStep() {
        if (solSteps.isEmpty()) return;
        stopAnim();
        if (animIdx < solSteps.size()) applyStep(animIdx++);
    }

    private void applyStep(int idx) {
        if (idx < 0 || idx >= solSteps.size()) return;
        board = Arrays.copyOf(solSteps.get(idx), 9);
        updateBoard();
        int total = solSteps.size() - 1;
        stepLbl.setText("Paso " + Math.min(idx, total) + " / " + total);
        titleStep.setText("Paso " + Math.min(idx, total) + " / " + total);
        progBar.setValue(Math.min(idx, total));
        if (idx >= solSteps.size() - 1) {
            stopAnim();
            log("✓ ¡Solución completada!");
        }
    }

    private void setAnimBtnsEnabled(boolean b) {
        prevBtn.setEnabled(b);
        playBtn.setEnabled(b);
        nextBtn.setEnabled(b);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Interacción manual con el tablero
    // ═══════════════════════════════════════════════════════════════════════

    private void clickTile(int idx) {
        if (animOn) return;
        int empty = -1;
        for (int i = 0; i < 9; i++) if (board[i] == 0) { empty = i; break; }
        if (adjacent(idx, empty)) {
            board[empty] = board[idx];
            board[idx] = 0;
            updateBoard();
            if (Arrays.equals(board, new int[]{1,2,3,4,5,6,7,8,0})) {
                log("🎉 ¡Resolviste el puzzle manualmente!");
            }
        }
    }

    private boolean adjacent(int a, int b) {
        int rA=a/3, cA=a%3, rB=b/3, cB=b%3;
        return (Math.abs(rA-rB)==1 && cA==cB) || (Math.abs(cA-cB)==1 && rA==rB);
    }

    private boolean solvable(List<Integer> p) {
        int inv = 0;
        for (int i = 0; i < p.size(); i++)
            for (int j = i+1; j < p.size(); j++)
                if (p.get(i) != 0 && p.get(j) != 0 && p.get(i) > p.get(j)) inv++;
        return inv % 2 == 0;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helpers UI
    // ═══════════════════════════════════════════════════════════════════════

    private void updateBoard() { if (puzzleBoard != null) puzzleBoard.repaint(); }

    private void log(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void resetStats() {
        nodesLbl.setText("Nodos: —");
        timeLbl .setText("Tiempo: —");
        stepLbl .setText("—");
        titleStep.setText("");
        progBar.setValue(0);
        progBar.setString("Listo");
        setAnimBtnsEnabled(false);
        logArea.setText("");
    }

    private static JLabel label(String txt, int size, int style, Color c) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(c);
        return l;
    }

    private static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(C_CARD);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER));
        return p;
    }

    @SuppressWarnings("unchecked")
    private static void styleCombo(JComboBox cb) {
        cb.setBackground(C_PANEL);
        cb.setForeground(C_TEXT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(C_BORDER));
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

    private static JButton iconBtn(String icon) {
        JButton b = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled() ? C_BORDER
                         : getModel().isPressed() ? C_ACCENT.darker()
                         : getModel().isRollover() ? C_ACCENT
                         : C_PANEL;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(52, 34));
        b.setForeground(C_TEXT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void addSection(JPanel p, String title) {
        JLabel lbl = label(title, 10, Font.BOLD, C_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(lbl);
    }

    private static Component gap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private static JPanel vbox(Color bg, Object... items) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(bg);
        for (Object o : items) {
            if (o instanceof Component) p.add((Component) o);
        }
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Tablero personalizado (Java2D)
    // ═══════════════════════════════════════════════════════════════════════
    class PuzzleBoard extends JPanel {
        private static final int SZ  = 112;  // tamaño de cada tile
        private static final int GAP = 8;    // espacio entre tiles
        private static final int ARC = 18;   // radio de redondeo
        private static final int PAD = 12;   // padding del panel

        PuzzleBoard() {
            int side = PAD * 2 + SZ * 3 + GAP * 2;
            setPreferredSize(new Dimension(side, side));
            setBackground(C_BG);
            setOpaque(true);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int col = (e.getX() - PAD) / (SZ + GAP);
                    int row = (e.getY() - PAD) / (SZ + GAP);
                    if (col >= 0 && col < 3 && row >= 0 && row < 3)
                        clickTile(row * 3 + col);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);

            int[] goal = {1,2,3,4,5,6,7,8,0};

            for (int i = 0; i < 9; i++) {
                int row = i / 3, col = i % 3;
                int x = PAD + col * (SZ + GAP);
                int y = PAD + row * (SZ + GAP);
                int val = board[i];

                if (val == 0) {
                    // hueco vacío
                    g2.setColor(new Color(20, 28, 46));
                    g2.fill(new RoundRectangle2D.Float(x, y, SZ, SZ, ARC, ARC));
                    g2.setColor(C_BORDER);
                    float[] dash = {8f, 5f};
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
                    g2.draw(new RoundRectangle2D.Float(x+1, y+1, SZ-2, SZ-2, ARC, ARC));
                    g2.setStroke(new BasicStroke(1));
                } else {
                    Color base  = TILE_CLR[val];
                    boolean ok  = (val == goal[i]);

                    // sombra suave
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fill(new RoundRectangle2D.Float(x+3, y+5, SZ, SZ, ARC, ARC));

                    // cuerpo del tile
                    g2.setColor(base);
                    g2.fill(new RoundRectangle2D.Float(x, y, SZ, SZ, ARC, ARC));

                    // reflejo superior
                    GradientPaint gp = new GradientPaint(x, y, new Color(255,255,255,55),
                                                          x, y + SZ/2, new Color(255,255,255,0));
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(x, y, SZ, SZ/2 + ARC/2, ARC, ARC));
                    g2.setPaint(base);

                    // borde: verde si está en posición correcta, oscuro si no
                    if (ok) {
                        g2.setColor(C_SUCCESS);
                        g2.setStroke(new BasicStroke(2.5f));
                    } else {
                        g2.setColor(base.darker().darker());
                        g2.setStroke(new BasicStroke(1.5f));
                    }
                    g2.draw(new RoundRectangle2D.Float(x+1, y+1, SZ-2, SZ-2, ARC, ARC));
                    g2.setStroke(new BasicStroke(1));

                    // número
                    g2.setColor(Color.WHITE);
                    Font f = new Font("SansSerif", Font.BOLD, 42);
                    g2.setFont(f);
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = String.valueOf(val);
                    int tx = x + (SZ - fm.stringWidth(txt)) / 2;
                    int ty = y + (SZ - fm.getHeight()) / 2 + fm.getAscent();
                    // sombra texto
                    g2.setColor(new Color(0,0,0,80));
                    g2.drawString(txt, tx+2, ty+2);
                    g2.setColor(Color.WHITE);
                    g2.drawString(txt, tx, ty);

                    // pequeño indicador "OK"
                    if (ok) {
                        g2.setColor(C_SUCCESS);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                        g2.drawString("✓", x + SZ - 18, y + 16);
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new PuzzleGUI().setVisible(true));
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  Solver A*  (lógica sin cambios)
// ════════════════════════════════════════════════════════════════════════════
class PuzzleSolver {
    private final int[] initialState;
    private final int heuristicType;
    private int exploredNodes;

    public PuzzleSolver(int[] initialState, int heuristicType) {
        this.initialState = Arrays.copyOf(initialState, initialState.length);
        this.heuristicType = heuristicType;
        this.exploredNodes = 0;
    }

    public LinkedList<int[]> solve() {
        PriorityQueue<Node> openSet  = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        Set<String>         closedSet = new HashSet<>();

        Node startNode = new Node(initialState, null, 0, heuristicType);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            exploredNodes++;

            if (current.isGoal()) return reconstructPath(current);

            closedSet.add(Arrays.toString(current.getState()));

            for (Node neighbor : current.getNeighbors()) {
                String ns = Arrays.toString(neighbor.getState());
                if (!closedSet.contains(ns)) {
                    Node existing = findInOpenSet(openSet, neighbor);
                    if (existing != null) {
                        if (existing.getF() > neighbor.getF()) {
                            openSet.remove(existing);
                            openSet.add(neighbor);
                        }
                    } else {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private Node findInOpenSet(PriorityQueue<Node> openSet, Node node) {
        for (Node n : openSet) if (n.equals(node)) return n;
        return null;
    }

    private LinkedList<int[]> reconstructPath(Node node) {
        LinkedList<int[]> path = new LinkedList<>();
        while (node != null) { path.addFirst(node.getState()); node = node.getParent(); }
        return path;
    }

    public int getExploredNodes() { return exploredNodes; }
}

// ════════════════════════════════════════════════════════════════════════════
//  Nodo del espacio de estados  (lógica sin cambios)
// ════════════════════════════════════════════════════════════════════════════
class Node implements Comparable<Node> {
    private final int[] state;
    private final Node  parent;
    private final int   g, h, heuristicType;

    public Node(int[] state, Node parent, int g, int heuristicType) {
        this.state         = Arrays.copyOf(state, state.length);
        this.parent        = parent;
        this.g             = g;
        this.heuristicType = heuristicType;
        this.h             = calcH();
    }

    private int calcH() {
        switch (heuristicType) { case 1: return h1(); case 3: return h3(); default: return h2(); }
    }

    private int h1() {
        int[] goal = {1,2,3,4,5,6,7,8,0}; int c = 0;
        for (int i = 0; i < 9; i++) if (state[i] != 0 && state[i] != goal[i]) c++;
        return c;
    }

    private int h2() {
        int[] goal = {1,2,3,4,5,6,7,8,0}; int d = 0;
        for (int i = 0; i < 9; i++) {
            if (state[i] == 0) continue;
            int v = state[i], gp = -1;
            for (int j = 0; j < 9; j++) if (goal[j] == v) { gp = j; break; }
            d += Math.abs(i/3 - gp/3) + Math.abs(i%3 - gp%3);
        }
        return d;
    }

    private int h3() {
        int[] goal = {1,2,3,4,5,6,7,8,0}; int s = 0;
        if (state[4] != goal[4]) s++;
        int[] nc = {0,1,2,3,5,6,7,8};
        for (int i : nc) {
            if (state[i] != 0) {
                int next = (i+1) % 9;
                if (state[i] != goal[i] && (next == 4 || state[next] != goal[next])) s += 2;
            }
        }
        return h2() + 3*s;
    }

    public int getF()   { return g + h; }
    public int[] getState() { return Arrays.copyOf(state, state.length); }
    public Node  getParent(){ return parent; }

    public boolean isGoal() { return Arrays.equals(state, new int[]{1,2,3,4,5,6,7,8,0}); }

    public Node[] getNeighbors() {
        int ep = -1;
        for (int i = 0; i < 9; i++) if (state[i] == 0) { ep = i; break; }
        int row = ep/3, col = ep%3;
        int[][] moves = {{-1,0},{1,0},{0,-1},{0,1}};
        Node[] nb = new Node[4]; int cnt = 0;
        for (int[] mv : moves) {
            int nr = row+mv[0], nc = col+mv[1];
            if (nr >= 0 && nr < 3 && nc >= 0 && nc < 3) {
                int np = nr*3+nc;
                int[] ns = Arrays.copyOf(state, 9);
                ns[ep] = ns[np]; ns[np] = 0;
                nb[cnt++] = new Node(ns, this, g+1, heuristicType);
            }
        }
        return Arrays.copyOf(nb, cnt);
    }

    @Override public int compareTo(Node o) { return Integer.compare(getF(), o.getF()); }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Node)) return false;
        return Arrays.equals(state, ((Node) o).state);
    }
    @Override public int hashCode() { return Arrays.hashCode(state); }
}
