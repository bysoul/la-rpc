package com.bys.larpc.rpcutil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RemoteAddress {
    @Value("${foo.hostName}")
    private String hostName;
    @Value("${foo.port}")
    private int port;

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }
}
