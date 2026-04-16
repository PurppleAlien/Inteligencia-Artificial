import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaz gráfica para el juego Misioneros y Caníbales.
 *
 * Estado: [mIzq, cIzq, bote, mDer, cDer]
 *   índice 0 = misioneros orilla izquierda
 *   índice 1 = caníbales  orilla izquierda
 *   índice 2 = posición del bote  (0=izq, 1=der)
 *   índice 3 = misioneros orilla derecha
 *   índice 4 = caníbales  orilla derecha
 *
 * Reutiliza CyM_SG_BFS y Nodo_BFS para la resolución automática.
 */
public class InterfazCyM extends JFrame {

    // ---------------------------------------------------------------
    // Estado del juego
    // ---------------------------------------------------------------
    /** Estado actual: [mIzq, cIzq, bote, mDer, cDer] */
    private int[] estadoActual;

    // ---------------------------------------------------------------
    // Componentes de la UI — orilla izquierda
    // ---------------------------------------------------------------
    private JLabel lblMIzq;
    private JLabel lblCIzq;
    private JLabel lblBoteIzq;

    // ---------------------------------------------------------------
    // Componentes de la UI — orilla derecha
    // ---------------------------------------------------------------
    private JLabel lblMDer;
    private JLabel lblCDer;
    private JLabel lblBoteDer;

    // ---------------------------------------------------------------
    // Componentes de la UI — estado textual y panel de dibujo
    // ---------------------------------------------------------------
    private JLabel lblEstado;
    private JLabel lblMensaje;
    private PanelRio panelRio;

    // ---------------------------------------------------------------
    // Botones de movimiento y control
    // ---------------------------------------------------------------
    private JButton btn1M;
    private JButton btn2M;
    private JButton btn1C;
    private JButton btn2C;
    private JButton btn1M1C;
    private JButton btnReiniciar;
    private JButton btnResolver;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public InterfazCyM() {
        super("Misioneros y Caníbales");
        estadoActual = new int[]{3, 3, 0, 0, 0};  // estado inicial
        inicializarUI();
        actualizarUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // ---------------------------------------------------------------
    // Construcción de la interfaz
    // ---------------------------------------------------------------

    /** Construye y organiza todos los paneles en el JFrame. */
    private void inicializarUI() {
        setLayout(new BorderLayout(8, 8));

        // Panel superior — estado textual
        JPanel panelTop = crearPanelEstado();
        add(panelTop, BorderLayout.NORTH);

        // Panel central — río + orillas
        JPanel panelCentral = new JPanel(new BorderLayout(8, 8));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel panelIzq = crearPanelOrilla("Orilla Izquierda", true);
        JPanel panelDer = crearPanelOrilla("Orilla Derecha",   false);

        panelRio = new PanelRio();
        panelRio.setPreferredSize(new Dimension(280, 260));

        panelCentral.add(panelIzq,  BorderLayout.WEST);
        panelCentral.add(panelRio,  BorderLayout.CENTER);
        panelCentral.add(panelDer,  BorderLayout.EAST);

        add(panelCentral, BorderLayout.CENTER);

        // Panel inferior — botones
        JPanel panelBotones = crearPanelBotones();
        add(panelBotones, BorderLayout.SOUTH);
    }

    /** Panel con la etiqueta de estado y mensajes. */
    private JPanel crearPanelEstado() {
        JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 2, 8));

        lblEstado = new JLabel("", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Monospaced", Font.BOLD, 14));

        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblMensaje.setForeground(Color.DARK_GRAY);

        p.add(lblEstado);
        p.add(lblMensaje);
        return p;
    }

