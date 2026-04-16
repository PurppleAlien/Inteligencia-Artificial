import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class AlgoritmoWangGUI_v1 extends JFrame {

    private JTextArea areaSalida;
    private JTextField campoPremisas;
    private JTextField campoConclusion;

    public AlgoritmoWangGUI_v1() {
        setTitle("Demostrador de Teoremas - Algoritmo de Wang");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 600));
        iniciarInterfaz();
    }

    private void iniciarInterfaz() {
    JPanel panelPrincipal = crearPanelPrincipal();  // ← Método correcto
    add(panelPrincipal);
}

    private JPanel crearPanelPrincipal() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // 1. Panel de entrada (parte superior)
    JPanel panelEntrada = new JPanel();
    panelEntrada.setLayout(new BoxLayout(panelEntrada, BoxLayout.Y_AXIS));
    panelEntrada.setBorder(BorderFactory.createTitledBorder("Entrada del Teorema"));

    // Componentes de entrada (premisas y conclusión)
    JPanel panelPremisas = new JPanel(new BorderLayout(5, 5));
    panelPremisas.add(new JLabel("Premisas (separadas por coma):"), BorderLayout.WEST);
    campoPremisas = new JTextField();
    panelPremisas.add(campoPremisas, BorderLayout.CENTER);

    JPanel panelConclusion = new JPanel(new BorderLayout(5, 5));
    panelConclusion.add(new JLabel("Conclusión:"), BorderLayout.WEST);
    campoConclusion = new JTextField();
    panelConclusion.add(campoConclusion, BorderLayout.CENTER);

    // Panel de botones
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    panelBotones.add(crearBoton("DEMOSTRAR", 120, 30));
    panelBotones.add(crearBoton("LIMPIAR", 120, 30));
    panelBotones.add(crearBoton("SALIR", 120, 30));

    // Agregar componentes al panel de entrada
    panelEntrada.add(panelPremisas);
    panelEntrada.add(Box.createVerticalStrut(10));
    panelEntrada.add(panelConclusion);
    panelEntrada.add(Box.createVerticalStrut(15));
    panelEntrada.add(panelBotones);

    // 2. Panel de instrucciones (parte central superior)
    JPanel instruccionesPanel = new JPanel(new BorderLayout());
    instruccionesPanel.setBorder(BorderFactory.createTitledBorder("Instrucciones de Uso"));
    
    JTextArea instruccionesText = new JTextArea(
        "Formato de conectivas lógicas:                   Ejemplos válidos:\n" +
        " - Negación (¬):   notP                                    Premisas: P->Q,Q->R,notR\n" +
        " - Conjunción (∧): PandQ                              Conclusión: notP\n" +
        " - Disyunción (∨): PorQ                                  Premisas: Por(QandR)\n" +
        " - Implicación (→): P->Q                                Conclusión: (PorQ)and(PorR)\n\n"+                                                 
        "Todas las prepociciones deven ir en MAYUSCULAS "
    );
    instruccionesText.setEditable(false);
    instruccionesText.setFont(new Font("Arial", Font.PLAIN, 12));
    instruccionesText.setBackground(new Color(240, 240, 240));
    instruccionesPanel.add(new JScrollPane(instruccionesText), BorderLayout.CENTER);

    // 3. Área de salida (parte central inferior)
    areaSalida = new JTextArea();
    areaSalida.setEditable(false);
    areaSalida.setFont(new Font("Consolas", Font.PLAIN, 13));
    JScrollPane panelDesplazamiento = new JScrollPane(areaSalida);
    panelDesplazamiento.setBorder(BorderFactory.createTitledBorder("Proceso de Demostración"));
    
    // Panel contenedor para instrucciones y salida
    JPanel panelCentral = new JPanel(new BorderLayout());
    panelCentral.add(instruccionesPanel, BorderLayout.NORTH);
    panelCentral.add(panelDesplazamiento, BorderLayout.CENTER);

    // Configuración final del panel principal
    panel.add(panelEntrada, BorderLayout.NORTH);
    panel.add(panelCentral, BorderLayout.CENTER);

    // Asignar acciones a los botones
    ((JButton)panelBotones.getComponent(0)).addActionListener(this::procesarTeorema);
    ((JButton)panelBotones.getComponent(1)).addActionListener(e -> {
        campoPremisas.setText("");
        campoConclusion.setText("");
        areaSalida.setText("");
    });
    ((JButton)panelBotones.getComponent(2)).addActionListener(e -> System.exit(0));

    return panel;
}

    private JButton crearBoton(String texto, int ancho, int alto) {
    JButton boton = new JButton(texto);
    boton.setFont(new Font("Arial", Font.BOLD, 14));
    boton.setBackground(Color.WHITE);  // Fondo blanco 
    boton.setForeground(Color.BLACK);  // Texto en negro
    boton.setFocusPainted(false);
    boton.setPreferredSize(new Dimension(ancho, alto));
    boton.setOpaque(true);
    boton.setBorderPainted(false);
    
    // Opcional: agregar un borde para mejor visibilidad
    boton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    
    return boton;
}

    private void procesarTeorema(ActionEvent e) {
        String premisasStr = campoPremisas.getText().replace(" ", "");
        String conclusionStr = campoConclusion.getText().replace(" ", "");

        if (premisasStr.isEmpty() || conclusionStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Debe ingresar premisas y conclusión", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        areaSalida.setText("");
        
        List<String> premisas = Arrays.asList(premisasStr.split(","));
        String conclusion = conclusionStr;

        areaSalida.append("--- INICIANDO DEMOSTRACIÓN ---\n\n");
        boolean resultado = demostrarTeorema(new ArrayList<>(premisas), conclusion, 0);

        areaSalida.append("\n--- RESULTADO FINAL ---\n");
        areaSalida.append("El teorema << " + premisasStr + " => " + conclusionStr + " >> " +
                (resultado ? "SÍ" : "NO") + " es válido\n");
    }

    private boolean demostrarTeorema(List<String> izquierda, String derecha, int nivel) {
        izquierda = simplificarFormulas(izquierda);
        derecha = simplificarFormula(derecha);

        imprimirPaso(izquierda, derecha, nivel, "Estado actual");

        // Verificar contradicción
        if (hayContradiccion(derecha)) {
            imprimirPaso(izquierda, derecha, nivel, "ÉXITO: contradicción en lado derecho");
            return true;
        }

        // Verificar coincidencia
        if (verificarCoincidencia(izquierda, derecha)) {
            imprimirPaso(izquierda, derecha, nivel, "ÉXITO: fórmula encontrada en ambos lados");
            return true;
        }

        // Verificar si no hay más pasos posibles
        if (!contieneConectivas(izquierda) && !contieneConectivas(derecha)) {
            imprimirPaso(izquierda, derecha, nivel, "FRACASO: no hay coincidencia y no quedan conectivas");
            return false;
        }

        // Paso 3: Eliminar conjunciones en antecedente
        for (int i = 0; i < izquierda.size(); i++) {
            String formula = izquierda.get(i);
            if (encontrarOperadorPrincipal(formula).equals("and")) {
                String[] partes = dividirFormula(formula, "and");
                if (partes != null) {
                    List<String> nuevoIzquierda = new ArrayList<>(izquierda);
                    nuevoIzquierda.remove(i);
                    nuevoIzquierda.add(partes[0]);
                    nuevoIzquierda.add(partes[1]);
                    imprimirPaso(nuevoIzquierda, derecha, nivel, 
                            "Paso 3: Descomponemos conjunción " + formula + " en " + partes[0] + " y " + partes[1]);
                    return demostrarTeorema(nuevoIzquierda, derecha, nivel + 1);
                }
            }
        }

        // Paso 4: Eliminar implicaciones en antecedente
        for (int i = 0; i < izquierda.size(); i++) {
            String formula = izquierda.get(i);
            if (encontrarOperadorPrincipal(formula).equals("->")) {
                String[] partes = dividirFormula(formula, "->");
                if (partes != null) {
                    List<String> nuevoIzquierda = new ArrayList<>(izquierda);
                    nuevoIzquierda.remove(i);
                    nuevoIzquierda.add("not" + partes[0]);
                    nuevoIzquierda.add(partes[1]);
                    imprimirPaso(nuevoIzquierda, derecha, nivel, 
                            "Paso 4: Convertimos " + formula + " a not" + partes[0] + " y " + partes[1]);
                    boolean resultado = demostrarTeorema(nuevoIzquierda, derecha, nivel + 1);
                    if (resultado) {
                        imprimirPaso(izquierda, derecha, nivel, "Retorno desde rama de implicación: ÉXITO");
                        return true;
                    }
                    izquierda.add(i, formula); // Restaurar para probar otros pasos
                    break;
                }
            }
        }

        // Paso 5: Eliminar implicaciones en consecuente
        if (encontrarOperadorPrincipal(derecha).equals("->")) {
            String[] partes = dividirFormula(derecha, "->");
            if (partes != null) {
                List<String> nuevoIzquierda = new ArrayList<>(izquierda);
                nuevoIzquierda.add(partes[0]);
                imprimirPaso(nuevoIzquierda, partes[1], nivel, 
                        "Paso 5: Convertimos " + derecha + " moviendo " + partes[0] + " a izquierda");
                boolean resultado = demostrarTeorema(nuevoIzquierda, partes[1], nivel + 1);
                if (resultado) {
                    imprimirPaso(izquierda, derecha, nivel, "Retorno desde implicación derecha: ÉXITO");
                    return true;
                }
            }
        }

        // Paso 6: Mover negaciones en antecedente
        for (int i = 0; i < izquierda.size(); i++) {
            String formula = izquierda.get(i);
            if (formula.startsWith("not")) {
                if (formula.startsWith("notnot")) {
                    String nuevaFormula = formula.substring(6);
                    List<String> nuevoIzquierda = new ArrayList<>(izquierda);
                    nuevoIzquierda.remove(i);
                    nuevoIzquierda.add(nuevaFormula);
                    imprimirPaso(nuevoIzquierda, derecha, nivel, 
                            "Paso 6: Eliminamos doble negación notnot" + nuevaFormula + " → " + nuevaFormula);
                    return demostrarTeorema(nuevoIzquierda, derecha, nivel + 1);
                } else {
                    String nuevaDerecha = derecha.isEmpty() ? formula.substring(3) : derecha + "," + formula.substring(3);
                    List<String> nuevoIzquierda = new ArrayList<>(izquierda);
                    nuevoIzquierda.remove(i);
                    imprimirPaso(nuevoIzquierda, nuevaDerecha, nivel, 
                            "Paso 6: Movemos not" + formula.substring(3) + " al lado derecho");
                    boolean resultado = demostrarTeorema(nuevoIzquierda, nuevaDerecha, nivel + 1);
                    if (resultado) {
                        imprimirPaso(izquierda, derecha, nivel, "Retorno desde negación izquierda: ÉXITO");
                        return true;
                    }
                    izquierda.add(i, formula);
                    break;
                }
            }
        }

        // Paso 7: Mover negaciones en consecuente
        if (derecha.startsWith("not")) {
            String formula = derecha.substring(3);
            List<String> nuevoIzquierda = new ArrayList<>(izquierda);
            nuevoIzquierda.add(formula);
            imprimirPaso(nuevoIzquierda, "", nivel, 
                    "Paso 7: Movemos not" + formula + " al lado izquierdo");
            boolean resultado = demostrarTeorema(nuevoIzquierda, "", nivel + 1);
            if (resultado) {
                imprimirPaso(izquierda, derecha, nivel, "Retorno desde negación derecha: ÉXITO");
                return true;
            }
        }

        // Paso 8: Eliminar disyunciones en antecedente
        for (int i = 0; i < izquierda.size(); i++) {
            String formula = izquierda.get(i);
            if (encontrarOperadorPrincipal(formula).equals("or")) {
                String[] partes = dividirFormula(formula, "or");
                if (partes != null) {
                    List<String> izq1 = new ArrayList<>(izquierda);
                    izq1.remove(i);
                    izq1.add(partes[0]);
                    
                    List<String> izq2 = new ArrayList<>(izquierda);
                    izq2.remove(i);
                    izq2.add(partes[1]);
                    
                    imprimirPaso(izq1, derecha, nivel, 
                            "Paso 8: Rama 1 de disyunción izquierda: " + partes[0]);
                    boolean rama1 = demostrarTeorema(izq1, derecha, nivel + 1);
                    
                    imprimirPaso(izq2, derecha, nivel, 
                            "Paso 8: Rama 2 de disyunción izquierda: " + partes[1]);
                    boolean rama2 = demostrarTeorema(izq2, derecha, nivel + 1);
                    
                    boolean resultado = rama1 || rama2;
                    if (resultado) {
                        imprimirPaso(izquierda, derecha, nivel, "Retorno desde disyunción izquierda: ÉXITO");
                    }
                    return resultado;
                }
            }
        }

        // Paso 9: Eliminar conjunciones en consecuente
        if (encontrarOperadorPrincipal(derecha).equals("and")) {
            String[] partes = dividirFormula(derecha, "and");
            if (partes != null) {
                imprimirPaso(izquierda, partes[0], nivel, 
                        "Paso 9: Rama 1 de conjunción derecha: " + partes[0]);
                boolean rama1 = demostrarTeorema(new ArrayList<>(izquierda), partes[0], nivel + 1);
                
                imprimirPaso(izquierda, partes[1], nivel, 
                        "Paso 9: Rama 2 de conjunción derecha: " + partes[1]);
                boolean rama2 = demostrarTeorema(new ArrayList<>(izquierda), partes[1], nivel + 1);
                
                boolean resultado = rama1 && rama2;
                if (resultado) {
                    imprimirPaso(izquierda, derecha, nivel, "Retorno desde conjunción derecha: ÉXITO");
                }
                return resultado;
            }
        }

        imprimirPaso(izquierda, derecha, nivel, "FRACASO: no hay más pasos posibles");
        return false;
    }

    private String[] dividirFormula(String formula, String operador) {
        formula = simplificarFormula(formula);
        int nivelParentesis = 0;
        int posOperador = -1;
        
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') nivelParentesis++;
            if (c == ')') nivelParentesis--;
            
            if (nivelParentesis == 0 && formula.substring(i).startsWith(operador)) {
                posOperador = i;
                break;
            }
        }
        
        if (posOperador == -1) return null;
        
        return new String[]{
            formula.substring(0, posOperador),
            formula.substring(posOperador + operador.length())
        };
    }

    private String encontrarOperadorPrincipal(String formula) {
        formula = simplificarFormula(formula);
        if (formula.startsWith("not")) return "not";
        
        int nivelParentesis = 0;
        String[] operadores = {"->", "and", "or"};
        
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') nivelParentesis++;
            if (c == ')') nivelParentesis--;
            
            if (nivelParentesis == 0) {
                for (String op : operadores) {
                    if (formula.substring(i).startsWith(op)) {
                        return op;
                    }
                }
            }
        }
        return ""; // Atómica
    }

    private List<String> simplificarFormulas(List<String> formulas) {
        List<String> resultado = new ArrayList<>();
        for (String formula : formulas) {
            resultado.add(simplificarFormula(formula));
        }
        return resultado;
    }

    private String simplificarFormula(String formula) {
    if (formula == null || formula.isEmpty()) return formula;
    
    formula = formula.replaceAll("\\s+", "");
    
    // Eliminar sólo paréntesis externos que no afecten la estructura
    while (puedeEliminarParentesisExternos(formula)) {
        formula = formula.substring(1, formula.length()-1);
    }
    
    // Simplificar negaciones
    if (formula.startsWith("not(") && formula.endsWith(")")) {
        String interior = formula.substring(4, formula.length()-1);
        return "not" + simplificarFormula(interior);
    }
    
    return formula;
}

