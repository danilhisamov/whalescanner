package com.danilkhisamov.whalescanner.model.bsc;

import lombok.Data;

@Data
public abstract class BscScanResponse<T> {
    private String status;
    private String message;
    private T result;
}
