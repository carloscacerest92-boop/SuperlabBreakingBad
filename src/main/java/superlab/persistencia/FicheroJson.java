package superlab.persistencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import superlab.modelo.EstadoLaboratorio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FicheroJson {
    private static final Logger logger = LogManager.getLogger(FicheroJson.class);
    private final Path ruta;
    private final ObjectMapper mapper;

    public FicheroJson(Path ruta) {
        this.ruta = ruta;
        this.mapper = new ObjectMapper();
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        try {
            if (ruta.getParent() != null && !Files.exists(ruta.getParent())) {
                Files.createDirectories(ruta.getParent());
            }
        } catch (IOException e) {
            logger.error("Error al crear directorios para el fichero JSON: {}", ruta, e);
        }
    }

    public void guardarEstado(EstadoLaboratorio estado) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(ruta.toFile(), estado);
        logger.info("Estado estructurado del laboratorio guardado en JSON.");
    }

    public EstadoLaboratorio leerEstado() throws IOException {
        if (!Files.exists(ruta)) {
            logger.warn("El fichero JSON no existe en la ruta: {}. Se asume estado vacío.", ruta);
            return new EstadoLaboratorio();
        }
        EstadoLaboratorio estado = mapper.readValue(ruta.toFile(), EstadoLaboratorio.class);
        logger.info("Estado del laboratorio cargado desde JSON correctamente.");
        return estado;
    }
}