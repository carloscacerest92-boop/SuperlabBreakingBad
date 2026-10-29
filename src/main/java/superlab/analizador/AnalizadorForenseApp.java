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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AnalizadorForenseApp {
    private static final Logger logger = LogManager.getLogger(AnalizadorForenseApp.class);

    public static void main(String[] args) {
        // Necesario para que Log4j2 genere el nombre del archivo de esta ejecución
        System.setProperty("logTimestamp", new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date()));

        logger.info("Iniciando analizador forense de datos...");

        // 1. Instanciar gestores para los ficheros de ENTRADA
        FicheroTexto textoIn = new FicheroTexto(Path.of("datos/historial_operaciones.txt"));
        FicheroBinario binarioIn = new FicheroBinario(Path.of("datos/telemetria_reactores.dat"));
        FicheroJson jsonIn = new FicheroJson(Path.of("datos/estado_laboratorio.json"));

        // 2. Instanciar gestores para los ficheros de SALIDA
        FicheroTexto textoOut = new FicheroTexto(Path.of("salida/informe_forense.txt"));
        FicheroBinario binarioOut = new FicheroBinario(Path.of("salida/telemetria_anomala.dat"));
        FicheroJson jsonOut = new FicheroJson(Path.of("salida/estado_afectado.json"));

        try {
            // Extraer datos
            List<String> historial = textoIn.leerHistorial();
            double temperatura = binarioIn.leerValor(0); // Índice 0 del reactor principal
            EstadoLaboratorio estado = jsonIn.leerEstado();

            Incidente incidenteDetectado = Incidente.INDETERMINADO;

            // Motor lógico: Relacionar información de las 3 fuentes
            boolean hayAlertaTexto = historial.stream().anyMatch(linea -> linea.contains("ALERTA CRÍTICA"));
            boolean hayTemperaturaAnomala = temperatura > 300.0;
            boolean hayEstructuraAfectada = estado.getElementos().stream()
                    .anyMatch(e -> e.getId().equals("R1") && e.getEstado().equals("SOBRECALENTADO"));

            if (hayAlertaTexto && hayTemperaturaAnomala && hayEstructuraAfectada) {
                incidenteDetectado = Incidente.SOBRECALENTAMIENTO_REACTOR;
            }

            logger.info("Análisis concluido. Incidente identificado: {}", incidenteDetectado);

            // Generar salidas si hay incidente
            if (incidenteDetectado != Incidente.INDETERMINADO) {
                textoOut.registrarOperacion("INFORME FORENSE DEFINITIVO");
                textoOut.registrarOperacion("Incidente: " + incidenteDetectado);
                textoOut.registrarOperacion("Efectos: Temperatura " + temperatura + "°C registrada. Reactor R1 comprometido.");
                textoOut.registrarOperacion("Conclusión: Riesgo de explosión térmica. El sistema de enfriamiento falló.");

                // El fichero binario de salida conserva SOLO los valores anómalos
                binarioOut.escribirRegistro(0, 1, 10, temperatura, 99);

                // El JSON conserva los elementos de la estructura afectados
                jsonOut.guardarEstado(estado);
            }

        } catch (IOException e) {
            logger.error("Error de E/S al leer o escribir las evidencias", e);
        }
    }
}