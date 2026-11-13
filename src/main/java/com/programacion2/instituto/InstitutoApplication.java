package com.programacion2.instituto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada: activa la configuración automática y el escaneo de componentes
// de este paquete y de todos sus subpaquetes.
@SpringBootApplication
public class InstitutoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstitutoApplication.class, args);
	}

}
