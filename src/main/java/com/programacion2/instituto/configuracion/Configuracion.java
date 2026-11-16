package com.programacion2.instituto.configuracion;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Marcador de que esta clase contiene configuración de Spring.
@Configuration
public class Configuracion {
    @Bean(destroyMethod = "close")
    public PersistenceManagerFactory persistenceManagerFactory() {
    
        return JDOHelper.getPersistenceManagerFactory("Instituto");
    }
}
