import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

public class AlgoritmoWangGUI extends JFrame {

    // ── Paleta oscura moderna ──────────────────────────────────────────────────
    private static final Color BG_DARK     = new Color(10,  15,  30);
    private static final Color BG_PANEL    = new Color(20,  30,  50);
    private static final Color BG_INPUT    = new Color(30,  42,  65);
    private static final Color ACCENT      = new Color(124, 58, 237);   // violeta
    private static final Color ACCENT_LT   = new Color(167,139,250);
    private static final Color SUCCESS     = new Color(16, 185,129);    // verde
    private static final Color DANGER      = new Color(239, 68, 68);    // rojo
    private static final Color WARNING     = new Color(245,158, 11);    // ámbar
    private static final Color TEXT_PRI    = new Color(226,232,240);
    private static final Color TEXT_SEC    = new Color(148,163,184);
    private static final Color BORDER_COL  = new Color(51,  65, 85);

    // ── Widgets ────────────────────────────────────────────────────────────────
    private JTextPane  areaSalida;
    private StyledDocument styledDoc;
    private JTextField campoPremisas;
    private JTextField campoConclusion;
    private JLabel     lblResultado;
    private JLabel     lblPasos;
    private int        contadorPasos = 0;

    // ══════════════════════════════════════════════════════════════════════════
    public AlgoritmoWangGUI() {
        setTitle("Demostrador de Teoremas — Algoritmo de Wang");
        setSize(950, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(720, 580));
        getContentPane().setBackground(BG_DARK);
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        construirUI();
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────
    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(crearHeader(),      BorderLayout.NORTH);
        add(crearCentro(),      BorderLayout.CENTER);
        add(crearStatusBar(),   BorderLayout.SOUTH);
    }

