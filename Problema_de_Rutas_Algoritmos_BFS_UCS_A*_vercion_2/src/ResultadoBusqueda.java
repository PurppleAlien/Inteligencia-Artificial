import java.util.List;

public class ResultadoBusqueda {
    private List<String> ruta;
    private int costo;
    private long tiempoEjecucion; // en milisegundos
    
    public ResultadoBusqueda(List<String> ruta, int costo, long tiempoEjecucion) {
        this.ruta = ruta;
        this.costo = costo;
        this.tiempoEjecucion = tiempoEjecucion;
    }
    
    public List<String> getRuta() {
        return ruta;
    }
    
    public int getCosto() {
        return costo;
    }
    
    public long getTiempoEjecucion() {
        return tiempoEjecucion;
    }
}