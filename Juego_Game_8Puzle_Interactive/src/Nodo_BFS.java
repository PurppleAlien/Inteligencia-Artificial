import java.util.Arrays;

public class Nodo_BFS {
    private int[] estado;
    private Nodo_BFS padre;
    private int costo; // g(n)
    private int heuristica; // h(n)
    private int f_n; // f(n) = g(n) + h(n)
    
    public Nodo_BFS(int[] estado, Nodo_BFS padre) {
        this.estado = Arrays.copyOf(estado, estado.length);
        this.padre = padre;
        this.costo = (padre != null) ? padre.getCosto() + 1 : 0;
        calcularHeuristicas();
    }
    
    private void calcularHeuristicas() {
        // Heurística 1: Tejas mal colocadas
        int h1 = calcularH1();
        // Heurística 2: Distancia Manhattan
        int h2 = calcularH2();
        // Heurística 3: h2 + 3*S(n)
        int cle = h2 + 3 * calcularS();
        
        // Seleccionar la heurística a usar (cambiar según sea necesario)
        this.heuristica = h2; // Por defecto usamos Manhattan
        this.f_n = this.costo + this.heuristica;
    }
    
    private int calcularH1() {
        // Tejas mal colocadas
        int contador = 0;
        int[] estadoFinal = {1, 2, 3, 4, 5, 6, 7, 8, 0};
        
        for (int i = 0; i < 9; i++) {
            if (estado[i] != 0 && estado[i] != estadoFinal[i]) {
                contador++;
            }
        }
        return contador;
    }
    
    private int calcularH2() {
        // Distancia Manhattan
        int distancia = 0;
        int[] estadoFinal = {1, 2, 3, 4, 5, 6, 7, 8, 0};
        
        for (int i = 0; i < 9; i++) {
            if (estado[i] != 0) {
                int valor = estado[i];
                int posicionFinal = -1;
                
                // Buscar la posición correcta del valor
                for (int j = 0; j < 9; j++) {
                    if (estadoFinal[j] == valor) {
                        posicionFinal = j;
                        break;
                    }
                }
                
                // Calcular distancia Manhattan
                int filaActual = i / 3;
                int colActual = i % 3;
                int filaFinal = posicionFinal / 3;
                int colFinal = posicionFinal % 3;
                
                distancia += Math.abs(filaActual - filaFinal) + Math.abs(colActual - colFinal);
            }
        }
        return distancia;
    }
    
    private int calcularS() {
        int s = 0;
        int[] estadoFinal = {1, 2, 3, 4, 5, 6, 7, 8, 0};
        
        // Verificar teja central (posición 4)
        if (estado[4] != estadoFinal[4]) {
            s += 1;
        }
        
        // Verificar secuencia de tejas no centrales
        int[] posicionesNoCentrales = {0, 1, 2, 3, 5, 6, 7, 8};
        
        for (int i = 0; i < posicionesNoCentrales.length; i++) {
            int pos = posicionesNoCentrales[i];
            if (estado[pos] != 0) {
                // Verificar si el sucesor es correcto
                int sucesorCorrecto = (pos < 8) ? estadoFinal[pos + 1] : -1;
                if (sucesorCorrecto != -1 && estado[pos] != estadoFinal[pos]) {
                    int posSucesor = -1;
                    for (int j = 0; j < 9; j++) {
                        if (estado[j] == sucesorCorrecto) {
                            posSucesor = j;
                            break;
                        }
                    }
                    
                    if (posSucesor != pos + 1) {
                        s += 2;
                    }
                }
            }
        }
        
        return s;
    }
    
    // Getters
    public int[] getEstado() { return Arrays.copyOf(estado, estado.length); }
    public Nodo_BFS getPadre() { return padre; }
    public int getCosto() { return costo; }
    public int getHeuristica() { return heuristica; }
    public int get_f_n() { return f_n; }
    
    // Método para verificar si es solución
    public boolean esSolucion() {
        int[] estadoFinal = {1, 2, 3, 4, 5, 6, 7, 8, 0};
        return Arrays.equals(estado, estadoFinal);
    }
    
    // Método para generar sucesores
    public Nodo_BFS[] generarSucesores() {
        Nodo_BFS[] sucesores = new Nodo_BFS[4]; // Máximo 4 movimientos posibles
        int posVacia = -1;
        
        // Encontrar posición del espacio vacío (0)
        for (int i = 0; i < 9; i++) {
            if (estado[i] == 0) {
                posVacia = i;
                break;
            }
        }
        
        int fila = posVacia / 3;
        int col = posVacia % 3;
        int index = 0;
        
        // Movimiento arriba
        if (fila > 0) {
            int[] nuevoEstado = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado, posVacia, posVacia - 3);
            sucesores[index++] = new Nodo_BFS(nuevoEstado, this);
        }
        
        // Movimiento abajo
        if (fila < 2) {
            int[] nuevoEstado = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado, posVacia, posVacia + 3);
            sucesores[index++] = new Nodo_BFS(nuevoEstado, this);
        }
        
        // Movimiento izquierda
        if (col > 0) {
            int[] nuevoEstado = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado, posVacia, posVacia - 1);
            sucesores[index++] = new Nodo_BFS(nuevoEstado, this);
        }
        
        // Movimiento derecha
        if (col < 2) {
            int[] nuevoEstado = Arrays.copyOf(estado, estado.length);
            intercambiar(nuevoEstado, posVacia, posVacia + 1);
            sucesores[index++] = new Nodo_BFS(nuevoEstado, this);
        }
        
        return Arrays.copyOf(sucesores, index);
    }
    
    private void intercambiar(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Nodo_BFS nodo = (Nodo_BFS) obj;
        return Arrays.equals(estado, nodo.estado);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(estado);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(estado) + " (g=" + costo + ", h=" + heuristica + ", f=" + f_n + ")";
    }
}