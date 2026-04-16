import java.util.LinkedList;

public class ColasGLL {
    private LinkedList<Nodo_BFS> info = new LinkedList<>();
    
    public void Encolar(Nodo_BFS x) {
        if (info.size() == 0) {
            info.add(x);
        } else if (x.get_f_n() < info.get(0).get_f_n()) {
            info.add(0, x);
        } else if (x.get_f_n() > info.get(info.size() - 1).get_f_n()) {
            info.add(info.size(), x);
        } else {
            int i = 0;
            while (x.get_f_n() > info.get(i).get_f_n()) {
                i++;
            }
            info.add(i, x);
        }
    }
    
    public void Append(Nodo_BFS x) {
        info.addLast(x);
    }
    
    public Nodo_BFS Desencolar() {
        return info.removeFirst();
    }
    
    public boolean estaVacia() {
        return info.isEmpty();
    }
    
    public int Tamanio() {
        return info.size();
    }
    
    public Nodo_BFS Obtener_Nodo(int index) {
        return info.get(index);
    }
    
    public void Remove(Nodo_BFS nodo) {
        info.remove(nodo);
    }
    
    public boolean contiene(Nodo_BFS nodo) {
        for (Nodo_BFS n : info) {
            if (n.equals(nodo)) {
                return true;
            }
        }
        return false;
    }
}