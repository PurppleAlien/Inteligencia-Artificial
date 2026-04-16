import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Buscaminas — versión mejorada.
 *
 * PROBLEMAS CRÍTICOS DEL ORIGINAL Y SUS CORRECCIONES:
 *
 * 1. STACK OVERFLOW (CRÍTICO)
 *    Original: expandirZona() era recursiva. En un tablero 16×30 con zona
 *    vacía grande, la pila de recursión podía superar 400 llamadas → StackOverflowError.
 *    Fix: revelarZona() usa una Deque (pila iterativa) — profundidad de pila = O(1).
 *
 * 2. SIN BANDERAS (funcionalidad esencial ausente)
 *    Original: no existía el clic derecho para marcar minas.
 *    Fix: clic derecho cicla OCULTO → BANDERA(🚩) → INTERROGANTE(?) → OCULTO.
 *
 * 3. SIN TEMPORIZADOR
 *    Original: no había cronómetro.
 *    Fix: javax.swing.Timer cuenta los segundos desde el primer clic.
 *
 * 4. SIN CONTADOR DE MINAS
 *    Original: no se mostraba cuántas minas quedan sin marcar.
 *    Fix: contador = minas_total - banderas_colocadas.
 *
 * 5. ACOPLAMIENTO MODELO-VISTA (violación de SRP)
 *    Original: el array JButton[][] actuaba simultáneamente como modelo y vista.
 *    Fix: modelo separado (arrays tableroLogico, estado[][]) + vista como
 *    JComponent con paintComponent() personalizado.
 *
 * 6. REINICIO AUTOMÁTICO (UX molesta)
 *    Original: JOptionPane + reinicio inmediato sin dar tiempo a ver el tablero.
 *    Fix: overlay sobre el tablero con mensaje de victoria/derrota. Nuevo juego
 *    solo al pulsar el botón o F2.
 *
 * 7. ZONA SEGURA EN PRIMER CLIC (3×3)
 *    Original: zona segura de solo 1 celda.
 *    Fix: ninguna mina puede estar en el 3×3 alrededor del primer clic.
 *
 * 8. CHORD REVEAL (función avanzada)
 *    Fix: doble clic sobre número revela vecinos si los flags coinciden.
 */
public class Buscaminas_BackTrack extends JFrame {

    // ── Estado de cada celda ──────────────────────────────────────────────────

    private static final int OCULTO      = 0;
    private static final int BANDERA     = 1;
    private static final int INTERROGANTE= 2;
    // estado REVELADO se deduce de revelado[][]

    // ── Modelo del juego ──────────────────────────────────────────────────────

    private int filas, columnas, totalMinas;
    private int[][] tableroLogico; // -1=mina, 0-8=adyacentes
    private int[][] estadoCelda;   // OCULTO / BANDERA / INTERROGANTE
    private boolean[][] revelado;
    private boolean primerClic;
    private boolean juegoTerminado;
    private boolean victoria;
    private int celdasReveladas;
    private int banderasColocadas;
    private int segundos;
    private int celdaExplotada_f = -1, celdaExplotada_c = -1; // para marcar la mina clickada

    // ── UI ────────────────────────────────────────────────────────────────────

    private TableroCanvas  canvas;
    private JLabel         lblMinas, lblTimer;
    private JButton        btnNuevo;
    private javax.swing.Timer reloj;

    // ── Paleta ───────────────────────────────────────────────────────────────

    private static final Color C_BASE      = new Color(30,  30,  46);
    private static final Color C_OCULTO    = new Color(69,  71,  90);
    private static final Color C_REVELADO  = new Color(42,  43,  58);
    private static final Color C_BORDE     = new Color(17,  17,  27);
    private static final Color C_EXPLOTA   = new Color(243, 139, 168, 160);
    private static final Color C_HIGHLIGHT = new Color(88,  91, 112);
    private static final Color[] NUM_COLOR = {
            null,
            new Color(137, 180, 250),  // 1 azul
            new Color(166, 227, 161),  // 2 verde
            new Color(243, 139, 168),  // 3 rojo
            new Color(180, 190, 254),  // 4 lavanda
            new Color(250, 179, 135),  // 5 naranja
            new Color(148, 226, 213),  // 6 teal
            new Color(205, 214, 244),  // 7 blanco
            new Color(108, 112, 134),  // 8 gris
    };

