import java.util.LinkedList;
// Clase 2: PuzleLineal_SG_BFS
class PuzleLineal_SG_BFS {
    private Nodo_BFS raiz;
    
    public PuzleLineal_SG_BFS(int[] estadoInicial) {
        this.raiz = new Nodo_BFS(estadoInicial, null);
    }
    
    public LinkedList<int[]> resolver() {
        ColasGLL cola = new ColasGLL();
        cola.encolar(raiz);
        LinkedList<int[]> solucion = new LinkedList<>();
        
        while (!cola.estaVacia()) {
            Nodo_BFS actual = cola.desencolar();
            
            if (actual.esSolucion()) {
                // Reconstruir camino
                while (actual != null) {
                    solucion.addFirst(actual.getEstado());
                    actual = actual.getPadre();
                }
                return solucion;
            }
            
            actual.expandir();
            if (actual.getHijo1() != null) cola.encolar(actual.getHijo1());
            if (actual.getHijo2() != null) cola.encolar(actual.getHijo2());
            if (actual.getHijo3() != null) cola.encolar(actual.getHijo3());
        }
        
        return null; // No se encontró solución
    }
}