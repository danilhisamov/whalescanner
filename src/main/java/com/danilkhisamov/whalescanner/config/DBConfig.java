package com.danilkhisamov.whalescanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;

@Configuration
public class DBConfig {
    @Bean
    public DBContext dbContext(@Value("${whalescanner.telegram.bot.name}") String botUsername) {
        return MapDBContext.onlineInstance(botUsername);
    }
}
