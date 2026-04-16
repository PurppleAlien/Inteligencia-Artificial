public class Nodo_BFS {
    private String datos;
    private Nodo_BFS[] hijos;
    private Nodo_BFS padre;
    private int coste;  // g(n)
    private int fn;     // f(n)

    public Nodo_BFS(String datos) {
        this.datos = datos;
        this.hijos = null;
        this.padre = null;
        this.coste = 0;
        this.fn = 0;
    }

    public String getDatos() {
        return datos;
    }

    public Nodo_BFS[] getHijos() {
        return hijos;
    }

    public void setHijos(Nodo_BFS[] hijos) {
        this.hijos = hijos;
    }

    public Nodo_BFS getPadre() {
        return padre;
    }

    public void setPadre(Nodo_BFS padre) {
        this.padre = padre;
    }

    public int getCoste() {
        return coste;
    }

    public void setCoste(int coste) {
        this.coste = coste;
    }

    public int getFn() {
        return fn;
    }

    public void setFn(int fn) {
        this.fn = fn;
    }

    public boolean igual(Nodo_BFS otro) {
        return this.datos.equals(otro.getDatos());
    }

    @Override
    public String toString() {
        return datos;
    }
}
