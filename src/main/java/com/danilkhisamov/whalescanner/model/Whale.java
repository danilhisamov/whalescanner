package com.danilkhisamov.whalescanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Entity
@Table(name = "whales")
@NoArgsConstructor
@AllArgsConstructor
public class Whale implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private String address;
    private String name;
    private String lastTransaction;
    @ElementCollection
    private Set<Long> subscribedChats = new HashSet<>();

    public String toSmallString() {
        return String.format("[%s]%s", name, address);
    }
}
