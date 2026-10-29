package superlab.persistencia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FicheroBinario {
    private static final Logger logger = LogManager.getLogger(FicheroBinario.class);
    private static final int TAMANO_REGISTRO = 20;
    private final Path ruta;

    public FicheroBinario(Path ruta) {
        this.ruta = ruta;
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        try {
            if (ruta.getParent() != null && !Files.exists(ruta.getParent())) {
                Files.createDirectories(ruta.getParent());
            }
        } catch (IOException e) {
            logger.error("Error al crear directorios para el fichero binario: {}", ruta, e);
        }
    }

    public void escribirRegistro(int posicionIndex, int idSensor, int tipoMedicion, double valor, int estado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta.toFile(), "rw")) {
            long desplazamiento = (long) posicionIndex * TAMANO_REGISTRO;
            raf.seek(desplazamiento);
            raf.writeInt(idSensor);
            raf.writeInt(tipoMedicion);
            raf.writeDouble(valor);
            raf.writeInt(estado);
            logger.info("Registro binario escrito en índice {}: ID={}, Valor={}", posicionIndex, idSensor, valor);
        }
    }

    public double leerValor(int posicionIndex) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta.toFile(), "r")) {
            // Saltamos idSensor (4) y tipoMedicion (4) sumando 8 bytes al inicio del registro
            long desplazamiento = ((long) posicionIndex * TAMANO_REGISTRO) + 8;
            raf.seek(desplazamiento);
            return raf.readDouble();
        }
    }

    public void actualizarEstado(int posicionIndex, int nuevoEstado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta.toFile(), "rw")) {
            // Saltamos idSensor (4), tipoMedicion (4) y valor (8) sumando 16 bytes
            long desplazamiento = ((long) posicionIndex * TAMANO_REGISTRO) + 16;
            raf.seek(desplazamiento);
            raf.writeInt(nuevoEstado);
            logger.info("Estado modificado en índice {} al valor {}", posicionIndex, nuevoEstado);
        }
    }
}