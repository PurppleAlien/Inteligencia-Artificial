import java.util.LinkedList;

public class ColasGLL_Ordenada_A{
    private LinkedList<Nodo_BFS> Info;

    public ColasGLL_Ordenada_A() {
        Info = new LinkedList<>();
    }

    public void encolar(Nodo_BFS x) {
        if (Info.isEmpty()) {
            Info.add(x);
        } else if (x.getFn() < Info.get(0).getFn()) {
            Info.add(0, x);
        } else if (x.getFn() > Info.get(Info.size() - 1).getFn()) {
            Info.add(x);
        } else {
            int i = 0;
            while (x.getFn() > Info.get(i).getFn()) {
                i++;
            }
            Info.add(i, x);
        }
    }

    public Nodo_BFS desencolar() {
        return Info.pollFirst();
    }

    public boolean estaVacia() {
        return Info.isEmpty();
    }

    public boolean contiene(Nodo_BFS nodoNuevo) {
        for (Nodo_BFS nodoExistente : Info) {
            if (nodoExistente.getDatos().equals(nodoNuevo.getDatos())) {
                return true;
            }
        }
        return false;
    }

    public boolean actualizarSiMejor(Nodo_BFS nodoNuevo) {
        for (int i = 0; i < Info.size(); i++) {
            Nodo_BFS nodoExistente = Info.get(i);
            if (nodoExistente.getDatos().equals(nodoNuevo.getDatos())) {
                if (nodoNuevo.getFn() < nodoExistente.getFn()) {
                    Info.remove(i);
                    encolar(nodoNuevo);
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
