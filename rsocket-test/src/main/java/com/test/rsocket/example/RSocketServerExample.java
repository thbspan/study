package com.test.rsocket.example;

import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.TcpServerTransport;

public class RSocketServerExample {
    public static void main(String[] args) {
        RSocketServer.create(new PingHandler())
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .bind(TcpServerTransport.create(7878))
                .block()
                .onClose()
                .block();
    }
}
