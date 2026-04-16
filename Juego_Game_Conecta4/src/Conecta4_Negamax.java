import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

/**
 * Conecta 4 — Negamax + Poda Alfa-Beta
 *  · Niveles de dificultad: Fácil (prof. 2) / Medio (4) / Difícil (6) / Experto (8)
 *  · Paleta de colores para cada jugador (10 colores disponibles)
 *  · La IA trabaja sobre una copia del tablero → sin parpadeos visuales
 */
public class Conecta4_Negamax {

    // ── Constantes del juego ──────────────────────────────────────
    static final int HUMANO      = 1;
    static final int COMPUTADORA = 2;
    static final int VACIO       = 0;
    static final int FILAS       = 6;
    static final int COLUMNAS    = 7;

    /** Columnas evaluadas del centro hacia los extremos (mejor poda α-β). */
    static final int[] ORDEN = {3, 2, 4, 1, 5, 0, 6};

    // ── Dificultad ────────────────────────────────────────────────
    static final int[]    PROFUNDIDADES = {2, 4, 6, 8};
    static final String[] NOMBRES_DIF  = {"Fácil", "Medio", "Difícil", "Experto"};

    // ── Paleta de colores ─────────────────────────────────────────
    static final Color[] PALETA = {
        new Color(220,  45,  45),   // Rojo
        new Color(240, 120,  20),   // Naranja
        new Color(240, 205,  30),   // Amarillo
        new Color( 55, 200,  80),   // Verde
        new Color( 25, 195, 195),   // Cian
        new Color( 65, 130, 225),   // Azul
        new Color(150,  55, 225),   // Violeta
        new Color(225,  75, 185),   // Rosa
        new Color(200, 200, 200),   // Plata
        new Color(255, 160,  95),   // Salmón
    };
    static final String[] NOMBRES_COLOR = {
        "Rojo","Naranja","Amarillo","Verde","Cian",
        "Azul","Violeta","Rosa","Plata","Salmón"
    };

    // ── Colores fijos de la app ───────────────────────────────────
    static final Color C_FONDO   = new Color(12,  28,  68);
    static final Color C_TABLERO = new Color(18,  52, 128);
    static final Color C_VACIO   = new Color(10,  36,  90);

    // ── Estado del juego (solo modificado en el EDT) ──────────────
    private final int[][] tablero = new int[FILAS][COLUMNAS];
    private boolean turnoHumano  = true;
    private boolean finalizado   = false;
    private int[][] ganadores    = null;
    private int scoreHum = 0, scoreComp = 0, scoreEmp = 0;

    // ── Configuración dinámica ────────────────────────────────────
    private int   profundidadIA = PROFUNDIDADES[2]; // Difícil por defecto
    private Color colorHumano   = PALETA[0];        // Rojo
    private Color colorComp     = PALETA[2];        // Amarillo

    // ── Referencias UI ───────────────────────────────────────────
    private int      colHover = -1;
    private JFrame   frame;
    private JLabel   lblEstado, lblScore;
    private JButton[] btnsCols;
    private JButton  btnColorHumano, btnColorComp;
    private TableroPanel panel;
    private JRadioButton[] rbDificultad;

    // ══════════════════════════════════════════════════════════════
    //  Panel de tablero personalizado
    // ══════════════════════════════════════════════════════════════
    class TableroPanel extends JPanel {
        static final int C = 85;   // tamaño de celda
        static final int P = 8;    // padding

        TableroPanel() {
            setPreferredSize(new Dimension(COLUMNAS * C, FILAS * C));
            setBackground(C_FONDO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);

            // fondo
            g2.setColor(C_FONDO);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // resaltado de columna hover
            if (colHover >= 0 && turnoHumano && !finalizado) {
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRect(colHover * C, 0, C, getHeight());
            }

            // cuerpo del tablero
            g2.setColor(C_TABLERO);
            g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, 16, 16);

            // celdas
            for (int i = 0; i < FILAS; i++)
                for (int j = 0; j < COLUMNAS; j++)
                    dibujarCelda(g2, i, j);

