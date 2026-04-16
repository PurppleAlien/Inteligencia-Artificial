// Clase 6: PilasGLL

import java.util.LinkedList;

class PilasGLL {
    private LinkedList<Nodo_DFS> info = new LinkedList<>();
    
    public void apilar(Nodo_DFS nodo) {
        info.addFirst(nodo);
    }
    
    public Nodo_DFS desapilar() {
        return info.removeFirst();
    }
    
    public boolean estaVacia() {
        return info.isEmpty();
    }
}