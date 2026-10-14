# Event Loop Telemetry and Predictive INP Optimization System (WebVitals Backend)

Sistema backend en **Spring Boot 3** integrado con **ONNX Runtime** para la recepción, auditoría, almacenamiento predictivo y recomendación de estrategias de optimización para métricas de **Core Web Vitals**, específicamente **INP (Interaction to Next Paint)**.

---

## 🚀 Arquitectura y Capacidades

- **Inferencia en Tiempo Real**: Inferencia de baja latencia mediante modelos serializados en formato ONNX (`RandomForestRegressor`) para anticipar degradaciones de latencia en el hilo principal del navegador.
- **Mecanismo de Resiliencia / Fallback**: Algoritmo heurístico matemático en Java en caso de no disponibilidad de la sesión ONNX o datos corruptos.
- **Auditoría y Persistencia**: Almacenamiento transaccional de telemetría por sesión (`PerformanceLog`) respaldado en **PostgreSQL**.
- **CORS Habilitado**: Configuración completa para integración con clientes frontend (Angular / SPA).
- **Manejo Centralizado de Excepciones**: Respuestas de error estandarizadas con `GlobalExceptionHandler`.

---

## 🛠️ Tecnologías

- **Java**: 21
- **Spring Boot**: 3.4.3 (Spring Data JPA, Spring Web)
- **ONNX Runtime**: 1.17.0
- **Base de Datos**: PostgreSQL (Producción/Dev), H2 en memoria (Testing)
- **Python (ML Training)**: Scikit-learn, skl2onnx, ONNX Runtime
- **Lombok**: Reducción de código boilerplate
- **Maven**: Gestión y compilación del proyecto

---

## 📦 Estructura del Proyecto

```text
webvitals_backend/
├── ml-model/                     # Pipeline de Machine Learning en Python
│   ├── train_inp_model.py        # Generación sintética y exportación a ONNX
│   └── model_inp_predictor.onnx  # Modelo exportado en formato ONNX
├── src/
│   ├── main/
│   │   ├── java/com/edu/vitals/
│   │   │   ├── config/           # Configuración de CORS y beans
│   │   │   ├── controller/       # Endpoints REST (/api/vitals)
│   │   │   ├── domain/           # Entidades JPA y DTOs
│   │   │   ├── exception/        # Manejo global de excepciones
│   │   │   ├── ml/               # InpInferenceEngine (ONNX Runtime)
│   │   │   ├── repository/       # Interfaces JPA
│   │   │   └── service/          # Lógica de auditoría y reglas Web Vitals
│   │   └── resources/
│   │       ├── application.properties
│   │       └── models/           # Modelo ONNX integrado en el JAR
│   └── test/                     # Pruebas unitarias y de integración
```

---

## 🌐 Endpoints de la API REST

Base URL: `/api/vitals`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/vitals/evaluate` | Evalúa telemetría del navegador, predice INP y retorna recomendaciones. |
| `GET` | `/api/vitals/metrics/session/{sessionId}` | Retorna el historial de telemetría registrado para una sesión. |
| `GET` | `/api/vitals/health` | Health check del servicio. |

### Ejemplo de Payload (`POST /api/vitals/evaluate`):

```json
{
  "sessionId": "usr-session-8821",
  "urlPath": "/catalog/products",
  "longTaskDurationMs": 140.5,
  "blockingTimeMs": 95.0,
  "domNodeCount": 2100.0,
  "eventLoopLagMs": 35.2,
  "optimizationApplied": false
}
```

### Ejemplo de Respuesta:

```json
{
  "predictedInpMs": 312.4,
  "vitalsCategory": "NEEDS_IMPROVEMENT",
  "architecturalAdvice": "Riesgo de retraso de renderizado. Fragmentar microtareas pesadas utilizando requestIdleCallback() o scheduler.yield().",
  "offloadToWorker": false,
  "yieldMainThread": true
}
```

---

## 🧪 Ejecución de Pruebas

Para compilar y ejecutar la suite completa de pruebas unitarias y de integración:

```bash
./mvnw clean test
```

Las pruebas validan:
1. Arranque de contexto Spring Boot.
2. Inferencia ONNX y vectorización matemática de fallback.
3. Categorización de umbrales `GOOD`, `NEEDS_IMPROVEMENT` y `POOR`.
4. Mapeo HTTP y serialización JSON de los controladores WebMvc.

---

## 📄 Licencia

Este proyecto está bajo propósitos académicos y de optimización de rendimiento web.
