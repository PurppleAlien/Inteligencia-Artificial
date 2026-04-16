import java.util.LinkedList;
// Clase 5: PuzleLineal_SG_DFS
class PuzleLineal_SG_DFS {
    private Nodo_DFS raiz;
    private int limiteProfundidad = 10; // Para evitar stack overflow
    
    public PuzleLineal_SG_DFS(int[] estadoInicial) {
        this.raiz = new Nodo_DFS(estadoInicial, null);
    }
    
    public LinkedList<int[]> resolver() {
        PilasGLL pila = new PilasGLL();
        pila.apilar(raiz);
        LinkedList<int[]> solucion = new LinkedList<>();
        
        while (!pila.estaVacia()) {
            Nodo_DFS actual = pila.desapilar();
            
            if (actual.esSolucion()) {
                // Reconstruir camino
                while (actual != null) {
                    solucion.addFirst(actual.getEstado());
                    actual = actual.getPadre();
                }
                return solucion;
            }
            
            if (profundidad(actual) < limiteProfundidad) {
                actual.expandir();
                // Apilar en orden inverso para procesar hijo1 primero
                if (actual.getHijo3() != null) pila.apilar(actual.getHijo3());
                if (actual.getHijo2() != null) pila.apilar(actual.getHijo2());
                if (actual.getHijo1() != null) pila.apilar(actual.getHijo1());
            }
        }
        
        return null; // No se encontró solución
    }
    
    private int profundidad(Nodo_DFS nodo) {
        int depth = 0;
        while (nodo.getPadre() != null) {
            depth++;
            nodo = nodo.getPadre();
        }
        return depth;
    }
}