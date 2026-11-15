# Instituto – Gestión de cursos

Trabajo práctico grupal de **Programación 2** – IFES, Tecnicatura en Análisis de Sistemas.

## Integrantes

- Bryan Hernandez
- Julieta Fernandez
- Santino Resch
- Fausto Kegel

## Descripción

Aplicación web para administrar un instituto educativo. Permite registrar y
mantener alumnos, profesores y cursos, asignar un profesor a cada curso e
inscribir alumnos en los cursos respetando el cupo disponible.

Los datos se guardan en una base MySQL, de modo que se conservan al cerrar y
volver a ejecutar la aplicación.


## Requisitos previos

- JDK 17 o superior
- Apache Maven 3.9 o superior
- MySQL Server 8.0 (corriendo en el puerto 3306)
- Conexión a internet (los estilos de Bootstrap se cargan desde un CDN)

## Preparar la base de datos

1. Abrir una conexión a MySQL con un usuario administrador (por ejemplo `root`),
   desde MySQL Workbench o la consola.
2. Ejecutar el script `script-bd.sql` de la raíz del repositorio. Crea la base
   `instituto` y el usuario `instituto` con sus permisos.
   - Con Workbench: *File → Open SQL Script*, elegir el archivo y ejecutarlo

   En el persistence.xml
 - Servidor : `localhost:3306` 
 - Base : `instituto` 
 - Usuario  `instituto` |
- Contraseña  `instituto_tp` 


## Ejecución

1. Clonar el repositorio:

git clone https://github.com/Briamher95/instituto-java-spring.git
cd instituto-java-spring

2. Ejecutar la aplicación:

mvn spring-boot:run