    /** Franja superior con gradiente violeta → azul oscuro */
    private JPanel crearHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(76,29,149), getWidth(), 0, BG_PANEL));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(0, 82));
        h.setBorder(BorderFactory.createEmptyBorder(14, 26, 14, 26));

        JLabel title = lbl("Algoritmo de Wang", Color.WHITE, 26, Font.BOLD);
        JLabel sub   = lbl("Demostrador de Teoremas — Lógica Computacional", new Color(196,181,253), 13, Font.PLAIN);

        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false); left.add(title); left.add(sub);

        JLabel sym = lbl("  ¬  ∧  ∨  →  ⊢  ", ACCENT_LT, 22, Font.BOLD);
        h.add(left, BorderLayout.WEST);
        h.add(sym,  BorderLayout.EAST);
        return h;
    }

    private JPanel crearCentro() {
        JPanel c = new JPanel(new BorderLayout(0, 14));
        c.setBackground(BG_DARK);
        c.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        c.add(crearPanelEntrada(), BorderLayout.NORTH);
        c.add(crearPanelSalida(), BorderLayout.CENTER);
        return c;
    }

    /** Panel de entrada: ejemplos, premisas, conclusión, botones */
    private JPanel crearPanelEntrada() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0 — selector de ejemplos
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        p.add(lbl("Ejemplos:", TEXT_SEC, 12, Font.PLAIN), g);

        String[] ejs = {
            "— seleccionar ejemplo —",
            "Modus Ponens:              P->Q, P  ⊢  Q",
            "Silogismo Hipotético:      P->Q, Q->R  ⊢  P->R",
            "Modus Tollens:             P->Q, notQ  ⊢  notP",
            "Doble Negación:            notnot P  ⊢  P",
            "Ley De Morgan (∧):         not(PandQ)  ⊢  notPornotQ",
            "Distribución:              Por(QandR)  ⊢  (PorQ)and(PorR)",
            "Contrapositiva:            P->Q  ⊢  notQ->notP",
        };
        JComboBox<String> combo = new JComboBox<>(ejs);
        estilizarCombo(combo);
        combo.addActionListener(e -> cargarEjemplo(combo.getSelectedIndex()));
        g.gridx = 1; g.gridy = 0; g.gridwidth = 3; g.weightx = 1.0;
        p.add(combo, g);

        // Fila 1 — premisas
        g.gridx = 0; g.gridy = 1; g.gridwidth = 1; g.weightx = 0;
        p.add(lbl("Premisas:", TEXT_SEC, 13, Font.PLAIN), g);
        campoPremisas = crearTextField("ej: P->Q,Q->R,notR");
        g.gridx = 1; g.gridy = 1; g.gridwidth = 3; g.weightx = 1.0;
        p.add(campoPremisas, g);

        // Fila 2 — conclusión
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.weightx = 0;
        p.add(lbl("Conclusión:", TEXT_SEC, 13, Font.PLAIN), g);
        campoConclusion = crearTextField("ej: notP");
        g.gridx = 1; g.gridy = 2; g.gridwidth = 3; g.weightx = 1.0;
        p.add(campoConclusion, g);

        // Fila 3 — nota de sintaxis
        g.gridx = 0; g.gridy = 3; g.gridwidth = 4;
        JLabel nota = lbl("  Sintaxis:  notP  |  PandQ  |  PorQ  |  P->Q  |  Variables en MAYÚSCULAS  |  Paréntesis: (PorQ)", TEXT_SEC, 11, Font.ITALIC);
        p.add(nota, g);

        // Fila 4 — botones
        g.gridx = 0; g.gridy = 4; g.gridwidth = 4;
        g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER;
        JPanel bts = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bts.setOpaque(false);
        bts.add(botonPrimario ("▶  DEMOSTRAR", ACCENT,   Color.WHITE,  this::procesarTeorema));
        bts.add(botonSecundario("↺  LIMPIAR",             e -> limpiar()));
        bts.add(botonSecundario("✕  SALIR",               e -> System.exit(0)));
        p.add(bts, g);
        return p;
    }

    /** Panel de salida: banner de resultado + JTextPane con colores */
    private JPanel crearPanelSalida() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_DARK);

        lblResultado = new JLabel("  Ingrese premisas y conclusión para comenzar", SwingConstants.LEFT);
        lblResultado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResultado.setForeground(TEXT_SEC);
        lblResultado.setOpaque(true);
        lblResultado.setBackground(BG_PANEL);
        lblResultado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        p.add(lblResultado, BorderLayout.NORTH);

        areaSalida = new JTextPane();
        areaSalida.setEditable(false);
        areaSalida.setBackground(new Color(8, 12, 22));
        areaSalida.setForeground(TEXT_PRI);
        areaSalida.setFont(new Font("Monospaced", Font.PLAIN, 12));
        styledDoc = areaSalida.getStyledDocument();

        JScrollPane sc = new JScrollPane(areaSalida);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        sc.getViewport().setBackground(new Color(8, 12, 22));
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel tituloLog = lbl("  Proceso de Demostración", TEXT_SEC, 11, Font.BOLD);
        tituloLog.setOpaque(true);
        tituloLog.setBackground(BG_PANEL);
        tituloLog.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_DARK);
        wrap.add(tituloLog, BorderLayout.NORTH);
        wrap.add(sc, BorderLayout.CENTER);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(14, 20, 38));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        bar.setPreferredSize(new Dimension(0, 26));
        lblPasos = lbl("  Pasos: 0  |  Listo", TEXT_SEC, 11, Font.PLAIN);
        JLabel ver = lbl("Algoritmo de Wang  v2.0  ", new Color(70, 80, 110), 11, Font.PLAIN);
        bar.add(lblPasos, BorderLayout.WEST);
        bar.add(ver,      BorderLayout.EAST);
        return bar;
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    private JLabel lbl(String t, Color c, int size, int style) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", style, size)); l.setForeground(c); return l;
    }

    private JTextField crearTextField(String ph) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    ((Graphics2D) g).setColor(new Color(90, 110, 145));
                    g.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    g.drawString(ph, 8, getHeight() / 2 + 4);
                }
            }
        };
        tf.setBackground(BG_INPUT); tf.setForeground(TEXT_PRI);
        tf.setCaretColor(ACCENT_LT);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    private void estilizarCombo(JComboBox<String> cb) {
        cb.setBackground(BG_INPUT); cb.setForeground(TEXT_PRI);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton botonPrimario(String t, Color bg, Color fg, ActionListener al) {
        return construirBoton(t, bg, fg, 170, 36, al);
    }

    private JButton botonSecundario(String t, ActionListener al) {
        return construirBoton(t, new Color(40, 55, 80), TEXT_PRI, 130, 36, al);
    }

    private JButton construirBoton(String t, Color bg, Color fg, int w, int h, ActionListener al) {
        final Color bgF = bg, fgF = fg;
        JButton btn = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bgF.darker()
                        : getModel().isRollover() ? bgF.brighter() : bgF;
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(fgF); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ── Ejemplos ──────────────────────────────────────────────────────────────

    private void cargarEjemplo(int i) {
        switch (i) {
            case 1: campoPremisas.setText("P->Q,P");         campoConclusion.setText("Q"); break;
            case 2: campoPremisas.setText("P->Q,Q->R");      campoConclusion.setText("P->R"); break;
            case 3: campoPremisas.setText("P->Q,notQ");      campoConclusion.setText("notP"); break;
            case 4: campoPremisas.setText("notnotP");         campoConclusion.setText("P"); break;
            case 5: campoPremisas.setText("not(PandQ)");     campoConclusion.setText("notPornotQ"); break;
            case 6: campoPremisas.setText("Por(QandR)");     campoConclusion.setText("(PorQ)and(PorR)"); break;
            case 7: campoPremisas.setText("P->Q");           campoConclusion.setText("notQ->notP"); break;
        }
    }

    private void limpiar() {
        campoPremisas.setText(""); campoConclusion.setText("");
        try { styledDoc.remove(0, styledDoc.getLength()); } catch (Exception ignored) {}
        contadorPasos = 0;
        lblResultado.setText("  Ingrese premisas y conclusión para comenzar");
        lblResultado.setForeground(TEXT_SEC); lblResultado.setBackground(BG_PANEL);
        lblPasos.setText("  Pasos: 0  |  Listo");
    }

    // ── Controlador principal ─────────────────────────────────────────────────

    private void procesarTeorema(ActionEvent e) {
        String pStr = campoPremisas.getText().replace(" ", "");
        String cStr = campoConclusion.getText().replace(" ", "");
        if (pStr.isEmpty() || cStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar premisas y conclusión.", "Entrada vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try { styledDoc.remove(0, styledDoc.getLength()); } catch (Exception ex) { /* ignorar */ }
        contadorPasos = 0;

        List<String> premisas = new ArrayList<>(Arrays.asList(pStr.split(",")));
        appendS("━━━  DEMOSTRACIÓN  ━━━\n\n", "hdr");
        appendS("Secuente:  ", "lbl"); appendS(pStr + "  ⊢  " + cStr + "\n\n", "fml");

        boolean ok = demostrar(premisas, cStr, 0);

        appendS("\n━━━  RESULTADO  ━━━\n", "hdr");
        if (ok) {
            appendS("✓  El teorema ES VÁLIDO (tautología demostrada)\n", "ok");
            lblResultado.setText("  ✓   " + pStr + "  ⊢  " + cStr + "   →   VÁLIDO");
            lblResultado.setForeground(SUCCESS);
            lblResultado.setBackground(new Color(5, 46, 22, 200));
        } else {
            appendS("✗  El teorema NO ES VÁLIDO (refutado)\n", "err");
            lblResultado.setText("  ✗   " + pStr + "  ⊢  " + cStr + "   →   NO VÁLIDO");
            lblResultado.setForeground(DANGER);
            lblResultado.setBackground(new Color(69, 10, 10, 200));
        }
        lblPasos.setText("  Pasos aplicados: " + contadorPasos + "  |  " +
            (ok ? "Demostración exitosa" : "Demostración fallida"));
    }

    // ── Escritura con estilos ─────────────────────────────────────────────────

    private void appendS(String text, String style) {
        try {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setFontFamily(a, "Monospaced");
            switch (style) {
                case "hdr":  StyleConstants.setForeground(a, ACCENT_LT);                StyleConstants.setBold(a, true); StyleConstants.setFontSize(a, 13); break;
                case "lbl":  StyleConstants.setForeground(a, TEXT_SEC);                 StyleConstants.setFontSize(a, 12); break;
                case "fml":  StyleConstants.setForeground(a, new Color(253,224,71));    StyleConstants.setBold(a, true);  StyleConstants.setFontSize(a, 13); break;
                case "ok":   StyleConstants.setForeground(a, SUCCESS);                  StyleConstants.setBold(a, true);  StyleConstants.setFontSize(a, 14); break;
                case "err":  StyleConstants.setForeground(a, DANGER);                   StyleConstants.setBold(a, true);  StyleConstants.setFontSize(a, 14); break;
                case "step": StyleConstants.setForeground(a, new Color(147,197,253));   StyleConstants.setFontSize(a, 12); break;
                case "node": StyleConstants.setForeground(a, TEXT_PRI);                 StyleConstants.setFontSize(a, 12); break;
                case "warn": StyleConstants.setForeground(a, WARNING);                  StyleConstants.setFontSize(a, 12); break;
                default:     StyleConstants.setForeground(a, TEXT_PRI);                 StyleConstants.setFontSize(a, 12);
            }
            styledDoc.insertString(styledDoc.getLength(), text, a);
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ALGORITMO DE WANG (secuente calculus / tableaux)
    // ══════════════════════════════════════════════════════════════════════════

    private boolean demostrar(List<String> izq, String der, int nivel) {
        izq = simplificarLista(izq);
        der = simplificar(der);
        contadorPasos++;

        String ind = "  ".repeat(nivel);
        String izqStr = String.join(", ", izq);
        String derStr = (der == null || der.isEmpty()) ? "⊥" : der;
        appendS(ind + "[" + contadorPasos + "]  " + izqStr + "  ⊢  " + derStr + "\n", "node");

        // Axioma 1: contradicción en consecuente
        if (contradiccion(der)) {
            appendS(ind + "  ✓ AXIOMA — contradicción en consecuente\n", "ok"); return true;
        }
        // Axioma 2: fórmula en ambos lados
        if (coincide(izq, der)) {
            appendS(ind + "  ✓ AXIOMA — misma fórmula en antecedente y consecuente\n", "ok"); return true;
        }
        // Sin conectivas → fallo
        if (!tieneConectivas(izq) && !tieneConectivas(der)) {
            appendS(ind + "  ✗ CERRADO — átomos sin coincidencia\n", "err"); return false;
        }

        // ∧L  — Conjunción en antecedente
        for (int i = 0; i < izq.size(); i++) {
            if (opPrincipal(izq.get(i)).equals("and")) {
                String[] p = dividir(izq.get(i), "and");
                if (p != null) {
                    List<String> nuevo = new ArrayList<>(izq); nuevo.remove(i);
                    nuevo.add(p[0]); nuevo.add(p[1]);
                    appendS(ind + "  ∧L: " + izq.get(i) + " → " + p[0] + ", " + p[1] + "\n", "step");
                    return demostrar(nuevo, der, nivel + 1);
                }
            }
        }

        // →L  — Implicación en antecedente
        for (int i = 0; i < izq.size(); i++) {
            if (opPrincipal(izq.get(i)).equals("->")) {
                String[] p = dividir(izq.get(i), "->");
                if (p != null) {
                    List<String> nuevo = new ArrayList<>(izq); nuevo.remove(i);
                    nuevo.add("not" + p[0]); nuevo.add(p[1]);
                    appendS(ind + "  →L: " + izq.get(i) + " → ¬" + p[0] + ", " + p[1] + "\n", "step");
                    boolean r = demostrar(nuevo, der, nivel + 1);
                    if (r) return true;
                    izq.add(i, izq.get(i - 1 + 1)); // restaurar (simple break)
                    break;
                }
            }
        }

        // →R  — Implicación en consecuente
        if (opPrincipal(der).equals("->")) {
            String[] p = dividir(der, "->");
            if (p != null) {
                List<String> nuevo = new ArrayList<>(izq); nuevo.add(p[0]);
                appendS(ind + "  →R: " + der + " → mover " + p[0] + " al antecedente\n", "step");
                boolean r = demostrar(nuevo, p[1], nivel + 1);
                if (r) return true;
            }
        }

        // ¬¬L — Doble negación en antecedente
        for (int i = 0; i < izq.size(); i++) {
            if (izq.get(i).startsWith("notnot")) {
                String f = izq.get(i).substring(6);
                List<String> nuevo = new ArrayList<>(izq); nuevo.remove(i); nuevo.add(f);
                appendS(ind + "  ¬¬L: eliminar doble negación → " + f + "\n", "step");
                return demostrar(nuevo, der, nivel + 1);
            }
        }

        // ¬L  — Negación en antecedente
        for (int i = 0; i < izq.size(); i++) {
            if (izq.get(i).startsWith("not")) {
                String f   = izq.get(i).substring(3);
                String nDer = (der == null || der.isEmpty()) ? f : der + "," + f;
                List<String> nuevo = new ArrayList<>(izq); nuevo.remove(i);
                appendS(ind + "  ¬L: mover ¬" + f + " al consecuente\n", "step");
                boolean r = demostrar(nuevo, nDer, nivel + 1);
                if (r) return true;
                izq.add(i, "not" + f);
                break;
            }
        }

        // ¬R  — Negación en consecuente
        if (der.startsWith("not")) {
            String f = der.substring(3);
            List<String> nuevo = new ArrayList<>(izq); nuevo.add(f);
            appendS(ind + "  ¬R: mover ¬" + f + " al antecedente\n", "step");
            boolean r = demostrar(nuevo, "", nivel + 1);
            if (r) return true;
        }

        // ∨L  — Disyunción en antecedente (bifurca)
        for (int i = 0; i < izq.size(); i++) {
            if (opPrincipal(izq.get(i)).equals("or")) {
                String[] p = dividir(izq.get(i), "or");
                if (p != null) {
                    List<String> izq1 = new ArrayList<>(izq); izq1.remove(i); izq1.add(p[0]);
                    List<String> izq2 = new ArrayList<>(izq); izq2.remove(i); izq2.add(p[1]);
                    appendS(ind + "  ∨L: " + izq.get(i) + " → Rama-A(" + p[0] + ")  Rama-B(" + p[1] + ")\n", "step");
                    boolean r1 = demostrar(izq1, der, nivel + 1);
                    boolean r2 = demostrar(izq2, der, nivel + 1);
                    return r1 || r2;
                }
            }
        }

        // ∧R  — Conjunción en consecuente (bifurca)
        if (opPrincipal(der).equals("and")) {
            String[] p = dividir(der, "and");
            if (p != null) {
                appendS(ind + "  ∧R: " + der + " → Rama-A(" + p[0] + ")  Rama-B(" + p[1] + ")\n", "step");
                boolean r1 = demostrar(new ArrayList<>(izq), p[0], nivel + 1);
                boolean r2 = demostrar(new ArrayList<>(izq), p[1], nivel + 1);
                return r1 && r2;
            }
        }

        appendS(ind + "  ✗ CERRADO — sin reglas aplicables\n", "err");
        return false;
    }

    // ── Utilidades lógicas ────────────────────────────────────────────────────

    private String[] dividir(String formula, String op) {
        formula = simplificar(formula);
        int nivel = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') nivel++; if (c == ')') nivel--;
            if (nivel == 0 && formula.substring(i).startsWith(op))
                return new String[]{ formula.substring(0, i), formula.substring(i + op.length()) };
        }
        return null;
    }

    private String opPrincipal(String formula) {
        formula = simplificar(formula);
        if (formula.startsWith("not")) return "not";
        int nivel = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') nivel++; if (c == ')') nivel--;
            if (nivel == 0)
                for (String op : new String[]{"->","and","or"})
                    if (formula.substring(i).startsWith(op)) return op;
        }
        return "";
    }

    private List<String> simplificarLista(List<String> fs) {
        List<String> r = new ArrayList<>();
        for (String f : fs) r.add(simplificar(f));
        return r;
    }

    private String simplificar(String f) {
        if (f == null || f.isEmpty()) return f;
        f = f.replaceAll("\\s+", "");
        while (quitarExterno(f)) f = f.substring(1, f.length() - 1);
        if (f.startsWith("not(") && f.endsWith(")"))
            return "not" + simplificar(f.substring(4, f.length() - 1));
        return f;
    }

    private boolean quitarExterno(String f) {
        if (!f.startsWith("(") || !f.endsWith(")")) return false;
        int bal = 0;
        for (int i = 1; i < f.length() - 1; i++) {
            if (f.charAt(i) == '(') bal++; if (f.charAt(i) == ')') bal--;
            if (bal < 0) return false;
        }
        return bal == 0;
    }

    private boolean contradiccion(String der) {
        if (der == null || der.isEmpty()) return false;
        Set<String> neg = new HashSet<>(), pos = new HashSet<>();
        for (String f : der.split(",")) {
            f = simplificar(f);
            if (f.startsWith("not")) { String a = simplificar(f.substring(3)); if (pos.contains(a)) return true; neg.add(a); }
            else { if (neg.contains(f)) return true; pos.add(f); }
        }
        return false;
    }

    private boolean coincide(List<String> izq, String der) {
        if (der == null || der.isEmpty()) return false;
        for (String fi : izq) {
            String si = simplificar(fi);
            for (String fd : der.split(",")) {
                String sd = simplificar(fd);
                if (si.equals(sd)) return true;
                if (sd.contains("or") && sd.contains(si)) return true;
            }
        }
        return false;
    }

    private boolean tieneConectivas(List<String> fs) {
        for (String f : fs) if (tieneConectivas(f)) return true; return false;
    }
    private boolean tieneConectivas(String f) {
        f = simplificar(f);
        return f.contains("not") || f.contains("and") || f.contains("or") || f.contains("->");
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlgoritmoWangGUI().setVisible(true));
    }
}
