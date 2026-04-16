import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Puzzle de Ordenación — Interfaz gráfica interactiva.
 *
 * El jugador puede:
 *  - Escribir un array personalizado o elegir un preset.
 *  - Hacer swaps manuales pulsando los botones de intercambio.
 *  - Pedir solución automática con DFS o BFS (animada paso a paso).
 *  - Ver el camino de pasos en el panel de log.
 */
public class Main extends JFrame {

    // ── Paleta ───────────────────────────────────────────────────────────────
    private static final Color C_BASE    = new Color(30,  30,  46);
    private static final Color C_MANTLE  = new Color(24,  24,  37);
    private static final Color C_SURFACE = new Color(49,  50,  68);
    private static final Color C_ELEVADO = new Color(69,  71,  90);
    private static final Color C_TEXTO   = new Color(205, 214, 244);
    private static final Color C_MUTED   = new Color(147, 153, 178);
    private static final Color C_VERDE   = new Color(166, 227, 161);
    private static final Color C_AZUL    = new Color(137, 180, 250);
    private static final Color C_ROJO    = new Color(243, 139, 168);
    private static final Color C_AMARILLO= new Color(249, 226, 175);
    private static final Color C_MORADO  = new Color(203, 166, 247);
    private static final Color C_MELOCOTON = new Color(250, 179, 135);

    // Colores por índice para los bloques
    private static final Color[] BLOQUES = {
        new Color(137, 180, 250),  // azul
        new Color(166, 227, 161),  // verde
        new Color(250, 179, 135),  // melocotón
        new Color(203, 166, 247),  // morado
        new Color(243, 139, 168),  // rojo
        new Color(249, 226, 175),  // amarillo
        new Color(148, 226, 213),  // teal
        new Color(180, 190, 254),  // lavanda
    };

    // ── Estado del juego ─────────────────────────────────────────────────────
    private int[]   estadoActual;
    private int[]   estadoInicial;
    private int     pasosManuales;
    private boolean animando;

    // Camino de la solución automática (para animación)
    private List<int[]> caminoSolucion;
    private int         indiceCamino;

    // ── Widgets ──────────────────────────────────────────────────────────────
    private ArrayCanvas  canvasArray;
    private JPanel       panelSwaps;
    private JTextArea    areaLog;
    private JLabel       lblEstado, lblPasos;
    private JButton      btnDFS, btnBFS, btnReset, btnPausaReanudar;
    private JTextField   campoArray;
    private javax.swing.Timer timerAnim;

    // ── Constructor ──────────────────────────────────────────────────────────
    public Main() {
        super("Puzzle DFS — Ordenación Interactiva");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BASE);
        setLayout(new BorderLayout(0, 0));

        estadoActual  = new int[]{4, 2, 3, 1};
        estadoInicial = estadoActual.clone();
        pasosManuales = 0;

