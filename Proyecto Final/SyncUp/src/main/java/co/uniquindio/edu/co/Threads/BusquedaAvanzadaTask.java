package co.uniquindio.edu.co.Threads;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Modelo.GeneroMusical;
import javafx.concurrent.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BusquedaAvanzadaTask extends Task<List<Cancion>> {

    private final List<Cancion> catalogo;
    private final String artista;
    private final GeneroMusical genero;
    private final Integer añoInicio;
    private final Integer añoFin;
    private final boolean usarLogicaAND; // true = AND, false = OR

    /**
     * Constructor de la tarea de búsqueda avanzada
     */
    public BusquedaAvanzadaTask(List<Cancion> catalogo,
                                String artista,
                                GeneroMusical genero,
                                Integer añoInicio,
                                Integer añoFin,
                                boolean usarLogicaAND) {
        this.catalogo = catalogo;
        this.artista = artista;
        this.genero = genero;
        this.añoInicio = añoInicio;
        this.añoFin = añoFin;
        this.usarLogicaAND = usarLogicaAND;
    }

    @Override
    protected List<Cancion> call() throws Exception {
        updateMessage("Iniciando búsqueda avanzada...");
        updateProgress(0, 100);

        List<Cancion> resultados = new ArrayList<>();
        int total = catalogo.size();
        int procesadas = 0;

        // Crear predicados para cada criterio
        List<Predicate<Cancion>> predicados = crearPredicados();

        if (predicados.isEmpty()) {
            updateMessage("No se especificaron criterios de búsqueda");
            return resultados;
        }

        // Buscar canciones que cumplan los criterios
        for (Cancion cancion : catalogo) {
            // Verificar si la tarea fue cancelada
            if (isCancelled()) {
                updateMessage("Búsqueda cancelada");
                break;
            }

            boolean cumpleCriterios;

            if (usarLogicaAND) {
                // Lógica AND: debe cumplir TODOS los criterios
                cumpleCriterios = predicados.stream().allMatch(p -> p.test(cancion));
            } else {
                // Lógica OR: debe cumplir AL MENOS UN criterio
                cumpleCriterios = predicados.stream().anyMatch(p -> p.test(cancion));
            }

            if (cumpleCriterios) {
                resultados.add(cancion);
            }

            // Actualizar progreso
            procesadas++;
            updateProgress(procesadas, total);
            updateMessage(String.format("Procesando: %d/%d canciones...", procesadas, total));

            // Simular un pequeño delay para demostrar concurrencia
            Thread.sleep(1);
        }

        updateMessage(String.format("Búsqueda completada. %d resultados encontrados.", resultados.size()));
        updateProgress(100, 100);

        return resultados;
    }

    /**
     * Crea predicados para cada criterio de búsqueda especificado
     */
    private List<Predicate<Cancion>> crearPredicados() {
        List<Predicate<Cancion>> predicados = new ArrayList<>();

        // Predicado para artista
        if (artista != null && !artista.trim().isEmpty()) {
            String artistaLower = artista.toLowerCase();
            predicados.add(cancion ->
                    cancion.getArtista().toLowerCase().contains(artistaLower)
            );
        }

        // Predicado para género
        if (genero != null) {
            predicados.add(cancion -> cancion.getGenero() == genero);
        }

        // Predicado para rango de años
        if (añoInicio != null && añoFin != null) {
            predicados.add(cancion ->
                    cancion.getAño() >= añoInicio && cancion.getAño() <= añoFin
            );
        } else if (añoInicio != null) {
            predicados.add(cancion -> cancion.getAño() >= añoInicio);
        } else if (añoFin != null) {
            predicados.add(cancion -> cancion.getAño() <= añoFin);
        }

        return predicados;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Búsqueda completada exitosamente");
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage("Error en la búsqueda: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Búsqueda cancelada por el usuario");
    }
}
