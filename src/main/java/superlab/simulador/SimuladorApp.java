package superlab.simulador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import superlab.excepciones.EventoCriticoException;
import superlab.modelo.EstadoLaboratorio;
import superlab.modelo.EstadoLaboratorio.Elemento;
import superlab.persistencia.FicheroBinario;
import superlab.persistencia.FicheroJson;
import superlab.persistencia.FicheroTexto;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class SimuladorApp {
    // El bloque static ejecuta el ajuste ANTES de que Log4j cargue la clase
    static {
        System.setProperty("logTimestamp", new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date()));
    }

    private static final Logger logger = LogManager.getLogger(SimuladorApp.class);

    public static void main(String[] args) {
        Path rutaTexto = Path.of("datos/historial_operaciones.txt");
        Path rutaBinario = Path.of("datos/telemetria_reactores.dat");
        Path rutaJson = Path.of("datos/estado_laboratorio.json");

        FicheroTexto ficheroTexto = new FicheroTexto(rutaTexto);
        FicheroBinario ficheroBinario = new FicheroBinario(rutaBinario);
        FicheroJson ficheroJson = new FicheroJson(rutaJson);

        boolean modoPrueba = args.length > 0 && args[0].equals("--incidente=SOBRECALENTAMIENTO_REACTOR");
        Random random = new Random();

        try {
            logger.info("Iniciando simulador del Superlaboratorio...");

            ficheroTexto.registrarOperacion("Inicio de ciclo de cocinado. Lote #402.");

            // idSensor=1, tipoMedicion=10 (Temp), valor=180.0, estado=1 (Operativo)
            ficheroBinario.escribirRegistro(0, 1, 10, 180.0, 1);

            EstadoLaboratorio estado = new EstadoLaboratorio("Zona Principal", Arrays.asList(
                    new Elemento("R1", "REACTOR", "OPERATIVO"),
                    new Elemento("V1", "VENTILACION", "OPERATIVO")
            ));
            ficheroJson.guardarEstado(estado);

            double temperaturaActual = 180.0 + (random.nextDouble() * 10 - 5);
            ficheroBinario.escribirRegistro(0, 1, 10, temperaturaActual, 1);
            ficheroTexto.registrarOperacion("Temperatura estable. Continuando síntesis.");

            if (modoPrueba || random.nextDouble() > 0.8) {
                logger.warn("Anomalía detectada en los sistemas físicos.");

                ficheroTexto.registrarOperacion("ALERTA CRÍTICA: La válvula de presión del reactor principal está bloqueada.");
                ficheroBinario.actualizarEstado(0, 99);
                ficheroBinario.escribirRegistro(0, 1, 10, 355.0, 99);

                estado.getElementos().get(0).setEstado("SOBRECALENTADO");
                ficheroJson.guardarEstado(estado);

                if (ficheroBinario.leerValor(0) > 300.0) {
                    throw new EventoCriticoException("Temperatura del reactor excedió límite crítico de 300°C.");
                }
            }

        } catch (EventoCriticoException e) {
            logger.error("EVENTO CRÍTICO CAPTURADO: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en la simulación", e);
        } finally {
            logger.info("Simulador finalizado.");
        }
    }
}