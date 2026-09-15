# Módulo automatizado de gestión y validación de reservas

Proyecto desarrollado como parte del trabajo de tesis orientado a la implementación de un módulo automatizado para la gestión y validación de reservas de ambientes de estudio en entornos universitarios.

## Tecnologías

- Java 21
- Spring Boot
- Spring Data JPA
- Thymeleaf
- HTML, CSS y JavaScript
- PostgreSQL
- Maven

## Estrategia de ramas

- `main`: contiene versiones estables y verificadas del proyecto.
- `develop`: contiene el desarrollo activo y los cambios en curso.

Los cambios se implementan primero en `develop` y se integran en `main` cuando representan un hito estable y probado.

## Convención de commits

Los commits utilizan la siguiente estructura:

```text
tipo: resumen breve en español
```

El resumen debe ser breve y describir claramente el cambio realizado.

Cuando sea necesario agregar una descripción extendida, esta se redactará en inglés y podrá generarse con apoyo de GitHub Copilot desde GitHub Desktop.

### Tipos

- `feat`: nueva funcionalidad.
- `fix`: corrección de errores.
- `refactor`: reestructuración de código sin cambiar su comportamiento funcional.
- `test`: creación o modificación de pruebas.
- `docs`: cambios de documentación.
- `style`: cambios de formato sin impacto en la lógica.
- `chore`: configuración, dependencias o tareas de mantenimiento.

## Base de datos

El proyecto utiliza PostgreSQL.

Base de datos de desarrollo:

```text
modulo_automatizado
```

Usuario de conexión:

```text
modulo_user
```

La estructura de la base de datos se gestionará principalmente mediante entidades JPA e Hibernate.

## Configuración local

Las credenciales no deben almacenarse directamente en el repositorio.

La contraseña de PostgreSQL se proporciona mediante la variable de entorno:

```text
DB_PASSWORD
```

La configuración de conexión se encuentra en:

```text
src/main/resources/application.properties
```

## Estado del proyecto

El proyecto se encuentra en desarrollo.

La rama `main` mantiene la última línea base estable y `develop` concentra los cambios en curso.