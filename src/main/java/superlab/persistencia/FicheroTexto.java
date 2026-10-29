package superlab.persistencia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FicheroTexto {
    private static final Logger logger = LogManager.getLogger(FicheroTexto.class);
    private final Path ruta;

    public FicheroTexto(Path ruta) {
        this.ruta = ruta;
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        try {
            if (ruta.getParent() != null && !Files.exists(ruta.getParent())) {
                Files.createDirectories(ruta.getParent());
            }
        } catch (IOException e) {
            logger.error("Error al crear los directorios para el fichero de texto: {}", ruta, e);
        }
    }

    public void registrarOperacion(String operacion) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(
                ruta,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            escritor.write(operacion);
            escritor.newLine();
            logger.info("Operación guardada en texto: {}", operacion);
        }
    }

    public List<String> leerHistorial() throws IOException {
        List<String> historial = new ArrayList<>();
        if (!Files.exists(ruta)) {
            logger.warn("El fichero de texto no existe para leer. Ruta: {}", ruta);
            return historial;
        }

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                historial.add(linea);
            }
            logger.info("Historial leído. Total de líneas: {}", historial.size());
        }
        return historial;
    }
}