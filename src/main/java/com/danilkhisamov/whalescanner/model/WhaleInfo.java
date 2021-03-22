package com.danilkhisamov.whalescanner.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
public class WhaleInfo implements Serializable {
    private String address;
    private String name;
    private String lastTransaction;
    private Set<Long> subscribedChats;
}
