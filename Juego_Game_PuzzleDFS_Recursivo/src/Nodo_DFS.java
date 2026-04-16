import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Nodo del espacio de búsqueda del puzzle de ordenación.
 *
 * MEJORAS sobre la versión original:
 * 1. Funciona con arrays de cualquier tamaño (original fijo en 4 elementos).
 *    Los operadores de intercambio adyacente se generan dinámicamente.
 * 2. La función de calidad es consistente: rango [0, n-1].
 *    Original añadía +1 bonus al estar ordenado, creando rango [0, n] — inconsistente.
 * 3. Se añadió el nombre del operador aplicado para mejor traza educativa.
 * 4. Método getPath() para reconstruir el camino completo hasta este nodo.
 */
public class Nodo_DFS {

    private final int[]     estado;
    private final Nodo_DFS  padre;
    private final String    operadorAplicado;  // descripción del intercambio
    private final int       profundidad;
    private final int       calidad;

    public Nodo_DFS(int[] estado, Nodo_DFS padre) {
        this(estado, padre, "inicio", padre == null ? 0 : padre.profundidad + 1);
    }

    public Nodo_DFS(int[] estado, Nodo_DFS padre, String operador, int profundidad) {
        this.estado           = Arrays.copyOf(estado, estado.length);
        this.padre            = padre;
        this.operadorAplicado = operador;
        this.profundidad      = profundidad;
        this.calidad          = calcularCalidad();
    }

    // ── Heurística ────────────────────────────────────────────────────────────
    //
    // Calidad = número de pares adyacentes en orden ascendente correcto.
    // Rango: 0 (completamente invertido) a n-1 (ordenado).
    //
    // FIX: La versión original añadía +1 cuando count == n-1, haciendo el
    // rango 0..n en lugar de 0..n-1. Esto creaba una inconsistencia en la
    // función de calidad y dificultaba comparar nodos padre-hijo.

    private int calcularCalidad() {
        int cuenta = 0;
        for (int i = 0; i < estado.length - 1; i++) {
            if (estado[i] < estado[i + 1]) cuenta++;
        }
        return cuenta;
    }

    // ── Expansión del nodo ────────────────────────────────────────────────────
    //
    // Genera hijos aplicando todos los intercambios adyacentes posibles.
    // FIX: Original tenía tres bloques if hardcodeados para índices 0,1,2,3.
    // Ahora funciona para arrays de cualquier longitud n >= 2.

    public List<Nodo_DFS> generarHijos() {
        List<Nodo_DFS> hijos = new ArrayList<>();
        for (int i = 0; i < estado.length - 1; i++) {
            int[] nuevoEstado = Arrays.copyOf(estado, estado.length);
            // Intercambio adyacente en posición i
            int tmp = nuevoEstado[i];
            nuevoEstado[i]     = nuevoEstado[i + 1];
            nuevoEstado[i + 1] = tmp;
            String nombreOp = "swap(" + i + "↔" + (i + 1) + ")";
            hijos.add(new Nodo_DFS(nuevoEstado, this, nombreOp, profundidad + 1));
        }
        return hijos;
    }

    // ── Test de objetivo ──────────────────────────────────────────────────────

    /** Retorna true si el array está ordenado en orden no decreciente. */
    public boolean esSolucion() {
        for (int i = 0; i < estado.length - 1; i++) {
            if (estado[i] > estado[i + 1]) return false;
        }
        return true;
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /** Comprueba si este nodo ya está en la lista dada (por valor del estado). */
    public boolean enLista(List<Nodo_DFS> lista) {
        for (Nodo_DFS n : lista) {
            if (Arrays.equals(estado, n.estado)) return true;
        }
        return false;
    }

    /** Reconstruye el camino completo desde la raíz hasta este nodo. */
    public List<Nodo_DFS> getCamino() {
        List<Nodo_DFS> camino = new ArrayList<>();
        Nodo_DFS nodo = this;
        while (nodo != null) {
            camino.add(0, nodo);
            nodo = nodo.padre;
        }
        return camino;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int[]    getEstado()            { return Arrays.copyOf(estado, estado.length); }
    public Nodo_DFS getPadre()             { return padre; }
    public String   getOperadorAplicado()  { return operadorAplicado; }
    public int      getProfundidad()       { return profundidad; }
    public int      getCalidad()           { return calidad; }

    @Override
    public String toString() { return Arrays.toString(estado); }
}
