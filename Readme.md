# Superlaboratorio Forense - Breaking Bad

URL de GitHub: https://github.com/carloscacerest92-boop/SuperlabBreakingBad

Aplicación Java para la simulación, persistencia y análisis forense de la telemetría e incidentes en un laboratorio de procesamiento.

---

## 🎭 Scenario e Incidentes

El sistema simula un entorno industrial con 6 incidentes posibles:
1. `SOBRECALENTAMIENTO_REACTOR`: Temperatura del reactor excede 300°C.
2. `FUGA_METILAMINA`: Caída drástica del flujo por debajo de 10 L/min.
3. `CONTAMINACION_MOSCA`: Nivel de pureza cae por debajo del 90%.
4. `FALLO_VENTILACION_GAS`: Concentración de gas tóxico excede 80 PPM.
5. `INCURSION_DEA`: Intrusión detectada en los sistemas de seguridad.
6. `EXPLOSION_CENTRIFUGADORA`: Revoluciones de la centrífuga superan 10.000 RPM.

---

## 📁 Ficheros y Persistencia

* `datos/historial_operaciones.txt`: Histórico secuencial (`NIO.2` / `BufferedWriter`).
* `datos/telemetria_reactores.dat`: Registros binarios de 20 bytes con acceso directo (`RandomAccessFile` / `seek`).
* `datos/estado_laboratorio.json`: Estado estructurado del dominio (`Jackson Databind`).

---

## 🚀 Instrucciones de Ejecución

### 1. Simulador
Clase principal: `superlab.simulador.SimuladorApp`.
* Modo normal: Ejecución sin argumentos.
* Modo prueba: Pasar `--incidente=SOBRECALENTAMIENTO_REACTOR` en Program Arguments.

### 2. Analizador Forense
Clase principal: `superlab.analizador.AnalizadorForenseApp`.
Lee los archivos de `datos/` y genera el informe en `salida/`.

### 3. Pruebas Automatizadas
Ejecutar la clase `superlab.LaboratorioTest` con JUnit 5 desde el IDE.

---

## ⚙️ Requisitos y Dependencias
* Java 25 / JDK 17+
* Maven
* Jackson Databind 2.17.0
* Log4j 2.23.1
* JUnit 5.10.2