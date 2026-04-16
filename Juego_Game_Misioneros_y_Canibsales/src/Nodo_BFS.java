import java.util.LinkedList;

public final class Nodo_BFS extends LinkedList<Object> {
    private int[] estado; // [mIzq, cIzq, bote, mDer, cDer]
    private Nodo_BFS padre;
    private Nodo_BFS hijo1;
    private Nodo_BFS hijo2;
    private Nodo_BFS hijo3;
    
    public Nodo_BFS(int[] estado) {
        this.estado = estado;
        this.padre = null;
        this.hijo1 = null;
        this.hijo2 = null;
        this.hijo3 = null;
    }
    
    public int[] getEstado() {
        return estado;
    }
    
    public Nodo_BFS getPadre() {
        return padre;
    }
    
    public void setPadre(Nodo_BFS padre) {
        this.padre = padre;
    }
    
    public Nodo_BFS getHijo1() {
        return hijo1;
    }
    
    public void setHijo1(Nodo_BFS hijo1) {
        this.hijo1 = hijo1;
    }
    
    public Nodo_BFS getHijo2() {
        return hijo2;
    }
    
    public void setHijo2(Nodo_BFS hijo2) {
        this.hijo2 = hijo2;
    }
    
    public Nodo_BFS getHijo3() {
        return hijo3;
    }
    
    public void setHijo3(Nodo_BFS hijo3) {
        this.hijo3 = hijo3;
    }
    
    @Override
    public String toString() {
        return "[" + estado[0] + "," + estado[1] + "," + estado[2] + "," + estado[3] + "," + estado[4] + "]";
    }
}