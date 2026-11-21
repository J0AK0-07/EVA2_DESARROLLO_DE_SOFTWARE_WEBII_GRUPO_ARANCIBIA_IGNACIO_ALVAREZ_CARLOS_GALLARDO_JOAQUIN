# Sabor Gourmet

Aplicación de reservas para la Evaluación de la Unidad 2 de **Desarrollo de Software Web II**.
Implementa Spring MVC, Spring Data JPA y Thymeleaf, con interfaz responsiva y una API REST que comparte los servicios de negocio.

## Inicio rápido en Windows

1. Instalar **Java 21**. Se recomienda JDK 21 para poder ejecutar y también compilar el proyecto.
2. Extraer todo el ZIP en una carpeta; no ejecutarlo directamente desde el archivo comprimido.
3. Abrir la carpeta `eva_2` y hacer doble clic en **`run.bat`**.
4. Esperar el mensaje `Started SaborGourmetApplication` en la consola.
5. Abrir **http://localhost:8080** en el navegador. Mantener la consola abierta.
6. Para detener el servidor, presionar **Ctrl+C** en la consola.

El ejecutable incluido en `dist/sabor-gourmet.jar` contiene las dependencias de la aplicación.
Su ejecución y los recursos de las pantallas no requieren internet. La primera **compilación del código fuente** sí necesita conexión para que Maven descargue sus dependencias.

En PowerShell también puede usarse `java -jar .\dist\sabor-gourmet.jar`, o `./run.ps1` si la configuración local permite scripts.
Para usar otro puerto: `java -jar .\dist\sabor-gourmet.jar --server.port=8081` y abrir http://localhost:8081.

## Linux y macOS

Con Java 21 instalado, ejecutar desde la carpeta del proyecto:

```sh
sh run.sh
```

## Qué incluye la entrega

| Ruta | Contenido |
|---|---|
| `src/main/java/cl/ipss/saborgourmet` | Entidades, DTOs, repositorios JPA, servicios, controladores y excepciones |
| `src/main/resources/templates` | Vistas dinámicas Thymeleaf |
| `src/main/resources/static` | CSS, JavaScript e iconos SVG locales |
| `src/test` | Pruebas de integración de reglas, HTTP, vistas y concurrencia |
| `dist/sabor-gourmet.jar` | Ejecutable compilado del código entregado |
| `maven` | Distribución Maven incluida, con sus licencias originales |
| `docs` | Informe, guía, documentación técnica y evidencias verificadas |
| `Postman_SaborGourmet_Collection.json` | Colección de demostración de la API |

## Pantallas

| Dirección | Función |
|---|---|
| `/` | Resumen del restaurante y próximas visitas |
| `/disponibilidad` | Buscar mesas por fecha, hora, duración y personas |
| `/reservas` | Listar, filtrar, editar, cancelar y eliminar reservas |
| `/reservas/nueva` | Crear una reserva con selección dinámica de mesas |
| `/clientes` | Registrar, consultar, editar y eliminar clientes sin reservas asociadas |
| `/mesas` | Administrar número, capacidad, ubicación y habilitación de mesas |

## Base de datos y persistencia

La configuración por defecto usa **H2 en disco**, una base de datos relacional administrada mediante **Hibernate y Spring Data JPA**.
Se crea automáticamente `data/sabor-gourmet.mv.db` dentro de la carpeta de trabajo al iniciar.
Las entidades `Cliente`, `Mesa` y `Reserva` se almacenan en tablas con claves primarias y relaciones `@ManyToOne`.

No se utiliza `datos.json` como almacenamiento. El archivo de base de datos no se distribuye para que cada instalación inicie con datos ficticios limpios.
Cuando la base está completamente vacía, se cargan **3 clientes, 5 mesas y 2 reservas para el día siguiente** de la zona horaria de Chile.
Al reiniciar una base con registros, no se vuelve a cargar la semilla.

Para conservar tus datos, mantén la carpeta `data`. Para respaldarla, detén primero la aplicación y copia esa carpeta.
Si deseas una demostración completamente nueva, detén el servidor y **renombra** `data` a una carpeta de respaldo antes de iniciarlo nuevamente.
Las pruebas automatizadas usan una base **en memoria independiente** y no modifican los datos de tu aplicación.

