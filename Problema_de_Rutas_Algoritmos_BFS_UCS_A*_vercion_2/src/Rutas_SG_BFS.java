import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Rutas_SG_BFS {
    private HashMap<String, String[]> conexiones;
    private Queue<Nodo_BFS> cola;
    private List<String> visitados;
    private int pasos;

    public Rutas_SG_BFS() {
        cola = new LinkedList<>();
        visitados = new ArrayList<>();
        pasos = 0;
        inicializarMapa();
    }

    private void inicializarMapa() {
        conexiones = new HashMap<>();
        
        conexiones.put("Acapulco", new String[]{"Toluca", "DF", "Cuernavaca"});
        conexiones.put("Cuernavaca", new String[]{"Acapulco", "DF"});
        conexiones.put("DF", new String[]{"Queretaro", "Toluca", "Pachuca", "Cuernavaca", "Poza Rica", "Puebla", "Veracruz", "Huatulco"});
        conexiones.put("Huatulco", new String[]{"DF", "Puebla"});
        conexiones.put("Pachuca", new String[]{"DF", "Poza Rica"});
        conexiones.put("Poza Rica", new String[]{"DF", "Pachuca"});
        conexiones.put("Puebla", new String[]{"DF", "Veracruz", "Huatulco"});
        conexiones.put("Queretaro", new String[]{"DF", "Toluca"});
        conexiones.put("Toluca", new String[]{"Queretaro", "DF", "Acapulco"});
        conexiones.put("Veracruz", new String[]{"DF", "Puebla"});
    }

    public String[] getConexiones(String ciudad) {
        return conexiones.get(ciudad);
    }

    public Nodo_BFS resolver(String inicio, String fin) {
        long startTime = System.currentTimeMillis();
        
        // Reiniciar estructuras para cada nueva búsqueda
        cola.clear();
        visitados.clear();
        pasos = 0;
        
        Nodo_BFS nodoInicial = new Nodo_BFS(inicio);
        cola.offer(nodoInicial);
        visitados.add(inicio);
        pasos++;

        while (!cola.isEmpty()) {
            Nodo_BFS actual = cola.poll();

            if (actual.getDatos().equals(fin)) {
                long endTime = System.currentTimeMillis();
                System.out.println("Busqueda en Amplitud - BFS");
                System.out.println("Solucion encontrada, se generaron " + pasos + " pasos para encontrarla");
                System.out.println("El tiempo de ejecucion de BFS fue de: " + (endTime - startTime) + " ms");
                return actual;
            }

            String[] ciudadesAdyacentes = conexiones.get(actual.getDatos());
            if (ciudadesAdyacentes != null) {
                for (String ciudad : ciudadesAdyacentes) {
                    if (!visitados.contains(ciudad)) {
                        Nodo_BFS hijo = new Nodo_BFS(ciudad);
                        hijo.setPadre(actual);
                        cola.offer(hijo);
                        visitados.add(ciudad);
                        pasos++;
                    }
                }
            }
        }
        
        // Si llegamos aquí, no se encontró solución
        long endTime = System.currentTimeMillis();
        System.out.println("Busqueda en Amplitud - BFS");
        System.out.println("No se encontró solución después de " + pasos + " pasos");
        System.out.println("El tiempo de ejecución de BFS fue de: " + (endTime - startTime) + " ms");
        return null;
    }

    public static void imprimirSolucion(Nodo_BFS nodo) {
        if (nodo == null) {
            System.out.println("No se encontró solución");
            return;
        }

        List<Nodo_BFS> camino = new ArrayList<>();
        while (nodo != null) {
            camino.add(0, nodo);
            nodo = nodo.getPadre();
        }

        System.out.println("La solución es:");
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
    }
}