    private int celdaPx = 34;

    // ── Constructor ──────────────────────────────────────────────────────────

    public Buscaminas_BackTrack() {
        super("BUSCAMINAS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BASE);

        buildUI();
        crearMenu();
        configurarJuego(9, 9, 10);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Barra superior ──
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(new Color(24, 24, 37));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(49, 50, 68)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        lblMinas = new JLabel("💣 10");
        lblMinas.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMinas.setForeground(new Color(243, 139, 168));

        lblTimer = new JLabel("⏱ 000");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTimer.setForeground(new Color(148, 226, 213));

        btnNuevo = boton("Nuevo juego");
        btnNuevo.addActionListener(e -> iniciarJuego());

        topBar.add(lblMinas, BorderLayout.WEST);
        topBar.add(btnNuevo, BorderLayout.CENTER);
        topBar.add(lblTimer, BorderLayout.EAST);

        // ── Barra de dificultad ──
        JPanel baraDif = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        baraDif.setBackground(new Color(24, 24, 37));
        baraDif.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(49, 50, 68)));

        JButton bFacil    = botonDif("Fácil",       "9 × 9  •  10 minas",  new Color(166, 227, 161));
        JButton bMedio    = botonDif("Intermedio",   "16 × 16  •  40 minas",new Color(137, 180, 250));
        JButton bDificil  = botonDif("Difícil",      "22 × 30  •  150 minas",new Color(243, 139, 168));
        JButton bPersonal = botonDif("Personalizado","Opciones…",            new Color(147, 153, 178));

        bFacil  .addActionListener(e -> configurarJuego(9,  9,  10));
        bMedio  .addActionListener(e -> configurarJuego(16, 16, 40));
        bDificil.addActionListener(e -> configurarJuego(22, 30, 150));
        bPersonal.addActionListener(e -> abrirOpciones());

        baraDif.add(bFacil);
        baraDif.add(bMedio);
        baraDif.add(bDificil);
        baraDif.add(bPersonal);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(topBar,  BorderLayout.NORTH);
        norte.add(baraDif, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ── Área del tablero ──
        JPanel areaTablero = new JPanel(new GridBagLayout());
        areaTablero.setBackground(C_BASE);
        canvas = new TableroCanvas();
        areaTablero.add(canvas);
        add(areaTablero, BorderLayout.CENTER);

        // ── Reloj ──
        reloj = new javax.swing.Timer(1000, e -> {
            if (!juegoTerminado && !primerClic) {
                segundos = Math.min(segundos + 1, 999);
                lblTimer.setText("⏱ " + String.format("%03d", segundos));
            }
        });
    }

