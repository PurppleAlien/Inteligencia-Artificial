import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Programación Lineal Entera — Problema de la ropa.
 * Maximizar: 6·x₁ + 4·x₂  (pantalones y camisetas)
 * Sujeto a:  3·x₁ + 2·x₂ ≤ 150  (horas de trabajo)
 *            4·x₁ + 3·x₂ ≤ 160  (metros de tela)
 *            x₁, x₂ ≥ 0, enteros
 *
 * Branch and Bound con interfaz gráfica:
 *  - Gráfico 2D con región factible y línea iso-ganancia animada
 *  - Árbol de poda / progreso mostrado en tiempo real
 */
public class BacktrackingPLE extends JFrame {

    // ── Paleta ───────────────────────────────────────────────────────────────
    private static final Color C_BASE    = new Color(30,  30,  46);
    private static final Color C_MANTLE  = new Color(24,  24,  37);
    private static final Color C_SURFACE = new Color(49,  50,  68);
    private static final Color C_TEXTO   = new Color(205, 214, 244);
    private static final Color C_MUTED   = new Color(147, 153, 178);
    private static final Color C_VERDE   = new Color(166, 227, 161);
    private static final Color C_AZUL    = new Color(137, 180, 250);
    private static final Color C_ROJO    = new Color(243, 139, 168);
    private static final Color C_AMARILLO= new Color(249, 226, 175);
    private static final Color C_MORADO  = new Color(203, 166, 247);

    // ── Parámetros del problema ──────────────────────────────────────────────
    private static final int C1 = 6, C2 = 4;
    private static final int[][] A = { {3, 2}, {4, 3} };
    private static final int[]   B = { 150, 160 };
    private static final String[] NOMBRES = { "Horas trabajo", "Metros tela" };
    private static final Color[]  RES_COLORES = { new Color(137,180,250,100), new Color(166,227,161,100) };

    // ── Estado del solver ────────────────────────────────────────────────────
    private int   mejorBeneficio = Integer.MIN_VALUE;
    private int[] mejorSolucion  = {0, 0};
    private int   nodosVisitados = 0, ramasPodadas = 0;
    private final List<int[]>  puntosVisitados = new ArrayList<>(); // [x1,x2,tipo] tipo=0:normal,1:mejor,2:podado
    private final List<String> logLineas       = new ArrayList<>();

    // ── Widgets ──────────────────────────────────────────────────────────────
    private GraficoPanel grafico;
    private JTextArea    areaLog;
    private JLabel       lblSol, lblNodos, lblPodas;
    private JButton      btnResolver;

    // ── Constructor ──────────────────────────────────────────────────────────
    public BacktrackingPLE() {
        super("PL Entera — Ropa (Branch & Bound)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BASE);
        setLayout(new BorderLayout(0, 0));

        buildUI();
        pack();
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        // Gráfico izquierda
        grafico = new GraficoPanel();
        grafico.setPreferredSize(new Dimension(520, 480));
        grafico.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));
        add(grafico, BorderLayout.CENTER);