            // anillos blancos en celdas ganadoras
            if (ganadores != null) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND,
                                              BasicStroke.JOIN_ROUND));
                for (int[] pos : ganadores) {
                    int x = pos[1]*C + P, y = pos[0]*C + P, d = C - 2*P;
                    g2.drawOval(x, y, d, d);
                }
                g2.setStroke(new BasicStroke(1f));
            }

            // pieza fantasma (hover preview)
            if (colHover >= 0 && turnoHumano && !finalizado) {
                int f = filaDisponible(colHover);
                if (f >= 0) {
                    int x = colHover*C + P, y = f*C + P, d = C - 2*P;
                    Color ch = colorHumano;
                    g2.setColor(new Color(ch.getRed(), ch.getGreen(), ch.getBlue(), 90));
                    g2.fillOval(x, y, d, d);
                }
            }
        }

        private void dibujarCelda(Graphics2D g2, int i, int j) {
            int x = j*C + P, y = i*C + P, d = C - 2*P;

            // sombra
            g2.setColor(new Color(0, 0, 0, 55));
            g2.fillOval(x+2, y+3, d, d);

            int ficha = tablero[i][j];
            if (ficha == VACIO) {
                g2.setColor(C_VACIO);
                g2.fillOval(x, y, d, d);
                return;
            }

            Color base = (ficha == HUMANO) ? colorHumano : colorComp;
            RadialGradientPaint grad = new RadialGradientPaint(
                x + d*0.34f, y + d*0.27f, d*0.74f,
                new float[]{0f, 1f},
                new Color[]{base.brighter().brighter(), base.darker().darker()}
            );
            g2.setPaint(grad);
            g2.fillOval(x, y, d, d);
            g2.setColor(base.darker().darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, d, d);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Construcción de interfaz
    // ══════════════════════════════════════════════════════════════
    public Conecta4_Negamax() {
        inicializar();
        buildUI();
    }

    void inicializar() {
        for (int[] fila : tablero) java.util.Arrays.fill(fila, VACIO);
        ganadores   = null;
        finalizado  = false;
        turnoHumano = true;
    }

    void buildUI() {
        frame = new JFrame("Conecta 4  ·  Negamax + Poda Alfa-Beta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(C_FONDO);

        // ── barra de puntuación con botones de color ──────────────
        JPanel barraScore = new JPanel(new BorderLayout(8, 0));
        barraScore.setBackground(new Color(8, 20, 55));
        barraScore.setBorder(new EmptyBorder(10, 14, 10, 14));

        btnColorHumano = crearBotonColor(colorHumano, "Cambiar color del Jugador");
        btnColorHumano.addActionListener(e -> mostrarSelectorColor(HUMANO));

        btnColorComp = crearBotonColor(colorComp, "Cambiar color de la Computadora");
        btnColorComp.addActionListener(e -> mostrarSelectorColor(COMPUTADORA));

        lblScore = new JLabel("Jugador  0   ·   Comp  0   ·   Empates  0",
                               SwingConstants.CENTER);
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblScore.setForeground(new Color(160, 190, 255));

        barraScore.add(btnColorHumano, BorderLayout.WEST);
        barraScore.add(lblScore,       BorderLayout.CENTER);
        barraScore.add(btnColorComp,   BorderLayout.EAST);

        // ── selector de dificultad ────────────────────────────────
        JPanel barraDif = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5));
        barraDif.setBackground(new Color(12, 25, 62));

        JLabel lDif = new JLabel("Dificultad:");
        lDif.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lDif.setForeground(new Color(140, 165, 210));
        barraDif.add(lDif);

        Color[] coloresDif = {
            new Color( 90, 200,  90),  // Fácil
            new Color(240, 190,  50),  // Medio
            new Color(235, 120,  40),  // Difícil
            new Color(220,  50,  50),  // Experto
        };
        ButtonGroup grupoDif = new ButtonGroup();
        rbDificultad = new JRadioButton[4];
        for (int i = 0; i < 4; i++) {
            rbDificultad[i] = new JRadioButton(NOMBRES_DIF[i]);
            rbDificultad[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
            rbDificultad[i].setForeground(coloresDif[i]);
            rbDificultad[i].setBackground(new Color(12, 25, 62));
            rbDificultad[i].setFocusPainted(false);
            final int prof = PROFUNDIDADES[i];
            rbDificultad[i].addActionListener(e -> profundidadIA = prof);
            grupoDif.add(rbDificultad[i]);
            barraDif.add(rbDificultad[i]);
        }
        rbDificultad[2].setSelected(true);  // Difícil por defecto

        // ── botones de columna ────────────────────────────────────
        JPanel panelBtns = new JPanel(new GridLayout(1, COLUMNAS, 4, 0));
        panelBtns.setBackground(C_FONDO);
        panelBtns.setBorder(new EmptyBorder(6, 7, 6, 7));
        btnsCols = new JButton[COLUMNAS];
        for (int j = 0; j < COLUMNAS; j++) {
            final int col = j;
            JButton b = new JButton("▼");
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setBackground(new Color(35, 80, 175));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> clic(col));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { colHover = col;  panel.repaint(); }
                public void mouseExited (MouseEvent e) { colHover = -1;   panel.repaint(); }
            });
            btnsCols[j] = b;
            panelBtns.add(b);
        }

        JPanel norte = new JPanel(new BorderLayout(0, 0));
        norte.setBackground(C_FONDO);
        norte.add(barraScore, BorderLayout.NORTH);
        norte.add(barraDif,   BorderLayout.CENTER);
        norte.add(panelBtns,  BorderLayout.SOUTH);

        // ── tablero ───────────────────────────────────────────────
        panel = new TableroPanel();

        // ── barra de estado ───────────────────────────────────────
        JPanel barraBot = new JPanel(new BorderLayout(12, 0));
        barraBot.setBackground(new Color(8, 20, 55));
        barraBot.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblEstado = new JLabel("Tu turno  ●", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEstado.setForeground(colorHumano);

        JButton btnRein = new JButton("↺  Nueva Partida");
        btnRein.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRein.setBackground(new Color(45, 100, 200));
        btnRein.setForeground(Color.WHITE);
        btnRein.setFocusPainted(false);
        btnRein.setBorderPainted(false);
        btnRein.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRein.addActionListener(e -> reiniciar());

        barraBot.add(lblEstado, BorderLayout.CENTER);
        barraBot.add(btnRein,   BorderLayout.EAST);

        frame.add(norte,    BorderLayout.NORTH);
        frame.add(panel,    BorderLayout.CENTER);
        frame.add(barraBot, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Crea un botón circular de color para la barra de puntuación. */
    JButton crearBotonColor(Color color, String tooltip) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // halo blanco al hacer hover
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 70));
                    g2.fillOval(1, 1, getWidth()-2, getHeight()-2);
                }
                g2.setColor(getBackground());
                g2.fillOval(4, 4, getWidth()-8, getHeight()-8);
                g2.setColor(getBackground().darker().darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(4, 4, getWidth()-8, getHeight()-8);
            }
        };
        b.setPreferredSize(new Dimension(34, 34));
        b.setBackground(color);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);
        return b;
    }

    // ══════════════════════════════════════════════════════════════
    //  Selector de color (diálogo modal)
    // ══════════════════════════════════════════════════════════════
    void mostrarSelectorColor(int jugador) {
        JDialog dial = new JDialog(frame,
            jugador == HUMANO ? "Color del Jugador" : "Color de la Computadora",
            true);
        dial.setLayout(new BorderLayout(0, 10));
        dial.getContentPane().setBackground(C_FONDO);
        ((JPanel) dial.getContentPane()).setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lbl = new JLabel(
            jugador == HUMANO ? "Elige tu color  (X)" : "Elige el color de la CPU  (O)",
            SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel grid = new JPanel(new GridLayout(2, 5, 10, 10));
        grid.setBackground(C_FONDO);

        Color colorActual = (jugador == HUMANO) ? colorHumano : colorComp;

        for (int idx = 0; idx < PALETA.length; idx++) {
            final Color c   = PALETA[idx];
            final String nombre = NOMBRES_COLOR[idx];
            final boolean esActual = c.equals(colorActual);

            JButton sw = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    // anillo blanco si es el color actualmente seleccionado
                    if (esActual) {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(0, 0, getWidth(), getHeight());
                    }
                    // halo hover
                    if (getModel().isRollover() && !esActual) {
                        g2.setColor(new Color(255, 255, 255, 60));
                        g2.fillOval(0, 0, getWidth(), getHeight());
                    }
                    RadialGradientPaint grad = new RadialGradientPaint(
                        getWidth()*0.38f, getHeight()*0.32f,
                        getWidth()*0.62f,
                        new float[]{0f, 1f},
                        new Color[]{c.brighter(), c.darker()}
                    );
                    g2.setPaint(grad);
                    g2.fillOval(esActual ? 5 : 3, esActual ? 5 : 3,
                                getWidth() - (esActual ? 10 : 6),
                                getHeight() - (esActual ? 10 : 6));
                }
            };
            sw.setPreferredSize(new Dimension(56, 56));
            sw.setContentAreaFilled(false);
            sw.setBorderPainted(false);
            sw.setFocusPainted(false);
            sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sw.setToolTipText(nombre);
            sw.addActionListener(e -> {
                if (jugador == HUMANO) {
                    colorHumano = c;
                    btnColorHumano.setBackground(c);
                    btnColorHumano.repaint();
                    if (turnoHumano && !finalizado) {
                        lblEstado.setForeground(c);
                    }
                } else {
                    colorComp = c;
                    btnColorComp.setBackground(c);
                    btnColorComp.repaint();
                    if (!turnoHumano && !finalizado) {
                        lblEstado.setForeground(c);
                    }
                }
                panel.repaint();
                dial.dispose();
            });
            grid.add(sw);
        }

        dial.add(lbl,  BorderLayout.NORTH);
        dial.add(grid, BorderLayout.CENTER);

        dial.pack();
        dial.setResizable(false);
        dial.setLocationRelativeTo(frame);
        dial.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  Lógica de juego (solo ejecutada en el EDT)
    // ══════════════════════════════════════════════════════════════
    void clic(int col) {
        if (!turnoHumano || finalizado) return;

        int fila = filaDisponible(col);
        if (fila < 0) {
            lblEstado.setText("¡Columna llena! Elige otra.");
            lblEstado.setForeground(Color.ORANGE);
            return;
        }

        mover(fila, col, HUMANO);

        int[][] win = buscarGanador(HUMANO);
        if (win != null) { terminar(win, "¡Jugador gana! 🎉", colorHumano, HUMANO); return; }
        if (esEmpate())  { terminar(null, "¡Empate!", Color.ORANGE, VACIO);          return; }

        turnoHumano = false;
        habilitarBtns(false);
        lblEstado.setText("Computadora pensando…");
        lblEstado.setForeground(colorComp);

        // copia inmutable → la IA trabaja sobre ella, nunca toca this.tablero
        final int[][] snapshot = copiarTablero();
        final int profSnapshot = profundidadIA;

        new SwingWorker<Integer, Void>() {
            protected Integer doInBackground() {
                return mejorMovimiento(snapshot, profSnapshot);
            }
            protected void done() {
                try {
                    int c = get();
                    int f = filaDisponible(c);
                    mover(f, c, COMPUTADORA);
                    int[][] w2 = buscarGanador(COMPUTADORA);
                    if (w2 != null) { terminar(w2, "¡Computadora gana!", colorComp, COMPUTADORA); return; }
                    if (esEmpate()) { terminar(null, "¡Empate!", Color.ORANGE, VACIO);             return; }
                    turnoHumano = true;
                    habilitarBtns(true);
                    lblEstado.setText("Tu turno  ●");
                    lblEstado.setForeground(colorHumano);
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    void terminar(int[][] win, String msg, Color color, int ganador) {
        ganadores  = win;
        finalizado = true;
        habilitarBtns(false);
        lblEstado.setText(msg);
        lblEstado.setForeground(color);
        if      (ganador == HUMANO)      scoreHum++;
        else if (ganador == COMPUTADORA) scoreComp++;
        else                             scoreEmp++;
        actualizarScore();
        panel.repaint();
    }

    void actualizarScore() {
        lblScore.setText(String.format(
            "Jugador  %d   ·   Comp  %d   ·   Empates  %d",
            scoreHum, scoreComp, scoreEmp));
    }

    void habilitarBtns(boolean on) {
        for (JButton b : btnsCols) b.setEnabled(on);
    }

    void mover(int fila, int col, int jugador) {
        tablero[fila][col] = jugador;
        panel.repaint();
    }

    void reiniciar() {
        inicializar();
        habilitarBtns(true);
        colHover = -1;
        lblEstado.setText("Tu turno  ●");
        lblEstado.setForeground(colorHumano);
        panel.repaint();
    }

    // ══════════════════════════════════════════════════════════════
    //  IA — Negamax + Poda Alfa-Beta
    //  Todas las funciones reciben int[][] b → nunca tocan this.tablero
    // ══════════════════════════════════════════════════════════════

    int mejorMovimiento(int[][] b, int prof) {
        // victoria inmediata
        for (int col : ORDEN) {
            int f = filaDisponible(b, col); if (f < 0) continue;
            b[f][col] = COMPUTADORA;
            boolean gana = verificarGanador(b, COMPUTADORA);
            b[f][col] = VACIO;
            if (gana) return col;
        }
        // bloqueo inmediato de derrota
        for (int col : ORDEN) {
            int f = filaDisponible(b, col); if (f < 0) continue;
            b[f][col] = HUMANO;
            boolean pierde = verificarGanador(b, HUMANO);
            b[f][col] = VACIO;
            if (pierde) return col;
        }
        // búsqueda completa
        int mejorCol = ORDEN[0], mejorVal = Integer.MIN_VALUE / 2;
        for (int col : ORDEN) {
            int f = filaDisponible(b, col); if (f < 0) continue;
            b[f][col] = COMPUTADORA;
            int val = -negamax(b, prof, Integer.MIN_VALUE/2, Integer.MAX_VALUE/2, HUMANO);
            b[f][col] = VACIO;
            if (val > mejorVal) { mejorVal = val; mejorCol = col; }
        }
        return mejorCol;
    }

    int negamax(int[][] b, int prof, int alfa, int beta, int jugActual) {
        int oponente = (jugActual == COMPUTADORA) ? HUMANO : COMPUTADORA;

        if (verificarGanador(b, oponente)) return -(10000 + prof);
        if (esEmpate(b) || prof == 0) {
            int eval = evaluarTablero(b);
            return (jugActual == COMPUTADORA) ? eval : -eval;
        }

        int maxVal = Integer.MIN_VALUE / 2;
        for (int col : ORDEN) {
            int f = filaDisponible(b, col); if (f < 0) continue;
            b[f][col] = jugActual;
            int val = -negamax(b, prof-1, -beta, -alfa, oponente);
            b[f][col] = VACIO;
            if (val > maxVal) maxVal = val;
            if (val > alfa)   alfa   = val;
            if (alfa >= beta) break;  // poda
        }
        return maxVal;
    }

    int evaluarTablero(int[][] b) {
        return contarLineas(b, COMPUTADORA) - contarLineas(b, HUMANO);
    }

    int contarLineas(int[][] b, int jugador) {
        int score = 0;
        for (int i = 0; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                score += puntuarVentana(b, i, j, 0,  1, jugador);
        for (int j = 0; j < COLUMNAS; j++)
            for (int i = 0; i <= FILAS-4; i++)
                score += puntuarVentana(b, i, j, 1,  0, jugador);
        for (int i = 3; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                score += puntuarVentana(b, i, j, -1, 1, jugador);
        for (int i = 0; i <= FILAS-4; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                score += puntuarVentana(b, i, j, 1,  1, jugador);
        return score;
    }

    int puntuarVentana(int[][] b, int fi, int fj, int di, int dj, int jugador) {
        int mias = 0, vacias = 0;
        for (int k = 0; k < 4; k++) {
            int v = b[fi + k*di][fj + k*dj];
            if      (v == jugador) mias++;
            else if (v == VACIO)   vacias++;
            else return 0;
        }
        if (mias == 4) return 10000;
        if (mias == 3 && vacias == 1) return 50;
        if (mias == 2 && vacias == 2) return  4;
        return 0;
    }

    // ── utilidades sobre la copia b ──────────────────────────────

    int filaDisponible(int[][] b, int col) {
        for (int i = FILAS-1; i >= 0; i--)
            if (b[i][col] == VACIO) return i;
        return -1;
    }

    boolean esEmpate(int[][] b) {
        for (int j = 0; j < COLUMNAS; j++)
            if (b[0][j] == VACIO) return false;
        return true;
    }

    boolean verificarGanador(int[][] b, int jugador) {
        for (int i = 0; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (b[i][j]==jugador && b[i][j+1]==jugador
                    && b[i][j+2]==jugador && b[i][j+3]==jugador) return true;
        for (int j = 0; j < COLUMNAS; j++)
            for (int i = 0; i <= FILAS-4; i++)
                if (b[i][j]==jugador && b[i+1][j]==jugador
                    && b[i+2][j]==jugador && b[i+3][j]==jugador) return true;
        for (int i = 3; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (b[i][j]==jugador && b[i-1][j+1]==jugador
                    && b[i-2][j+2]==jugador && b[i-3][j+3]==jugador) return true;
        for (int i = 0; i <= FILAS-4; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (b[i][j]==jugador && b[i+1][j+1]==jugador
                    && b[i+2][j+2]==jugador && b[i+3][j+3]==jugador) return true;
        return false;
    }

    // ── utilidades sobre this.tablero (solo EDT) ─────────────────

    int filaDisponible(int col) {
        for (int i = FILAS-1; i >= 0; i--)
            if (tablero[i][col] == VACIO) return i;
        return -1;
    }

    boolean esEmpate() {
        for (int j = 0; j < COLUMNAS; j++)
            if (tablero[0][j] == VACIO) return false;
        return true;
    }

    int[][] buscarGanador(int jugador) {
        for (int i = 0; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (tablero[i][j]==jugador && tablero[i][j+1]==jugador
                    && tablero[i][j+2]==jugador && tablero[i][j+3]==jugador)
                    return new int[][]{{i,j},{i,j+1},{i,j+2},{i,j+3}};
        for (int j = 0; j < COLUMNAS; j++)
            for (int i = 0; i <= FILAS-4; i++)
                if (tablero[i][j]==jugador && tablero[i+1][j]==jugador
                    && tablero[i+2][j]==jugador && tablero[i+3][j]==jugador)
                    return new int[][]{{i,j},{i+1,j},{i+2,j},{i+3,j}};
        for (int i = 3; i < FILAS; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (tablero[i][j]==jugador && tablero[i-1][j+1]==jugador
                    && tablero[i-2][j+2]==jugador && tablero[i-3][j+3]==jugador)
                    return new int[][]{{i,j},{i-1,j+1},{i-2,j+2},{i-3,j+3}};
        for (int i = 0; i <= FILAS-4; i++)
            for (int j = 0; j <= COLUMNAS-4; j++)
                if (tablero[i][j]==jugador && tablero[i+1][j+1]==jugador
                    && tablero[i+2][j+2]==jugador && tablero[i+3][j+3]==jugador)
                    return new int[][]{{i,j},{i+1,j+1},{i+2,j+2},{i+3,j+3}};
        return null;
    }

    int[][] copiarTablero() {
        int[][] copia = new int[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++) copia[i] = tablero[i].clone();
        return copia;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Conecta4_Negamax::new);
    }
}
