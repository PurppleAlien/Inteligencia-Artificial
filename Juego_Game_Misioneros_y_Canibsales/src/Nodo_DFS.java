import java.util.LinkedList;

public final class Nodo_DFS extends LinkedList<Object> {
    private int[] estado; // [mIzq, cIzq, bote, mDer, cDer]
    private Nodo_DFS padre;
    private Nodo_DFS hijo1;
    private Nodo_DFS hijo2;
    private Nodo_DFS hijo3;
    
    public Nodo_DFS(int[] estado) {
        this.estado = estado;
        this.padre = null;
        this.hijo1 = null;
        this.hijo2 = null;
        this.hijo3 = null;
    }
    
    public int[] getEstado() {
        return estado;
    }
    
    public Nodo_DFS getPadre() {
        return padre;
    }
    
    public void setPadre(Nodo_DFS padre) {
        this.padre = padre;
    }
    
    public Nodo_DFS getHijo1() {
        return hijo1;
    }
    
    public void setHijo1(Nodo_DFS hijo1) {
        this.hijo1 = hijo1;
    }
    
    public Nodo_DFS getHijo2() {
        return hijo2;
    }
    
    public void setHijo2(Nodo_DFS hijo2) {
        this.hijo2 = hijo2;
    }
    
    public Nodo_DFS getHijo3() {
        return hijo3;
    }
    
    public void setHijo3(Nodo_DFS hijo3) {
        this.hijo3 = hijo3;
    }
    
    @Override
    public String toString() {
        return "[" + estado[0] + "," + estado[1] + "," + estado[2] + "," + estado[3] + "," + estado[4] + "]";
    }
}