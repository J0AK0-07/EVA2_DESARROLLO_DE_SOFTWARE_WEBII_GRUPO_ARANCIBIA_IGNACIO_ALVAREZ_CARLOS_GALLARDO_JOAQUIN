# Documentación de archivos Java

SaborGourmetApplication.java  Contiene main y @SpringBootApplication. Inicia el contexto de Spring, la configuración automática y el servidor embebido de la aplicación.

config/DemoDataInitializer.java  Implementa CommandLineRunner. run carga tres clientes ficticios, cinco mesas y dos reservas para el día siguiente solo cuando todas las tablas de negocio están vacías. Se desactiva durante las pruebas.

config/WebConfig.java  Declara localeResolver con configuración es-CL. Define el idioma de presentación y la interpretación regional de los mensajes del sistema web.

model/Cliente.java  Representa a un cliente persistente. Define su identificador, campos obligatorios y restricción única de correo. Sus accesores permiten al servicio normalizar y actualizar los datos.

model/Mesa.java  Representa una mesa y su configuración operativa. Almacena número, capacidad, ubicación y habilitación, con un número único por mesa.

model/Reserva.java  Representa las asociaciones con cliente y mesa y el intervalo completo de la reserva. definirHorario calcula inicio y fin; los accesores derivados obtienen fecha, horas y duración sin duplicar columnas. Mantiene el estado y las observaciones.

model/EstadoReserva.java  Enum que define ACTIVA, CANCELADA y COMPLETADA. Los flujos implementados crean reservas activas y permiten cancelarlas; COMPLETADA queda definida como estado de dominio para una posible ampliación, sin presentarse como una operación disponible en la interfaz.

dto/ClienteDTO.java  Recibe nombre, correo y teléfono para crear o editar clientes. @NotBlank, @Email y @Size comprueban campos obligatorios, formato y longitudes antes de llamar al servicio.

dto/MesaDTO.java  Recibe número, capacidad, ubicación y habilitación. @NotNull, @Min y @Max establecen los rangos; el servicio verifica además el número único y las reservas asociadas.

dto/ReservaRequestDTO.java  Recibe identificadores de cliente y mesa, fecha, hora, duración, comensales y observaciones. Utiliza validación y DateTimeFormat para el enlace del formulario. Las mismas propiedades se reciben en JSON.

dto/ReservaResponseDTO.java  Presenta el resultado enriquecido con datos de cliente y mesa, estado y horario. getFechaFin permite indicar cuándo un turno termina en el día siguiente sin exponer directamente la entidad JPA.

dto/DisponibilidadConsultaDTO.java  Agrupa los parámetros de búsqueda, la hora final y el listado de mesas disponibles. También informa el total de resultados para la API y la vista de disponibilidad.

dto/ErrorResponseDTO.java  Estandariza timestamp, estado HTTP, tipo de error, mensaje y ruta de la solicitud. Permite interpretar las respuestas fallidas de la API sin exponer trazas internas.

repository/ClienteRepository.java  Extiende JpaRepository para el CRUD. findByEmailIgnoreCase detecta correos duplicados; bloquearPorId obtiene un bloqueo de escritura al modificar o relacionar el cliente dentro de una transacción.

repository/MesaRepository.java  Extiende JpaRepository, ordena las mesas por número y permite bloquear una mesa por identificador. buscarDisponibles usa JPQL con NOT EXISTS y ordena por capacidad y número, sin una consulta adicional por cada mesa.

repository/ReservaRepository.java  Implementa búsquedas con filtros, lectura de reservas con EntityGraph, comprobación de solapamientos y bloqueo de reservas. Los métodos existsByCliente_Id y existsByMesa_Id verifican referencias mediante relaciones persistentes explícitas.

service/HorarioService.java  Centraliza validar: exige inicio futuro en America/Santiago, de 1 a 100 personas y de 15 a 480 minutos. Es reutilizado al consultar disponibilidad y al guardar reservas, manteniendo las mismas reglas en ambos flujos.

service/ClienteService.java  listarTodos y obtenerPorId consultan clientes. crear y actualizar validan datos y unicidad y normalizan el correo. eliminar obtiene un bloqueo y comprueba que no exista historial asociado. Las escrituras utilizan @Transactional.

