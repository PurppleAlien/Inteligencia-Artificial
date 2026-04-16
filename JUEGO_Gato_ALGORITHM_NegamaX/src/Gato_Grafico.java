import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Interfaz gráfica para el Juego del Gato con Negamax.
 *
 * Mejoras respecto a la versión anterior:
 *  - Tablero dibujado con Graphics2D: X en rojo, O en azul (mismo estilo
 *    que Gato_SG_GUI para coherencia visual entre proyectos).
 *  - Resaltado de línea ganadora (fondo verde + línea tachante).
 *  - Marcador de partidas persistente.
 *  - Hard mode: la computadora siempre va primero (también en iniciarJuego).
 *  - SwingWorker: la IA no bloquea la interfaz.
 *  - Selector de dificultad estilizado con colores.
 */
public class Gato_Grafico {

    // Colores sincronizados con Gato_SG_GUI
    static final Color C_FONDO    = new Color(25,  30,  45);
    static final Color C_CELDA    = new Color(38,  45,  65);
    static final Color C_LINEA    = new Color(80,  95, 130);
    static final Color C_HUMANO   = new Color(220,  60,  60);
    static final Color C_COMP     = new Color(70,  140, 220);
    static final Color C_GANADORA = new Color(50,  160,  80);

    private final Clase_Gato juego = new Clase_Gato();
    private boolean juegoIniciado  = false;
    private boolean turnoHumano    = true;
    private int[] celdasGan        = null;
    private int scoreHum = 0, scoreComp = 0, scoreEmp = 0;

    private JFrame frame;
    private JLabel lblEstado, lblScore;
    private JRadioButton[] rbDificultad;
    private JButton btnIniciar;
    private TableroPanel panel;

    // ══════════════════════════════════════════════════════════════
    //  Panel de tablero personalizado (idéntico al de Gato_SG_GUI)
    // ══════════════════════════════════════════════════════════════
    class TableroPanel extends JPanel {

        static final int CELDA        = 160;
        static final int GROSOR_LINEA = 6;
        static final int MARGEN_PIEZA = 28;
        static final int GROSOR_PIEZA = 14;

        TableroPanel() {
            setPreferredSize(new Dimension(3 * CELDA, 3 * CELDA));
            setBackground(C_FONDO);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    int col  = e.getX() / CELDA;
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

            int W = getWidth(), H = getHeight();
            int cx = W / 3, cy = H / 3;
            int[][] estado = juego.getTablero();

            // fondos de celda
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    boolean esGan = celdasGan != null && contieneIndice(celdasGan, i*3+j);
                    g2.setColor(esGan ? C_GANADORA.darker() : C_CELDA);
                    g2.fillRoundRect(j*cx+4, i*cy+4, cx-8, cy-8, 14, 14);
                }
            }

            // cuadrícula
            g2.setColor(C_LINEA);
            g2.setStroke(new BasicStroke(GROSOR_LINEA, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND));
            g2.drawLine(cx,   4,   cx,   H-4);
            g2.drawLine(2*cx, 4,   2*cx, H-4);
            g2.drawLine(4,    cy,  W-4,  cy);
            g2.drawLine(4,    2*cy,W-4,  2*cy);

            // piezas
            g2.setStroke(new BasicStroke(GROSOR_PIEZA, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int x = j*cx + MARGEN_PIEZA;
                    int y = i*cy + MARGEN_PIEZA;
                    int w = cx - 2*MARGEN_PIEZA;
                    int h = cy - 2*MARGEN_PIEZA;
                    if (estado[i][j] == Clase_Gato.HUMANO) {
                        g2.setColor(C_HUMANO);
                        g2.drawLine(x, y, x+w, y+h);
                        g2.drawLine(x+w, y, x, y+h);
                    } else if (estado[i][j] == Clase_Gato.COMPUTADORA) {
                        g2.setColor(C_COMP);
                        g2.drawOval(x, y, w, h);
                    }
                }
            }

