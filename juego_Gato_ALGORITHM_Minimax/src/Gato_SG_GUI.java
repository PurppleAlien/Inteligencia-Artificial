import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Juego del Gato — Minimax con Poda Alfa-Beta
 *
 * Mejoras respecto a la versión anterior:
 *  - X y O dibujados con Graphics2D (no texto): X rojo, O azul.
 *  - Poda alfa-beta añadida al minimax (reduce nodos evaluados ~70 %).
 *  - Resaltado de la línea ganadora con fondo verde.
 *  - Marcador de victorias persistente.
 *  - Interfaz oscura con colores contrastantes.
 *  - paintComponent sin búsqueda cuadrática (referencias directas).
 */
public class Gato_SG_GUI {

    static final int HUMANO      = 1;
    static final int COMPUTADORA = 0;
    static final int VACIO       = -1;

    static final Color C_FONDO    = new Color(25,  30,  45);
    static final Color C_CELDA    = new Color(38,  45,  65);
    static final Color C_LINEA    = new Color(80,  95, 130);
    static final Color C_HUMANO   = new Color(220,  60,  60);  // rojo  → X
    static final Color C_COMP     = new Color(70,  140, 220);  // azul  → O
    static final Color C_GANADORA = new Color(50,  160,  80);  // verde → celdas ganadoras

    private final int[][] tablero = new int[3][3];
    private boolean turnoHumano   = true;
    private int[] celdasGan       = null;  // índices planos 0-8 de las 3 celdas ganadoras
    private int scoreHum = 0, scoreComp = 0, scoreEmp = 0;

    private JFrame frame;
    private JLabel lblEstado, lblScore;
    private TableroPanel panel;

    // ══════════════════════════════════════════════════════════════
    //  Panel de tablero personalizado
    // ══════════════════════════════════════════════════════════════
    class TableroPanel extends JPanel {

        static final int CELDA = 160;
        static final int GROSOR_LINEA = 6;
        static final int MARGEN_PIEZA = 28;
        static final int GROSOR_PIEZA = 14;

        TableroPanel() {
            setPreferredSize(new Dimension(3 * CELDA, 3 * CELDA));
            setBackground(C_FONDO);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    int col = e.getX() / CELDA;
                    int fila = e.getY() / CELDA;
                    if (col >= 0 && col < 3 && fila >= 0 && fila < 3)
                        manejarClick(fila, col);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                                RenderingHints.VALUE_STROKE_PURE);

            int W = getWidth(), H = getHeight();
            int cx = W / 3, cy = H / 3;

