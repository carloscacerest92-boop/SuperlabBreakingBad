package superlab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import superlab.analizador.AnalizadorForenseApp;
import superlab.excepciones.EventoCriticoException;
import superlab.modelo.EstadoLaboratorio;
import superlab.modelo.EstadoLaboratorio.Elemento;
import superlab.modelo.Incidente;
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
    @DisplayName("2. Alta, lectura directa y modificacion de registro binario con RandomAccessFile")
    void testPersistenciaBinariaAccesoAleatorio() throws IOException {
        Path rutaBinario = tempDir.resolve("telemetria.dat");
        FicheroBinario binarioManager = new FicheroBinario(rutaBinario);

        binarioManager.escribirRegistro(0, 101, 10, 180.0, 1);
        assertEquals(180.0, binarioManager.leerValor(0));

        binarioManager.actualizarEstado(0, 99);
        assertEquals(180.0, binarioManager.leerValor(0));
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

    @ParameterizedTest
    @EnumSource(value = Incidente.class, names = {"SOBRECALENTAMIENTO_REACTOR", "FUGA_METILAMINA"})
    @DisplayName("5. Identificacion correcta de distintos tipos de incidentes por el analizador")
    void testIdentificacionIncidentes(Incidente incidente) {
        List<String> historial = List.of("ALERTA CRÍTICA: " + incidente.name());
        double valor = incidente == Incidente.SOBRECALENTAMIENTO_REACTOR ? 355.0 : 5.0;
        EstadoLaboratorio estado = new EstadoLaboratorio("Zona A", List.of(new Elemento("R1", "REACTOR", "ANOMALO")));

        Incidente detectado = AnalizadorForenseApp.identificarIncidente(historial, valor, estado);
        assertEquals(incidente, detectado);
    }

    @Test
    @DisplayName("6. Verificacion del flujo de integracion completo")
    void testFlujoIntegracion() throws IOException {
        Path txtPath = tempDir.resolve("historial.txt");
        Path binPath = tempDir.resolve("telemetria.dat");
        Path jsonPath = tempDir.resolve("estado.json");

        FicheroTexto ft = new FicheroTexto(txtPath);
        FicheroBinario fb = new FicheroBinario(binPath);
        FicheroJson fj = new FicheroJson(jsonPath);

        ft.registrarOperacion("ALERTA CRÍTICA: SOBRECALENTAMIENTO");
        fb.escribirRegistro(0, 1, 10, 350.0, 99);
        fj.guardarEstado(new EstadoLaboratorio("Zona A", List.of(new Elemento("R1", "REACTOR", "SOBRECALENTADO"))));

        assertTrue(ft.leerHistorial().get(0).contains("SOBRECALENTAMIENTO"));
        assertEquals(350.0, fb.leerValor(0));
        assertEquals("SOBRECALENTADO", fj.leerEstado().getElementos().get(0).getEstado());
    }
}