            // línea tachante
            if (celdasGan != null) {
                int i0 = celdasGan[0]/3, j0 = celdasGan[0]%3;
                int i2 = celdasGan[2]/3, j2 = celdasGan[2]%3;
                g2.setColor(new Color(C_GANADORA.getRed(), C_GANADORA.getGreen(),
                                       C_GANADORA.getBlue(), 200));
                g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND,
                                              BasicStroke.JOIN_ROUND));
                g2.drawLine(j0*cx+cx/2, i0*cy+cy/2, j2*cx+cx/2, i2*cy+cy/2);
            }

            // overlay "Inicia juego" si no ha empezado
            if (!juegoIniciado) {
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, W, H);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "Selecciona dificultad e Inicia";
                g2.drawString(txt, (W - fm.stringWidth(txt))/2, H/2);
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
    public Gato_Grafico() {
        buildUI();
    }

    void buildUI() {
        frame = new JFrame("Juego del Gato  ·  Negamax + Niveles");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(C_FONDO);

        // ── barra de puntuación ──
        JPanel barraScore = new JPanel(new BorderLayout());
        barraScore.setBackground(new Color(15, 18, 32));
        barraScore.setBorder(new EmptyBorder(9, 16, 9, 16));
        lblScore = new JLabel("X  0   ·   O  0   ·   Empates  0",
                               SwingConstants.CENTER);
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblScore.setForeground(new Color(160, 175, 210));
        barraScore.add(lblScore);

        // ── panel de dificultad ──
        JPanel panelDif = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelDif.setBackground(new Color(20, 25, 40));
        panelDif.setBorder(new EmptyBorder(2, 8, 2, 8));

        ButtonGroup grupo = new ButtonGroup();
        rbDificultad = new JRadioButton[3];
        String[] nombres = {"Fácil", "Medio", "Difícil"};
        Color[]  colores = {new Color(90,200,100), new Color(240,190,50), new Color(220,80,80)};
        for (int i = 0; i < 3; i++) {
            rbDificultad[i] = new JRadioButton(nombres[i]);
            rbDificultad[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            rbDificultad[i].setForeground(colores[i]);
            rbDificultad[i].setBackground(new Color(20, 25, 40));
            rbDificultad[i].setFocusPainted(false);
            rbDificultad[i].setActionCommand(String.valueOf(i + 1));
            grupo.add(rbDificultad[i]);
            panelDif.add(rbDificultad[i]);
        }
        rbDificultad[1].setSelected(true);  // medio por defecto

        btnIniciar = new JButton("▶  Iniciar Juego");
        estilizarBoton(btnIniciar, new Color(45, 100, 200));
        btnIniciar.addActionListener(e -> iniciarJuego());
        panelDif.add(btnIniciar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(C_FONDO);
        norte.add(barraScore, BorderLayout.NORTH);
        norte.add(panelDif,   BorderLayout.SOUTH);

        // ── tablero ──
        panel = new TableroPanel();

        // ── barra de estado ──
        JPanel barraBot = new JPanel(new BorderLayout(12, 0));
        barraBot.setBackground(new Color(15, 18, 32));
        barraBot.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblEstado = new JLabel("Selecciona dificultad e inicia el juego.",
                                SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEstado.setForeground(new Color(160, 175, 210));

        JButton btnRein = new JButton("↺  Reiniciar");
        estilizarBoton(btnRein, new Color(55, 80, 140));
        btnRein.addActionListener(e -> reiniciarPartida());

        barraBot.add(lblEstado, BorderLayout.CENTER);
        barraBot.add(btnRein,   BorderLayout.EAST);

        frame.add(norte,    BorderLayout.NORTH);
        frame.add(panel,    BorderLayout.CENTER);
        frame.add(barraBot, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
    //  Control del juego
    // ══════════════════════════════════════════════════════════════

    void iniciarJuego() {
        int nivel = Integer.parseInt(obtenerDificultadSeleccionada());
        juego.setDificultad(nivel);
        juego.reiniciarJuego();
        celdasGan     = null;
        juegoIniciado = true;
        turnoHumano   = true;

        String nomDif = nombreDificultad(nivel);

        // En difícil, la computadora siempre sale primero
        if (nivel == 3) {
            turnoHumano = false;
            lblEstado.setText("Difícil — Computadora sale primero…");
            lblEstado.setForeground(C_COMP);
            panel.repaint();
            ejecutarTurnoComputadora();
        } else {
            lblEstado.setText("Juego iniciado — Dificultad: " + nomDif + " · Tu turno (X)");
            lblEstado.setForeground(C_HUMANO);
            panel.repaint();
        }
    }

    void reiniciarPartida() {
        if (!juegoIniciado) { iniciarJuego(); return; }
        int nivel = juego.getDificultad();
        juego.reiniciarJuego();
        celdasGan   = null;
        turnoHumano = true;

        if (nivel == 3) {
            turnoHumano = false;
            lblEstado.setText("Reiniciado — Computadora sale primero…");
            lblEstado.setForeground(C_COMP);
            panel.repaint();
            ejecutarTurnoComputadora();
        } else {
            lblEstado.setText("Reiniciado — Tu turno (X)");
            lblEstado.setForeground(C_HUMANO);
            panel.repaint();
        }
    }

    void manejarClick(int fila, int col) {
        if (!juegoIniciado || !turnoHumano || celdasGan != null) return;
        if (!juego.movimientoValido(fila, col)) return;

        juego.realizarMovimiento(fila, col, Clase_Gato.HUMANO);
        panel.repaint();

        int[] win = buscarGanador(Clase_Gato.HUMANO);
        if (win != null) { terminar(win, "¡Ganaste! 🎉", C_HUMANO, Clase_Gato.HUMANO); return; }
        if (juego.esEmpate()) { terminar(null, "¡Empate!", Color.ORANGE, Clase_Gato.VACIO); return; }

        turnoHumano = false;
        lblEstado.setText("Computadora piensa…");
        lblEstado.setForeground(C_COMP);
        ejecutarTurnoComputadora();
    }

    void ejecutarTurnoComputadora() {
        new SwingWorker<int[], Void>() {
            protected int[] doInBackground() {
                return juego.obtenerMovimientoComputadora();
            }
            protected void done() {
                try {
                    int[] mov = get();
                    if (mov[0] < 0) return;
                    juego.realizarMovimiento(mov[0], mov[1], Clase_Gato.COMPUTADORA);
                    panel.repaint();
                    int[] w = buscarGanador(Clase_Gato.COMPUTADORA);
                    if (w != null) { terminar(w, "¡CPU gana!", C_COMP, Clase_Gato.COMPUTADORA); return; }
                    if (juego.esEmpate()) { terminar(null, "¡Empate!", Color.ORANGE, Clase_Gato.VACIO); return; }
                    turnoHumano = true;
                    lblEstado.setText("Tu turno  (X)");
                    lblEstado.setForeground(C_HUMANO);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    void terminar(int[] win, String msg, Color color, int ganador) {
        celdasGan = win;
        if      (ganador == Clase_Gato.HUMANO)      scoreHum++;
        else if (ganador == Clase_Gato.COMPUTADORA) scoreComp++;
        else                                         scoreEmp++;
        lblEstado.setText(msg);
        lblEstado.setForeground(color);
        lblScore.setText(String.format("X  %d   ·   O  %d   ·   Empates  %d",
                                        scoreHum, scoreComp, scoreEmp));
        panel.repaint();
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilidades
    // ══════════════════════════════════════════════════════════════

    String obtenerDificultadSeleccionada() {
        for (JRadioButton rb : rbDificultad)
            if (rb.isSelected()) return rb.getActionCommand();
        return "2";
    }

    String nombreDificultad(int nivel) {
        return switch (nivel) {
            case 1 -> "Fácil";
            case 2 -> "Medio";
            case 3 -> "Difícil";
            default -> "?";
        };
    }

    /** Devuelve índices planos de la línea ganadora, o null. */
    int[] buscarGanador(int jugador) {
        int[][] t = juego.getTablero();
        for (int i = 0; i < 3; i++)
            if (t[i][0]==jugador && t[i][1]==jugador && t[i][2]==jugador)
                return new int[]{i*3, i*3+1, i*3+2};
        for (int j = 0; j < 3; j++)
            if (t[0][j]==jugador && t[1][j]==jugador && t[2][j]==jugador)
                return new int[]{j, 3+j, 6+j};
        if (t[0][0]==jugador && t[1][1]==jugador && t[2][2]==jugador) return new int[]{0,4,8};
        if (t[0][2]==jugador && t[1][1]==jugador && t[2][0]==jugador) return new int[]{2,4,6};
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gato_Grafico g = new Gato_Grafico();
            g.frame.setLocationRelativeTo(null);
        });
    }
}
