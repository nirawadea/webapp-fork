package com.neu.edu.Assingment1.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.beans.BeanProperty;
@Configuration
@Component
public class DbConnection {

     @Value("${spring.datasource.url}")
     private String host;
     @Value("${spring.datasource.username}")
     private String username;
     @Value(("${spring.datasource.password}"))
     private String password;

    @Bean
    public DataSource createConnection()
    {
        return DataSourceBuilder.create().url(host).username(username).password(password).build();
    }
}
