package com.webank.wecube.core.support.cmdb;

public class CmdbDataNotFoundException extends CmdbRemoteCallException {
    private static final long serialVersionUID = 1L;

    public CmdbDataNotFoundException(String message) {
        super(message);
    }
}
