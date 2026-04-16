import java.util.LinkedList;

public class ColasGLL_Ordenada {
    private LinkedList<Nodo_BFS> info = new LinkedList<>();

    public void encolarOrdenado(Nodo_BFS val) {
        if (info.size() == 0) {
            info.add(val);
        } else if (info.get(0).getCoste() > val.getCoste()) {
            info.add(0, val);
        } else if (info.get(info.size() - 1).getCoste() < val.getCoste()) {
            info.add(info.size(), val);
        } else {
            int i = 0;
            while (info.get(i).getCoste() < val.getCoste()) {
                i++;
            }
            info.add(i, val);
        }
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