    /**
     * Panel de una orilla.
     * @param titulo  título visible
     * @param esIzq   true → orilla izquierda, false → derecha
     */
    private JPanel crearPanelOrilla(String titulo, boolean esIzq) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder(titulo));
        p.setPreferredSize(new Dimension(170, 240));
        p.setBackground(new Color(245, 222, 179));   // arena

        JLabel lblIconoM = new JLabel("Misioneros:");
        JLabel lblIconoC = new JLabel("Caníbales:");
        JLabel lblIconoBote = new JLabel("Bote:");

        Font fBig = new Font("SansSerif", Font.BOLD, 16);

        if (esIzq) {
            lblMIzq   = new JLabel("3");
            lblCIzq   = new JLabel("3");
            lblBoteIzq = new JLabel("⛵");
            lblMIzq.setFont(fBig);
            lblCIzq.setFont(fBig);
            lblBoteIzq.setFont(new Font("SansSerif", Font.PLAIN, 28));

            agregarFila(p, lblIconoM,    lblMIzq);
            agregarFila(p, lblIconoC,    lblCIzq);
            agregarFila(p, lblIconoBote, lblBoteIzq);
        } else {
            lblMDer   = new JLabel("0");
            lblCDer   = new JLabel("0");
            lblBoteDer = new JLabel("");
            lblMDer.setFont(fBig);
            lblCDer.setFont(fBig);
            lblBoteDer.setFont(new Font("SansSerif", Font.PLAIN, 28));

            agregarFila(p, lblIconoM,    lblMDer);
            agregarFila(p, lblIconoC,    lblCDer);
            agregarFila(p, lblIconoBote, lblBoteDer);
        }

        return p;
    }

    /** Agrega una fila etiqueta-valor centrada en un panel BoxLayout vertical. */
    private void agregarFila(JPanel panel, JLabel etiqueta, JLabel valor) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        fila.setOpaque(false);
        fila.add(etiqueta);
        fila.add(valor);
        panel.add(fila);
    }

    /** Panel inferior con todos los botones de acción. */
    private JPanel crearPanelBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        p.setBorder(BorderFactory.createTitledBorder("Controles — elige qué llevar en el bote"));

        btn1M   = new JButton("1 Misionero");
        btn2M   = new JButton("2 Misioneros");
        btn1C   = new JButton("1 Caníbal");
        btn2C   = new JButton("2 Caníbales");
        btn1M1C = new JButton("1M + 1C");
        btnReiniciar = new JButton("Reiniciar");
        btnResolver  = new JButton("Resolver (BFS)");

        // Colores para distinguir los botones
        btn1M.setBackground(new Color(135, 206, 250));
        btn2M.setBackground(new Color(100, 180, 240));
        btn1C.setBackground(new Color(255, 160, 122));
        btn2C.setBackground(new Color(240, 110,  80));
        btn1M1C.setBackground(new Color(144, 238, 144));
        btnReiniciar.setBackground(new Color(255, 215, 0));
        btnResolver.setBackground(new Color(186, 85, 211));
        btnResolver.setForeground(Color.WHITE);

        // Listeners — cada botón define el movimiento (misioneros, caníbales)
        btn1M.addActionListener(e   -> intentarMovimiento(1, 0));
        btn2M.addActionListener(e   -> intentarMovimiento(2, 0));
        btn1C.addActionListener(e   -> intentarMovimiento(0, 1));
        btn2C.addActionListener(e   -> intentarMovimiento(0, 2));
        btn1M1C.addActionListener(e -> intentarMovimiento(1, 1));
        btnReiniciar.addActionListener(e -> reiniciar());
        btnResolver.addActionListener(e  -> resolverAutomaticamente());

        p.add(btn1M);
        p.add(btn2M);
        p.add(btn1C);
        p.add(btn2C);
        p.add(btn1M1C);
        p.add(Box.createHorizontalStrut(20));
        p.add(btnReiniciar);
        p.add(btnResolver);
        return p;
    }

    // ---------------------------------------------------------------
    // Lógica de movimiento (validación replicada de CyM_SG_BFS)
    // ---------------------------------------------------------------

    /**
     * Intenta aplicar un movimiento al estado actual.
     * El bote se mueve desde donde está hacia el otro lado.
     *
     * @param m  misioneros a transportar
     * @param c  caníbales  a transportar
     */
    private void intentarMovimiento(int m, int c) {
        int[] nuevoEstado = calcularNuevoEstado(estadoActual, m, c);

        if (nuevoEstado == null) {
            mostrarMensaje("Movimiento inválido: no hay suficientes personas en esa orilla.", Color.RED);
            return;
        }

        if (!esEstadoValido(nuevoEstado)) {
            mostrarMensaje("¡Movimiento peligroso! Los caníbales superarían a los misioneros.", Color.RED);
            return;
        }

        estadoActual = nuevoEstado;
        actualizarUI();

        if (esSolucion(estadoActual)) {
            mostrarMensaje("¡Felicitaciones! Todos cruzaron el río correctamente.", new Color(0, 128, 0));
            deshabilitarBotonesMovimiento();
            JOptionPane.showMessageDialog(this,
                    "¡Ganaste! Llevaste a todos los misioneros y caníbales al otro lado.",
                    "Victoria", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarMensaje("Movimiento aplicado. Turno actual.", Color.DARK_GRAY);
        }
    }

    /**
     * Calcula el nuevo estado al mover m misioneros y c caníbales.
     * Devuelve null si la cantidad a mover supera lo disponible.
     */
    private int[] calcularNuevoEstado(int[] estado, int m, int c) {
        int[] nuevo = new int[5];

        if (estado[2] == 0) {           // bote en izquierda → cruzar hacia derecha
            nuevo[0] = estado[0] - m;
            nuevo[1] = estado[1] - c;
            nuevo[2] = 1;
            nuevo[3] = estado[3] + m;
            nuevo[4] = estado[4] + c;
        } else {                         // bote en derecha   → cruzar hacia izquierda
            nuevo[0] = estado[0] + m;
            nuevo[1] = estado[1] + c;
            nuevo[2] = 0;
            nuevo[3] = estado[3] - m;
            nuevo[4] = estado[4] - c;
        }

        // Verificar que no haya negativos antes de seguir
        for (int v : nuevo) {
            if (v < 0) return null;
        }
        return nuevo;
    }

    /**
     * Valida un estado según las reglas del juego.
     * Réplica de CyM_SG_BFS.esEstadoValido() para uso interno de la UI.
     */
    private boolean esEstadoValido(int[] estado) {
        for (int v : estado) {
            if (v < 0) return false;
        }
        if (estado[0] > 3 || estado[1] > 3 || estado[3] > 3 || estado[4] > 3) {
            return false;
        }
        // Caníbales no pueden superar a misioneros cuando hay misioneros presentes
        if ((estado[0] > 0 && estado[0] < estado[1]) ||
            (estado[3] > 0 && estado[3] < estado[4])) {
            return false;
        }
        return true;
    }

    /** Comprueba si el estado es el estado meta: [0,0,1,3,3]. */
    private boolean esSolucion(int[] estado) {
        return estado[0] == 0 && estado[1] == 0 &&
               estado[2] == 1 && estado[3] == 3 && estado[4] == 3;
    }

    // ---------------------------------------------------------------
    // Resolución automática con BFS
    // ---------------------------------------------------------------

    /**
     * Usa CyM_SG_BFS para obtener la solución óptima y la anima
     * paso a paso con un Timer de Swing.
     */
    private void resolverAutomaticamente() {
        // Reiniciar primero para partir del estado inicial
        estadoActual = new int[]{3, 3, 0, 0, 0};
        actualizarUI();

        CyM_SG_BFS bfs = new CyM_SG_BFS();
        Nodo_BFS solucion = bfs.resolver();

        if (solucion == null) {
            JOptionPane.showMessageDialog(this,
                    "BFS no encontró solución.", "Sin solución",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reconstruir el camino desde el nodo solución hasta la raíz
        List<int[]> camino = new ArrayList<>();
        Nodo_BFS nodo = solucion;
        while (nodo != null) {
            camino.add(0, nodo.getEstado());
            nodo = nodo.getPadre();
        }

        deshabilitarBotonesMovimiento();
        btnResolver.setEnabled(false);

        // Animar cada paso con un Timer (700 ms entre pasos)
        int[] paso = {0};
        Timer timer = new Timer(700, null);
        timer.addActionListener(e -> {
            if (paso[0] < camino.size()) {
                estadoActual = camino.get(paso[0]).clone();
                actualizarUI();
                mostrarMensaje("Paso " + paso[0] + " / " + (camino.size() - 1), Color.BLUE);
                paso[0]++;
            } else {
                ((Timer) e.getSource()).stop();
                habilitarBotonesMovimiento();
                btnResolver.setEnabled(true);
                JOptionPane.showMessageDialog(InterfazCyM.this,
                        "¡Solución BFS completada en " + (camino.size() - 1) + " pasos!",
                        "Solución", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        timer.start();
    }

    // ---------------------------------------------------------------
    // Reinicio
    // ---------------------------------------------------------------

    /** Reinicia el juego al estado inicial. */
    private void reiniciar() {
        estadoActual = new int[]{3, 3, 0, 0, 0};
        habilitarBotonesMovimiento();
        btnResolver.setEnabled(true);
        actualizarUI();
        mostrarMensaje("Juego reiniciado. ¡Mueve el bote!", Color.DARK_GRAY);
    }

    // ---------------------------------------------------------------
    // Actualización de la UI
    // ---------------------------------------------------------------

    /**
     * Refresca todas las etiquetas, el panel de dibujo y la
     * cadena de estado textual con el valor de estadoActual.
     */
    private void actualizarUI() {
        // Etiquetas de orilla izquierda
        lblMIzq.setText(String.valueOf(estadoActual[0]));
        lblCIzq.setText(String.valueOf(estadoActual[1]));
        lblBoteIzq.setText(estadoActual[2] == 0 ? "⛵" : "");

        // Etiquetas de orilla derecha
        lblMDer.setText(String.valueOf(estadoActual[3]));
        lblCDer.setText(String.valueOf(estadoActual[4]));
        lblBoteDer.setText(estadoActual[2] == 1 ? "⛵" : "");

        // Estado textual
        String ladoBote = estadoActual[2] == 0 ? "Izquierda" : "Derecha";
        lblEstado.setText(
            "Izq: M=" + estadoActual[0] + " C=" + estadoActual[1] +
            "  |  Bote: " + ladoBote +
            "  |  Der: M=" + estadoActual[3] + " C=" + estadoActual[4]
        );

        // Redibujar el panel gráfico del río
        panelRio.setEstado(estadoActual);
        panelRio.repaint();
    }

    /** Muestra un mensaje de retroalimentación con el color indicado. */
    private void mostrarMensaje(String texto, Color color) {
        lblMensaje.setText(texto);
        lblMensaje.setForeground(color);
    }

    // ---------------------------------------------------------------
    // Helpers para habilitar/deshabilitar botones
    // ---------------------------------------------------------------

    private void deshabilitarBotonesMovimiento() {
        btn1M.setEnabled(false);
        btn2M.setEnabled(false);
        btn1C.setEnabled(false);
        btn2C.setEnabled(false);
        btn1M1C.setEnabled(false);
    }

    private void habilitarBotonesMovimiento() {
        btn1M.setEnabled(true);
        btn2M.setEnabled(true);
        btn1C.setEnabled(true);
        btn2C.setEnabled(true);
        btn1M1C.setEnabled(true);
    }

    // ---------------------------------------------------------------
    // Panel gráfico del río
    // ---------------------------------------------------------------

    /**
     * Componente personalizado que dibuja el río, el bote y los
     * personajes (misioneros = azul, caníbales = rojo) en ambas orillas.
     */
    private static class PanelRio extends JPanel {

        private int[] estado = {3, 3, 0, 0, 0};

        public void setEstado(int[] estado) {
            this.estado = estado.clone();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // ---- Fondo ----
            // Orilla izquierda
            g2.setColor(new Color(210, 180, 140));
            g2.fillRect(0, 0, w / 3, h);
            // Río
            g2.setColor(new Color(64, 164, 223));
            g2.fillRect(w / 3, 0, w / 3, h);
            // Orilla derecha
            g2.setColor(new Color(210, 180, 140));
            g2.fillRect(2 * w / 3, 0, w / 3, h);

            // Líneas divisorias
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(w / 3,     0, w / 3,     h);
            g2.drawLine(2 * w / 3, 0, 2 * w / 3, h);

            // ---- Bote ----
            int boteX;
            if (estado[2] == 0) {
                boteX = w / 3 - 30;     // pegado al borde izquierdo del río
            } else {
                boteX = 2 * w / 3 - 10; // pegado al borde derecho del río
            }
            dibujarBote(g2, boteX, h / 2 - 20);

            // ---- Personajes orilla izquierda ----
            dibujarPersonajes(g2, estado[0], estado[1],
                              10, h / 2 - 60, w / 3 - 20);

            // ---- Personajes orilla derecha ----
            dibujarPersonajes(g2, estado[3], estado[4],
                              2 * w / 3 + 10, h / 2 - 60, w / 3 - 20);
        }

        /** Dibuja el bote como un rectángulo redondeado con un mástil. */
        private void dibujarBote(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(139, 90, 43));
            g2.fillRoundRect(x, y + 20, 40, 16, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y + 20, 40, 16, 8, 8);
            // Vela / mástil
            g2.setColor(new Color(255, 220, 100));
            int[] xPts = {x + 20, x + 10, x + 30};
            int[] yPts = {y,      y + 20,  y + 20};
            g2.fillPolygon(xPts, yPts, 3);
            g2.setColor(Color.DARK_GRAY);
            g2.drawPolygon(xPts, yPts, 3);
        }

        /**
         * Dibuja los personajes en una orilla.
         * Misioneros = círculos azules, caníbales = círculos rojos.
         */
        private void dibujarPersonajes(Graphics2D g2,
                                       int misioneros, int canibales,
                                       int xBase, int yBase, int ancho) {
            int tam  = 22;
            int paso = ancho / 4;
            int col  = 0;

            // Misioneros
            for (int i = 0; i < misioneros; i++) {
                int px = xBase + (col % 3) * paso;
                int py = yBase + (col / 3) * (tam + 8);
                g2.setColor(new Color(70, 130, 180));
                g2.fillOval(px, py, tam, tam);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("M", px + 6, py + 15);
                col++;
            }

            // Caníbales
            for (int i = 0; i < canibales; i++) {
                int px = xBase + (col % 3) * paso;
                int py = yBase + (col / 3) * (tam + 8);
                g2.setColor(new Color(200, 60, 50));
                g2.fillOval(px, py, tam, tam);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("C", px + 6, py + 15);
                col++;
            }
        }
    }

    // ---------------------------------------------------------------
    // Punto de entrada
    // ---------------------------------------------------------------

    /** Lanza la interfaz gráfica en el hilo de Swing (EDT). */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(InterfazCyM::new);
    }
}
