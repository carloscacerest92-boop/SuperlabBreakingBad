package superlab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import superlab.excepciones.EventoCriticoException;
import superlab.modelo.EstadoLaboratorio;
import superlab.modelo.EstadoLaboratorio.Elemento;
import superlab.persistencia.FicheroBinario;
import superlab.persistencia.FicheroJson;
import superlab.persistencia.FicheroTexto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaboratorioTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("1. Escritura y lectura correcta de historial de texto")
    void testPersistenciaTexto() throws IOException {
        Path rutaTexto = tempDir.resolve("historial.txt");
        FicheroTexto textoManager = new FicheroTexto(rutaTexto);

        textoManager.registrarOperacion("Lote de prueba iniciado.");
        List<String> lineas = textoManager.leerHistorial();

        assertEquals(1, lineas.size());
        assertEquals("Lote de prueba iniciado.", lineas.get(0));
    }

    @Test
    @DisplayName("2. Escritura, lectura directa y modificacion parcial en binario con RandomAccessFile")
    void testPersistenciaBinariaAccesoAleatorio() throws IOException {
        Path rutaBinario = tempDir.resolve("telemetria.dat");
        FicheroBinario binarioManager = new FicheroBinario(rutaBinario);

        // Alta de registro
        binarioManager.escribirRegistro(0, 101, 10, 180.0, 1);
        assertEquals(180.0, binarioManager.leerValor(0));

        // Modificación parcial sin reescribir todo el fichero
        binarioManager.actualizarEstado(0, 99);
        assertEquals(180.0, binarioManager.leerValor(0)); // El valor sigue intacto
    }

    @Test
    @DisplayName("3. Serializacion y deserializacion JSON con Jackson")
    void testPersistenciaJson() throws IOException {
        Path rutaJson = tempDir.resolve("estado.json");
        FicheroJson jsonManager = new FicheroJson(rutaJson);

        EstadoLaboratorio estadoOriginal = new EstadoLaboratorio("Zona A", Arrays.asList(
                new Elemento("R1", "REACTOR", "OPERATIVO")
        ));

        jsonManager.guardarEstado(estadoOriginal);
        EstadoLaboratorio estadoLeido = jsonManager.leerEstado();

        assertNotNull(estadoLeido);
        assertEquals("Zona A", estadoLeido.getZona());
        assertEquals(1, estadoLeido.getElementos().size());
        assertEquals("OPERATIVO", estadoLeido.getElementos().get(0).getEstado());
    }

    @Test
    @DisplayName("4. Provocacion controlada de excepcion critica")
    void testExcepcionCritica() {
        assertThrows(EventoCriticoException.class, () -> {
            double temperatura = 350.0;
            if (temperatura > 300.0) {
                throw new EventoCriticoException("Temperatura critica excedida");
            }
        });
    }

    @Test
    @DisplayName("5. Verificacion del flujo de integracion completo")
    void testFlujoIntegracion() throws IOException {
        Path txtPath = tempDir.resolve("historial.txt");
        Path binPath = tempDir.resolve("telemetria.dat");
        Path jsonPath = tempDir.resolve("estado.json");

        FicheroTexto ft = new FicheroTexto(txtPath);
        FicheroBinario fb = new FicheroBinario(binPath);
        FicheroJson fj = new FicheroJson(jsonPath);

        ft.registrarOperacion("ALERTA CRÍTICA: Fallo detectado");
        fb.escribirRegistro(0, 1, 10, 350.0, 99);
        fj.guardarEstado(new EstadoLaboratorio("Zona A", List.of(new Elemento("R1", "REACTOR", "SOBRECALENTADO"))));

        assertTrue(ft.leerHistorial().get(0).contains("ALERTA CRÍTICA"));
        assertEquals(350.0, fb.leerValor(0));
        assertEquals("SOBRECALENTADO", fj.leerEstado().getElementos().get(0).getEstado());
    }
}