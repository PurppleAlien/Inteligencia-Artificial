import java.util.ArrayList;
import java.util.List;

public class CyM_SG_DFS_Recursivo {
    private List<Nodo_DFS> visitados;
    private int nodosGenerados;
    private Nodo_DFS solucion;
    
    public CyM_SG_DFS_Recursivo() {
        visitados = new ArrayList<>();
        nodosGenerados = 0;
        solucion = null;
    }
    
    public Nodo_DFS resolver() {
        int[] estadoInicial = {3, 3, 0, 0, 0};
        Nodo_DFS nodoInicial = new Nodo_DFS(estadoInicial);
        visitados.add(nodoInicial);
        nodosGenerados++;
        
        dfs(nodoInicial);
        
        if (solucion != null) {
            System.out.println("Busqueda en profundidad recursiva Finalizada");
            System.out.println("se generaron " + nodosGenerados + " nodos");
        } else {
            System.out.println("No se encontró solución");
        }
        
        return solucion;
    }
    
    private void dfs(Nodo_DFS actual) {
        if (solucion != null) {
            return; // Ya se encontró solución
        }
        
        if (esSolucion(actual.getEstado())) {
            solucion = actual;
            return;
        }
        
        // Generar hijos
        /*Define 5 posibles movimientos del bote:
            {1, 0}: 1 misionero
            {2, 0}: 2 misioneros
            {0, 1}: 1 caníbal
            {0, 2}: 2 caníbales
            {1, 1}: 1 misionero y 1 caníbal
         */
        List<Nodo_DFS> hijos = generarHijos(actual);
        
        for (Nodo_DFS hijo : hijos) {
            if (!estaVisitado(hijo)) {
                visitados.add(hijo);
                nodosGenerados++;
                dfs(hijo);
            }
        }
    }
    
    private boolean esSolucion(int[] estado) {
        return estado[0] == 0 && estado[1] == 0 && estado[2] == 1 && estado[3] == 3 && estado[4] == 3;
    }
    

    private boolean estaVisitado(Nodo_DFS nodo) {
        for (Nodo_DFS visitado : visitados) {
            if (sonIguales(visitado.getEstado(), nodo.getEstado())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean sonIguales(int[] estado1, int[] estado2) {
        for (int i = 0; i < 5; i++) {
            if (estado1[i] != estado2[i]) {
                return false;
            }
        }
        return true;
    }
    
    private List<Nodo_DFS> generarHijos(Nodo_DFS padre) {
        List<Nodo_DFS> hijos = new ArrayList<>();
        int[] estadoActual = padre.getEstado();
        
        // Posibles movimientos
        int[][] movimientos = {
            {1, 0}, {2, 0}, {0, 1}, {0, 2}, {1, 1}
        };
        
        for (int[] movimiento : movimientos) {
            int m = movimiento[0];
            int c = movimiento[1];
            
            if (estadoActual[2] == 0) { // Bote en izquierda
                int[] nuevoEstado = {
                    estadoActual[0] - m,
                    estadoActual[1] - c,
                    1,
                    estadoActual[3] + m,
                    estadoActual[4] + c
                };
                
                if (esEstadoValido(nuevoEstado)) {
                    Nodo_DFS hijo = new Nodo_DFS(nuevoEstado);
                    hijo.setPadre(padre);
                    hijos.add(hijo);
                }
            } else { // Bote en derecha
                int[] nuevoEstado = {
                    estadoActual[0] + m,
                    estadoActual[1] + c,
                    0,
                    estadoActual[3] - m,
                    estadoActual[4] - c
                };
                

                
                if (esEstadoValido(nuevoEstado)) {
                    Nodo_DFS hijo = new Nodo_DFS(nuevoEstado);
                    hijo.setPadre(padre);
                    hijos.add(hijo);
                }
            }
        }
        
        return hijos;
    }

    /*No hay números negativos No hay más de 3 misioneros/caníbales en ninguna orilla
      Los misioneros no son superados en número por caníbales en ninguna orilla */ 
    
    private boolean esEstadoValido(int[] estado) {
        // No puede haber números negativos
        for (int i = 0; i < 5; i++) {
            if (estado[i] < 0) {
                return false;
            }
        }
        
        // No puede haber más de 3 misioneros o caníbales en cualquier lado
        if (estado[0] > 3 || estado[1] > 3 || estado[3] > 3 || estado[4] > 3) {
            return false;
        }
        
        // En cualquier orilla, si hay misioneros, no pueden ser superados en número por caníbales
        if ((estado[0] > 0 && estado[0] < estado[1]) || (estado[3] > 0 && estado[3] < estado[4])) {
            return false;
        }
        
        return true;
    }
    
    public static void imprimirSolucion(Nodo_DFS nodo) {
        if (nodo == null) {
            System.out.println("No se encontró solución");
            return;
        }
        
        List<Nodo_DFS> camino = new ArrayList<>();
        while (nodo != null) {
            camino.add(0, nodo);
            nodo = nodo.getPadre();
        }
        
        System.out.println("La solucion es:");
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
    }
}