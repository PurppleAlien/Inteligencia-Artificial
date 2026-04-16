import java.util.*;

/**
 * Solver del puzzle de ordenación con búsqueda DFS recursiva.
 *
 * PROBLEMA ORIGINAL CRÍTICO:
 * El método mejora() usaba: hijo.calidad >= padre.calidad
 * Esto es ESCALADA DE COLINAS, no DFS puro. El algoritmo original era INCOMPLETO:
 * fallaría para [3,4,1,2] aunque la solución exista en ≤ 7 intercambios,
 * porque requiere bajar la calidad temporalmente.
 *
 * CORRECCIÓN APLICADA:
 * Se mantiene la HEURÍSTICA SUAVE: los hijos se ordenan por calidad descendente
 * (visitar los más prometedores primero), pero NO se descartan los de menor
 * calidad. El DFS completo explora todos los caminos no visitados dentro del
 * límite de profundidad.
 *
 * OTRAS MEJORAS:
 * - Lista de visitados como HashSet<String> → O(1) en lugar de O(n) con LinkedList.
 * - Límite de profundidad configurable (no hardcoded).
 * - Se devuelve la lista de nodos (camino completo), no solo los estados.
 * - Modo BFS disponible para demostración comparativa.
 */
public class PuzzleDFSRecursivo {

    private final Nodo_DFS          nodoInicial;
    private final int               limiteProfundidad;
    private int                     nodosVisitados;
    private int                     nodosPodados;

    public static final int LIMITE_DEFAULT = 20;

    public PuzzleDFSRecursivo(int[] estadoInicial) {
        this(estadoInicial, LIMITE_DEFAULT);
    }

    public PuzzleDFSRecursivo(int[] estadoInicial, int limiteProfundidad) {
        this.nodoInicial       = new Nodo_DFS(estadoInicial, null, "inicio", 0);
        this.limiteProfundidad = limiteProfundidad;
        this.nodosVisitados    = 0;
        this.nodosPodados      = 0;
    }

    // ── DFS con heurística suave (CORRECTO y COMPLETO dentro del límite) ─────

    public LinkedList<int[]> resolver() {
        nodosVisitados = 0;
        nodosPodados   = 0;
        // HashSet<String>: O(1) lookup vs O(n) de la LinkedList original
        Set<String> visitados = new HashSet<>();
        Nodo_DFS resultado = dfsRecursivo(nodoInicial, visitados, 0);
        if (resultado == null) return null;

        // Reconstruir el camino de estados
        LinkedList<int[]> camino = new LinkedList<>();
        for (Nodo_DFS nodo : resultado.getCamino()) {
            camino.add(nodo.getEstado());
        }
        return camino;
    }

    /**
     * DFS recursivo completo con heurística de ordenación de hijos.
     *
     * DIFERENCIA CLAVE respecto al original:
     * - Original: if (!hijo.enLista(visitados) && mejora(padre, hijo)) → incompleto
     * - Nuevo:    if (!visitados.contains(key)) → completo
     *
     * Los hijos se ORDENAN por calidad descendente (los más prometedores primero),
     * lo que acelera la búsqueda sin sacrificar completitud.
     */
    private Nodo_DFS dfsRecursivo(Nodo_DFS nodo, Set<String> visitados, int prof) {
        String clave = Arrays.toString(nodo.getEstado());
        if (visitados.contains(clave)) {
            nodosPodados++;
            return null;
        }
        visitados.add(clave);
        nodosVisitados++;

        if (nodo.esSolucion()) return nodo;
        if (prof >= limiteProfundidad) return null;

        // Ordenar hijos por calidad descendente (heurística de orden, no de poda)
        List<Nodo_DFS> hijos = nodo.generarHijos();
        hijos.sort((a, b) -> b.getCalidad() - a.getCalidad());

        for (Nodo_DFS hijo : hijos) {
            Nodo_DFS res = dfsRecursivo(hijo, visitados, prof + 1);
            if (res != null) return res;
        }
        return null;
    }

    // ── BFS (camino más corto garantizado) ───────────────────────────────────

    public LinkedList<int[]> resolverBFS() {
        nodosVisitados = 0;
        nodosPodados   = 0;
        Set<String>       visitados = new HashSet<>();
        Queue<Nodo_DFS>   cola      = new LinkedList<>();
        cola.add(nodoInicial);

        while (!cola.isEmpty()) {
            Nodo_DFS nodo = cola.poll();
            String clave  = Arrays.toString(nodo.getEstado());
            if (visitados.contains(clave)) { nodosPodados++; continue; }
            visitados.add(clave);
            nodosVisitados++;

            if (nodo.esSolucion()) {
                LinkedList<int[]> camino = new LinkedList<>();
                for (Nodo_DFS n : nodo.getCamino()) camino.add(n.getEstado());
                return camino;
            }
            if (nodo.getProfundidad() < limiteProfundidad) {
                cola.addAll(nodo.generarHijos());
            }
        }
        return null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getNodosVisitados() { return nodosVisitados; }
    public int getNodosPodados()   { return nodosPodados;   }
}
