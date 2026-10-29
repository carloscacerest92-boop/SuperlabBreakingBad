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
            double valorMedicion = binarioIn.leerValor(0);
            EstadoLaboratorio estado = jsonIn.leerEstado();

            Incidente incidenteDetectado = identificarIncidente(historial, valorMedicion, estado);
            logger.info("Análisis concluido. Incidente identificado: {}", incidenteDetectado);

            if (incidenteDetectado != Incidente.INDETERMINADO) {
                textoOut.registrarOperacion("INFORME FORENSE DEFINITIVO");
                textoOut.registrarOperacion("Incidente: " + incidenteDetectado);
                textoOut.registrarOperacion("Efectos: Valor anómalo " + valorMedicion + " registrado en telemetría.");
                textoOut.registrarOperacion("Conclusión: Compromiso del sistema verificado.");

                binarioOut.escribirRegistro(0, 1, 10, valorMedicion, 99);
                jsonOut.guardarEstado(estado);
            }

        } catch (IOException e) {
            logger.error("Error de E/S al leer o escribir las evidencias", e);
        }
    }

    public static Incidente identificarIncidente(List<String> historial, double valor, EstadoLaboratorio estado) {
        String textoUnificado = String.join(" ", historial).toUpperCase();
        boolean estadoAfectado = estado.getElementos() != null && estado.getElementos().stream()
                .anyMatch(e -> !e.getEstado().equals("OPERATIVO"));

        if (textoUnificado.contains("SOBRECALENTAMIENTO") && valor > 300.0) {
            return Incidente.SOBRECALENTAMIENTO_REACTOR;
        } else if (textoUnificado.contains("FUGA") && valor < 10.0) {
            return Incidente.FUGA_METILAMINA;
        } else if (textoUnificado.contains("CONTAMINACION") && valor < 90.0) {
            return Incidente.CONTAMINACION_MOSCA;
        } else if (textoUnificado.contains("VENTILACION") && valor > 80.0) {
            return Incidente.FALLO_VENTILACION_GAS;
        } else if (textoUnificado.contains("INCURSION") || valor == 999.0) {
            return Incidente.INCURSION_DEA;
        } else if (textoUnificado.contains("EXPLOSION") && valor > 10000.0) {
            return Incidente.EXPLOSION_CENTRIFUGADORA;
        }

        return Incidente.INDETERMINADO;
    }
}