private boolean puedeEliminarParentesisExternos(String formula) {
    if (!formula.startsWith("(") || !formula.endsWith(")")) {
        return false;
    }
    
    int balance = 0;
    for (int i = 1; i < formula.length()-1; i++) {
        char c = formula.charAt(i);
        if (c == '(') balance++;
        if (c == ')') balance--;
        if (balance < 0) return false;
    }
    return balance == 0;
}

    private boolean parentesisBalanceados(String s) {
        int balance = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false;
        }
        return balance == 0;
    }

    private boolean hayContradiccion(String derecha) {
        if (derecha == null || derecha.isEmpty()) return false;
        
        List<String> formulas = Arrays.asList(derecha.split(","));
        Set<String> negaciones = new HashSet<>();
        Set<String> afirmaciones = new HashSet<>();
        
        for (String formula : formulas) {
            formula = simplificarFormula(formula);
            if (formula.startsWith("not")) {
                String atomo = simplificarFormula(formula.substring(3));
                if (afirmaciones.contains(atomo)) {
                    return true;
                }
                negaciones.add(atomo);
            } else {
                if (negaciones.contains(formula)) {
                    return true;
                }
                afirmaciones.add(formula);
            }
        }
        return false;
    }

        private boolean verificarCoincidencia(List<String> izquierda, String derecha) {
    if (derecha == null || derecha.isEmpty()) return false;
    
    List<String> derechaList = Arrays.asList(derecha.split(","));
    
    for (String formulaIzq : izquierda) {
        String izqSimpl = simplificarFormula(formulaIzq);
        
        for (String formulaDer : derechaList) {
            String derSimpl = simplificarFormula(formulaDer);
            
            // Coincidencia directa
            if (izqSimpl.equals(derSimpl)) {
                return true;
            }
            
            // Caso especial: P coincide con PorQ
            if (derSimpl.contains("or") && derSimpl.contains(izqSimpl)) {
                return true;
            }
        }
    }
    return false;
}

    private boolean contieneConectivas(List<String> formulas) {
        for (String formula : formulas) {
            if (contieneConectivas(formula)) {
                return true;
            }
        }
        return false;
    }

    private boolean contieneConectivas(String formula) {
        formula = simplificarFormula(formula);
        return formula.contains("not") || formula.contains("and") ||
               formula.contains("or") || formula.contains("->");
    }

    private void imprimirPaso(List<String> izquierda, String derecha, int nivel, String mensaje) {
        StringBuilder indentacion = new StringBuilder();
        for (int i = 0; i < nivel; i++) {
            indentacion.append("| ");
        }

        areaSalida.append(indentacion + mensaje + "\n");
        areaSalida.append(indentacion + "Lado izquierdo: " + String.join(", ", izquierda) + "\n");
        areaSalida.append(indentacion + "Lado derecho: " + 
            (derecha == null || derecha.isEmpty() ? "<vacío>" : derecha) + "\n");
        areaSalida.append(indentacion + "----------------\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AlgoritmoWangGUI_v1 gui = new AlgoritmoWangGUI_v1();
            gui.setVisible(true);
        });
    }
}