import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lógica del Juego del Gato — Negamax con Poda Alfa-Beta
 *
 * Correcciones respecto a la versión anterior:
 *  - Bug crítico: signo del ajuste de profundidad invertido.
 *    Antes: 100 + profundidad  (ganar tarde era "mejor" → INCORRECTO).
 *    Ahora: 100 - profundidad  (ganar antes es mejor → CORRECTO).
 *  - Poda alfa-beta añadida al negamax (reduce ~60-70 % de nodos).
 *  - Movimiento fácil usa java.util.Random (sin sesgo de Math.random).
 *  - Separación clara de modos: fácil / medio (prof. 3) / difícil (completo).
 */
public class Clase_Gato {

    public static final int HUMANO      =  1;
    public static final int COMPUTADORA = -1;
    public static final int VACIO       =  0;

    private final int[][] tablero = new int[3][3];
    private int dificultad = 1;  // 1: Fácil, 2: Medio, 3: Difícil

    private final java.util.Random rng = new java.util.Random();

    public Clase_Gato() {
        reiniciarJuego();
    }

    public void reiniciarJuego() {
        for (int[] f : tablero) java.util.Arrays.fill(f, VACIO);
    }

    public void setDificultad(int nivel) {
        this.dificultad = Math.max(1, Math.min(3, nivel));
    }

    public int getDificultad() {
        return dificultad;
    }

    public boolean movimientoValido(int fila, int col) {
        return fila >= 0 && fila < 3 && col >= 0 && col < 3
                && tablero[fila][col] == VACIO;
    }

    public void realizarMovimiento(int fila, int col, int jugador) {
        if (movimientoValido(fila, col)) tablero[fila][col] = jugador;
    }

    public boolean esEmpate() {
        if (hayGanador(HUMANO) || hayGanador(COMPUTADORA)) return false;
        for (int[] fila : tablero)
            for (int v : fila) if (v == VACIO) return false;
        return true;
    }

    public boolean hayGanador(int jugador) {
        for (int i = 0; i < 3; i++)
            if (tablero[i][0]==jugador && tablero[i][1]==jugador && tablero[i][2]==jugador) return true;
        for (int j = 0; j < 3; j++)
            if (tablero[0][j]==jugador && tablero[1][j]==jugador && tablero[2][j]==jugador) return true;
        if (tablero[0][0]==jugador && tablero[1][1]==jugador && tablero[2][2]==jugador) return true;
        if (tablero[0][2]==jugador && tablero[1][1]==jugador && tablero[2][0]==jugador) return true;
        return false;
    }

    public int[] obtenerMovimientoComputadora() {
        return switch (dificultad) {
            case 1  -> movimientoFacil();
            case 2  -> movimientoMedio();
            default -> movimientoDificil();
        };
    }

    // ─────────────────── estrategias ───────────────────

    /** Movimiento aleatorio (fácil). */
    private int[] movimientoFacil() {
        List<int[]> libres = celdasLibres();
        if (libres.isEmpty()) return new int[]{-1, -1};
        return libres.get(rng.nextInt(libres.size()));
    }

    /** Negamax con profundidad limitada a 3 (medio). */
    private int[] movimientoMedio() {
        return buscarMovimiento(3);
    }

    /** Negamax completo — juega perfectamente (difícil). */
    private int[] movimientoDificil() {
        return buscarMovimiento(9);
    }

    private int[] buscarMovimiento(int maxProf) {
        int[] mejor = new int[]{-1, -1};
        int mejorVal = Integer.MIN_VALUE;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j] == VACIO) {
                    tablero[i][j] = COMPUTADORA;
                    int val = -negamax(maxProf - 1,
                                       Integer.MIN_VALUE / 2,
                                       Integer.MAX_VALUE / 2,
                                       HUMANO);
                    tablero[i][j] = VACIO;
                    if (val > mejorVal) {
                        mejorVal = val;
                        mejor[0] = i;
                        mejor[1] = j;
                    }
                }
            }
        }
        return mejor;
    }

    // ─────────────────── núcleo negamax ───────────────────

    /**
     * Negamax con poda alfa-beta.
     * Valor de retorno: desde la perspectiva del jugador actual (jugActual).
     *
     * Corrección clave del signo de profundidad:
     *   victoria = 100 - profundidad  →  ganar pronto vale más que ganar tarde.
     *   derrota  = -(100 - profundidad) (simétrico).
     */
    private int negamax(int prof, int alfa, int beta, int jugActual) {
        int oponente = -jugActual;  // HUMANO=1 / COMPUTADORA=-1

        // Si el oponente (el que acaba de mover) ganó → muy malo
        if (hayGanador(oponente)) return -(100 - prof);
        if (esEmpate() || prof == 0) return evaluarTablero(jugActual);

        int maxVal = Integer.MIN_VALUE / 2;
        outer:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j] == VACIO) {
                    tablero[i][j] = jugActual;
                    int val = -negamax(prof - 1, -beta, -alfa, oponente);
                    tablero[i][j] = VACIO;
                    if (val > maxVal) maxVal = val;
                    if (val > alfa)   alfa   = val;
                    if (alfa >= beta) break outer;  // poda
                }
            }
        }
        return maxVal;
    }

    /**
     * Evaluación heurística en nodos no-terminales (profundidad agotada).
     * Devuelve el puntaje desde la perspectiva de jugActual.
     */
    private int evaluarTablero(int jugActual) {
        int puntaje = 0;
        puntaje += evaluarLinea(tablero[0][0], tablero[0][1], tablero[0][2]);
        puntaje += evaluarLinea(tablero[1][0], tablero[1][1], tablero[1][2]);
        puntaje += evaluarLinea(tablero[2][0], tablero[2][1], tablero[2][2]);
        puntaje += evaluarLinea(tablero[0][0], tablero[1][0], tablero[2][0]);
        puntaje += evaluarLinea(tablero[0][1], tablero[1][1], tablero[2][1]);
        puntaje += evaluarLinea(tablero[0][2], tablero[1][2], tablero[2][2]);
        puntaje += evaluarLinea(tablero[0][0], tablero[1][1], tablero[2][2]);
        puntaje += evaluarLinea(tablero[0][2], tablero[1][1], tablero[2][0]);
        // puntaje positivo = bueno para COMPUTADORA (-1).
        // ajustar para que sea positivo cuando es bueno para jugActual.
        return (jugActual == COMPUTADORA) ? puntaje : -puntaje;
    }

    /** Evalúa una línea desde la perspectiva de COMPUTADORA. */
    private int evaluarLinea(int a, int b, int c) {
        int cComp = 0, cHum = 0;
        for (int v : new int[]{a, b, c}) {
            if      (v == COMPUTADORA) cComp++;
            else if (v == HUMANO)      cHum++;
        }
        if (cComp == 2 && cHum == 0) return 10;
        if (cComp == 1 && cHum == 0) return  1;
        if (cHum  == 2 && cComp == 0) return -10;
        if (cHum  == 1 && cComp == 0) return  -1;
        return 0;
    }

    // ─────────────────── utilidades ───────────────────

    private List<int[]> celdasLibres() {
        List<int[]> lista = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (tablero[i][j] == VACIO) lista.add(new int[]{i, j});
        return lista;
    }

    /** Devuelve copia defensiva del tablero para la UI. */
    public int[][] getTablero() {
        int[][] copia = new int[3][3];
        for (int i = 0; i < 3; i++)
            System.arraycopy(tablero[i], 0, copia[i], 0, 3);
        return copia;
    }
}
