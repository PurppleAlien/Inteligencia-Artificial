import java.util.Arrays;

class Nodo_DFS {
    private int[] estado;
    private Nodo_DFS padre;
    private Nodo_DFS hijo1;
    private Nodo_DFS hijo2;
    private Nodo_DFS hijo3;
    
    public Nodo_DFS(int[] estado, Nodo_DFS padre) {
        this.estado = Arrays.copyOf(estado, estado.length);
        this.padre = padre;
        this.hijo1 = null;
        this.hijo2 = null;
        this.hijo3 = null;
    }
    
    // Getters
    public int[] getEstado() { 
        return Arrays.copyOf(estado, estado.length); 
    }
    
    public Nodo_DFS getPadre() { 
        return padre; 
    }
    
    public Nodo_DFS getHijo1() { 
        return hijo1; 
    }
    
    public Nodo_DFS getHijo2() { 
        return hijo2; 
    }
    
    public Nodo_DFS getHijo3() { 
        return hijo3; 
    }
    
    // Setters para hijos
    public void setHijo1(Nodo_DFS hijo) { 
        this.hijo1 = hijo; 
    }
    
    public void setHijo2(Nodo_DFS hijo) { 
        this.hijo2 = hijo; 
    }
    
    public void setHijo3(Nodo_DFS hijo) { 
        this.hijo3 = hijo; 
    }
    
    public boolean esSolucion() {
        for (int i = 0; i < estado.length - 1; i++) {
            if (estado[i] > estado[i + 1]) {
                return false;
            }
        }
        return true;
    }
    
    // Expande el nodo generando sus tres hijos posibles
    public void expandir() {
        // Operador izquierda (intercambiar posiciones 0 y 1)
        if (hijo1 == null) {
            int[] nuevoEstado1 = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado1, 0, 1);
            hijo1 = new Nodo_DFS(nuevoEstado1, this);
        }
        
        // Operador central (intercambiar posiciones 1 y 2)
        if (hijo2 == null) {
            int[] nuevoEstado2 = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado2, 1, 2);
            hijo2 = new Nodo_DFS(nuevoEstado2, this);
        }
        
        // Operador derecha (intercambiar posiciones 2 y 3)
        if (hijo3 == null) {
            int[] nuevoEstado3 = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado3, 2, 3);
            hijo3 = new Nodo_DFS(nuevoEstado3, this);
        }
    }
    
    // Método auxiliar para intercambiar elementos en un array
    private void intercambiar(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    // Método para obtener la representación en cadena del estado
    @Override
    public String toString() {
        return Arrays.toString(estado);
    }
    
    // Método para verificar si dos nodos tienen el mismo estado
    public boolean igualEstado(Nodo_DFS otro) {
        return Arrays.equals(this.estado, otro.estado);
    }
    
    // Método para calcular la profundidad del nodo en el árbol
    public int getProfundidad() {
        int profundidad = 0;
        Nodo_DFS actual = this;
        while (actual.padre != null) {
            profundidad++;
            actual = actual.padre;
        }
        return profundidad;
    }
}