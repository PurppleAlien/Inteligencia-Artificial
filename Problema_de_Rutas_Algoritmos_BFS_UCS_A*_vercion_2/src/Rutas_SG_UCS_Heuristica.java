import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rutas_SG_UCS_Heuristica {
    private HashMap<String, HashMap<String, Integer>> conexiones;
    private HashMap<String, double[]> coordenadas;

    public Rutas_SG_UCS_Heuristica() {
        conexiones = new HashMap<>();
        coordenadas = new HashMap<>();
        inicializarMapa();
        inicializarCoordenadas();
    }

    private void inicializarMapa() {
        conexiones = new HashMap<>();

        // Conexiones desde Acapulco
        HashMap<String, Integer> acapulco = new HashMap<>();
        acapulco.put("Toluca", 388);
        acapulco.put("DF", 382);
        acapulco.put("Cuernavaca", 291);
        conexiones.put("Acapulco", acapulco);

        // Conexiones desde Cuernavaca
        HashMap<String, Integer> cuernavaca = new HashMap<>();
        cuernavaca.put("Acapulco", 291);
        cuernavaca.put("DF", 49);
        conexiones.put("Cuernavaca", cuernavaca);

        // Conexiones desde DF
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

        // Conexiones desde Huatulco
        HashMap<String, Integer> huatulco = new HashMap<>();
        huatulco.put("DF", 695);
        huatulco.put("Puebla", 596);
        conexiones.put("Huatulco", huatulco);

        // Conexiones desde Pachuca
        HashMap<String, Integer> pachuca = new HashMap<>();
        pachuca.put("DF", 90);
        pachuca.put("Poza Rica", 183);
        conexiones.put("Pachuca", pachuca);

        // Conexiones desde Poza Rica
        HashMap<String, Integer> pozaRica = new HashMap<>();
        pozaRica.put("DF", 276);
        pozaRica.put("Pachuca", 183);
        conexiones.put("Poza Rica", pozaRica);

        // Conexiones desde Puebla
        HashMap<String, Integer> puebla = new HashMap<>();
        puebla.put("DF", 134);
        puebla.put("Veracruz", 273);
        puebla.put("Huatulco", 596);
        conexiones.put("Puebla", puebla);

        // Conexiones desde Queretaro
        HashMap<String, Integer> queretaro = new HashMap<>();
        queretaro.put("DF", 218);
        queretaro.put("Toluca", 196);
        conexiones.put("Queretaro", queretaro);

        // Conexiones desde Toluca
        HashMap<String, Integer> toluca = new HashMap<>();
        toluca.put("Queretaro", 196);
        toluca.put("DF", 60);
        toluca.put("Acapulco", 388);
        conexiones.put("Toluca", toluca);

        // Conexiones desde Veracruz
        HashMap<String, Integer> veracruz = new HashMap<>();
        veracruz.put("DF", 397);
        veracruz.put("Puebla", 273);
        conexiones.put("Veracruz", veracruz);
    }

    private void inicializarCoordenadas() {
        coordenadas.put("Acapulco", new double[]{16.85, 99.82});
        coordenadas.put("DF", new double[]{19.25, 99.13});
        coordenadas.put("Cuernavaca", new double[]{18.92, 99.22});
        coordenadas.put("Puebla", new double[]{19.04, 98.21});
        coordenadas.put("Pachuca", new double[]{20.10, 98.76});
        coordenadas.put("Toluca", new double[]{19.28, 99.66});
        coordenadas.put("Queretaro", new double[]{20.56, 100.39});
        coordenadas.put("Huatulco", new double[]{15.83, 96.31});
        coordenadas.put("Veracruz", new double[]{19.17, 96.13});
        coordenadas.put("Poza Rica", new double[]{20.53, 97.46});
    }

    // Método para calcular la distancia geodésica en km entre dos puntos lat-lon
    public double geodist(double lat1, double lon1, double lat2, double lon2) {
        double grad_rad = 0.01745329;
        double rad_grad = 57.29577951;
        double longitud = (lon1 - lon2);
        double valor = (Math.sin(lat1 * grad_rad) * Math.sin(lat2 * grad_rad))
                + (Math.cos(lat1 * grad_rad) * Math.cos(lat2 * grad_rad) * Math.cos(longitud * grad_rad));
        return (Math.acos(valor) * rad_grad) * 111.32;
    }

    // Heurística: distancia estimada en línea recta del nodo al destino + costo acumulado g(n)
    public int heuristica(Nodo_BFS nodo, String destino) {
        double lat1 = coordenadas.get(nodo.getDatos())[0];
        double lon1 = coordenadas.get(nodo.getDatos())[1];
        double lat2 = coordenadas.get(destino)[0];
        double lon2 = coordenadas.get(destino)[1];

        int distanciaLineaRecta = (int) geodist(lat1, lon1, lat2, lon2);
        return nodo.getCoste() + distanciaLineaRecta;
    }

    // Algoritmo A* que devuelve ruta, costo y tiempo de ejecución
    public ResultadoBusqueda buscarSolucionAStar(String origen, String destino) {
        long inicio = System.currentTimeMillis();
        
        List<String> resultado = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        ColasGLL_Ordenada_A frontera = new ColasGLL_Ordenada_A();
        int costoTotal = 0;

        Nodo_BFS nodoInicial = new Nodo_BFS(origen);
        nodoInicial.setCoste(0);
        nodoInicial.setFn(heuristica(nodoInicial, destino));
        frontera.encolar(nodoInicial);

        while (!frontera.estaVacia()) {
            Nodo_BFS actual = frontera.desencolar();
            String ciudadActual = actual.getDatos();

            if (ciudadActual.equals(destino)) {
                // Reconstruir camino
                Nodo_BFS aux = actual;
                while (aux != null) {
                    resultado.add(0, aux.getDatos());
                    aux = aux.getPadre();
                }
                costoTotal = actual.getCoste();
                long fin = System.currentTimeMillis();
                return new ResultadoBusqueda(resultado, costoTotal, fin - inicio);
            }

            visitados.add(ciudadActual);

            if (conexiones.containsKey(ciudadActual)) {
                for (Map.Entry<String, Integer> hijoEntry : conexiones.get(ciudadActual).entrySet()) {
                    String hijoCiudad = hijoEntry.getKey();
                    int costo = hijoEntry.getValue();

                    if (!visitados.contains(hijoCiudad)) {
                        Nodo_BFS hijo = new Nodo_BFS(hijoCiudad);
                        hijo.setPadre(actual);
                        hijo.setCoste(actual.getCoste() + costo);
                        hijo.setFn(heuristica(hijo, destino));

                        if (!frontera.contiene(hijo)) {
                            frontera.encolar(hijo);
                        } else {
                            frontera.actualizarSiMejor(hijo);
                        }
                    }
                }
            }
        }
        
        long fin = System.currentTimeMillis();
        return new ResultadoBusqueda(resultado, 0, fin - inicio);
    }
}