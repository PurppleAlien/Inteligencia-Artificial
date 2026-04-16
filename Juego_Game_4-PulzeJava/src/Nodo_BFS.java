import java.util.Arrays;

// Clase 1: Nodo_BFS para búsqueda por amplitud
class Nodo_BFS {
    private int[] estado;
    private Nodo_BFS padre;
    private Nodo_BFS hijo1;
    private Nodo_BFS hijo2;
    private Nodo_BFS hijo3;
    
    public Nodo_BFS(int[] estado, Nodo_BFS padre) {
        this.estado = estado;
        this.padre = padre;
    }
    
    public int[] getEstado() { return estado; }
    public Nodo_BFS getPadre() { return padre; }
    public Nodo_BFS getHijo1() { return hijo1; }
    public Nodo_BFS getHijo2() { return hijo2; }
    public Nodo_BFS getHijo3() { return hijo3; }
    
    public void setHijo1(Nodo_BFS hijo) { this.hijo1 = hijo; }
    public void setHijo2(Nodo_BFS hijo) { this.hijo2 = hijo; }
    public void setHijo3(Nodo_BFS hijo) { this.hijo3 = hijo; }
    
    public boolean esSolucion() {
        for (int i = 0; i < estado.length - 1; i++) {
            if (estado[i] > estado[i + 1]) {
                return false;
            }
        }
        return true;
    }
    
    public void expandir() {
        // Operador izquierda (intercambiar posiciones 0 y 1)
        int[] nuevoEstado1 = Arrays.copyOf(estado, estado.length);
        intercambiar(nuevoEstado1, 0, 1);
        hijo1 = new Nodo_BFS(nuevoEstado1, this);
        
        // Operador central (intercambiar posiciones 1 y 2)
        int[] nuevoEstado2 = Arrays.copyOf(estado, estado.length);
        intercambiar(nuevoEstado2, 1, 2);
        hijo2 = new Nodo_BFS(nuevoEstado2, this);
        
        // Operador derecha (intercambiar posiciones 2 y 3)
        int[] nuevoEstado3 = Arrays.copyOf(estado, estado.length);
        intercambiar(nuevoEstado3, 2, 3);
        hijo3 = new Nodo_BFS(nuevoEstado3, this);
    }
    
    private void intercambiar(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}