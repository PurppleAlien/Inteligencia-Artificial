import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * N-Reinas — Interfaz gráfica animada.
 *
 * FIX tablero invisible:
 *  - TableroCanvas ahora extiende JPanel (opaco por defecto → fondo limpiado
 *    en cada repaint por super.paintComponent).
 *  - Colores estilo ajedrez clásico (crema + marrón) con alto contraste.
 *  - getPreferredSize() dinámico según el N actual.
 */
public class NReinasGUI extends JFrame {

    // ── Paleta ───────────────────────────────────────────────────────────────
    private static final Color C_BASE     = new Color(30,  30,  46);
    private static final Color C_MANTLE   = new Color(24,  24,  37);
    private static final Color C_SURFACE  = new Color(49,  50,  68);
    private static final Color C_ELEVADO  = new Color(69,  71,  90);
    private static final Color C_TEXTO    = new Color(205, 214, 244);
    private static final Color C_MUTED    = new Color(108, 112, 134);
    private static final Color C_VERDE    = new Color(166, 227, 161);
    private static final Color C_AZUL     = new Color(137, 180, 250);
    private static final Color C_ROJO     = new Color(243, 139, 168);
    private static final Color C_AMARILLO = new Color(249, 226, 175);
    private static final Color C_MORADO   = new Color(203, 166, 247);

    // Colores del tablero estilo ajedrez (alto contraste)
    private static final Color C_CELDA_CLARA  = new Color(240, 217, 181);
    private static final Color C_CELDA_OSCURA = new Color(181, 136,  99);

    // ── Estado ───────────────────────────────────────────────────────────────
    private int n = 8;
    private NQueensSolver solver;
    private List<NQueensSolver.Paso> pasos;
    private int indicePaso;
    private int indiceSolucion;
    private int[] tableroDisplay;
    private int   filaActiva = -1, colActiva = -1;
    private NQueensSolver.TipoPaso tipoActivo;

    // ── UI ────────────────────────────────────────────────────────────────────
    private TableroCanvas tableroCanvas;
    private JSpinner      spinnerN;
    private JSlider       sliderVelocidad;
    private JButton       btnResolverTodo, btnAnimar, btnDetener;
    private JButton       btnAnterior, btnSiguiente;
    private JLabel        lblContadorSol, lblNavegacion, lblMetricas, lblEstado;
    private JTextArea     areaLog;
    private javax.swing.Timer timerAnim;

    // ── Constructor ──────────────────────────────────────────────────────────

