import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Programación Lineal Entera — Problema de la carpintería.
 * Maximizar: 20·s + 30·m  (ganancia por sillas y mesas)
 * Sujeto a:   2·s +  4·m ≤ 140  (horas de trabajo)
 *             3·s +  5·m ≤ 180  (tablones de madera)
 *             s, m ≥ 0, enteros
 *
 * Branch and Bound con interfaz gráfica:
 *  - Gráfico 2D con región factible y línea iso-ganancia animada
 *  - Árbol de poda / progreso mostrado en tiempo real
 */
public class BacktrackingCarpinteriaPLE extends JFrame {

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
    private static final int C_SILLA = 20, C_MESA = 30;
    private static final int[][] COEF = { {2, 4}, {3, 5} };
    private static final int[]   LIM  = { 140, 180 };
    private static final String[] NOMBRES = { "Horas trabajo", "Tablones madera" };

    // ── Estado del solver ────────────────────────────────────────────────────
    private int   mejorGanancia = Integer.MIN_VALUE;
    private int[] mejorSolucion = {0, 0};
    private int   nodosVisitados = 0, ramasPodadas = 0;
    private final List<int[]>  puntosVisitados = new ArrayList<>();
    private final List<String> logLineas       = new ArrayList<>();

    // ── Widgets ──────────────────────────────────────────────────────────────
    private GraficoPanel grafico;
    private JTextArea    areaLog;
    private JLabel       lblSol, lblNodos, lblPodas;
    private JButton      btnResolver;

