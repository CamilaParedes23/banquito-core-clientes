package com.banquito.core.customer.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerGrpcServer {
    private static final Logger log = LoggerFactory.getLogger(CustomerGrpcServer.class);
    private final CustomerQueryGrpcService customerQueryGrpcService;
    private final int port;
    private Server server;

    public CustomerGrpcServer(CustomerQueryGrpcService customerQueryGrpcService,
                              @Value("${banquito.grpc.server.port:9092}") int port) {
        this.customerQueryGrpcService = customerQueryGrpcService;
        this.port = port;
    }

    @PostConstruct
    public void start() throws Exception {
        this.server = ServerBuilder.forPort(port).addService(customerQueryGrpcService).build().start();
        log.info("Customer gRPC server started on port {}", port);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("Customer gRPC server stopped");
        }
    }
}
