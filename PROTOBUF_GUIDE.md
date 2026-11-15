# Guía de Contrato Protocol Buffers (gRPC) - Backend

## 1. Introducción
Protocol Buffers (`.proto`) es el formato de serialización binaria ultraligero y neutral en plataforma de Google utilizado por el servicio **VitalsAuditorService**. A diferencia de JSON, Protobuf genera representaciones compactas de bytes que reducen drásticamente la latencia de red y eliminan el tiempo de análisis de texto en CPU.

---

## 2. Definición del Esquema (`vitals_telemetry.proto`)

```protobuf
syntax = "proto3";

package com.edu.vitals.grpc;

option java_multiple_files = true;
option java_package = "com.edu.vitals.grpc";
option java_outer_classname = "VitalsTelemetryProto";

service VitalsAuditorService {
  rpc EvaluateSingleFrame (TelemetryFrame) returns (OptimizationVerdict);
  rpc StreamTelemetry (stream TelemetryFrame) returns (StreamSummary);
}

message TelemetryFrame {
  string session_id = 1;
  string url_path = 2;
  float long_task_duration_ms = 3;
  float blocking_time_ms = 4;
  float dom_node_count = 5;
  float event_loop_lag_ms = 6;
  int64 timestamp_epoch = 7;
}

message OptimizationVerdict {
  float predicted_inp_ms = 1;
  string vitals_category = 2;
  string architectural_advice = 3;
  bool offload_to_worker = 4;
  bool yield_main_thread = 5;
}

message StreamSummary {
  int32 frames_processed = 1;
  float average_predicted_inp = 2;
}
```

---

## 3. Reglas de Evolución de Contrato

1. **Inmutabilidad de Etiquetas Numéricas**: Nunca cambies el número de etiqueta (`= 1`, `= 2`, etc.) asignado a un campo existente.
2. **Campos Obsoletos (`reserved`)**: Si un campo se retira del modelo, márcalo con `reserved` para evitar colisiones futuras.
3. **Compatibilidad Hacia Atrás**: Los campos nuevos deben ser opcionales (comportamiento predeterminado en `proto3`).