    // ── Constructor ──────────────────────────────────────────────────────────
    public BacktrackingCarpinteriaPLE() {
        super("PL Entera — Carpintería (Branch & Bound)");
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
        grafico = new GraficoPanel();
        grafico.setPreferredSize(new Dimension(520, 480));
        grafico.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));
        add(grafico, BorderLayout.CENTER);

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

        p.add(lbl("PL Entera — Carpintería", new Font("Dialog", Font.BOLD, 17), C_MORADO));
        p.add(lbl("max  20·s + 30·m", new Font("Dialog", Font.PLAIN, 12), C_TEXTO));
        p.add(lbl("s.a. 2s+4m≤140  (horas)",   new Font("Dialog", Font.PLAIN, 11), C_AZUL));
        p.add(lbl("     3s+5m≤180  (tablones)", new Font("Dialog", Font.PLAIN, 11), C_VERDE));
        p.add(lbl("     s, m ≥ 0,  enteros",    new Font("Dialog", Font.PLAIN, 11), C_MUTED));
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
        mejorGanancia = Integer.MIN_VALUE;
        mejorSolucion = new int[]{0, 0};
        nodosVisitados = ramasPodadas = 0;
        puntosVisitados.clear();
        logLineas.clear();
        areaLog.setText("");

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                int maxS = maxSillas();
                publish("Branch & Bound: s ∈ [0, " + maxS + "]\n");

                for (int s = 0; s <= maxS; s++) {
                    double cota = cotaSuperior(s);
                    if (cota <= mejorGanancia) {
                        ramasPodadas++;
                        publish(String.format("  PODA s=%d : cota=%.1f ≤ mejor=%d\n",
                                s, cota, mejorGanancia));
                        final int fs = s;
                        SwingUtilities.invokeLater(() -> {
                            puntosVisitados.add(new int[]{fs, 0, 2});
                            grafico.repaint();
                        });
                        Thread.sleep(40);
                        continue;
                    }

                    publish(String.format("  RAMA s=%d : cota=%.1f\n", s, cota));
                    int maxM = maxMesas();
                    for (int m = maxM; m >= 0; m--) {
                        if (!esFeasible(s, m)) continue;
                        nodosVisitados++;
                        int gan = C_SILLA * s + C_MESA * m;
                        final int fs = s, fm = m;
                        if (gan > mejorGanancia) {
                            mejorGanancia   = gan;
                            mejorSolucion[0] = s;
                            mejorSolucion[1] = m;
                            publish(String.format("★ s=%d, m=%d → $%d\n", s, m, gan));
                            SwingUtilities.invokeLater(() -> {
                                puntosVisitados.add(new int[]{fs, fm, 1});
                                grafico.mejorS = fs; grafico.mejorM = fm;
                                grafico.repaint();
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                puntosVisitados.add(new int[]{fs, fm, 0});
                                grafico.repaint();
                            });
                        }
                        Thread.sleep(15);
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String s : chunks) areaLog.append(s);
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }

            @Override
            protected void done() {
                lblSol.setText(String.format("s=%d, m=%d → $%d",
                        mejorSolucion[0], mejorSolucion[1], mejorGanancia));
                lblNodos.setText("Nodos evaluados: " + nodosVisitados);
                lblPodas.setText("Ramas podadas:  "  + ramasPodadas);
                grafico.repaint();
                btnResolver.setEnabled(true);
            }
        }.execute();
    }

    private boolean esFeasible(int s, int m) {
        if (s < 0 || m < 0) return false;
        for (int i = 0; i < COEF.length; i++)
            if (COEF[i][0] * s + COEF[i][1] * m > LIM[i]) return false;
        return true;
    }

    private double cotaSuperior(int s) {
        double maxM = Double.MAX_VALUE;
        for (int i = 0; i < COEF.length; i++) {
            if (COEF[i][1] > 0) {
                double r = (double)(LIM[i] - COEF[i][0] * s) / COEF[i][1];
                if (r < 0) return Double.NEGATIVE_INFINITY;
                maxM = Math.min(maxM, r);
            }
        }
        return C_SILLA * s + C_MESA * (maxM == Double.MAX_VALUE ? 0 : maxM);
    }

    private int maxSillas() {
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < COEF.length; i++)
            if (COEF[i][0] > 0) m = Math.min(m, LIM[i] / COEF[i][0]);
        return m;
    }

    private int maxMesas() {
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < COEF.length; i++)
            if (COEF[i][1] > 0) m = Math.min(m, LIM[i] / COEF[i][1]);
        return m;
    }

    // ── Panel gráfico ─────────────────────────────────────────────────────────
    class GraficoPanel extends JPanel {
        int mejorS = -1, mejorM = -1;
        private static final int PAD = 56;

        GraficoPanel() { setBackground(C_BASE); }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - PAD * 2;
            int h = getHeight() - PAD * 2;
            int maxSv = maxSillas() + 5;
            int maxMv = maxMesas() + 5;
            double sx = (double) w / maxSv;
            double sy = (double) h / maxMv;

            // Fondo área
            g.setColor(C_SURFACE);
            g.fillRect(PAD, PAD, w, h);

            // Grid
            g.setColor(new Color(88, 91, 112, 60));
            g.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x <= maxSv; x += 10) {
                int px = PAD + (int)(x * sx);
                g.drawLine(px, PAD, px, PAD + h);
            }
            for (int y = 0; y <= maxMv; y += 5) {
                int py = PAD + h - (int)(y * sy);
                g.drawLine(PAD, py, PAD + w, py);
            }

            // Región factible
            for (int sv = 0; sv <= maxSv; sv++) {
                for (int mv = 0; mv <= maxMv; mv++) {
                    if (esFeasible(sv, mv)) {
                        int px = PAD + (int)(sv * sx);
                        int py = PAD + h - (int)(mv * sy);
                        g.setColor(new Color(166, 227, 161, 18));
                        g.fillRect(px, py, Math.max(1,(int)sx), Math.max(1,(int)sy));
                    }
                }
            }

            // Líneas de restricción
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < COEF.length; i++) {
                g.setColor(i == 0 ? C_AZUL : C_VERDE);
                double y0 = (double) LIM[i] / COEF[i][1];
                double x0 = (double) LIM[i] / COEF[i][0];
                int px1 = PAD;
                int py1 = PAD + h - (int)(y0 * sy);
                int px2 = PAD + (int)(x0 * sx);
                int py2 = PAD + h;
                g.drawLine(px1, py1, px2, py2);
                g.setFont(new Font("Dialog", Font.PLAIN, 10));
                g.drawString(NOMBRES[i], px1 + 4, py1 - 4);
            }

            // Ejes
            g.setColor(C_MUTED);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(PAD, PAD + h, PAD + w, PAD + h);
            g.drawLine(PAD, PAD,     PAD, PAD + h);

            // Puntos B&B
            for (int[] pt : puntosVisitados) {
                int px = PAD + (int)(pt[0] * sx);
                int py = PAD + h - (int)(pt[1] * sy);
                if (pt[2] == 2) {
                    g.setColor(new Color(C_ROJO.getRed(), C_ROJO.getGreen(), C_ROJO.getBlue(), 160));
                    g.fillOval(px - 3, py - 3, 6, 6);
                } else if (pt[2] == 1) {
                    g.setColor(C_AMARILLO);
                    g.fillOval(px - 5, py - 5, 10, 10);
                } else {
                    g.setColor(new Color(C_AZUL.getRed(), C_AZUL.getGreen(), C_AZUL.getBlue(), 100));
                    g.fillOval(px - 2, py - 2, 5, 5);
                }
            }

            // Óptimo destacado
            if (mejorS >= 0) {
                int px = PAD + (int)(mejorS * sx);
                int py = PAD + h - (int)(mejorM * sy);
                g.setColor(C_AMARILLO);
                g.setStroke(new BasicStroke(2f));
                g.drawOval(px - 8, py - 8, 16, 16);
                g.setFont(new Font("Dialog", Font.BOLD, 11));
                g.drawString(String.format("(%d,%d)", mejorS, mejorM), px + 10, py - 4);

                // Iso-ganancia
                int gan = C_SILLA * mejorS + C_MESA * mejorM;
                double isoM0 = (double) gan / C_MESA;
                double isoS0 = (double) gan / C_SILLA;
                g.setColor(new Color(249, 226, 175, 180));
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0, new float[]{6, 4}, 0));
                g.drawLine(PAD, PAD + h - (int)(isoM0 * sy),
                           PAD + (int)(isoS0 * sx), PAD + h);
            }

            // Etiquetas
            g.setFont(new Font("Dialog", Font.PLAIN, 11));
            g.setColor(C_MUTED);
            g.drawString("s (sillas)", PAD + w / 2 - 30, PAD + h + 36);
            AffineTransform at = g.getTransform();
            g.rotate(-Math.PI / 2, PAD - 36, PAD + h / 2);
            g.drawString("m (mesas)", PAD - 36, PAD + h / 2);
            g.setTransform(at);

            g.setFont(new Font("Dialog", Font.PLAIN, 9));
            g.setColor(C_MUTED);
            for (int x = 0; x <= maxSv; x += 10) {
                g.drawString(String.valueOf(x), PAD + (int)(x*sx) - 5, PAD + h + 14);
            }
            for (int y = 0; y <= maxMv; y += 5) {
                g.drawString(String.valueOf(y), PAD - 26, PAD + h - (int)(y*sy) + 4);
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
        SwingUtilities.invokeLater(BacktrackingCarpinteriaPLE::new);
    }
}
