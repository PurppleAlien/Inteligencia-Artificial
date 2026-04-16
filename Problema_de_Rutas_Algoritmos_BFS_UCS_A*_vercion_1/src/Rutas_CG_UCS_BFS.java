import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Rutas_CG_UCS_BFS extends JFrame {
    private JComboBox<String> ciudadesInicio, ciudadesFin;
    private JRadioButton bfsRadio, ucsRadio;
    private JTextArea resultadoArea;
    private ButtonGroup grupoBusqueda;

    public Rutas_CG_UCS_BFS() {
        setTitle("Problema Búsqueda de Rutas");
        setSize(850, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con imagen y controles
        JPanel panelSuperior = new JPanel(new BorderLayout(15, 15));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel para la imagen del grafo (izquierda)
        JPanel panelImagen = new JPanel(new BorderLayout());
        panelImagen.setPreferredSize(new Dimension(600, 400));
        
        try {
            // Cargar imagen desde src como recurso
            ImageIcon imagenOriginal = new ImageIcon(getClass().getResource("/G1.png"));
            
            // Redimensionar manteniendo proporción (1084x902 -> 550x457)
            int nuevoAncho = 450;
            int nuevoAlto = (int) (300 * (nuevoAncho / 350));
            Image imagenRedimensionada = imagenOriginal.getImage()
                .getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
            
            JLabel labelGrafo = new JLabel(new ImageIcon(imagenRedimensionada));
            labelGrafo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Mapa de Conexiones"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            panelImagen.add(labelGrafo);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("No se pudo cargar el mapa de conexiones", JLabel.CENTER);
            errorLabel.setForeground(Color.RED);
            panelImagen.add(errorLabel);
        }

        // Panel de controles (derecha)
        JPanel panelControles = new JPanel(new GridLayout(5, 1, 10, 10));
        panelControles.setBorder(BorderFactory.createTitledBorder("Configuración de Búsqueda"));
        panelControles.setPreferredSize(new Dimension(350, 400));

        // Panel para selección de ciudades
        JPanel panelCiudades = new JPanel(new GridLayout(2, 2, 5, 10));
        panelCiudades.add(new JLabel("Estado inicial:"));
        ciudadesInicio = new JComboBox<>(new String[]{"", "Acapulco", "Cuernavaca", "DF", "Huatulco", 
            "Pachuca", "Poza Rica", "Puebla", "Queretaro", "Toluca", "Veracruz"});
        ciudadesInicio.setFont(new Font("Arial", Font.PLAIN, 14));
        panelCiudades.add(ciudadesInicio);

        panelCiudades.add(new JLabel("Estado final:"));
        ciudadesFin = new JComboBox<>(new String[]{"", "Acapulco", "Cuernavaca", "DF", "Huatulco", 
            "Pachuca", "Poza Rica", "Puebla", "Queretaro", "Toluca", "Veracruz"});
        ciudadesFin.setFont(new Font("Arial", Font.PLAIN, 14));
        panelCiudades.add(ciudadesFin);

        // Panel para selección de algoritmo
        JPanel panelAlgoritmo = new JPanel(new GridLayout(3, 1, 5, 5));
        panelAlgoritmo.setBorder(BorderFactory.createTitledBorder("Algoritmo de Búsqueda"));
        bfsRadio = new JRadioButton("Búsqueda en Amplitud (BFS)");
        ucsRadio = new JRadioButton("Búsqueda de Costo Uniforme (UCS)");
        grupoBusqueda = new ButtonGroup();
        grupoBusqueda.add(bfsRadio);
        grupoBusqueda.add(ucsRadio);
        panelAlgoritmo.add(bfsRadio);
        panelAlgoritmo.add(ucsRadio);

        // Botón de búsqueda
        JButton comenzarBtn = new JButton("Ejecutar Búsqueda");
        comenzarBtn.setBackground(new Color(0, 102, 204));
        comenzarBtn.setForeground(Color.BLACK);
        comenzarBtn.setFont(new Font("Arial", Font.BOLD, 14));
        comenzarBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarBusqueda();
            }
        });

        // Ensamblar panel de controles
        panelControles.add(panelCiudades);
        panelControles.add(panelAlgoritmo);
        panelControles.add(new JLabel()); // Espacio vacío
        panelControles.add(comenzarBtn);

        // Área de resultados
        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        resultadoArea.setFont(new Font("Courier New", Font.PLAIN, 13));
        resultadoArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Resultados de la Búsqueda"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane scrollPane = new JScrollPane(resultadoArea);
        scrollPane.setPreferredSize(new Dimension(1050, 550));

        // Organización final
        panelSuperior.add(panelImagen, BorderLayout.CENTER);
        panelSuperior.add(panelControles, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Configuración final de la ventana
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void ejecutarBusqueda() {
        resultadoArea.setText("");
        
        // Validaciones
        if (ciudadesInicio.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un estado inicial", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (ciudadesFin.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un estado final", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!bfsRadio.isSelected() && !ucsRadio.isSelected()) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un algoritmo de búsqueda", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String inicio = (String) ciudadesInicio.getSelectedItem();
        String fin = (String) ciudadesFin.getSelectedItem();
        
        if (bfsRadio.isSelected()) {
            ejecutarBFS(inicio, fin);
        } else {
            ejecutarUCS(inicio, fin);
        }
    }

    private void ejecutarBFS(String inicio, String fin) {
        try {
            PrintStream originalOut = System.out;
            StringBuilder consoleOutput = new StringBuilder();
            
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    consoleOutput.append((char) b);
                }
            }));
            
            Rutas_SG_BFS bfs = new Rutas_SG_BFS();
            Nodo_BFS solucion = bfs.resolver(inicio, fin);
            System.setOut(originalOut);
            
            // Mostrar resultados
            resultadoArea.append("=== BÚSQUEDA EN AMPLITUD (BFS) ===\n");
            resultadoArea.append("• Estado inicial: " + inicio + "\n");
            resultadoArea.append("• Estado final: " + fin + "\n\n");
            resultadoArea.append(consoleOutput.toString() + "\n");
            
            if (solucion != null) {
                resultadoArea.append("■ SOLUCIÓN ENCONTRADA:\n");
                List<String> camino = new ArrayList<>();
                Nodo_BFS actual = solucion;
                while (actual != null) {
                    camino.add(0, actual.getDatos());
                    actual = actual.getPadre();
                }
                
                resultadoArea.append("↳ Ruta: " + String.join(" → ", camino) + "\n");
                resultadoArea.append("↳ Total de ciudades en la ruta: " + camino.size() + "\n");
                
                // Mostrar conexiones para depuración
                resultadoArea.append("\nConexiones disponibles desde " + inicio + ":\n");
                String[] conexiones = bfs.getConexiones(inicio);
                if (conexiones != null) {
                    resultadoArea.append("• " + String.join("\n• ", conexiones) + "\n");
                }
            } else {
                resultadoArea.append("■ NO SE ENCONTRÓ RUTA ENTRE LAS CIUDADES SELECCIONADAS\n");
            }
        } catch (Exception e) {
            resultadoArea.append("ERROR en BFS: " + e.getMessage() + "\n");
        }
    }

    private void ejecutarUCS(String inicio, String fin) {
        try {
            PrintStream originalOut = System.out;
            StringBuilder consoleOutput = new StringBuilder();
            
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    consoleOutput.append((char) b);
                }
            }));
            
            Rutas_SG_UCS ucs = new Rutas_SG_UCS();
            Nodo_BFS solucion = ucs.resolver(inicio, fin);
            System.setOut(originalOut);
            
            // Mostrar resultados
            resultadoArea.append("=== BÚSQUEDA DE COSTO UNIFORME (UCS) ===\n");
            resultadoArea.append("• Estado inicial: " + inicio + "\n");
            resultadoArea.append("• Estado final: " + fin + "\n\n");
            resultadoArea.append(consoleOutput.toString() + "\n");
            
            if (solucion != null) {
                resultadoArea.append("■ SOLUCIÓN ENCONTRADA:\n");
                List<String> camino = new ArrayList<>();
                Nodo_BFS actual = solucion;
                while (actual != null) {
                    camino.add(0, actual.getDatos());
                    actual = actual.getPadre();
                }
                
                resultadoArea.append("↳ Ruta: " + String.join(" → ", camino) + "\n");
                resultadoArea.append("↳ Total de ciudades: " + camino.size() + "\n");
                resultadoArea.append("↳ Costo total: " + solucion.getCoste() + " km\n");
                
                // Detalle de costos
                resultadoArea.append("\n■ DETALLE DE COSTOS:\n");
                actual = solucion;
                while (actual != null && actual.getPadre() != null) {
                    int costoEtapa = actual.getCoste() - actual.getPadre().getCoste();
                    resultadoArea.append(String.format(
                        "• %s → %s: %d km\n", 
                        actual.getPadre().getDatos(), 
                        actual.getDatos(), 
                        costoEtapa));
                    actual = actual.getPadre();
                }
            } else {
                resultadoArea.append("■ NO SE ENCONTRÓ RUTA ENTRE LAS CIUDADES SELECCIONADAS\n");
            }
        } catch (Exception e) {
            resultadoArea.append("ERROR en UCS: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new Rutas_CG_UCS_BFS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}