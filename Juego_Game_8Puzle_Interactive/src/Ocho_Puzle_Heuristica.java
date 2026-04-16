import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Ocho_Puzle_Heuristica {
    private int[] estadoInicial;
    private int heuristicaSeleccionada; // 1: h1, 2: h2, 3: h3
    
    public Ocho_Puzle_Heuristica(int[] estadoInicial, int heuristica) {
        this.estadoInicial = Arrays.copyOf(estadoInicial, estadoInicial.length);
        this.heuristicaSeleccionada = heuristica;
    }
    
    public LinkedList<int[]> resolver() {
        long inicio = System.currentTimeMillis();
        int nodosExplorados = 0;
        
        ColasGLL nodosFrontera = new ColasGLL();
        Set<String> nodosVisitados = new HashSet<>();
        
        Nodo_BFS nodoInicial = new Nodo_BFS(estadoInicial, null);
        nodosFrontera.Encolar(nodoInicial);
        
        while (!nodosFrontera.estaVacia()) {
            Nodo_BFS nodoActual = nodosFrontera.Desencolar();
            nodosExplorados++;
            
            if (nodoActual.esSolucion()) {
                long fin = System.currentTimeMillis();
                double tiempo = (fin - inicio) / 1000.0;
                
                // Reconstruir camino
                LinkedList<int[]> solucion = new LinkedList<>();
                Nodo_BFS actual = nodoActual;
                
                while (actual != null) {
                    solucion.addFirst(actual.getEstado());
                    actual = actual.getPadre();
                }
                
                // Mostrar resultados
                System.out.println("Algoritmo A*");
                System.out.println("Se ha encontrado una solución");
                System.out.println("Se exploraron " + nodosExplorados + " nodos");
                System.out.println("El tiempo de ejecución fue de: " + tiempo + " segundos");
                System.out.println("La solución es:");
                
                for (int[] estado : solucion) {
                    System.out.print(Arrays.toString(estado) + " -> ");
                }
                System.out.println("SOLUCIÓN");
                
                return solucion;
            }
            
            nodosVisitados.add(Arrays.toString(nodoActual.getEstado()));
            
            // Generar sucesores
            Nodo_BFS[] sucesores = nodoActual.generarSucesores();
            for (Nodo_BFS sucesor : sucesores) {
                String claveEstado = Arrays.toString(sucesor.getEstado());
                
                if (!nodosVisitados.contains(claveEstado)) {
                    if (nodosFrontera.contiene(sucesor)) {
                        // Verificar si el nuevo camino es mejor
                        for (int j = 0; j < nodosFrontera.Tamanio(); j++) {
                            Nodo_BFS nodoEnFrontera = nodosFrontera.Obtener_Nodo(j);
                            if (nodoEnFrontera.equals(sucesor) && 
                                nodoEnFrontera.get_f_n() > sucesor.get_f_n()) {
                                nodosFrontera.Remove(nodoEnFrontera);
                                nodosFrontera.Encolar(sucesor);
                                break;
                            }
                        }
                    } else {
                        nodosFrontera.Encolar(sucesor);
                    }
                }
            }
        }
        
        System.out.println("No se encontró solución para el estado inicial: " + Arrays.toString(estadoInicial));
        return null;
    }
    
    
}