import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Rutas_SG_UCS {
    private HashMap<String, HashMap<String, Integer>> conexiones;
    private ColasGLL_Ordenada cola;
    private List<Nodo_BFS> visitados;
    private int pasos;

    public Rutas_SG_UCS() {
        cola = new ColasGLL_Ordenada();
        visitados = new ArrayList<>();
        pasos = 0;
        inicializarMapa();
    }

    private void inicializarMapa() {
        conexiones = new HashMap<>();

        // Inicializar cada ciudad con sus conexiones y costos
        HashMap<String, Integer> acapulco = new HashMap<>();
        acapulco.put("Toluca", 388);
        acapulco.put("DF", 382);
        acapulco.put("Cuernavaca", 291);
        conexiones.put("Acapulco", acapulco);

        HashMap<String, Integer> cuernavaca = new HashMap<>();
        cuernavaca.put("Acapulco", 291);
        cuernavaca.put("DF", 49);
        conexiones.put("Cuernavaca", cuernavaca);

        HashMap<String, Integer> df = new HashMap<>();
        df.put("Queretaro", 218);
        df.put("Toluca", 60);
        df.put("Pachuca", 90);
        df.put("Cuernavaca", 49);
        df.put("Poza Rica", 276);
        df.put("Puebla", 134);
        df.put("Veracruz", 397);
        df.put("Huatulco", 695);
        conexiones.put("DF", df);

        HashMap<String, Integer> huatulco = new HashMap<>();
        huatulco.put("DF", 695);
        huatulco.put("Puebla", 596);
        conexiones.put("Huatulco", huatulco);

        HashMap<String, Integer> pachuca = new HashMap<>();
        pachuca.put("DF", 90);
        pachuca.put("Poza Rica", 183);
        conexiones.put("Pachuca", pachuca);

        HashMap<String, Integer> pozaRica = new HashMap<>();
        pozaRica.put("DF", 276);
        pozaRica.put("Pachuca", 183);
        conexiones.put("Poza Rica", pozaRica);

        HashMap<String, Integer> puebla = new HashMap<>();
        puebla.put("DF", 134);
        puebla.put("Veracruz", 273);
        puebla.put("Huatulco", 596);
        conexiones.put("Puebla", puebla);

        HashMap<String, Integer> queretaro = new HashMap<>();
        queretaro.put("DF", 218);
        queretaro.put("Toluca", 196);
        conexiones.put("Queretaro", queretaro);

        HashMap<String, Integer> toluca = new HashMap<>();
        toluca.put("Queretaro", 196);
        toluca.put("DF", 60);
        toluca.put("Acapulco", 388);
        conexiones.put("Toluca", toluca);

        HashMap<String, Integer> veracruz = new HashMap<>();
        veracruz.put("DF", 397);
        veracruz.put("Puebla", 273);
        conexiones.put("Veracruz", veracruz);
    }

    public Nodo_BFS resolver(String inicio, String fin) {
        long startTime = System.currentTimeMillis();
        
        // Reiniciar estructuras para cada búsqueda
        cola = new ColasGLL_Ordenada();
        visitados.clear();
        pasos = 0;
        
        Nodo_BFS nodoInicial = new Nodo_BFS(inicio);
        nodoInicial.setCoste(0);
        cola.encolarOrdenado(nodoInicial);
        pasos++;

        while (!cola.estaVacia()) {
            Nodo_BFS actual = cola.desencolar();

            // Solo marcamos como visitado cuando lo sacamos de la cola
            visitados.add(actual);

            if (actual.getDatos().equals(fin)) {
                long endTime = System.currentTimeMillis();
                System.out.println("Busqueda de Coste Uniforme - UCS");
                System.out.println("Solucion encontrada, se generaron " + pasos + " pasos para encontrarla");
                System.out.println("El tiempo de ejecucion de UCS fue de: " + (endTime - startTime) + " ms");
                return actual;
            }

            HashMap<String, Integer> conexionesCiudad = conexiones.get(actual.getDatos());
            if (conexionesCiudad != null) {
                for (String ciudad : conexionesCiudad.keySet()) {
                    int coste = conexionesCiudad.get(ciudad);
                    Nodo_BFS hijo = new Nodo_BFS(ciudad);
                    hijo.setCoste(actual.getCoste() + coste);
                    hijo.setPadre(actual);

                    if (!estaVisitado(hijo)) {
                        if (estaEnFrontera(hijo)) {
                            Nodo_BFS existente = obtenerDeFrontera(hijo);
                            if (existente.getCoste() > hijo.getCoste()) {
                                cola.remove(existente);
                                cola.encolarOrdenado(hijo);
                                pasos++;
                            }
                        } else {
                            cola.encolarOrdenado(hijo);
                            pasos++;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean estaVisitado(Nodo_BFS nodo) {
        for (Nodo_BFS visitado : visitados) {
            if (visitado.getDatos().equals(nodo.getDatos())) {
                return true;
            }
        }
        return false;
    }

    private boolean estaEnFrontera(Nodo_BFS nodo) {
        for (Nodo_BFS n : cola.getEstructura()) {
            if (n.getDatos().equals(nodo.getDatos())) {
                return true;
            }
        }
        return false;
    }

    private Nodo_BFS obtenerDeFrontera(Nodo_BFS nodo) {
        for (Nodo_BFS n : cola.getEstructura()) {
            if (n.getDatos().equals(nodo.getDatos())) {
                return n;
            }
        }
        return null;
    }

    public static void imprimirSolucion(Nodo_BFS nodo) {
        if (nodo == null) {
            System.out.println("No se encontró solución");
            return;
        }

        List<Nodo_BFS> camino = new ArrayList<>();
        int costoTotal = nodo.getCoste();
        Nodo_BFS actual = nodo;
        
        while (actual != null) {
            camino.add(0, actual);
            actual = actual.getPadre();
        }

        System.out.println("La solución es:");
        for (int i = 0; i < camino.size(); i++) {
            System.out.print(camino.get(i));
            if (i < camino.size() - 1) {
                System.out.print(" -> ");
            }
        }
        
        System.out.println("\nCosto total del recorrido: " + costoTotal + " km");
        
        // Mostrar detalles de costos entre nodos
        System.out.println("\nDetalle de costos:");
        for (int i = 1; i < camino.size(); i++) {
            Nodo_BFS origen = camino.get(i-1);
            Nodo_BFS destino = camino.get(i);
            int costoEtapa = destino.getCoste() - origen.getCoste();
            System.out.println(origen.getDatos() + " -> " + destino.getDatos() + ": " + costoEtapa + " km");
        }
    }
}