service/MesaService.java  Gestiona el CRUD y delega la disponibilidad al repositorio. actualizar impide reducir capacidad por debajo de una reserva activa o desactivar una mesa con reservas futuras. eliminar protege el historial. consultarDisponibilidad valida el intervalo y admite excluir una reserva al editar.

service/ReservaService.java  listar y obtenerPorId producen DTOs enriquecidos. crear y modificar utilizan aplicarDatos para validar, bloquear la mesa y consultar conflictos antes de guardar. cancelar cambia el estado; eliminar retira el registro. Las operaciones completas se ejecutan dentro de transacciones JPA.

controller/ClienteController.java  Expone GET, POST, PUT y DELETE de clientes bajo /api/clientes. Aplica @Valid a las entradas y devuelve ResponseEntity con el código HTTP correspondiente, delegando cada operación en ClienteService.

controller/MesaController.java  Expone el CRUD de mesas y GET /api/mesas/disponibles. Convierte los parámetros de fecha y hora; admite excluirReservaId para recalcular el turno durante una edición. Delega la consulta y las escrituras en MesaService.

controller/ReservaController.java  Expone listados con filtros, lectura, creación, modificación, cancelación y eliminación. cancelar acepta un motivo opcional y cambia el estado mediante PATCH; cancelarPut conserva una alternativa con PUT. El controlador no calcula solapamientos ni escribe directamente en la base.

web/InicioController.java  inicio prepara las estadísticas y próximas reservas usando los servicios. Devuelve la vista inicio, con datos reales de la base y fechas calculadas según la zona horaria configurada.

web/ReservaWebController.java  Prepara agenda, disponibilidad y formularios de creación/edición. guardar utiliza BindingResult para conservar campos y mensajes; cancelar y eliminar redirigen con un mensaje de resultado. preparar obtiene los clientes y mesas aptas para el formulario.

web/ClienteWebController.java  listar, nuevo y editar preparan las vistas de clientes. guardar aplica Bean Validation y delega creación o actualización. eliminar informa si el historial impide borrar el cliente; las operaciones exitosas usan redirección para evitar repetir el POST al actualizar la página.

web/MesaWebController.java  Prepara las vistas de administración de mesas. guardar conserva errores de validación y negocio en el formulario; eliminar comunica el resultado. Reutiliza MesaService, por lo que la interfaz y la API aplican las mismas restricciones.

exception/BadRequestException.java  Expresa solicitudes que no cumplen las reglas de entrada, como capacidad insuficiente, horarios pasados o duración inválida. El tratamiento REST corresponde a HTTP 400.

exception/ConflictException.java  Expresa un conflicto con el estado existente, como un cruce de reservas, un dato único repetido o un intento de eliminar registros vinculados. La API responde HTTP 409.

exception/ResourceNotFoundException.java  Expresa la ausencia del cliente, mesa o reserva solicitada. Se transforma en una respuesta HTTP 404 tanto para la API como para las vistas.

exception/GlobalExceptionHandler.java  Limita su alcance a los controladores REST. Convierte excepciones de negocio, validación, conversión de parámetros e integridad de datos a ErrorResponseDTO. Los errores inesperados se registran sin devolver detalles internos al cliente.

web/WebExceptionHandler.java  Limita su alcance a los controladores web y responde con la plantilla error. Mantiene códigos 400, 404 y 409 para las páginas. fechaMinima proporciona la fecha de Chile para las restricciones de los formularios.

ReservaIntegrationTest.java  Comprueba creación y modificación, cruces de horario, turnos contiguos y medianoche, cancelación, capacidad, fechas, duración, integridad del historial, unicidad, filtros y CRUD de clientes y mesas usando repositorios reales.

ApiAndViewsIntegrationTest.java  Comprueba códigos HTTP, validación de entradas, cancelación, conflictos, redirecciones y renderizado real de las plantillas principales y sus formularios de edición mediante MockMvc con el contexto de Spring completo.

ConcurrenciaIntegrationTest.java  Ejecuta dos solicitudes con clientes distintos y transacciones simultáneas sobre una misma mesa y turno. Verifica que exista exactamente una creación y un conflicto, en lugar de dos reservas superpuestas.