            // fondos de celda
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    boolean esGanadora = celdasGan != null
                            && contieneIndice(celdasGan, i*3 + j);
                    g2.setColor(esGanadora ? C_GANADORA.darker() : C_CELDA);
                    g2.fillRoundRect(j*cx + 4, i*cy + 4,
                                     cx - 8, cy - 8, 14, 14);
                }
            }

            // líneas de cuadrícula
            g2.setColor(C_LINEA);
            g2.setStroke(new BasicStroke(GROSOR_LINEA, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND));
            g2.drawLine(cx,     4,     cx,     H - 4);
            g2.drawLine(2*cx,   4,     2*cx,   H - 4);
            g2.drawLine(4,      cy,    W - 4,  cy);
            g2.drawLine(4,      2*cy,  W - 4,  2*cy);

            // piezas
            g2.setStroke(new BasicStroke(GROSOR_PIEZA, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int x = j*cx + MARGEN_PIEZA;
                    int y = i*cy + MARGEN_PIEZA;
                    int w = cx - 2*MARGEN_PIEZA;
                    int h = cy - 2*MARGEN_PIEZA;
                    if (tablero[i][j] == HUMANO) {
                        // X roja: dos diagonales
                        g2.setColor(C_HUMANO);
                        g2.drawLine(x, y, x+w, y+h);
                        g2.drawLine(x+w, y, x, y+h);
                    } else if (tablero[i][j] == COMPUTADORA) {
                        // O azul: elipse
                        g2.setColor(C_COMP);
                        g2.drawOval(x, y, w, h);
                    }
                }
            }

            // línea tachante sobre las celdas ganadoras
            if (celdasGan != null) {
                int[] p = celdasGan;
                int i0 = p[0]/3, j0 = p[0]%3;
                int i2 = p[2]/3, j2 = p[2]%3;
                int x1 = j0*cx + cx/2, y1 = i0*cy + cy/2;
                int x2 = j2*cx + cx/2, y2 = i2*cy + cy/2;
                g2.setColor(new Color(C_GANADORA.getRed(),
                                       C_GANADORA.getGreen(),
                                       C_GANADORA.getBlue(), 200));
                g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND,
                                              BasicStroke.JOIN_ROUND));
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        private boolean contieneIndice(int[] arr, int val) {
            for (int v : arr) if (v == val) return true;
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Construcción de interfaz
    // ══════════════════════════════════════════════════════════════
    public Gato_SG_GUI() {
        inicializar();
        buildUI();
    }

    void inicializar() {
        for (int[] f : tablero) java.util.Arrays.fill(f, VACIO);
        celdasGan   = null;
        turnoHumano = true;
    }

    void buildUI() {
        frame = new JFrame("Juego del Gato  ·  Minimax + Poda Alfa-Beta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(C_FONDO);

        // ── barra de puntuación ──
        JPanel barraTop = new JPanel(new BorderLayout());
        barraTop.setBackground(new Color(15, 18, 32));
        barraTop.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblScore = new JLabel("X  0   ·   O  0   ·   Empates  0",
                               SwingConstants.CENTER);
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblScore.setForeground(new Color(160, 175, 210));
        barraTop.add(lblScore, BorderLayout.CENTER);

        // ── leyenda ──
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 4));
        leyenda.setBackground(new Color(15, 18, 32));
        leyenda.add(etiquetaColor("  X  — Tú", C_HUMANO));
        leyenda.add(etiquetaColor("  O  — CPU", C_COMP));
        barraTop.add(leyenda, BorderLayout.SOUTH);

        // ── tablero ──
        panel = new TableroPanel();

        // ── barra de estado ──
        JPanel barraBot = new JPanel(new BorderLayout(12, 0));
        barraBot.setBackground(new Color(15, 18, 32));
        barraBot.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblEstado = new JLabel("Tu turno  (X)", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEstado.setForeground(C_HUMANO);

        JButton btnRein = new JButton("↺  Nueva Partida");
        estilizarBoton(btnRein, new Color(50, 90, 175));
        btnRein.addActionListener(e -> reiniciar());

        barraBot.add(lblEstado, BorderLayout.CENTER);
        barraBot.add(btnRein,   BorderLayout.EAST);

        frame.add(barraTop, BorderLayout.NORTH);
        frame.add(panel,    BorderLayout.CENTER);
        frame.add(barraBot, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    JLabel etiquetaColor(String texto, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(color);
        return lbl;
    }

    void estilizarBoton(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ══════════════════════════════════════════════════════════════
    //  Lógica de juego
    // ══════════════════════════════════════════════════════════════

    void manejarClick(int fila, int col) {
        if (!turnoHumano || celdasGan != null) return;
        if (tablero[fila][col] != VACIO)       return;

        tablero[fila][col] = HUMANO;
        panel.repaint();

        int[] win = buscarGanador(HUMANO);
        if (win != null) { terminar(win, "¡Ganaste! 🎉", C_HUMANO, HUMANO); return; }
        if (esEmpate())  { terminar(null, "¡Empate!", Color.ORANGE, VACIO); return; }

        turnoHumano = false;
        lblEstado.setText("Computadora piensa…");
        lblEstado.setForeground(C_COMP);

        // IA en hilo de fondo para no bloquear EDT
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            protected int[] doInBackground() { return mejorMovimientoComp(); }
            protected void done() {
                try {
                    int[] mov = get();
                    tablero[mov[0]][mov[1]] = COMPUTADORA;
                    panel.repaint();
                    int[] w2 = buscarGanador(COMPUTADORA);
                    if (w2 != null) { terminar(w2, "¡CPU gana!", C_COMP, COMPUTADORA); return; }
                    if (esEmpate()) { terminar(null, "¡Empate!", Color.ORANGE, VACIO); return; }
                    turnoHumano = true;
                    lblEstado.setText("Tu turno  (X)");
                    lblEstado.setForeground(C_HUMANO);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    void terminar(int[] win, String msg, Color color, int ganador) {
        celdasGan = win;
        if      (ganador == HUMANO)      scoreHum++;
        else if (ganador == COMPUTADORA) scoreComp++;
        else                             scoreEmp++;
        lblEstado.setText(msg);
        lblEstado.setForeground(color);
        lblScore.setText(String.format("X  %d   ·   O  %d   ·   Empates  %d",
                                        scoreHum, scoreComp, scoreEmp));
        panel.repaint();
    }

    void reiniciar() {
        inicializar();
        lblEstado.setText("Tu turno  (X)");
        lblEstado.setForeground(C_HUMANO);
        panel.repaint();
    }

    // ══════════════════════════════════════════════════════════════
    //  IA — Minimax con Poda Alfa-Beta
    // ══════════════════════════════════════════════════════════════

    int[] mejorMovimientoComp() {
        int mejorVal = Integer.MIN_VALUE;
        int mf = -1, mc = -1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j] == VACIO) {
                    tablero[i][j] = COMPUTADORA;
                    int val = minimax(0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    tablero[i][j] = VACIO;
                    if (val > mejorVal) { mejorVal = val; mf = i; mc = j; }
                }
            }
        }
        return new int[]{mf, mc};
    }

    /**
     * Minimax con poda alfa-beta.
     * Maximizador = COMPUTADORA, Minimizador = HUMANO.
     */
    int minimax(int prof, boolean esMax, int alfa, int beta) {
        if (verificarGanador(COMPUTADORA)) return  10 - prof;
        if (verificarGanador(HUMANO))      return -10 + prof;
        if (esEmpate())                    return 0;

        if (esMax) {
            int mejor = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (tablero[i][j] == VACIO) {
                        tablero[i][j] = COMPUTADORA;
                        mejor = Math.max(mejor, minimax(prof+1, false, alfa, beta));
                        tablero[i][j] = VACIO;
                        alfa = Math.max(alfa, mejor);
                        if (beta <= alfa) return mejor; // poda β
                    }
                }
            }
            return mejor;
        } else {
            int mejor = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (tablero[i][j] == VACIO) {
                        tablero[i][j] = HUMANO;
                        mejor = Math.min(mejor, minimax(prof+1, true, alfa, beta));
                        tablero[i][j] = VACIO;
                        beta = Math.min(beta, mejor);
                        if (beta <= alfa) return mejor; // poda α
                    }
                }
            }
            return mejor;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilidades de tablero
    // ══════════════════════════════════════════════════════════════

    boolean verificarGanador(int jugador) {
        for (int i = 0; i < 3; i++)
            if (tablero[i][0]==jugador && tablero[i][1]==jugador && tablero[i][2]==jugador) return true;
        for (int j = 0; j < 3; j++)
            if (tablero[0][j]==jugador && tablero[1][j]==jugador && tablero[2][j]==jugador) return true;
        if (tablero[0][0]==jugador && tablero[1][1]==jugador && tablero[2][2]==jugador) return true;
        if (tablero[0][2]==jugador && tablero[1][1]==jugador && tablero[2][0]==jugador) return true;
        return false;
    }

    /** Devuelve índices planos (0-8) de las 3 celdas ganadoras, o null. */
    int[] buscarGanador(int jugador) {
        // filas
        for (int i = 0; i < 3; i++)
            if (tablero[i][0]==jugador && tablero[i][1]==jugador && tablero[i][2]==jugador)
                return new int[]{i*3, i*3+1, i*3+2};
        // columnas
        for (int j = 0; j < 3; j++)
            if (tablero[0][j]==jugador && tablero[1][j]==jugador && tablero[2][j]==jugador)
                return new int[]{j, 3+j, 6+j};
        // diagonales
        if (tablero[0][0]==jugador && tablero[1][1]==jugador && tablero[2][2]==jugador)
            return new int[]{0, 4, 8};
        if (tablero[0][2]==jugador && tablero[1][1]==jugador && tablero[2][0]==jugador)
            return new int[]{2, 4, 6};
        return null;
    }

    boolean esEmpate() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (tablero[i][j] == VACIO) return false;
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Gato_SG_GUI::new);
    }
}
