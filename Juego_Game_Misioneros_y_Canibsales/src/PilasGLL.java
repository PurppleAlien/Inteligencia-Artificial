import java.util.LinkedList;

public class PilasGLL {
    private LinkedList<Nodo_DFS> info = new LinkedList<>();
    
    public void apilar(Nodo_DFS nodo) {
        info.addFirst(nodo);
    }
    
    public Nodo_DFS desapilar() {
        return info.pollFirst();
    }
    
    public boolean estaVacia() {
        return info.isEmpty();
    }
    
    public int tamaño() {
        return info.size();
    }
}