    public NReinasGUI() {
        super("N-Reinas — Visualizador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BASE);
        setLayout(new BorderLayout());

        tableroDisplay = new int[n];
        Arrays.fill(tableroDisplay, -1);

        buildUI();
        pack();
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void buildUI() {
        // Canvas centrado en un wrapper con relleno
        tableroCanvas = new TableroCanvas();
        JPanel wrapCanvas = new JPanel(new GridBagLayout());
        wrapCanvas.setBackground(C_BASE);
        wrapCanvas.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));
        wrapCanvas.add(tableroCanvas);
        add(wrapCanvas, BorderLayout.CENTER);

        // Panel derecho: controles + log
        JPanel panelDerecho = new JPanel(new BorderLayout(0, 0));
        panelDerecho.setBackground(C_MANTLE);
        panelDerecho.setPreferredSize(new Dimension(270, 0));
        panelDerecho.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, C_ELEVADO));
        panelDerecho.add(buildPanelControles(), BorderLayout.NORTH);
        panelDerecho.add(buildPanelLog(),       BorderLayout.CENTER);
        panelDerecho.add(buildBarraEstado(),    BorderLayout.SOUTH);
        add(panelDerecho, BorderLayout.EAST);
    }

    private JPanel buildPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(C_MANTLE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JLabel titulo = lbl("N-Reinas", new Font("Segoe UI", Font.BOLD, 20), C_MORADO);
        panel.add(titulo);
        panel.add(lbl("Backtracking · bitmask O(1)", new Font("Segoe UI", Font.PLAIN, 11), C_MUTED));

        // Selector N
        panel.add(secLabel("Tamaño del tablero (N):"));
        spinnerN = new JSpinner(new SpinnerNumberModel(8, 4, 14, 1));
        estilizarSpinner(spinnerN);
        spinnerN.setMaximumSize(new Dimension(110, 30));
        spinnerN.setAlignmentX(LEFT_ALIGNMENT);
        spinnerN.addChangeListener(e -> resetearParaN((int) spinnerN.getValue()));
        panel.add(spinnerN);

        // Velocidad
        panel.add(secLabel("Velocidad de animación:"));
        sliderVelocidad = new JSlider(20, 800, 200);
        sliderVelocidad.setBackground(C_MANTLE);
        sliderVelocidad.setInverted(true);
        sliderVelocidad.setMaximumSize(new Dimension(230, 28));
        sliderVelocidad.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(sliderVelocidad);

        JPanel lblsVel = new JPanel(new BorderLayout());
        lblsVel.setBackground(C_MANTLE);
        lblsVel.setMaximumSize(new Dimension(230, 16));
        lblsVel.add(lbl("Rápido", new Font("Segoe UI", Font.PLAIN, 10), C_MUTED), BorderLayout.WEST);
        lblsVel.add(lbl("Lento",  new Font("Segoe UI", Font.PLAIN, 10), C_MUTED), BorderLayout.EAST);
        panel.add(lblsVel);

        // Botones
        panel.add(secLabel("Acciones:"));
        btnResolverTodo = boton("⚡ Resolver todo", C_VERDE);
        btnResolverTodo.addActionListener(e -> resolverTodo());
        panel.add(btnResolverTodo);
        panel.add(Box.createVerticalStrut(4));

        btnAnimar = boton("▶  Animar backtracking", C_AZUL);
        btnAnimar.addActionListener(e -> alternarAnimacion());
        panel.add(btnAnimar);
        panel.add(Box.createVerticalStrut(4));

        btnDetener = boton("■  Detener / Reiniciar", C_ROJO);
        btnDetener.addActionListener(e -> detenerYReiniciar());
        panel.add(btnDetener);

        // Navegador
        panel.add(secLabel("Soluciones encontradas:"));
        lblContadorSol = lbl("Pulsa 'Resolver todo'", new Font("Segoe UI", Font.PLAIN, 11),
                new Color(147, 153, 178));
        panel.add(lblContadorSol);
        panel.add(Box.createVerticalStrut(6));

        JPanel panelNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelNav.setBackground(C_MANTLE);
        btnAnterior  = botonChico("◀", C_MORADO);
        btnSiguiente = botonChico("▶", C_MORADO);
        lblNavegacion = lbl("—", new Font("Segoe UI", Font.BOLD, 13), C_TEXTO);
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        btnAnterior .addActionListener(e -> navegarSolucion(-1));
        btnSiguiente.addActionListener(e -> navegarSolucion(+1));
        panelNav.add(btnAnterior);
        panelNav.add(lblNavegacion);
        panelNav.add(btnSiguiente);
        panel.add(panelNav);

        return panel;
    }

    private JPanel buildPanelLog() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(C_MANTLE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JLabel titulo = lbl("Registro del algoritmo", new Font("Segoe UI", Font.PLAIN, 11), C_MUTED);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        panel.add(titulo, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setBackground(new Color(17, 17, 27));
        areaLog.setForeground(C_VERDE);
        areaLog.setCaretColor(C_VERDE);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createLineBorder(C_ELEVADO));
        scroll.getViewport().setBackground(new Color(17, 17, 27));
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnLimpiar = botonChico("Limpiar", C_MUTED);
        btnLimpiar.addActionListener(e -> areaLog.setText(""));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        bottom.setBackground(C_MANTLE);
        bottom.add(btnLimpiar);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBarraEstado() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(new Color(17, 17, 27));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_ELEVADO));
        lblEstado   = lbl("Listo", new Font("Segoe UI", Font.PLAIN, 11), new Color(147,153,178));
        lblMetricas = lbl("",      new Font("Segoe UI", Font.PLAIN, 11), C_MUTED);
        bar.add(lblEstado);
        bar.add(lblMetricas);
        return bar;
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    private void resetearParaN(int nuevoN) {
        detenerYReiniciar();
        n = nuevoN;
        tableroDisplay = new int[n];
        Arrays.fill(tableroDisplay, -1);
        lblContadorSol.setText("Pulsa 'Resolver todo'");
        lblNavegacion.setText("—");
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        tableroCanvas.recalcularTamano();
        tableroCanvas.repaint();
        areaLog.setText("");
        pack();
        setStatus("Tablero reiniciado a " + n + "×" + n);
    }

    private void resolverTodo() {
        detenerYReiniciar();
        btnResolverTodo.setEnabled(false);
        setStatus("Resolviendo N=" + n + " ...");

        new SwingWorker<Void, Void>() {
            long ms;
            @Override protected Void doInBackground() {
                solver = new NQueensSolver(n);
                long t0 = System.currentTimeMillis();
                solver.resolverRapido();
                ms = System.currentTimeMillis() - t0;
                return null;
            }
            @Override protected void done() {
                int total = solver.getSoluciones().size();
                lblContadorSol.setText(total + " soluciones");
                indiceSolucion = 0;
                mostrarSolucion();
                btnAnterior.setEnabled(total > 1);
                btnSiguiente.setEnabled(total > 1);
                setStatus("Completado — " + total + " soluciones");
                lblMetricas.setText("Nodos: " + solver.getNodosExplorados() + "  " + ms + "ms");
                log("[OK] N=" + n + " → " + total + " soluciones | "
                        + solver.getNodosExplorados() + " nodos | " + ms + "ms");
                btnResolverTodo.setEnabled(true);
            }
        }.execute();
    }

    private void alternarAnimacion() {
        if (timerAnim != null && timerAnim.isRunning()) {
            timerAnim.stop();
            btnAnimar.setText("▶  Reanudar animación");
            setStatus("Pausado en paso " + indicePaso);
        } else if (pasos != null && indicePaso < pasos.size()) {
            iniciarTimer();
        } else {
            btnAnimar.setEnabled(false);
            setStatus("Preparando pasos...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    solver = new NQueensSolver(n);
                    int maxSol = n <= 6 ? 50 : 5;
                    solver.resolverConPasos(maxSol);
                    pasos = solver.getPasos();
                    return null;
                }
                @Override protected void done() {
                    indicePaso = 0;
                    Arrays.fill(tableroDisplay, -1);
                    filaActiva = -1; colActiva = -1; tipoActivo = null;
                    log("[ANIM] " + pasos.size() + " pasos — visualizando...");
                    btnAnimar.setEnabled(true);
                    iniciarTimer();
                }
            }.execute();
        }
    }

    private void iniciarTimer() {
        btnAnimar.setText("⏸  Pausar animación");
        timerAnim = new javax.swing.Timer(sliderVelocidad.getValue(), e -> avanzarPaso());
        timerAnim.start();
        setStatus("Animando...");
    }

    private void avanzarPaso() {
        if (pasos == null || indicePaso >= pasos.size()) {
            timerAnim.stop();
            btnAnimar.setText("▶  Animar backtracking");
            setStatus("Animación completa — " + (solver == null ? 0 :
                    solver.getSoluciones().size()) + " soluciones");
            return;
        }
        NQueensSolver.Paso paso = pasos.get(indicePaso++);
        tableroDisplay = paso.tablero.clone();
        filaActiva = paso.fila;
        colActiva  = paso.col;
        tipoActivo = paso.tipo;
        tableroCanvas.repaint();

        String tag = switch (paso.tipo) {
            case COLOCAR    -> "COLOCAR";
            case RETROCEDER -> "VOLVER ";
            case SOLUCION   -> "HALLADA";
        };
        log("[" + tag + "] " + paso.mensaje);
        timerAnim.setDelay(sliderVelocidad.getValue());
        lblMetricas.setText("Paso " + indicePaso + "/" + pasos.size());
    }

    private void detenerYReiniciar() {
        if (timerAnim != null) timerAnim.stop();
        pasos = null; indicePaso = 0;
        if (tableroDisplay != null) Arrays.fill(tableroDisplay, -1);
        filaActiva = -1; colActiva = -1; tipoActivo = null;
        btnAnimar.setText("▶  Animar backtracking");
        tableroCanvas.repaint();
        setStatus("Reiniciado");
    }

    private void navegarSolucion(int delta) {
        if (solver == null || solver.getSoluciones().isEmpty()) return;
        int total = solver.getSoluciones().size();
        indiceSolucion = (indiceSolucion + delta + total) % total;
        mostrarSolucion();
    }

    private void mostrarSolucion() {
        if (solver == null || solver.getSoluciones().isEmpty()) return;
        tableroDisplay = solver.getSoluciones().get(indiceSolucion).clone();
        filaActiva = -1; colActiva = -1; tipoActivo = null;
        tableroCanvas.repaint();
        lblNavegacion.setText((indiceSolucion + 1) + " / " + solver.getSoluciones().size());
        setStatus("Solución " + (indiceSolucion + 1));
    }

    // ── Canvas del tablero ────────────────────────────────────────────────────
    // Extiende JPanel para ser OPACO por defecto:
    // super.paintComponent() limpia el fondo en cada repaint.

    private class TableroCanvas extends JPanel {

        private static final int CELDA    = 60;
        private static final int PADDING  = 28;
        private static final int COORD_PX = 20;

        TableroCanvas() {
            setBackground(C_BASE);
            recalcularTamano();
        }

        /** Actualiza el preferred size según el N actual. */
        void recalcularTamano() {
            int side = CELDA * n + PADDING * 2 + COORD_PX;
            setPreferredSize(new Dimension(side, side));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);  // ← JPanel pinta el fondo C_BASE

            if (n <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Centro el tablero en el canvas
            int boardPx = CELDA * n;
            int ox = (getWidth()  - boardPx) / 2;
            int oy = (getHeight() - boardPx) / 2;

            dibujarTablero(g2, ox, oy);
            dibujarCoordenadas(g2, ox, oy);
            g2.dispose();
        }

        private void dibujarTablero(Graphics2D g2, int ox, int oy) {
            for (int fila = 0; fila < n; fila++) {
                for (int col = 0; col < n; col++) {
                    int x = ox + col * CELDA;
                    int y = oy + fila * CELDA;

                    // Color base estilo ajedrez
                    Color bg = (fila + col) % 2 == 0 ? C_CELDA_CLARA : C_CELDA_OSCURA;

                    // Resaltar celda activa
                    if (tipoActivo != null && fila == filaActiva && col == colActiva) {
                        bg = switch (tipoActivo) {
                            case COLOCAR    -> new Color(100, 200, 100);
                            case RETROCEDER -> new Color(220,  80,  80);
                            case SOLUCION   -> new Color( 80, 220, 150);
                        };
                    }

                    g2.setColor(bg);
                    g2.fillRect(x, y, CELDA, CELDA);

                    // Borde de celda
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.drawRect(x, y, CELDA, CELDA);

                    // Reina
                    if (tableroDisplay != null
                            && fila < tableroDisplay.length
                            && tableroDisplay[fila] == col) {
                        dibujarReina(g2, x, y,
                                fila == filaActiva
                                && tipoActivo == NQueensSolver.TipoPaso.RETROCEDER);
                    }
                }
            }

            // Borde exterior del tablero
            g2.setColor(new Color(0, 0, 0, 140));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRect(ox, oy, CELDA * n, CELDA * n);
            g2.setStroke(new BasicStroke(1));
        }

        private void dibujarReina(Graphics2D g2, int x, int y, boolean desvanecida) {
            // Sombra
            g2.setColor(new Color(0, 0, 0, 100));
            g2.setFont(new Font("Dialog", Font.BOLD, 32));
            g2.drawString("♛", x + 11, y + CELDA - 9);

            // Reina (color según estado)
            float alpha = desvanecida ? 0.3f : 1.0f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            boolean retrocediendo = tipoActivo == NQueensSolver.TipoPaso.RETROCEDER
                    && y / CELDA == filaActiva;
            g2.setColor(retrocediendo ? new Color(255, 80, 80) : new Color(30, 30, 10));
            g2.drawString("♛", x + 10, y + CELDA - 10);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        private void dibujarCoordenadas(Graphics2D g2, int ox, int oy) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(C_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < n; i++) {
                String col = String.valueOf((char) ('a' + i));
                g2.drawString(col,
                        ox + i * CELDA + (CELDA - fm.stringWidth(col)) / 2,
                        oy + n * CELDA + 16);
                String row = String.valueOf(n - i);
                g2.drawString(row,
                        ox - 16,
                        oy + i * CELDA + CELDA / 2 + 4);
            }
        }
    }

    // ── Utilidades UI ────────────────────────────────────────────────────────

    private JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    private JLabel secLabel(String t) {
        JLabel l = lbl(t, new Font("Segoe UI", Font.PLAIN, 11), C_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        return l;
    }

    private JButton boton(String texto, Color acento) {
        JButton btn = new JButton(texto);
        btn.setBackground(C_SURFACE);
        btn.setForeground(acento);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(acento.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    private JButton botonChico(String t, Color a) {
        JButton b = boton(t, a);
        b.setMaximumSize(new Dimension(70, 28));
        return b;
    }

    private void estilizarSpinner(JSpinner s) {
        s.setBackground(C_ELEVADO); s.setForeground(C_TEXTO);
        if (s.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(C_ELEVADO);
            de.getTextField().setForeground(C_TEXTO);
            de.getTextField().setCaretColor(C_TEXTO);
            de.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    private void setStatus(String t) {
        SwingUtilities.invokeLater(() -> lblEstado.setText(t));
    }

    // ── Solver (clase interna estática — sin duplicación) ─────────────────────

    static class NQueensSolver {
        enum TipoPaso { COLOCAR, RETROCEDER, SOLUCION }

        static class Paso {
            final TipoPaso tipo; final int fila, col; final int[] tablero; final String mensaje;
            Paso(TipoPaso t, int f, int c, int[] tab, String m) {
                tipo = t; fila = f; col = c; tablero = tab.clone(); mensaje = m;
            }
        }

        private final int n;
        private final List<int[]> soluciones = new ArrayList<>();
        private final List<Paso>  pasos      = new ArrayList<>();
        private int nodosExplorados;

        NQueensSolver(int n) { this.n = n; }

        void resolverRapido() {
            soluciones.clear(); pasos.clear(); nodosExplorados = 0;
            int[] tab = new int[n]; Arrays.fill(tab, -1);
            btRapido(0, 0, 0, 0, tab);
        }

        private void btRapido(int fila, int cols, int dB, int dA, int[] tab) {
            if (fila == n) { soluciones.add(tab.clone()); return; }
            nodosExplorados++;
            int todas = (1 << n) - 1;
            int libres = todas & ~(cols | dB | dA);
            while (libres != 0) {
                int bit = libres & (-libres);
                int col = Integer.numberOfTrailingZeros(bit);
                tab[fila] = col;
                btRapido(fila+1, cols|bit, (dB|bit)<<1, (dA|bit)>>1, tab);
                tab[fila] = -1;
                libres &= libres - 1;
            }
        }

        void resolverConPasos(int maxSol) {
            soluciones.clear(); pasos.clear(); nodosExplorados = 0;
            int[] tab = new int[n]; Arrays.fill(tab, -1);
            btConPasos(tab, 0, maxSol);
        }

        private boolean btConPasos(int[] tab, int fila, int max) {
            if (fila == n) {
                soluciones.add(tab.clone());
                pasos.add(new Paso(TipoPaso.SOLUCION, -1, -1, tab,
                        "✓ SOLUCIÓN #" + soluciones.size() + " encontrada"));
                return soluciones.size() >= max;
            }
            nodosExplorados++;
            for (int col = 0; col < n; col++) {
                tab[fila] = col;
                if (esValido(tab, fila, col)) {
                    pasos.add(new Paso(TipoPaso.COLOCAR, fila, col, tab,
                            "Fila " + fila + " → columna " + col));
                    if (btConPasos(tab, fila + 1, max)) return true;
                    pasos.add(new Paso(TipoPaso.RETROCEDER, fila, col, tab,
                            "Retroceder: fila " + fila + ", col " + col));
                }
                tab[fila] = -1;
            }
            return false;
        }

        private boolean esValido(int[] tab, int fila, int col) {
            for (int f = 0; f < fila; f++) {
                if (tab[f] == col) return false;
                if (Math.abs(tab[f] - col) == Math.abs(f - fila)) return false;
            }
            return true;
        }

        List<int[]> getSoluciones()      { return soluciones; }
        List<Paso>  getPasos()           { return pasos; }
        int         getNodosExplorados() { return nodosExplorados; }
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background",     new Color(30, 30, 46));
        UIManager.put("Label.foreground",     new Color(205, 214, 244));
        UIManager.put("ScrollBar.thumb",      new Color(69, 71, 90));
        UIManager.put("ScrollBar.track",      new Color(24, 24, 37));
        UIManager.put("OptionPane.background",new Color(49, 50, 68));
        SwingUtilities.invokeLater(NReinasGUI::new);
    }
}
