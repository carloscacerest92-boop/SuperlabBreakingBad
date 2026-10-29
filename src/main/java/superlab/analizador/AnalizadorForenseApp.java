package superlab.analizador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import superlab.modelo.EstadoLaboratorio;
import superlab.modelo.Incidente;
import superlab.persistencia.FicheroBinario;
import superlab.persistencia.FicheroJson;
import superlab.persistencia.FicheroTexto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AnalizadorForenseApp {
    static {
        System.setProperty("logTimestamp", new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date()));
    }

    private static final Logger logger = LogManager.getLogger(AnalizadorForenseApp.class);

    public static void main(String[] args) {
        logger.info("Iniciando analizador forense de datos...");

        FicheroTexto textoIn = new FicheroTexto(Path.of("datos/historial_operaciones.txt"));
        FicheroBinario binarioIn = new FicheroBinario(Path.of("datos/telemetria_reactores.dat"));
        FicheroJson jsonIn = new FicheroJson(Path.of("datos/estado_laboratorio.json"));

        FicheroTexto textoOut = new FicheroTexto(Path.of("salida/informe_forense.txt"));
        FicheroBinario binarioOut = new FicheroBinario(Path.of("salida/telemetria_anomala.dat"));
        FicheroJson jsonOut = new FicheroJson(Path.of("salida/estado_afectado.json"));

        try {
            List<String> historial = textoIn.leerHistorial();
            double temperatura = binarioIn.leerValor(0);
            EstadoLaboratorio estado = jsonIn.leerEstado();

            Incidente incidenteDetectado = Incidente.INDETERMINADO;

            boolean hayAlertaTexto = historial.stream().anyMatch(linea -> linea.contains("ALERTA CRÍTICA"));
            boolean hayTemperaturaAnomala = temperatura > 300.0;
            boolean hayEstructuraAfectada = estado.getElementos() != null && estado.getElementos().stream()
                    .anyMatch(e -> e.getId().equals("R1") && e.getEstado().equals("SOBRECALENTADO"));

            if (hayAlertaTexto && hayTemperaturaAnomala && hayEstructuraAfectada) {
                incidenteDetectado = Incidente.SOBRECALENTAMIENTO_REACTOR;
            }

            logger.info("Análisis concluido. Incidente identificado: {}", incidenteDetectado);

            if (incidenteDetectado != Incidente.INDETERMINADO) {
                textoOut.registrarOperacion("INFORME FORENSE DEFINITIVO");
                textoOut.registrarOperacion("Incidente: " + incidenteDetectado);
                textoOut.registrarOperacion("Efectos: Temperatura " + temperatura + "°C registrada. Reactor R1 comprometido.");
                textoOut.registrarOperacion("Conclusión: Riesgo de explosión térmica. El sistema de enfriamiento falló.");

                binarioOut.escribirRegistro(0, 1, 10, temperatura, 99);
                jsonOut.guardarEstado(estado);
            }

        } catch (IOException e) {
            logger.error("Error de E/S al leer o escribir las evidencias", e);
        }
    }
}