        // Panel derecho
        JPanel derecho = new JPanel(new BorderLayout(0, 8));
        derecho.setBackground(C_MANTLE);
        derecho.setPreferredSize(new Dimension(310, 0));
        derecho.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, C_SURFACE));

        derecho.add(buildPanelInfo(),    BorderLayout.NORTH);
        derecho.add(buildPanelLog(),     BorderLayout.CENTER);
        derecho.add(buildPanelMetrics(), BorderLayout.SOUTH);
        add(derecho, BorderLayout.EAST);
    }

    private JPanel buildPanelInfo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_MANTLE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

        p.add(lbl("PL Entera — Ropa", new Font("Dialog", Font.BOLD, 17), C_MORADO));
        p.add(lbl("max  6·x₁ + 4·x₂", new Font("Dialog", Font.PLAIN, 12), C_TEXTO));
        p.add(lbl("s.a. 3x₁+2x₂≤150  (horas)", new Font("Dialog", Font.PLAIN, 11), C_AZUL));
        p.add(lbl("     4x₁+3x₂≤160  (tela)",  new Font("Dialog", Font.PLAIN, 11), C_VERDE));
        p.add(lbl("     x₁, x₂ ≥ 0,  enteros", new Font("Dialog", Font.PLAIN, 11), C_MUTED));
        p.add(Box.createVerticalStrut(10));

        btnResolver = boton("▶  Resolver con B&B", true);
        btnResolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnResolver.addActionListener(e -> resolver());
        p.add(btnResolver);

        p.add(Box.createVerticalStrut(10));
        lblSol = lbl("Solución: —", new Font("Dialog", Font.BOLD, 13), C_AMARILLO);
        p.add(lblSol);

        return p;
    }

    private JPanel buildPanelLog() {
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(24, 24, 37));
        areaLog.setForeground(C_TEXTO);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        areaLog.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 4));

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, C_SURFACE));
        scroll.getViewport().setBackground(C_MANTLE);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_MANTLE);
        p.add(scroll);
        return p;
    }

    private JPanel buildPanelMetrics() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(C_MANTLE);
        p.setBorder(BorderFactory.createEmptyBorder(8, 14, 14, 14));

        lblNodos = lbl("Nodos evaluados: —", new Font("Dialog", Font.PLAIN, 12), C_MUTED);
        lblPodas = lbl("Ramas podadas:  —", new Font("Dialog", Font.PLAIN, 12), C_MUTED);
        p.add(lblNodos);
        p.add(lblPodas);
        return p;
    }

    // ── Branch and Bound ─────────────────────────────────────────────────────
    private void resolver() {
        btnResolver.setEnabled(false);
        mejorBeneficio = Integer.MIN_VALUE;
        mejorSolucion  = new int[]{0, 0};
        nodosVisitados = ramasPodadas = 0;
        puntosVisitados.clear();
        logLineas.clear();
        areaLog.setText("");

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                int maxX1 = maxX1();
                publish("Branch & Bound: x₁ ∈ [0, " + maxX1 + "]\n");

                for (int x1 = 0; x1 <= maxX1; x1++) {
                    double cota = cotaSuperior(x1);
                    if (cota <= mejorBeneficio) {
                        ramasPodadas++;
                        publish(String.format("  PODA x₁=%d : cota=%.1f ≤ mejor=%d\n",
                                x1, cota, mejorBeneficio));
                        // marca roja en x1, x2=0 aproximado
                        final int fx1 = x1;
                        SwingUtilities.invokeLater(() -> {
                            puntosVisitados.add(new int[]{fx1, 0, 2});
                            grafico.repaint();
                        });
                        Thread.sleep(40);
                        continue;
                    }

                    publish(String.format("  RAMA x₁=%d : cota=%.1f\n", x1, cota));
                    int maxX2 = maxX2();
                    for (int x2 = maxX2; x2 >= 0; x2--) {
                        if (!esFeasible(x1, x2)) continue;
                        nodosVisitados++;
                        int ben = C1 * x1 + C2 * x2;
                        final int fx1 = x1, fx2 = x2;
                        if (ben > mejorBeneficio) {
                            mejorBeneficio   = ben;
                            mejorSolucion[0] = x1;
                            mejorSolucion[1] = x2;
                            String msg = String.format("★ x₁=%d, x₂=%d → %d€\n", x1, x2, ben);
                            publish(msg);
                            SwingUtilities.invokeLater(() -> {
                                puntosVisitados.add(new int[]{fx1, fx2, 1});
                                grafico.mejorX1 = fx1; grafico.mejorX2 = fx2;
                                grafico.repaint();
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                puntosVisitados.add(new int[]{fx1, fx2, 0});
                                grafico.repaint();
                            });
                        }
                        Thread.sleep(15);
                        break; // solo el mejor x2 para esta rama es relevante para el display
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String s : chunks) {
                    areaLog.append(s);
                    logLineas.add(s);
                }
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }

            @Override
            protected void done() {
                lblSol.setText(String.format("x₁=%d, x₂=%d → %d€",
                        mejorSolucion[0], mejorSolucion[1], mejorBeneficio));
                lblNodos.setText("Nodos evaluados: " + nodosVisitados);
                lblPodas.setText("Ramas podadas:  "  + ramasPodadas);
                grafico.repaint();
                btnResolver.setEnabled(true);
            }
        }.execute();
    }

    private boolean esFeasible(int x1, int x2) {
        if (x1 < 0 || x2 < 0) return false;
        for (int i = 0; i < A.length; i++)
            if (A[i][0] * x1 + A[i][1] * x2 > B[i]) return false;
        return true;
    }

    private double cotaSuperior(int x1) {
        double maxX2 = Double.MAX_VALUE;
        for (int i = 0; i < A.length; i++) {
            if (A[i][1] > 0) {
                double r = (double)(B[i] - A[i][0] * x1) / A[i][1];
                if (r < 0) return Double.NEGATIVE_INFINITY;
                maxX2 = Math.min(maxX2, r);
            }
        }
        return C1 * x1 + C2 * (maxX2 == Double.MAX_VALUE ? 0 : maxX2);
    }

    private int maxX1() {
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < A.length; i++)
            if (A[i][0] > 0) m = Math.min(m, B[i] / A[i][0]);
        return m;
    }

    private int maxX2() {
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < A.length; i++)
            if (A[i][1] > 0) m = Math.min(m, B[i] / A[i][1]);
        return m;
    }

    // ── Panel gráfico ─────────────────────────────────────────────────────────
    class GraficoPanel extends JPanel {
        int mejorX1 = -1, mejorX2 = -1;
        private static final int PAD = 56;

        GraficoPanel() {
            setBackground(C_BASE);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - PAD * 2;
            int h = getHeight() - PAD * 2;
            int maxX1v = maxX1() + 5;
            int maxX2v = maxX2() + 5;

            // Función de escala
            double sx = (double) w / maxX1v;
            double sy = (double) h / maxX2v;

            // Ejes
            g.setColor(C_SURFACE);
            g.fillRect(PAD, PAD, w, h);
            g.setColor(C_MUTED);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(PAD, PAD + h, PAD + w, PAD + h); // eje x
            g.drawLine(PAD, PAD,     PAD, PAD + h);     // eje y

            // Grid ligero
            g.setColor(new Color(88, 91, 112, 60));
            g.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x <= maxX1v; x += 10) {
                int px = PAD + (int)(x * sx);
                g.drawLine(px, PAD, px, PAD + h);
            }
            for (int y = 0; y <= maxX2v; y += 10) {
                int py = PAD + h - (int)(y * sy);
                g.drawLine(PAD, py, PAD + w, py);
            }

            // Región factible (puntos que satisfacen ambas restricciones)
            for (int x1 = 0; x1 <= maxX1v; x1++) {
                for (int x2 = 0; x2 <= maxX2v; x2++) {
                    if (esFeasible(x1, x2)) {
                        int px = PAD + (int)(x1 * sx);
                        int py = PAD + h - (int)(x2 * sy);
                        g.setColor(new Color(166, 227, 161, 18));
                        g.fillRect(px, py, Math.max(1,(int)sx), Math.max(1,(int)sy));
                    }
                }
            }

            // Líneas de restricción
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < A.length; i++) {
                g.setColor(i == 0 ? C_AZUL : C_VERDE);
                // A[i][0]*x1 + A[i][1]*x2 = B[i]  →  x2 = (B[i] - A[i][0]*x1) / A[i][1]
                int px1 = PAD;
                int px2 = PAD + w;
                double y1r = (double) B[i] / A[i][1];
                double x1r = (double) B[i] / A[i][0];
                int py1 = PAD + h - (int)(y1r * sy);
                int py2 = PAD + h - (int)(0 * sy);
                int px2r = PAD + (int)(x1r * sx);
                g.drawLine(px1, py1, px2r, py2);

                // etiqueta
                g.setFont(new Font("Dialog", Font.PLAIN, 10));
                g.drawString(NOMBRES[i], px1 + 4, py1 - 4);
            }

            // Puntos visitados por el B&B
            for (int[] pt : puntosVisitados) {
                int px = PAD + (int)(pt[0] * sx);
                int py = PAD + h - (int)(pt[1] * sy);
                if (pt[2] == 2) {           // podado
                    g.setColor(new Color(C_ROJO.getRed(), C_ROJO.getGreen(), C_ROJO.getBlue(), 160));
                    g.fillOval(px - 3, py - 3, 6, 6);
                } else if (pt[2] == 1) {    // mejor
                    g.setColor(C_AMARILLO);
                    g.fillOval(px - 5, py - 5, 10, 10);
                    g.setColor(C_BASE);
                    g.drawOval(px - 5, py - 5, 10, 10);
                } else {                    // visitado normal
                    g.setColor(new Color(C_AZUL.getRed(), C_AZUL.getGreen(), C_AZUL.getBlue(), 100));
                    g.fillOval(px - 2, py - 2, 5, 5);
                }
            }

            // Solución óptima destacada
            if (mejorX1 >= 0) {
                int px = PAD + (int)(mejorX1 * sx);
                int py = PAD + h - (int)(mejorX2 * sy);
                g.setColor(C_AMARILLO);
                g.setStroke(new BasicStroke(2f));
                g.drawOval(px - 8, py - 8, 16, 16);
                g.setFont(new Font("Dialog", Font.BOLD, 11));
                g.setColor(C_AMARILLO);
                g.drawString(String.format("(%d,%d)", mejorX1, mejorX2), px + 10, py - 4);

                // Línea iso-ganancia
                int ben = C1 * mejorX1 + C2 * mejorX2;
                // C1*x1 + C2*x2 = ben  →  cuando x1=0: x2=ben/C2; cuando x2=0: x1=ben/C1
                int isoX1end = ben / C1;
                int isoX2start = ben / C2;
                int ipx1 = PAD;
                int ipy1 = PAD + h - (int)(isoX2start * sy);
                int ipx2 = PAD + (int)(isoX1end * sx);
                int ipy2 = PAD + h;
                g.setColor(new Color(249, 226, 175, 180));
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0, new float[]{6, 4}, 0));
                g.drawLine(ipx1, ipy1, ipx2, ipy2);
            }

            // Etiquetas de ejes
            g.setFont(new Font("Dialog", Font.PLAIN, 11));
            g.setColor(C_MUTED);
            g.drawString("x₁ (pantalones)", PAD + w / 2 - 40, PAD + h + 36);
            // x₂ label vertical
            AffineTransform at = g.getTransform();
            g.rotate(-Math.PI / 2, PAD - 36, PAD + h / 2);
            g.drawString("x₂ (camisetas)", PAD - 36, PAD + h / 2);
            g.setTransform(at);

            // Tick labels
            g.setColor(C_MUTED);
            g.setFont(new Font("Dialog", Font.PLAIN, 9));
            for (int x = 0; x <= maxX1v; x += 10) {
                int px = PAD + (int)(x * sx);
                g.drawString(String.valueOf(x), px - 5, PAD + h + 14);
            }
            for (int y = 0; y <= maxX2v; y += 10) {
                int py = PAD + h - (int)(y * sy);
                g.drawString(String.valueOf(y), PAD - 26, py + 4);
            }
        }
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────
    private static JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private static JButton boton(String txt, boolean primario) {
        JButton btn = new JButton(txt);
        btn.setBackground(primario ? new Color(64, 66, 90) : new Color(49, 50, 68));
        btn.setForeground(primario ? C_VERDE : C_MUTED);
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primario ? new Color(88,91,112) : new Color(49,50,68)),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BacktrackingPLE::new);
    }
}
