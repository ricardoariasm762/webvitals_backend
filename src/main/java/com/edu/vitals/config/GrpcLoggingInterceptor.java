package com.edu.vitals.config;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * Global gRPC interceptor logging incoming RPC requests and measuring execution duration.
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        long startTime = System.currentTimeMillis();
        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.debug("[gRPC INBOUND] Method: {}", methodName);

        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - startTime;
                if (status.isOk()) {
                    log.info("[gRPC OK] Method: {} | Duration: {} ms", methodName, duration);
                } else {
                    log.warn("[gRPC FAIL] Method: {} | Code: {} | Duration: {} ms",
                            methodName, status.getCode(), duration);
                }
                super.close(status, trailers);
            }
        }, headers);
    }
}

