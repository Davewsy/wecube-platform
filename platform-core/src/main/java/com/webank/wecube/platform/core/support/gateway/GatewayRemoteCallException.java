package com.webank.wecube.platform.core.support.gateway;

import com.webank.wecube.platform.core.support.RemoteCallException;
import com.webank.wecube.platform.core.support.gateway.dto.GatewayResponse;

public class GatewayRemoteCallException extends RemoteCallException {

    private transient GatewayResponse gatewayResponse;

    public GatewayRemoteCallException(String message) {
        super(message);
    }

    public GatewayRemoteCallException(String message, GatewayResponse gatewayResponse) {
        super(message);
        this.gatewayResponse = gatewayResponse;
    }

    public GatewayRemoteCallException(String message, GatewayResponse gatewayResponse, Throwable cause) {
        super(message, cause);
        this.gatewayResponse = gatewayResponse;
    }

    public GatewayResponse getGatewayResponse() {
        return gatewayResponse;
    }

    @Override
    public String getErrorMessage() {
        return String.format("%s (GATEWAY_ERROR_CODE: %s)", this.getMessage(), getStatusCode(gatewayResponse));
    }

    @Override
    public Object getErrorData() {
        return gatewayResponse == null ? null : gatewayResponse.getData();
    }

    private String getStatusCode(GatewayResponse gatewayResponse) {
        return gatewayResponse == null ? null : gatewayResponse.getStatus();
    }
}
