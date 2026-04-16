public class Main {
    public static void main(String[] args) {
        // Prueba de Búsqueda en Amplitud (BFS)
        System.out.println("=== BÚSQUEDA EN AMPLITUD (BFS) ===");
        try {
            long inicioBFS = System.currentTimeMillis();
            CyM_SG_BFS bfs = new CyM_SG_BFS();
            Nodo_BFS solucionBFS = bfs.resolver();
            long finBFS = System.currentTimeMillis();
            System.out.println("El tiempo de ejecucion de BFS fue de: " + (finBFS - inicioBFS) + " ms");
            CyM_SG_BFS.imprimirSolucion(solucionBFS);
        } catch (Exception e) {
            System.err.println("Error en BFS: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Prueba de Búsqueda en Profundidad con Pila (DFS)
        System.out.println("\n=== BÚSQUEDA EN PROFUNDIDAD (DFS CON PILA) ===");
        try {
            long inicioDFS = System.currentTimeMillis();
            CyM_SG_DFS dfs = new CyM_SG_DFS();
            Nodo_DFS solucionDFS = dfs.resolver();
            long finDFS = System.currentTimeMillis();
            System.out.println("El tiempo de ejecucion de DFS fue de: " + (finDFS - inicioDFS) + " ms");
            CyM_SG_DFS.imprimirSolucion(solucionDFS);
        } catch (Exception e) {
            System.err.println("Error en DFS: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Prueba de Búsqueda en Profundidad Recursiva (DFS Recursivo)
        System.out.println("\n=== BÚSQUEDA EN PROFUNDIDAD RECURSIVA (DFS RECURSIVO) ===");
        try {
            long inicioDFSRec = System.currentTimeMillis();
            CyM_SG_DFS_Recursivo dfsRec = new CyM_SG_DFS_Recursivo();
            Nodo_DFS solucionDFSRec = dfsRec.resolver();
            long finDFSRec = System.currentTimeMillis();
            System.out.println("El tiempo de ejecucion de DFS Recursivo fue de: " + (finDFSRec - inicioDFSRec) + " ms");
            CyM_SG_DFS_Recursivo.imprimirSolucion(solucionDFSRec);
        } catch (StackOverflowError e) {
            System.err.println("Error: StackOverflow en DFS Recursivo. La profundidad de recursión es demasiado grande.");
        } catch (Exception e) {
            System.err.println("Error en DFS Recursivo: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== PRUEBAS COMPLETADAS ===");
    }
}