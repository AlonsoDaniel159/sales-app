# Sales App

Sales App es una aplicación desarrollada en **Java** utilizando el framework **Spring Boot**. Este proyecto tiene como objetivo gestionar las ventas, productos, clientes, proveedores y roles de un sistema de ventas.

## Estructura del Proyecto

El proyecto está organizado de la siguiente manera:

```
sales-app/
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── alonso/
│   │   │           └── salesapp/
│   │   │               ├── SalesAppApplication.java
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── mapper/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── alonso/
│       │           └── salesapp/
│       │               ├── SalesAppApplicationTests.java
│       │               └── repository/
├── target/
└── ...
```

### Descripción de los paquetes principales

- **config/**: Contiene las configuraciones del proyecto, como la configuración de Swagger (`OpenAPIConfig`) y otras configuraciones relacionadas con seguridad y serialización.
- **controller/**: Controladores REST que manejan las solicitudes HTTP y exponen los endpoints de la API.
- **dto/**: Clases de transferencia de datos (DTOs) utilizadas para enviar y recibir datos entre el cliente y el servidor.
- **exception/**: Manejo de excepciones globales y personalizadas.
- **mapper/**: Clases para mapear entidades a DTOs y viceversa.
- **model/**: Clases que representan las entidades del dominio.
- **repository/**: Interfaces que extienden `JpaRepository` para interactuar con la base de datos.
- **service/**: Contiene la lógica de negocio de la aplicación.

## Requisitos Previos

- **Java 17** o superior.
- **Maven 3.8+**.
- **Base de datos PostgreSQL** 

## Configuración del Proyecto

1. Clona el repositorio:
   ```bash
   git clone https://github.com/AlonsoDaniel159/sales-app.git
   cd sales-app
   ```

2. Configura las variables de entorno necesarias en tu sistema o en un archivo `.env`:
   ```
   SERVER_PORT=8080
   DB_URL=jdbc:mysql://localhost:3306/salesdb
   DB_USERNAME=tu_usuario
   DB_PASSWORD=tu_contraseña
   ```

3. Configura el archivo `application.properties` o `application.yml` en `src/main/resources/`:
   ```properties
   server.port=${SERVER_PORT:8080}
   spring.datasource.url=${DB_URL}
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

4. Construye el proyecto con Maven:
   ```bash
   ./mvnw clean install
   ```

5. Ejecuta la aplicación:
   ```bash
   java -jar target/sales-app-0.0.1-SNAPSHOT.jar
   ```

## Documentación de la API

La documentación de la API está disponible a través de **Swagger**. Una vez que la aplicación esté en ejecución, puedes acceder a la documentación en:

```
http://localhost:8080/swagger-ui/index.html
```

## Funcionalidades

- Gestión de productos.
- Gestión de clientes.
- Gestión de proveedores.
- Gestión de roles.
- Gestión de ventas y detalles de ventas.
- Manejo de errores globales.

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Security**
- **Swagger OpenAPI**
- **Maven**
- **MySQL**

## Estructura de Configuración

El proyecto utiliza un enfoque estandarizado para la configuración:

1. **Archivo `application.properties` o `application.yml`**: Contiene configuraciones predeterminadas.
2. **Variables de entorno**: Para valores sensibles como contraseñas y configuraciones específicas del entorno.
3. **Integración con Docker**: Se pueden definir variables de entorno en un archivo `docker-compose.yml` para entornos de contenedores.

## Contacto

- **Autor**: Alonso Quispe
- **Email**: alonsodaniel619@gmail.com
- **Teléfono**: +51 957 501 458
