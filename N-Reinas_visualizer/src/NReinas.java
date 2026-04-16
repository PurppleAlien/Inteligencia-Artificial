import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * N-Queens — versión consola con backtracking optimizado.
 *
 * MEJORAS sobre la versión original:
 * 1. Solver con bitmasks: detección de conflictos en O(1) en lugar de O(n).
 *    Tres enteros codifican columnas, diagonal-\ y diagonal-/ ocupadas.
 * 2. Se eliminó la duplicación: NReinas y NReinasSolver eran idénticos.
 * 3. Métricas de rendimiento: nodos explorados y tiempo de ejecución.
 * 4. Impresión opcional del tablero (evita miles de líneas para N grande).
 */
public class NReinas {

    private final int n;
    private final List<int[]> soluciones = new ArrayList<>();
    private int nodosExplorados = 0;

    public NReinas(int n) {
        if (n < 1) throw new IllegalArgumentException("N debe ser >= 1");
        this.n = n;
    }

    // ── Solver con bitmask ────────────────────────────────────────────────────
    //
    // cols     → bit k activado = columna k ocupada
    // diagBaja → bit k activado = diagonal '\' desde posición actual ocupada
    // diagAlta → bit k activado = diagonal '/' desde posición actual ocupada
    //
    // Al descender de fila r a r+1:
    //   diagBaja se desplaza a la izquierda (la misma '\' está una columna más a la derecha)
    //   diagAlta se desplaza a la derecha  (la misma '/' está una columna más a la izquierda)

    public void resolver() {
        soluciones.clear();
        nodosExplorados = 0;
        int[] tablero = new int[n];
        Arrays.fill(tablero, -1);
        backtracking(0, 0, 0, 0, tablero);
    }

    private void backtracking(int fila, int cols, int diagBaja, int diagAlta, int[] tablero) {
        if (fila == n) {
            soluciones.add(tablero.clone());
            return;
        }
        nodosExplorados++;

        int todas    = (1 << n) - 1;          // máscara con n bits activados
        int bloqueadas = cols | diagBaja | diagAlta;
        int libres     = todas & ~bloqueadas;  // posiciones sin conflicto

        while (libres != 0) {
            int bit = libres & (-libres);      // bit de menor peso (primera columna libre)
            int col = Integer.numberOfTrailingZeros(bit);
            tablero[fila] = col;

            backtracking(
                fila + 1,
                cols | bit,
                (diagBaja | bit) << 1,
                (diagAlta | bit) >> 1,
                tablero
            );

            tablero[fila] = -1;
            libres &= libres - 1;  // quitar el bit procesado
        }
    }

    // ── Salida ────────────────────────────────────────────────────────────────

    public void imprimirResultados(boolean mostrarTableros) {
        System.out.println("=".repeat(50));
        System.out.println("Problema de las " + n + " reinas");
        System.out.println("=".repeat(50));
        System.out.println("Soluciones encontradas : " + soluciones.size());
        System.out.println("Nodos explorados       : " + nodosExplorados);

        if (mostrarTableros) {
            for (int i = 0; i < soluciones.size(); i++) {
                System.out.println("\nSolución #" + (i + 1) + " : "
                        + Arrays.toString(soluciones.get(i)));
                imprimirTablero(soluciones.get(i));
            }
        }
    }

    private void imprimirTablero(int[] solucion) {
        String separador = "+" + "---+".repeat(n);
        for (int fila = 0; fila < n; fila++) {
            System.out.println(separador);
            StringBuilder fila_sb = new StringBuilder("|");
            for (int col = 0; col < n; col++) {
                fila_sb.append(solucion[fila] == col ? " ♛ |" : "   |");
            }
            System.out.println(fila_sb);
        }
        System.out.println(separador);
    }

    public List<int[]> getSoluciones()   { return soluciones; }
    public int         getNodosExplorados() { return nodosExplorados; }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        int n = 8;  // Cambiar aquí para probar otros valores

        NReinas problema = new NReinas(n);

        System.out.println("Resolviendo N=" + n + " reinas...");
        long t0 = System.currentTimeMillis();
        problema.resolver();
        long ms = System.currentTimeMillis() - t0;

        // Para N <= 6 mostramos tableros; para N mayor solo el conteo
        problema.imprimirResultados(n <= 6);
        System.out.println("Tiempo de ejecución    : " + ms + " ms");
        System.out.println("=".repeat(50));

        // Referencia: N=8→92, N=10→724, N=12→14200
    }
}
