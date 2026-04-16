import java.util.LinkedList;

public class ColasGLL {
    private LinkedList<Nodo_BFS> info = new LinkedList<>();

    public void encolar(Nodo_BFS nodo) {
        info.addLast(nodo);
    }

    public Nodo_BFS desencolar() {
        return info.removeFirst();
    }

    public boolean estaVacia() {
        return info.isEmpty();
    }

    public int tamaño() {
        return info.size();
    }

    public void remove(Nodo_BFS nodo) {
        info.remove(nodo);
    }

    public LinkedList<Nodo_BFS> getEstructura() {
        return info;
    }
}