        buildUI();
        pack();
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        setVisible(true);
        actualizarSwapButtons();
    }

    // ── Construcción UI ───────────────────────────────────────────────────────
    private void buildUI() {
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildRight(),  BorderLayout.EAST);
    }

    /** Barra superior: campo de entrada + presets */
    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setBackground(C_MANTLE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_SURFACE));

        p.add(lbl("Array:", 12, C_MUTED));
        campoArray = new JTextField("4, 2, 3, 1", 14);
        estilizarCampo(campoArray);
        p.add(campoArray);

        JButton btnCargar = boton("Cargar", false);
        btnCargar.addActionListener(e -> cargarDesdeTexto());
        p.add(btnCargar);

        p.add(lbl("  Presets:", 12, C_MUTED));
        for (String preset : new String[]{"4,2,3,1", "3,4,1,2", "4,3,2,1", "1,2,3,4"}) {
            JButton b = boton(preset, false);
            b.setFont(new Font("Monospaced", Font.PLAIN, 11));
            b.addActionListener(e -> {
                campoArray.setText(preset);
                cargarDesdeTexto();
            });
            p.add(b);
        }
        return p;
    }

    /** Centro: canvas del array + botones de swap + botones de control */
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(C_BASE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 8));

        // Canvas
        canvasArray = new ArrayCanvas();
        canvasArray.setPreferredSize(new Dimension(460, 140));
        p.add(canvasArray, BorderLayout.NORTH);

        // Botones de swap (se generan en actualizarSwapButtons)
        panelSwaps = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panelSwaps.setBackground(C_BASE);
        p.add(panelSwaps, BorderLayout.CENTER);

        // Botones de control
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botones.setBackground(C_BASE);

        btnDFS = boton("▶  Auto DFS", true);
        btnBFS = boton("▶  Auto BFS", true);
        btnReset = boton("↺  Reiniciar", false);
        btnPausaReanudar = boton("⏸  Pausar", false);
        btnPausaReanudar.setVisible(false);

        btnDFS.addActionListener(e -> iniciarAutoSolve(false));
        btnBFS.addActionListener(e -> iniciarAutoSolve(true));
        btnReset.addActionListener(e -> resetear());
        btnPausaReanudar.addActionListener(e -> pausarReanudar());

        botones.add(btnDFS);
        botones.add(btnBFS);
        botones.add(btnReset);
        botones.add(btnPausaReanudar);
        p.add(botones, BorderLayout.SOUTH);

        // Etiquetas de estado
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        infoRow.setBackground(C_BASE);
        lblPasos  = lbl("Pasos: 0", 12, C_MUTED);
        lblEstado = lbl("", 13, C_VERDE);
        infoRow.add(lblPasos);
        infoRow.add(lblEstado);

        JPanel centerWrap = new JPanel(new BorderLayout(0, 8));
        centerWrap.setBackground(C_BASE);
        centerWrap.add(p, BorderLayout.CENTER);
        centerWrap.add(infoRow, BorderLayout.SOUTH);
        return centerWrap;
    }

    /** Panel derecho: log de pasos */
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(C_MANTLE);
        p.setPreferredSize(new Dimension(240, 0));
        p.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, C_SURFACE));

        JLabel titulo = lbl("  Historial de pasos", 13, C_MORADO);
        titulo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_SURFACE));
        titulo.setOpaque(true);
        titulo.setBackground(C_MANTLE);
        p.add(titulo, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(C_MANTLE);
        areaLog.setForeground(C_TEXTO);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        areaLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 6));

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(C_MANTLE);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── Lógica del juego ─────────────────────────────────────────────────────

    private void cargarDesdeTexto() {
        try {
            String[] partes = campoArray.getText().trim().split("[,\\s]+");
            int[] arr = new int[partes.length];
            for (int i = 0; i < partes.length; i++)
                arr[i] = Integer.parseInt(partes[i].trim());
            if (arr.length < 2 || arr.length > 8) {
                JOptionPane.showMessageDialog(this,
                        "El array debe tener entre 2 y 8 elementos.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            detenerAnimacion();
            estadoActual  = arr;
            estadoInicial = arr.clone();
            pasosManuales = 0;
            caminoSolucion = null;
            areaLog.setText("");
            lblEstado.setText("");
            actualizarUI();
            log("Cargado: " + java.util.Arrays.toString(estadoActual));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato inválido. Usa números separados por comas.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetear() {
        detenerAnimacion();
        estadoActual  = estadoInicial.clone();
        pasosManuales = 0;
        caminoSolucion = null;
        areaLog.setText("");
        lblEstado.setText("");
        log("Reiniciado: " + java.util.Arrays.toString(estadoActual));
        actualizarUI();
    }

    private void aplicarSwap(int i) {
        if (animando) return;
        int tmp = estadoActual[i];
        estadoActual[i]     = estadoActual[i + 1];
        estadoActual[i + 1] = tmp;
        pasosManuales++;
        log(String.format("  swap(%d↔%d) → %s", i, i+1,
                java.util.Arrays.toString(estadoActual)));
        actualizarUI();
        if (estaOrdenado()) {
            lblEstado.setText("¡Ordenado en " + pasosManuales + " pasos!");
            log("✓ ¡Completado!");
        }
    }

    private boolean estaOrdenado() {
        for (int i = 0; i < estadoActual.length - 1; i++)
            if (estadoActual[i] > estadoActual[i + 1]) return false;
        return true;
    }

    // ── Auto-solve animado ───────────────────────────────────────────────────

    private void iniciarAutoSolve(boolean usarBFS) {
        if (animando) return;
        detenerAnimacion();
        estadoActual = estadoInicial.clone();
        pasosManuales = 0;
        areaLog.setText("");
        lblEstado.setText("Calculando...");
        actualizarUI();

        final boolean bfs = usarBFS;
        new SwingWorker<List<int[]>, Void>() {
            @Override
            protected List<int[]> doInBackground() {
                PuzzleDFSRecursivo solver = new PuzzleDFSRecursivo(estadoInicial);
                java.util.LinkedList<int[]> camino = bfs ? solver.resolverBFS() : solver.resolver();
                if (camino == null) return null;
                return new java.util.ArrayList<>(camino);
            }

            @Override
            protected void done() {
                try {
                    List<int[]> camino = get();
                    if (camino == null) {
                        lblEstado.setText("Sin solución dentro del límite.");
                        return;
                    }
                    caminoSolucion = camino;
                    indiceCamino   = 0;
                    String modo = bfs ? "BFS" : "DFS";
                    log(modo + " → " + (camino.size() - 1) + " pasos");
                    // Mostrar camino completo en log
                    for (int i = 0; i < camino.size(); i++) {
                        log(String.format("  [%d] %s", i,
                                java.util.Arrays.toString(camino.get(i))));
                    }
                    lblEstado.setText("Animando solución " + modo + "...");
                    arrancarTimer();
                } catch (Exception ex) {
                    lblEstado.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void arrancarTimer() {
        animando = true;
        btnDFS.setEnabled(false);
        btnBFS.setEnabled(false);
        btnPausaReanudar.setVisible(true);
        btnPausaReanudar.setText("⏸  Pausar");

        timerAnim = new javax.swing.Timer(600, e -> avanzarAnimacion());
        timerAnim.start();
    }

    private void avanzarAnimacion() {
        if (caminoSolucion == null || indiceCamino >= caminoSolucion.size()) {
            detenerAnimacion();
            lblEstado.setText("¡Ordenado en " + (caminoSolucion != null
                    ? caminoSolucion.size() - 1 : 0) + " pasos!");
            return;
        }
        estadoActual = caminoSolucion.get(indiceCamino).clone();
        indiceCamino++;
        pasosManuales = indiceCamino - 1;
        canvasArray.repaint();
        lblPasos.setText("Paso: " + pasosManuales);
    }

    private void pausarReanudar() {
        if (timerAnim == null) return;
        if (timerAnim.isRunning()) {
            timerAnim.stop();
            btnPausaReanudar.setText("▶  Reanudar");
        } else {
            timerAnim.start();
            btnPausaReanudar.setText("⏸  Pausar");
        }
    }

    private void detenerAnimacion() {
        if (timerAnim != null) { timerAnim.stop(); timerAnim = null; }
        animando = false;
        btnDFS.setEnabled(true);
        btnBFS.setEnabled(true);
        btnPausaReanudar.setVisible(false);
    }

    // ── Helpers de actualización UI ──────────────────────────────────────────

    private void actualizarUI() {
        canvasArray.repaint();
        actualizarSwapButtons();
        lblPasos.setText("Pasos: " + pasosManuales);
    }

    private void actualizarSwapButtons() {
        panelSwaps.removeAll();
        panelSwaps.add(lbl("Intercambios:", 11, C_MUTED));
        for (int i = 0; i < estadoActual.length - 1; i++) {
            final int idx = i;
            JButton b = boton(String.format("swap(%d↔%d)", i, i+1), false);
            b.setForeground(C_AZUL);
            b.addActionListener(e -> aplicarSwap(idx));
            panelSwaps.add(b);
        }
        panelSwaps.revalidate();
        panelSwaps.repaint();
    }

    private void log(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    // ── Canvas del array ─────────────────────────────────────────────────────
    class ArrayCanvas extends JPanel {
        private static final int BLOQUE_W = 70;
        private static final int BLOQUE_H = 80;
        private static final int GAP      = 8;
        private static final int RADIO    = 12;

        ArrayCanvas() {
            setBackground(C_BASE);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (estadoActual == null || estadoActual.length == 0) return;

            int n    = estadoActual.length;
            int totalW = n * BLOQUE_W + (n - 1) * GAP;
            int startX = (getWidth()  - totalW) / 2;
            int startY = (getHeight() - BLOQUE_H) / 2;

            // Encontrar valor máximo para escala de altura
            int maxVal = 0;
            for (int v : estadoActual) maxVal = Math.max(maxVal, v);

            for (int i = 0; i < n; i++) {
                int val = estadoActual[i];
                int x   = startX + i * (BLOQUE_W + GAP);

                // Altura proporcional al valor
                int bh   = (int)(BLOQUE_H * 0.4 + BLOQUE_H * 0.6 * val / maxVal);
                int y    = startY + BLOQUE_H - bh;

                // Color según valor (mapea valor → color fijo para consistencia visual)
                Color base = BLOQUES[(val - 1) % BLOQUES.length];
                Color dark = base.darker().darker();

                // Sombra
                g.setColor(new Color(0, 0, 0, 60));
                g.fillRoundRect(x + 3, y + 3, BLOQUE_W, bh, RADIO, RADIO);

                // Bloque principal con gradiente
                GradientPaint gp = new GradientPaint(x, y, base, x + BLOQUE_W, y + bh, dark);
                g.setPaint(gp);
                g.fillRoundRect(x, y, BLOQUE_W, bh, RADIO, RADIO);

                // Borde
                g.setColor(base.brighter());
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(x, y, BLOQUE_W, bh, RADIO, RADIO);

                // Valor centrado
                g.setColor(Color.WHITE);
                g.setFont(new Font("Dialog", Font.BOLD, 22));
                FontMetrics fm = g.getFontMetrics();
                String txt = String.valueOf(val);
                int tx = x + (BLOQUE_W - fm.stringWidth(txt)) / 2;
                int ty = y + (bh + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(txt, tx, ty);

                // Índice debajo del bloque
                g.setColor(C_MUTED);
                g.setFont(new Font("Dialog", Font.PLAIN, 10));
                g.drawString("[" + i + "]",
                        x + (BLOQUE_W - g.getFontMetrics().stringWidth("[" + i + "]")) / 2,
                        startY + BLOQUE_H + 16);
            }

            // Indicador de ordenado
            if (estaOrdenado()) {
                g.setColor(new Color(166, 227, 161, 180));
                g.setFont(new Font("Dialog", Font.BOLD, 14));
                g.drawString("✓ ORDENADO", startX, startY - 10);
            }
        }
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────
    private static JLabel lbl(String t, int size, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Dialog", Font.PLAIN, size));
        l.setForeground(c);
        return l;
    }

    private static JButton boton(String txt, boolean primario) {
        JButton btn = new JButton(txt);
        btn.setBackground(primario ? new Color(64, 66, 90) : new Color(49, 50, 68));
        btn.setForeground(primario ? C_VERDE : C_MUTED);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primario
                        ? new Color(88, 91, 112) : new Color(69, 71, 90)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void estilizarCampo(JTextField tf) {
        tf.setBackground(new Color(49, 50, 68));
        tf.setForeground(new Color(205, 214, 244));
        tf.setCaretColor(new Color(205, 214, 244));
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    // ── Main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
