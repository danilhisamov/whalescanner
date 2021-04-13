package com.danilkhisamov.whalescanner.repository;

import com.danilkhisamov.whalescanner.model.Whale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhaleRepository extends JpaRepository<Whale, Long> {
    Whale findByAddress(String address);
}
