# Event Loop Telemetry and Predictive INP Optimization System (WebVitals Backend)

Sistema backend en **Spring Boot 3** integrado con **ONNX Runtime**, **gRPC (Protocol Buffers)** y **WebSockets (STOMP)** para la recepción en tiempo real, auditoría, almacenamiento predictivo y recomendación de estrategias de optimización para métricas de **Core Web Vitals**, específicamente **INP (Interaction to Next Paint)**.

---

## 🚀 Arquitectura y Capacidades

- **Ingesta de Alta Frecuencia con gRPC**: Servicio `VitalsGrpcServiceImpl` con contrato Protocol Buffers (`vitals_telemetry.proto`) que procesa streams y evaluaciones unarias binarias en el puerto `9090`.
- **Difusión en Tiempo Real con WebSockets (STOMP)**: Emisión push inmediata de veredictos hacia `/topic/vitals/{sessionId}` tras cada inferencia de ONNX, eliminando el polling desde el frontend.
- **Inferencia en Tiempo Real (ONNX Runtime)**: Inferencia de baja latencia mediante modelos serializados en formato ONNX (`RandomForestRegressor`) para anticipar degradaciones de latencia en el hilo principal del navegador.
- **Mecanismo de Resiliencia / Fallback**: Algoritmo heurístico matemático en Java en caso de no disponibilidad de la sesión ONNX o datos corruptos.
- **Auditoría y Persistencia**: Almacenamiento transaccional de telemetría por sesión (`PerformanceLog`) respaldado en **PostgreSQL**.
- **CORS Habilitado y Proxy Envoy**: Compatibilidad total con clientes web (`gRPC-Web`) a través de Envoy en el puerto `8080`.
- **Manejo Centralizado de Excepciones**: Respuestas de error estandarizadas con `GlobalExceptionHandler`.

---

## 🛠️ Tecnologías

- **Java**: 21
- **Spring Boot**: 3.4.3 (Spring Data JPA, Spring Web, Spring WebSocket)
- **gRPC**: 1.62.2 & gRPC Server Spring Boot Starter 3.1.0.RELEASE
- **Protocol Buffers**: 3.25.3
- **ONNX Runtime**: 1.17.0
- **Base de Datos**: PostgreSQL (Producción/Dev), H2 en memoria (Testing)
- **Proxy L7**: Envoy (traductor gRPC-Web a gRPC HTTP/2 nativo)
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
│   │   │   ├── config/           # Configuración de CORS y WebSocketConfig (STOMP)
│   │   │   ├── controller/       # Endpoints REST (/api/vitals)
│   │   │   ├── domain/           # Entidades JPA y DTOs
│   │   │   ├── exception/        # Manejo global de excepciones
│   │   │   ├── ml/               # InpInferenceEngine (ONNX Runtime)
│   │   │   ├── repository/       # Interfaces JPA
│   │   │   └── service/          # PerformanceAuditService y VitalsGrpcServiceImpl
│   │   ├── proto/                # Contrato vitals_telemetry.proto
│   │   └── resources/
│   │       ├── application.properties
│   │       └── models/           # Modelo ONNX integrado en el JAR
│   └── test/                     # Pruebas unitarias y de integración
```

---

## 🌐 Servicios de Comunicación

### 1. Servicio gRPC (`vitals_telemetry.proto` en puerto 9090 / Envoy 8080)

| RPC | Tipo | Descripción |
| :--- | :--- | :--- |
| `EvaluateSingleFrame` | Unario | Evalúa un paquete `TelemetryFrame` binario y retorna `OptimizationVerdict`. |
| `StreamTelemetry` | Client Streaming | Ingesta continua de métricas en tiempo real sobre HTTP/2. |

### 2. Broker WebSocket STOMP (puerto 8081)

- **Endpoint de Conexión**: `/ws-vitals`
- **Tópico de Suscripción**: `/topic/vitals/{sessionId}`
- **Prefijo de Aplicación**: `/app`

### 3. API REST (Fallback tradicional)

Base URL: `/api/vitals`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/vitals/evaluate` | Evalúa telemetría del navegador, predice INP y retorna recomendaciones. |
| `GET` | `/api/vitals/metrics/session/{sessionId}` | Retorna el historial de telemetría registrado para una sesión. |
| `GET` | `/api/vitals/health` | Health check del servicio. |

---

## 🧪 Ejecución de Pruebas

Para compilar y ejecutar la suite completa de pruebas unitarias y de integración (12 pruebas):

```bash
./mvnw clean test
```

Las pruebas validan:
1. Arranque de contexto Spring Boot.
2. Servicios gRPC (`VitalsGrpcServiceImplTest`) con llamadas unarias y streaming.
3. Inferencia ONNX y vectorización matemática de fallback.
4. Categorización de umbrales `GOOD`, `NEEDS_IMPROVEMENT` y `POOR`.
5. Mapeo HTTP y serialización JSON de los controladores WebMvc.

---

## 📄 Licencia

Este proyecto está bajo propósitos académicos y de optimización de rendimiento web.
1. Arranque de contexto Spring Boot.
2. Inferencia ONNX y vectorización matemática de fallback.
3. Categorización de umbrales `GOOD`, `NEEDS_IMPROVEMENT` y `POOR`.
4. Mapeo HTTP y serialización JSON de los controladores WebMvc.

---

## 📄 Licencia

Este proyecto está bajo propósitos académicos y de optimización de rendimiento web.
