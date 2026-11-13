package com.programacion2.instituto.configuracion;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Indica que esta clase contiene configuración de Spring.
@Configuration
public class Configuracion {

    // Registra la fábrica de persistencia como un bean de Spring.
    // destroyMethod indica que Spring debe llamar a close() al cerrar la aplicación.
    @Bean(destroyMethod = "close")
    public PersistenceManagerFactory persistenceManagerFactory() {
        // "Instituto" es el nombre de la unidad de persistencia de persistence.xml.
        return JDOHelper.getPersistenceManagerFactory("Instituto");
    }
}
