package com.webank.wecube.platform.core.support.gateway.dto;

import java.util.List;

public class GatewayResponse<DATATYPE> {

    public static final String RESULT_CODE_OK = "OK";

    private String status;
    private String message;

    private DATATYPE data;

    public static class DefaultGatewayResponse extends GatewayResponse<Object> {
    }

    public static class IntegerGatewayResponse extends GatewayResponse<Integer> {
    }

    public static class ListDataResponse extends GatewayResponse<List> {
    }

    public static class DefaultPluginResponse extends GatewayResponse<Object> {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DATATYPE getData() {
        return data;
    }

    public void setData(DATATYPE data) {
        this.data = data;
    }

}
