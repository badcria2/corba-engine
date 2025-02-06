# Corba Engine

Corba Engine es un motor de reglas desarrollado en Java utilizando Spring Boot, Drools, MongoDB y Kafka. Expone un endpoint GraphQL a través de WebFlux para facilitar la interacción con otros servicios.

## Características

- **Motor de Reglas Drools:** Permite gestionar la lógica de negocio a través de reglas de Drools, facilitando su modificación y mantenimiento.
- **Persistencia con MongoDB:** Utiliza MongoDB para almacenar datos y configuraciones de manera flexible y escalable.
- **Integración con Kafka:** Implementa Kafka para la comunicación asíncrona, garantizando una interacción eficiente y desacoplada entre servicios.
- **API GraphQL con WebFlux:** Expone un API GraphQL accesible en `/oc/graphql`, permitiendo consultas eficientes.
- **Spring Boot:** Simplifica la configuración, ejecución y despliegue de la aplicación.

## Requisitos

Antes de ejecutar la aplicación, asegúrate de contar con:

- Java 8
- Maven
- MongoDB en ejecución
- Kafka en ejecución

## Configuración

### 1. MongoDB
Asegúrate de tener una instancia de MongoDB en ejecución. La configuración de la conexión se encuentra en `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:85857855@localhost:28000/drools?authSource=admin
```

### 2. Kafka
Configura y ejecuta una instancia de Kafka. La configuración del productor y consumidor se encuentra en `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: mi-grupo-consumidor
      auto-offset-reset: latest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### 3. GraphQL
El endpoint GraphQL está disponible en `/oc/graphql`. Puedes utilizar herramientas como **GraphiQL** o **Postman** para interactuar con la API.

### 4. Dependencias principales
El proyecto utiliza las siguientes dependencias clave:

- Spring Boot Starter Data MongoDB
- Spring Boot Starter Web
- Spring Boot Starter WebFlux
- Apache Avro
- Lombok
- Spring Kafka
- Drools Core
- Drools Spring

Puedes encontrar la lista completa de dependencias en `pom.xml`.

## Construcción y Ejecución

Para compilar y construir el proyecto, ejecuta:

```bash
mvn clean install
```

Para ejecutar la aplicación, usa el siguiente comando:

```bash
mvn spring-boot:run
```

## Uso

### API GraphQL
Una vez en ejecución, puedes interactuar con la API GraphQL en `/oc/graphql`.

### Endpoints REST
El servicio expone los siguientes endpoints para la gestión de reglas:

- **POST** `/rules` → Crea una nueva regla. (Requiere un objeto `Rule` en el cuerpo de la solicitud).
- **GET** `/rules` → Obtiene la lista de reglas existentes.
- **GET** `/rules/{id}` → Obtiene una regla por su ID.
- **PUT** `/rules/{id}` → Actualiza una regla existente. (Requiere el ID de la regla y un objeto `Rule` actualizado en el cuerpo de la solicitud).
- **DELETE** `/rules/{id}` → Elimina una regla por su ID.
- **GET** `/rules/search?pattern={nombre}` → Busca reglas por nombre.
- **POST** `/rules/validar-edad` → Ejecuta reglas para validar la edad de una persona. (Requiere un objeto `Persona` en el cuerpo de la solicitud).

### Ejemplo de solicitud POST para crear una regla

```json
{
  "_id": "675a1df1625d148cec6ddca5",
  "name": "abcd_updated",
  "imports": [
    "corba.engine.models.KafkaData",
    "corba.engine.models.Tags",
    "java.util.Map",
    "corba.engine.rules.EventCorbaService",
    "java.util.List"
  ],
  "actions": [
    "$actionService.evalueAvailablesGroups($kafkaData);",
    "$actionService.enviarcampania($persona);"
  ],
  "packageName": "corba.engine.rules",
  "conditions": [
    "$kafkaData : KafkaData(tags.source == '10.95.90.85')",
    "$actionService : EventCorbaService()",
    "$power : Double(this != null && this > -9.0 ) from $kafkaData.getTargetOutputPower()",
    "$persona : Persona(edad > 18)"
  ]
}
```

### Ejemplo de regla almacenada en MongoDB

```json
{
  "_id": {
    "$oid": "675a1df1625d148cec6ddca5"
  },
  "packageName": "corba.engine.rules",
  "imports": [
    "corba.engine.models.KafkaData",
    "corba.engine.models.Tags",
    "java.util.Map",
    "corba.engine.rules.EventCorbaService",
    "java.util.List"
  ],
  "name": "eventoPostman",
  "conditions": [
    "$kafkaData : KafkaData(tags.source == '10.95.90.87')",
    "$actionService : EventCorbaService()",
    "$power : Double(this != null && this  > -9.2 ) from $kafkaData.getTargetOutputPower()"
  ],
  "actions": [
    "$actionService.evalueAvailablesGroups($kafkaData);"
  ],
  "_class": "corba.engine.rules.Rule"
}
```
 