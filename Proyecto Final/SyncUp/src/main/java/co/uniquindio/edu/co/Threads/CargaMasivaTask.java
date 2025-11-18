package co.uniquindio.edu.co.Threads;

import co.uniquindio.edu.co.Modelo.Cancion;
import co.uniquindio.edu.co.Utils.CSVImporter;
import javafx.concurrent.Task;
import java.util.List;

public class CargaMasivaTask extends Task<List<Cancion>> {

    private final String rutaArchivo;
    private final boolean esCSV;

    /**
     * Constructor de la tarea de carga masiva
     */
    public CargaMasivaTask(String rutaArchivo, boolean esCSV) {
        this.rutaArchivo = rutaArchivo;
        this.esCSV = esCSV;
    }

    @Override
    protected List<Cancion> call() throws Exception {
        updateMessage("Iniciando carga de archivo...");
        updateProgress(0, 100);

        // Verificar que el archivo exista
        java.io.File archivo = new java.io.File(rutaArchivo);
        if (!archivo.exists()) {
            throw new java.io.FileNotFoundException("El archivo no existe: " + rutaArchivo);
        }

        updateMessage("Leyendo archivo...");
        updateProgress(25, 100);

        // Cargar canciones según el tipo de archivo
        List<Cancion> canciones;

        if (esCSV) {
            canciones = CSVImporter.importarCanciones(rutaArchivo);
        } else {
            canciones = CSVImporter.importarCancionesTextoPlano(rutaArchivo);
        }

        updateProgress(75, 100);

        // Verificar si la tarea fue cancelada
        if (isCancelled()) {
            updateMessage("Carga cancelada");
            return null;
        }

        updateMessage(String.format("Carga completada. %d canciones importadas.", canciones.size()));
        updateProgress(100, 100);

        // Simular procesamiento
        Thread.sleep(500);

        return canciones;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Archivo cargado exitosamente");
    }

    @Override
    protected void failed() {
        super.failed();
        Throwable exception = getException();
        String mensaje = exception != null ? exception.getMessage() : "Error desconocido";
        updateMessage("Error al cargar archivo: " + mensaje);
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Carga cancelada por el usuario");
    }
}
