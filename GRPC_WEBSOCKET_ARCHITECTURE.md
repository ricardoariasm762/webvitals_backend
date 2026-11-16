# Arquitectura Híbrida: Ingesta gRPC + Difusión WebSockets STOMP

## 1. Justificación de Diseño
Para auditorías de latencia crítica en Core Web Vitals, los flujos tradicionales basados en REST y sondeo (polling) introducen dos problemas:
1. **Sobrecarga de CPU**: Serializar y parsear JSON en cada cuadro de telemetría penaliza el presupuesto del hilo principal en el navegador.
2. **Latencia Innecesaria**: El sondeo periódico periódicamente envía peticiones vacías o introduce demoras de cientos de milisegundos para reflejar una predicción.

---

## 2. Flujo de Datos Híbrido

1. **Ingesta Binaria (Cliente $\to$ Servidor)**:
   - El cliente web transmite paquetes binarios `TelemetryFrame` a través de HTTP/2 mediante gRPC-Web hacia el proxy Envoy (:8080).
   - Envoy traslada la petición a gRPC nativo en el puerto :9090 hacia `VitalsGrpcServiceImpl`.

2. **Inferencia ONNX Runtime**:
   - `InpInferenceEngine` evalúa el vector matemático prediciendo el INP en milisegundos con latencia menor a 1ms.

3. **Difusión Fan-Out (Servidor $\to$ Clientes Suscritos)**:
   - `SimpMessagingTemplate` emite inmediatamente el veredicto hacia el tópico dinámico `/topic/vitals/{sessionId}`.
   - El osciloscopio en Angular se actualiza en tiempo real con 0ms de latencia de sondeo.