## Reglas de negocio

- El cliente y la mesa deben existir; la mesa debe estar habilitada y tener capacidad suficiente.
- La fecha y hora de inicio deben ser futuras, según `America/Santiago`.
- Se permiten de 1 a 100 personas y duraciones entre 15 y 480 minutos; la duración predeterminada es 120 minutos.
- Los intervalos se comparan con fechas y horas completas: se cubren turnos que cruzan medianoche y se permiten turnos contiguos.
- Cada escritura usa una transacción. La mesa se bloquea durante la comprobación y el guardado para impedir reservas simultáneas superpuestas.
- Una reserva cancelada libera disponibilidad y conserva su registro. No se puede editar una reserva cancelada.
- Los clientes y mesas con historial asociado no se eliminan; se mantienen las claves foráneas. Las reservas pueden eliminarse administrativamente.
- No se puede reducir la capacidad por debajo de una reserva activa ni desactivar una mesa con reservas futuras sin resolver esas reservas.
- El correo del cliente y el número de mesa son únicos; también tienen restricciones en la base de datos.

## Compilar y ejecutar las pruebas

En Windows, ejecutar **`test.bat`** para las pruebas, o **`compilar.bat`** para probar, compilar y actualizar el JAR de `dist`.

```powershell
.\maven\bin\mvn.cmd clean verify
```

En Linux/macOS:

```sh
sh maven/bin/mvn clean verify
```

El resultado esperado es `BUILD SUCCESS`, con cero fallos y errores. Los resultados de una ejecución se escriben en `target/surefire-reports`.
Los resultados comprobados al preparar esta entrega están en `docs/evidencias/RESULTADOS_PRUEBAS.txt` y sus XML adjuntos.
Si modificas el código, recompila antes de volver a usar `run.bat`, porque ese archivo inicia el JAR existente.

## API REST y Postman

La API responde en JSON bajo `/api`; la interfaz web responde en HTML. Ambas llaman a los mismos servicios.

| Recurso | Operaciones |
|---|---|
| `/api/clientes` | GET, POST |
| `/api/clientes/{id}` | GET, PUT, DELETE |
| `/api/mesas` | GET, POST |
| `/api/mesas/{id}` | GET, PUT, DELETE |
| `/api/mesas/disponibles` | GET con `fecha`, `hora`, `personas`, `duracion`; `excluirReservaId` opcional para edición |
| `/api/reservas` | GET con filtros opcionales `fecha`, `clienteId`, `mesaId`, `estado`; POST |
| `/api/reservas/{id}` | GET, PUT, DELETE |
| `/api/reservas/{id}/cancelar` | PATCH; PUT alternativo |

Se usan 200 para consultas/actualizaciones, 201 para creación, 204 para eliminación, 400 para datos inválidos, 404 para recursos inexistentes y 409 para conflictos.
Importar la colección JSON en Postman y ejecutarla en orden; crea sus propios registros, recoge los identificadores y utiliza una fecha futura.

## Perfil MySQL opcional

El funcionamiento probado por defecto usa H2. Si se desea MySQL, crear previamente la base `saborgourmet`, configurar el servidor y ejecutar:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/saborgourmet?serverTimezone=America/Santiago'
$env:DB_USER = 'usuario'
$env:DB_PASSWORD = 'su_clave'
java -jar .\dist\sabor-gourmet.jar --spring.profiles.active=mysql
```

Este perfil se entrega como configuración adicional; las evidencias de esta entrega corresponden a H2.

## Alcance de la demostración

La aplicación se enlaza por defecto a `127.0.0.1`. Incluye las funciones de clientes y administración en una instalación local de evaluación.
La pauta no solicita autenticación, pagos ni notificaciones por correo; esas funciones no se presentan como implementadas.
La guía de evidencias ayuda a explicar y comprobar el proyecto; el enunciado solicita código e informe y no exige un video.

## Antes de subir a la plataforma

Revisar los integrantes y completar el nombre real del docente en la portada del informe Word. El avance original identifica a Carlos Oriel Álvarez Arévalo; se incorporó a Ignacio Arancibia en la versión preparada.
El nombre de los documentos sigue el formato `EVU2_GRUPO_APELLIDO_NOMBRE` indicado en la evaluación.