    private void crearMenu() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(24, 24, 37));

        JMenu menu = new JMenu("Juego");
        menu.setForeground(new Color(205, 214, 244));
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem mNuevo    = item("Nuevo juego    F2");
        JMenuItem mOpciones = item("Opciones        F3");
        JMenuItem mSalir    = item("Salir");

        mNuevo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        mOpciones.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        mNuevo   .addActionListener(e -> iniciarJuego());
        mOpciones.addActionListener(e -> abrirOpciones());
        mSalir   .addActionListener(e -> System.exit(0));

        menu.add(mNuevo); menu.add(mOpciones);
        menu.addSeparator(); menu.add(mSalir);
        bar.add(menu);
        setJMenuBar(bar);
    }

    // ── Configuración y nueva partida ─────────────────────────────────────────

    /** Calcula el tamaño de celda en px para que el tablero quepa en ~900×700. */
    private void calcularCeldaPx() {
        // Reservar ~50px para la barra superior y ~60px para la barra de dificultad
        int maxW = 900, maxH = 640;
        int byCols = maxW / columnas;
        int byRows = maxH / filas;
        celdaPx = Math.max(18, Math.min(40, Math.min(byCols, byRows)));
    }

    public void configurarJuego(int f, int c, int m) {
        filas = f; columnas = c; totalMinas = Math.min(m, f * c - 9);
        calcularCeldaPx();
        iniciarJuego();
    }

    public void iniciarJuego() {
        reloj.stop();
        primerClic     = true;
        juegoTerminado = false;
        victoria       = false;
        celdasReveladas   = 0;
        banderasColocadas = 0;
        segundos          = 0;
        celdaExplotada_f  = -1;
        celdaExplotada_c  = -1;

        tableroLogico = new int[filas][columnas];
        estadoCelda   = new int[filas][columnas];
        revelado      = new boolean[filas][columnas];

        lblMinas.setText("💣 " + totalMinas);
        lblTimer.setText("⏱ 000");

        canvas.setPreferredSize(new Dimension(columnas * celdaPx, filas * celdaPx));
        canvas.repaint();
        pack();
        setLocationRelativeTo(null);
    }

    // ── Colocación de minas y cálculo de adyacentes ───────────────────────────

    private void colocarMinas(int sfila, int scol) {
        Set<Integer> zonaSegura = new HashSet<>();
        for (int df = -1; df <= 1; df++)
            for (int dc = -1; dc <= 1; dc++) {
                int nf = sfila + df, nc = scol + dc;
                if (enRango(nf, nc)) zonaSegura.add(nf * columnas + nc);
            }

        Random rng = new Random();
        int colocadas = 0;
        while (colocadas < totalMinas) {
            int f = rng.nextInt(filas), c = rng.nextInt(columnas);
            if (tableroLogico[f][c] != -1 && !zonaSegura.contains(f * columnas + c)) {
                tableroLogico[f][c] = -1;
                colocadas++;
            }
        }

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (tableroLogico[f][c] == -1) continue;
                int cnt = 0;
                for (int[] v : vecinos(f, c))
                    if (tableroLogico[v[0]][v[1]] == -1) cnt++;
                tableroLogico[f][c] = cnt;
            }
        }
    }

    // ── Acciones del jugador ──────────────────────────────────────────────────

    private void clicIzquierdo(int f, int c) {
        if (juegoTerminado || revelado[f][c]
                || estadoCelda[f][c] == BANDERA) return;

        if (primerClic) {
            primerClic = false;
            colocarMinas(f, c);
            reloj.start();
        }

        if (tableroLogico[f][c] == -1) {
            // Mina: revelar todo
            celdaExplotada_f = f;
            celdaExplotada_c = c;
            revelado[f][c] = true;
            revelarTodasLasMinas();
            terminarJuego(false);
        } else {
            revelarZona(f, c);
            verificarVictoria();
        }
        canvas.repaint();
    }

    private void clicDerecho(int f, int c) {
        if (juegoTerminado || revelado[f][c]) return;
        switch (estadoCelda[f][c]) {
            case OCULTO      -> { estadoCelda[f][c] = BANDERA;      banderasColocadas++; }
            case BANDERA     -> { estadoCelda[f][c] = INTERROGANTE; banderasColocadas--; }
            case INTERROGANTE-> { estadoCelda[f][c] = OCULTO; }
        }
        lblMinas.setText("💣 " + (totalMinas - banderasColocadas));
        canvas.repaint();
    }

    /** Chord: si el número ya tiene todos sus vecinos marcados con bandera,
     *  revela el resto. Puede explotar si hay una bandera mal colocada. */
    private void dobleClic(int f, int c) {
        if (!revelado[f][c] || tableroLogico[f][c] <= 0) return;
        long flags = 0;
        for (int[] v : vecinos(f, c))
            if (estadoCelda[v[0]][v[1]] == BANDERA) flags++;
        if (flags == tableroLogico[f][c]) {
            for (int[] v : vecinos(f, c)) {
                if (!revelado[v[0]][v[1]] && estadoCelda[v[0]][v[1]] != BANDERA) {
                    clicIzquierdo(v[0], v[1]);
                    if (juegoTerminado) break;
                }
            }
        }
    }

    // ── Expansión iterativa (FIX: elimina riesgo de StackOverflow) ────────────

    private void revelarZona(int startF, int startC) {
        Deque<int[]> pila = new ArrayDeque<>();
        pila.push(new int[]{startF, startC});

        while (!pila.isEmpty()) {
            int[] pos = pila.pop();
            int f = pos[0], c = pos[1];
            if (revelado[f][c] || estadoCelda[f][c] == BANDERA) continue;

            revelado[f][c] = true;
            celdasReveladas++;

            if (tableroLogico[f][c] == 0) {
                for (int[] v : vecinos(f, c)) {
                    if (!revelado[v[0]][v[1]]) pila.push(v);
                }
            }
        }
    }

    private void revelarTodasLasMinas() {
        for (int f = 0; f < filas; f++)
            for (int c = 0; c < columnas; c++)
                if (tableroLogico[f][c] == -1 && estadoCelda[f][c] != BANDERA)
                    revelado[f][c] = true;
    }

    private void verificarVictoria() {
        int noMinas = filas * columnas - totalMinas;
        if (celdasReveladas >= noMinas) {
            // Marcar automáticamente todas las minas restantes
            for (int f = 0; f < filas; f++)
                for (int c = 0; c < columnas; c++)
                    if (tableroLogico[f][c] == -1 && !revelado[f][c]) {
                        estadoCelda[f][c] = BANDERA;
                        banderasColocadas++;
                    }
            lblMinas.setText("💣 0");
            terminarJuego(true);
        }
    }

    private void terminarJuego(boolean gano) {
        juegoTerminado = true;
        victoria       = gano;
        reloj.stop();
        canvas.repaint();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private java.util.List<int[]> vecinos(int f, int c) {
        java.util.List<int[]> lista = new ArrayList<>(8);
        for (int df = -1; df <= 1; df++)
            for (int dc = -1; dc <= 1; dc++)
                if ((df != 0 || dc != 0) && enRango(f + df, c + dc))
                    lista.add(new int[]{f + df, c + dc});
        return lista;
    }

    private boolean enRango(int f, int c) {
        return f >= 0 && f < filas && c >= 0 && c < columnas;
    }

    public void abrirOpciones() {
        new Opciones(this).setVisible(true);
    }

    // ── Canvas del tablero ────────────────────────────────────────────────────

    private class TableroCanvas extends JComponent {

        private int hoverF = -1, hoverC = -1;

        TableroCanvas() {
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    int f = e.getY() / celdaPx, c = e.getX() / celdaPx;
                    if (!enRango(f, c)) return;
                    if (SwingUtilities.isLeftMouseButton(e))
                        clicIzquierdo(f, c);
                    else if (SwingUtilities.isRightMouseButton(e))
                        clicDerecho(f, c);
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        int f = e.getY() / celdaPx, c = e.getX() / celdaPx;
                        if (enRango(f, c)) dobleClic(f, c);
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int f = e.getY() / celdaPx, c = e.getX() / celdaPx;
                    if (hoverF != f || hoverC != c) {
                        hoverF = f; hoverC = c; repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    hoverF = -1; hoverC = -1; repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int f = 0; f < filas; f++)
                for (int c = 0; c < columnas; c++)
                    pintarCelda(g2, f, c);

            if (juegoTerminado) pintarOverlay(g2);
            g2.dispose();
        }

        private void pintarCelda(Graphics2D g2, int f, int c) {
            int x = c * celdaPx, y = f * celdaPx;
            boolean hover = (f == hoverF && c == hoverC) && !juegoTerminado;

            if (revelado[f][c]) {
                // Celda revelada
                boolean esExplosion = (f == celdaExplotada_f && c == celdaExplotada_c);
                Color bg = esExplosion ? C_EXPLOTA
                         : ((f + c) % 2 == 0) ? C_REVELADO
                         : new Color(38, 39, 53);
                g2.setColor(bg);
                g2.fillRect(x, y, celdaPx, celdaPx);

                if (tableroLogico[f][c] == -1) {
                    dibujarCentrado(g2, "💣", x, y, celdaPx, 18,
                            new Color(205, 214, 244));
                } else if (tableroLogico[f][c] > 0) {
                    g2.setColor(NUM_COLOR[tableroLogico[f][c]]);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    String num = String.valueOf(tableroLogico[f][c]);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(num,
                            x + (celdaPx - fm.stringWidth(num)) / 2,
                            y + (celdaPx + fm.getAscent() - fm.getDescent()) / 2);
                }
            } else {
                // Celda oculta — efecto 3D sutil
                Color base = hover ? C_HIGHLIGHT : C_OCULTO;
                g2.setColor(base);
                g2.fillRect(x, y, celdaPx, celdaPx);

                // Bisel superior-izquierdo (luz)
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRect(x, y, celdaPx, 2);
                g2.fillRect(x, y, 2, celdaPx);
                // Bisel inferior-derecho (sombra)
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(x + celdaPx - 2, y, 2, celdaPx);
                g2.fillRect(x, y + celdaPx - 2, celdaPx, 2);

                if (estadoCelda[f][c] == BANDERA) {
                    dibujarCentrado(g2, "🚩", x, y, celdaPx, 16,
                            new Color(243, 139, 168));
                } else if (estadoCelda[f][c] == INTERROGANTE) {
                    g2.setColor(new Color(249, 226, 175));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("?",
                            x + (celdaPx - fm.stringWidth("?")) / 2,
                            y + (celdaPx + fm.getAscent() - fm.getDescent()) / 2);
                }
            }

            // Borde de celda
            g2.setColor(C_BORDE);
            g2.drawRect(x, y, celdaPx, celdaPx);
        }

        private void dibujarCentrado(Graphics2D g2, String txt, int cx, int cy,
                int sz, int fs, Color color) {
            g2.setColor(color);
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, fs));
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx + (sz - fm.stringWidth(txt)) / 2;
            int ty = cy + (sz + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(txt, tx, ty);
        }

        private void pintarOverlay(Graphics2D g2) {
            String msg = victoria ? "¡GANASTE! 🎉" : "PERDISTE 💥";
            Color  col = victoria ? new Color(166, 227, 161) : new Color(243, 139, 168);
            String sub = victoria
                    ? "Tiempo: " + segundos + "s  |  Pulsa F2 para jugar de nuevo"
                    : "Pulsa F2 para intentarlo de nuevo";

            g2.setColor(new Color(17, 17, 27, 190));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(msg);
            int tx = (getWidth() - tw) / 2;
            int ty = getHeight() / 2;

            g2.setColor(new Color(49, 50, 68));
            g2.fillRoundRect(tx - 24, ty - 40, tw + 48, 58, 14, 14);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(tx - 24, ty - 40, tw + 48, 58, 14, 14);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(col);
            g2.drawString(msg, tx, ty);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(147, 153, 178));
            FontMetrics fms = g2.getFontMetrics();
            g2.drawString(sub, (getWidth() - fms.stringWidth(sub)) / 2, ty + 24);
        }
    }

    // ── Utilidades de UI ─────────────────────────────────────────────────────

    private JButton botonDif(String titulo, String subtitulo, Color accent) {
        JButton btn = new JButton("<html><center><b>" + titulo + "</b><br>"
                + "<span style='font-size:9px;color:#888'>" + subtitulo + "</span></center></html>");
        btn.setBackground(new Color(38, 39, 53));
        btn.setForeground(accent);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(69, 71, 90)),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton boton(String txt) {
        JButton btn = new JButton(txt);
        btn.setBackground(new Color(49, 50, 68));
        btn.setForeground(new Color(166, 227, 161));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(69, 71, 90)),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JMenuItem item(String txt) {
        JMenuItem mi = new JMenuItem(txt);
        mi.setBackground(new Color(49, 50, 68));
        mi.setForeground(new Color(205, 214, 244));
        mi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return mi;
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        UIManager.put("Panel.background",       new Color(30,  30,  46));
        UIManager.put("MenuBar.background",     new Color(24,  24,  37));
        UIManager.put("Menu.background",        new Color(24,  24,  37));
        UIManager.put("Menu.foreground",        new Color(205, 214, 244));
        UIManager.put("MenuItem.background",    new Color(49,  50,  68));
        UIManager.put("MenuItem.foreground",    new Color(205, 214, 244));
        UIManager.put("OptionPane.background",  new Color(49,  50,  68));
        UIManager.put("OptionPane.messageForeground", new Color(205, 214, 244));
        UIManager.put("Button.background",      new Color(49,  50,  68));
        UIManager.put("Button.foreground",      new Color(205, 214, 244));
        SwingUtilities.invokeLater(Buscaminas_BackTrack::new);
    }
}
