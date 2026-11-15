package com.edu.vitals.config;

import io.grpc.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrpcLoggingInterceptorTest {

    @Mock
    private ServerCall<String, String> serverCall;

    @Mock
    private ServerCallHandler<String, String> serverCallHandler;

    @Mock
    private ServerCall.Listener<String> listener;

    @Mock
    private MethodDescriptor<String, String> methodDescriptor;

    @Test
    void testInterceptCall() {
        GrpcLoggingInterceptor interceptor = new GrpcLoggingInterceptor();
        Metadata headers = new Metadata();

        when(serverCall.getMethodDescriptor()).thenReturn(methodDescriptor);
        when(methodDescriptor.getFullMethodName()).thenReturn("com.edu.vitals.grpc.VitalsAuditorService/EvaluateSingleFrame");
        when(serverCallHandler.startCall(any(), eq(headers))).thenReturn(listener);

        ServerCall.Listener<String> result = interceptor.interceptCall(serverCall, headers, serverCallHandler);

        assertNotNull(result);
        verify(serverCallHandler, times(1)).startCall(any(), eq(headers));
    }
}
