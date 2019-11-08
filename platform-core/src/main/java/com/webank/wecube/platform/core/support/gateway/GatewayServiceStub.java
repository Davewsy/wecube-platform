package com.webank.wecube.platform.core.support.gateway;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.ApplicationProperties.GatewayProperties;
import com.webank.wecube.platform.core.support.cmdb.dto.CmdbResponse;
import com.webank.wecube.platform.core.support.gateway.dto.GatewayResponse;
import com.webank.wecube.platform.core.support.gateway.dto.GatewayResponse.DefaultGatewayResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.DefaultPluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.PluginLoggingInfoRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.DefaultPluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.PluginRunScriptResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.ResultData;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRunScriptOutput;

@Service
public class GatewayServiceStub {

    private static final Logger log = LoggerFactory.getLogger(GatewayServiceStub.class);

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GatewayProperties gatewayProperties;

    private static final String REGISTER_URL = "/gateway/v1/route-items";

    public Object registerUrlToGateway(String instanceAddress, PluginRequest<Map<String, Object>> request) {
        return postForResponse(asGatewayServerUrl(REGISTER_URL), request, GatewayResponse.class);
    }

    @SuppressWarnings("unchecked")
    public <D, R extends GatewayResponse> D postForResponse(String targetUrl, Object postObject,
            Class<R> responseType) {
        log.info("About to POST {} with postObject {}", targetUrl, postObject);
        R gatewayResponse = restTemplate.postForObject(targetUrl, postObject, responseType);

        log.info("Gaateway response: {} ", gatewayResponse);
        validateGatewayResponse(gatewayResponse, false);
        return (D) gatewayResponse.getData();
    }

    private void validateGatewayResponse(GatewayResponse<?> gatewayResponse, boolean dataRequired) {
        if (gatewayResponse == null) {
            throw new GatewayRemoteCallException("Plugin call failure due to no response.");
        }
        if (!GatewayResponse.RESULT_CODE_OK.equalsIgnoreCase(gatewayResponse.getStatus())) {
            throw new GatewayRemoteCallException("Plugin call error: " + gatewayResponse.getMessage(), gatewayResponse);
        }
        if (dataRequired && gatewayResponse.getData() == null) {
            throw new GatewayRemoteCallException("Gateway call failure due to unexpected empty response.",
                    gatewayResponse);
        }
    }

    private String asGatewayServerUrl(String originPath, Object... pathVariables) {
        String gatewayAddress = gatewayProperties.getAddress();
        String solvedPath = originPath;
        if (pathVariables != null && pathVariables.length > 0) {
            solvedPath = String.format(originPath, pathVariables);
        }
        return "http://" + gatewayAddress + solvedPath;
    }
}
