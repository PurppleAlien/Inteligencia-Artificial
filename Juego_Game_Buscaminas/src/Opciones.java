import javax.swing.*;
import java.awt.*;

/**
 * Diálogo de opciones de dificultad del Buscaminas.
 *
 * MEJORAS:
 * - Tema oscuro consistente con el juego principal.
 * - Validación mejorada: mensaje de error específico por campo.
 * - Los campos personalizados muestran los rangos válidos como placeholder.
 */
public class Opciones extends JDialog {

    private static final Color C_BG      = new Color(49,  50,  68);
    private static final Color C_SURFACE = new Color(69,  71,  90);
    private static final Color C_TEXTO   = new Color(205, 214, 244);
    private static final Color C_MUTED   = new Color(147, 153, 178);
    private static final Color C_VERDE   = new Color(166, 227, 161);
    private static final Color C_ROJO    = new Color(243, 139, 168);

    private final JRadioButton rbPrincipiante, rbIntermedio, rbExperto, rbPersonalizado;
    private final ButtonGroup  grupo;
    private final JTextField   campoFilas, campoColumnas, campoMinas;
    private final Buscaminas_BackTrack juego;

    public Opciones(Buscaminas_BackTrack padre) {
        super(padre, "Opciones de dificultad", true);
        this.juego = padre;

        setSize(380, 320);
        setLocationRelativeTo(padre);
        setResizable(false);
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(0, 0));

        // ── Opciones de dificultad ──
        JPanel panelOpc = new JPanel();
        panelOpc.setLayout(new BoxLayout(panelOpc, BoxLayout.Y_AXIS));
        panelOpc.setBackground(C_BG);
        panelOpc.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        JLabel titulo = new JLabel("Selecciona la dificultad");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(C_TEXTO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelOpc.add(titulo);

        grupo = new ButtonGroup();
        rbPrincipiante = radioButton("Fácil         — 9×9, 10 minas",   true);
        rbIntermedio   = radioButton("Intermedio    — 16×16, 40 minas", false);
        rbExperto      = radioButton("Difícil       — 22×30, 150 minas",false);
        rbPersonalizado= radioButton("Personalizado:", false);

        grupo.add(rbPrincipiante);
        grupo.add(rbIntermedio);
        grupo.add(rbExperto);
        grupo.add(rbPersonalizado);

        panelOpc.add(rbPrincipiante);
        panelOpc.add(Box.createVerticalStrut(4));
        panelOpc.add(rbIntermedio);
        panelOpc.add(Box.createVerticalStrut(4));
        panelOpc.add(rbExperto);
        panelOpc.add(Box.createVerticalStrut(4));
        panelOpc.add(rbPersonalizado);

        // ── Campos personalizados ──
        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 8, 4));
        panelCampos.setBackground(C_BG);
        panelCampos.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        panelCampos.add(labelOpc("Filas (5-24):"));
        panelCampos.add(labelOpc("Cols (5-40):"));
        panelCampos.add(labelOpc("Minas:"));
        panelCampos.add(new JLabel(""));

        campoFilas    = campo("9");
        campoColumnas = campo("9");
        campoMinas    = campo("10");

        panelCampos.add(campoFilas);
        panelCampos.add(campoColumnas);
        panelCampos.add(campoMinas);
        panelCampos.add(new JLabel(""));

        setCamposHabilitados(false);
        panelOpc.add(panelCampos);

        // Activar/desactivar campos
        rbPersonalizado.addActionListener(e -> setCamposHabilitados(true));
        rbPrincipiante .addActionListener(e -> setCamposHabilitados(false));
        rbIntermedio   .addActionListener(e -> setCamposHabilitados(false));
        rbExperto      .addActionListener(e -> setCamposHabilitados(false));

        add(panelOpc, BorderLayout.CENTER);

        // ── Botones ──
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panelBotones.setBackground(new Color(38, 39, 53));
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_SURFACE));

        JButton btnCancelar = boton("Cancelar", false);
        JButton btnAceptar  = boton("Aceptar",  true);

        btnCancelar.addActionListener(e -> dispose());
        btnAceptar .addActionListener(e -> aplicar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void setCamposHabilitados(boolean b) {
        campoFilas   .setEnabled(b);
        campoColumnas.setEnabled(b);
        campoMinas   .setEnabled(b);
    }

    private void aplicar() {
        int f, c, m;

        if (rbPrincipiante.isSelected()) {
            juego.configurarJuego(9, 9, 10);
        } else if (rbIntermedio.isSelected()) {
            juego.configurarJuego(16, 16, 40);
        } else if (rbExperto.isSelected()) {
            juego.configurarJuego(22, 30, 150);
        } else {
            try {
                f = Integer.parseInt(campoFilas.getText().trim());
                c = Integer.parseInt(campoColumnas.getText().trim());
                m = Integer.parseInt(campoMinas.getText().trim());
            } catch (NumberFormatException ex) {
                error("Ingresa solo números enteros en los tres campos.");
                return;
            }

            if (f < 5 || f > 24) { error("Filas debe estar entre 5 y 24."); return; }
            if (c < 5 || c > 40) { error("Columnas debe estar entre 5 y 40."); return; }
            int maxMinas = f * c - 9;
            if (m < 1 || m > maxMinas) {
                error("Minas debe estar entre 1 y " + maxMinas + " para este tablero.");
                return;
            }
            juego.configurarJuego(f, c, m);
        }
        dispose();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Helpers de UI ────────────────────────────────────────────────────────

    private JRadioButton radioButton(String txt, boolean sel) {
        JRadioButton rb = new JRadioButton(txt, sel);
        rb.setBackground(C_BG);
        rb.setForeground(C_TEXTO);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setFocusPainted(false);
        return rb;
    }

    private JTextField campo(String placeholder) {
        JTextField tf = new JTextField(placeholder, 5);
        tf.setBackground(C_SURFACE);
        tf.setForeground(C_TEXTO);
        tf.setCaretColor(C_TEXTO);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return tf;
    }

    private JLabel labelOpc(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(C_MUTED);
        return l;
    }

    private JButton boton(String txt, boolean primario) {
        JButton btn = new JButton(txt);
        btn.setBackground(primario ? new Color(49, 50, 68) : new Color(38, 39, 53));
        btn.setForeground(primario ? C_VERDE : C_MUTED);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primario
                        ? new Color(69, 71, 90) : new Color(49, 50, 68)),
                BorderFactory.createEmptyBorder(5, 18, 5, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
