package com.neu.edu.Assingment1.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbConnection {

    @Value("${spring.datasource.url}")
    private String host;
    @Value("${spring.datasource.username}")
    private String username;
    @Value(("${spring.datasource.password}"))
    private String password;

    public DataSource createConnection() {
        return DataSourceBuilder.create().url(host).username(username).password(password).build();
    }
}
