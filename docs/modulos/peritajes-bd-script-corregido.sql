-- ==========================================================
-- Base de datos integrada del sistema judicial — VERSIÓN 3 (incluye cambios de Peritajes, Equipo 3)
-- Módulos: Oficialía de Partes (Ofc), Audiencias (Aud), Peritajes (Per),
--          Segunda Instancia (Seg) y Copias Certificadas (Cop)
-- Motor: MySQL 8.0.16 o superior (requiere soporte de CHECK)
-- Script DDL: las tablas se crean en orden de dependencia de llaves foráneas
--
-- CAMBIOS DE PERITAJES respecto al script original (buscar "CAMBIO PERITAJES"):
--   1. user_Per: intentos_fallidos y bloqueado_hasta (RF 1.1: bloqueo de 15 min después de 3 intentos).
--   2. peritaje_Per: fecha_apertura, fecha_entrega y fecha_cancelacion (RF 4.2, 4.4 y 2.4).
--   3. dictamen_Per: version (RF 4.3: aviso "Hay cambios más recientes en otra ventana" en el autoguardado).
--   4. bitacora_Per: referencia (folio del peritaje o número de registro del perito, para filtrar la bitácora).
--   5. Trigger trg_user_Per_login_au: al cambiar la contraseña en user_Per también se cambia en Login
--      (RF 1.2; el trigger original solo copiaba la contraseña al crear el usuario).
--
-- CORRECCIONES DE LA REVISIÓN (buscar "CORRECCIÓN"):
--   6. Los triggers AFTER UPDATE buscan en Login por el correo ANTERIOR (OLD.correo) y actualizan
--      correo y contraseña; antes, si cambiaba el correo, Login se quedaba con los datos viejos.
--   7. El trigger AFTER UPDATE se agrega también a user_Ofc, user_Aud y user_Seg para que
--      Login quede sincronizado en todos los módulos.
--   8. CHECK en peritaje_Per (fecha_cancelacion solo si está 'Cancelado'; fecha_entrega solo si está
--      'Dictamen Entregado').
--
-- CAMBIOS DE LA VERSIÓN 3 (buscar "VERSIÓN 3"), por el inicio de sesión único del núcleo:
--   9. intentos_fallidos y bloqueado_hasta pasan de user_Per a Login (el bloqueo de 3 intentos aplica a todos
--      los módulos). El CHECK de intentos también pasa a Login.
--  10. bitacora_Per: nombre_usuario y rol_usuario. El administrador general (SISTEMA_ADMIN) no está en user_Per,
--      así que su id_usuario va NULL y su nombre queda en estas columnas.
--  11. asignacion_Per: la asignación automática es la que no tiene perito saliente. En una reasignación
--      manual, id_usuario_cambio NULL significa que la hizo el administrador general.
-- Contraseñas: todos los módulos las guardan cifradas con PBKDF2 (nucleo/acceso/Contrasena); el trigger
-- las copia a Login y el inicio de sesión del núcleo las revisa ahí.
-- Si la base ya está creada con una versión anterior, NO se vuelve a correr este archivo completo:
-- original → versión 2: peritajes-bd-cambios.sql · versión 2 → versión 3: peritajes-bd-cambios-v3.sql.
-- ==========================================================

CREATE DATABASE IF NOT EXISTS sistema_judicial
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sistema_judicial;

-- Catálogo auxiliar de roles. Lo referencian todas las tablas de usuario (user_*).
CREATE TABLE Rol (
    id_rol INT NOT NULL AUTO_INCREMENT,
    rol VARCHAR(50) NOT NULL,
    CONSTRAINT pk_Rol PRIMARY KEY (id_rol),
    CONSTRAINT uq_Rol_rol UNIQUE (rol)
) ENGINE=InnoDB;

-- Credenciales de acceso de todo el sistema. Se llena automáticamente mediante triggers AFTER INSERT en cada tabla user_*; la PK garantiza que un correo no se repita entre módulos.
-- VERSIÓN 3: intentos_fallidos y bloqueado_hasta (antes en user_Per): 3 intentos fallidos bloquean
-- la cuenta 15 minutos en cualquier módulo. Los usa el inicio de sesión del núcleo (/login).
CREATE TABLE Login (
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    CONSTRAINT pk_Login PRIMARY KEY (correo),
    CONSTRAINT ck_Login_intentos CHECK (intentos_fallidos >= 0)
) ENGINE=InnoDB;

-- Usuarios del módulo de Oficialía de Partes; también son los usuarios del módulo de Copias Certificadas.
CREATE TABLE user_Ofc (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    Status BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_Ofc PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Ofc_correo UNIQUE (correo),
    CONSTRAINT fk_user_Ofc_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
) ENGINE=InnoDB;

-- Catálogo de distritos judiciales. Compartido por todos los módulos.
CREATE TABLE distrito_Ofc (
    clave_distrito VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cabecera VARCHAR(150) NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_distrito_Ofc PRIMARY KEY (clave_distrito)
) ENGINE=InnoDB;

-- Catálogo de juzgados. Compartido por todos los módulos.
CREATE TABLE juzgado_Ofc (
    clave_juzgado VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    clave_distrito VARCHAR(10) NOT NULL,
    CONSTRAINT pk_juzgado_Ofc PRIMARY KEY (clave_juzgado),
    CONSTRAINT fk_juzgado_Ofc_clave_distrito FOREIGN KEY (clave_distrito) REFERENCES distrito_Ofc(clave_distrito)
) ENGINE=InnoDB;

-- Oficialías de partes registradas.
CREATE TABLE oficialia_Ofc (
    clave_oficialia VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    clave_juzgado VARCHAR(10) NOT NULL,
    CONSTRAINT pk_oficialia_Ofc PRIMARY KEY (clave_oficialia),
    CONSTRAINT fk_oficialia_Ofc_clave_juzgado FOREIGN KEY (clave_juzgado) REFERENCES juzgado_Ofc(clave_juzgado)
) ENGINE=InnoDB;

-- Relaciona a cada usuario con la oficialía de partes en la que trabaja.
CREATE TABLE oficialia_personal_Ofc (
    id_op_trabajador INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    clave_oficialia VARCHAR(10) NOT NULL,
    CONSTRAINT pk_oficialia_personal_Ofc PRIMARY KEY (id_op_trabajador),
    CONSTRAINT fk_oficialia_personal_Ofc_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Ofc(id_usuario),
    CONSTRAINT fk_oficialia_personal_Ofc_clave_oficialia FOREIGN KEY (clave_oficialia) REFERENCES oficialia_Ofc(clave_oficialia)
) ENGINE=InnoDB;

-- Catálogo de materias legales. Compartido por todos los módulos.
CREATE TABLE materia_Ofc (
    clave_materia VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_materia_Ofc PRIMARY KEY (clave_materia),
    CONSTRAINT uq_materia_Ofc_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- Personas físicas o morales que participan como partes en una demanda.
CREATE TABLE persona_Ofc (
    id_persona INT NOT NULL AUTO_INCREMENT,
    tipo_persona ENUM('FISICA','MORAL') NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NULL,
    apellido_materno VARCHAR(50) NULL,
    rfc VARCHAR(13) NULL,
    curp VARCHAR(18) NULL,
    CONSTRAINT pk_persona_Ofc PRIMARY KEY (id_persona)
) ENGINE=InnoDB;

-- Demandas recibidas en Oficialía de Partes. La consultan Audiencias y Peritajes.
CREATE TABLE demanda_Ofc (
    folio VARCHAR(20) NOT NULL,
    clave_materia VARCHAR(10) NOT NULL,
    id_demandante INT NULL,
    id_demandado INT NULL,
    descripcion VARCHAR(255) NOT NULL,
    clave_oficialia VARCHAR(10) NOT NULL,
    fecha_recepcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE','PROCEDENTE','IMPROCEDENTE') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_demanda_Ofc PRIMARY KEY (folio),
    CONSTRAINT fk_demanda_Ofc_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(clave_materia),
    CONSTRAINT fk_demanda_Ofc_id_demandante FOREIGN KEY (id_demandante) REFERENCES persona_Ofc(id_persona),
    CONSTRAINT fk_demanda_Ofc_id_demandado FOREIGN KEY (id_demandado) REFERENCES persona_Ofc(id_persona),
    CONSTRAINT fk_demanda_Ofc_clave_oficialia FOREIGN KEY (clave_oficialia) REFERENCES oficialia_Ofc(clave_oficialia)
) ENGINE=InnoDB;

-- Documentos anexos a una demanda.
CREATE TABLE documento_Ofc (
    id_documento INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(20) NOT NULL,
    ruta_archivo BLOB NOT NULL,
    CONSTRAINT pk_documento_Ofc PRIMARY KEY (id_documento),
    CONSTRAINT fk_documento_Ofc_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio)
) ENGINE=InnoDB;

-- Usuarios del módulo de Audiencias (administradores y abogados de audiencia).
CREATE TABLE user_Aud (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    cedula_profesional VARCHAR(20) NULL,
    fecha_nombramiento DATE NULL,
    Status BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_user_Aud PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Aud_correo UNIQUE (correo),
    CONSTRAINT fk_user_Aud_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
) ENGINE=InnoDB;

-- Mesas de audiencia. Cada mesa pertenece a un juzgado y una materia y tiene un abogado fijo (1:1). El distrito se obtiene del juzgado.
CREATE TABLE mesa_audiencia_Aud (
    id_mesa INT NOT NULL AUTO_INCREMENT,
    nombre_mesa VARCHAR(100) NOT NULL,
    clave_juzgado VARCHAR(10) NOT NULL,
    clave_materia VARCHAR(10) NOT NULL,
    id_abogado_asignado INT NOT NULL,
    CONSTRAINT pk_mesa_audiencia_Aud PRIMARY KEY (id_mesa),
    CONSTRAINT uq_mesa_audiencia_Aud_id_abogado_asignado UNIQUE (id_abogado_asignado),
    CONSTRAINT uq_mesa_juzgado UNIQUE (nombre_mesa, clave_juzgado),
    CONSTRAINT fk_mesa_audiencia_Aud_clave_juzgado FOREIGN KEY (clave_juzgado) REFERENCES juzgado_Ofc(clave_juzgado),
    CONSTRAINT fk_mesa_audiencia_Aud_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(clave_materia),
    CONSTRAINT fk_mesa_audiencia_Aud_id_abogado_asignado FOREIGN KEY (id_abogado_asignado) REFERENCES user_Aud(id_usuario)
) ENGINE=InnoDB;

-- Expediente generado cuando una demanda procede. Lo consultan Audiencias, Segunda Instancia y Copias Certificadas.
CREATE TABLE expediente_Ofc (
    num_expediente VARCHAR(20) NOT NULL,
    folio VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVA','CONCLUIDA') NOT NULL DEFAULT 'ACTIVA',
    clasificacion ENUM('PUBLICO','PRIVADO') NOT NULL,
    id_mesa INT NOT NULL,
    CONSTRAINT pk_expediente_Ofc PRIMARY KEY (num_expediente),
    CONSTRAINT uq_expediente_Ofc_folio UNIQUE (folio),
    CONSTRAINT fk_expediente_Ofc_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_expediente_Ofc_id_mesa FOREIGN KEY (id_mesa) REFERENCES mesa_audiencia_Aud(id_mesa)
) ENGINE=InnoDB;

-- Salas de Segunda Instancia.
CREATE TABLE sala_Seg (
    id_sala BIGINT NOT NULL AUTO_INCREMENT,
    clave_distrito VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    estatus BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sala_Seg PRIMARY KEY (id_sala),
    CONSTRAINT fk_sala_Seg_clave_distrito FOREIGN KEY (clave_distrito) REFERENCES distrito_Ofc(clave_distrito)
) ENGINE=InnoDB;

-- Registro en Oficialía del envío de un expediente a Segunda Instancia.
CREATE TABLE segunda_instancia_Ofc (
    id_segunda_instancia INT NOT NULL AUTO_INCREMENT,
    num_expediente VARCHAR(20) NOT NULL,
    id_sala BIGINT NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('REGISTRADA','ENVIADA','RECIBIDA') NOT NULL DEFAULT 'REGISTRADA',
    CONSTRAINT pk_segunda_instancia_Ofc PRIMARY KEY (id_segunda_instancia),
    CONSTRAINT fk_segunda_instancia_Ofc_num_expediente FOREIGN KEY (num_expediente) REFERENCES expediente_Ofc(num_expediente),
    CONSTRAINT fk_segunda_instancia_Ofc_id_sala FOREIGN KEY (id_sala) REFERENCES sala_Seg(id_sala)
) ENGINE=InnoDB;

-- Bitácora de operaciones de Oficialía de Partes y de Copias Certificadas.
CREATE TABLE bitacora_Ofc (
    id_bitacora INT NOT NULL AUTO_INCREMENT,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fuente ENUM('APLICACION','BASE DE DATOS') NOT NULL,
    nombre_tabla VARCHAR(50) NOT NULL,
    operacion ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    id_registro VARCHAR(50) NOT NULL,
    id_usuario_aplicacion INT NULL,
    usuario_bd VARCHAR(50) NULL,
    datos_anteriores JSON NULL,
    datos_nuevos JSON NULL,
    CONSTRAINT pk_bitacora_Ofc PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_Ofc_id_usuario_aplicacion FOREIGN KEY (id_usuario_aplicacion) REFERENCES user_Ofc(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Audiencias de una demanda. El abogado que atiende se obtiene de la mesa del expediente de la demanda.
CREATE TABLE audiencia_Aud (
    id_audiencia INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(20) NOT NULL,
    fecha_hora_inicio DATETIME NOT NULL,
    estado_audiencia ENUM('PROGRAMADA','SUSPENDIDA_POR_INASISTENCIA','CELEBRADA','REPROGRAMADA') NOT NULL DEFAULT 'PROGRAMADA',
    declaracion_demandante TEXT NULL,
    declaracion_demandado TEXT NULL,
    declaracion_rep_legales TEXT NULL,
    acta_firmada_url VARCHAR(500) NULL,
    CONSTRAINT pk_audiencia_Aud PRIMARY KEY (id_audiencia),
    CONSTRAINT fk_audiencia_Aud_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio)
) ENGINE=InnoDB;

-- Pase de lista individual de los participantes de una audiencia.
CREATE TABLE participante_audiencia_Aud (
    id_participante INT NOT NULL AUTO_INCREMENT,
    id_audiencia INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    rol_participante ENUM('DEMANDANTE','DEMANDADO','REP_LEGAL','PERITO') NOT NULL,
    asistencia BOOLEAN NULL,
    CONSTRAINT pk_participante_audiencia_Aud PRIMARY KEY (id_participante),
    CONSTRAINT fk_participante_audiencia_Aud_id_audiencia FOREIGN KEY (id_audiencia) REFERENCES audiencia_Aud(id_audiencia)
) ENGINE=InnoDB;

-- Solicitudes de peritaje que Audiencias envía al módulo de Peritajes. El perito asignado se obtiene de asignacion_Per.
CREATE TABLE solicitud_perito_Aud (
    id_solicitud_perito INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(20) NOT NULL,
    clave_materia VARCHAR(10) NOT NULL,
    CONSTRAINT pk_solicitud_perito_Aud PRIMARY KEY (id_solicitud_perito),
    CONSTRAINT fk_solicitud_perito_Aud_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_solicitud_perito_Aud_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(clave_materia)
) ENGINE=InnoDB;

-- Resolución de la demanda emitida en Audiencias (una por demanda).
CREATE TABLE resolucion_demanda_Aud (
    id_resolucion INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(20) NOT NULL,
    id_abogado INT NOT NULL,
    resolucion TEXT NOT NULL,
    sentido ENUM('FAVOR_DEMANDANTE','FAVOR_DEMANDADO','ACUERDO_PARTES') NOT NULL,
    fecha_resolucion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_resolucion_demanda_Aud PRIMARY KEY (id_resolucion),
    CONSTRAINT uq_resolucion_demanda_Aud_folio UNIQUE (folio),
    CONSTRAINT fk_resolucion_demanda_Aud_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_resolucion_demanda_Aud_id_abogado FOREIGN KEY (id_abogado) REFERENCES user_Aud(id_usuario)
) ENGINE=InnoDB;

-- Bitácora inalterable de Audiencias. Nombre y rol del actor se obtienen de user_Aud y Rol.
CREATE TABLE bitacora_Aud (
    id_bitacora BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario INT NULL,
    tipo_accion VARCHAR(100) NOT NULL,
    operacion ENUM('CONSULTA','CREACION','EDICION','FINALIZACION') NOT NULL,
    referencia VARCHAR(100) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bitacora_Aud PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_Aud_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Aud(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Usuarios del módulo de Peritajes (administradores y peritos).
-- VERSIÓN 3: intentos_fallidos y bloqueado_hasta pasaron a Login.
CREATE TABLE user_Per (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    Status BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_user_Per PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Per_correo UNIQUE (correo),
    CONSTRAINT fk_user_Per_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
) ENGINE=InnoDB;

-- Acreditaciones académicas, certificaciones y especialidades del perito.
CREATE TABLE educacion_especialidad_Per (
    id_especialidad INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    descripcion_especialidad VARCHAR(200) NOT NULL,
    CONSTRAINT pk_educacion_especialidad_Per PRIMARY KEY (id_especialidad),
    CONSTRAINT fk_educacion_especialidad_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario)
) ENGINE=InnoDB;

-- Tabla intermedia: materias que cada perito tiene habilitadas.
CREATE TABLE perito_materia_Per (
    id_usuario INT NOT NULL,
    clave_materia VARCHAR(10) NOT NULL,
    CONSTRAINT pk_perito_materia_Per PRIMARY KEY (id_usuario, clave_materia),
    CONSTRAINT fk_perito_materia_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario),
    CONSTRAINT fk_perito_materia_Per_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(clave_materia)
) ENGINE=InnoDB;

-- Expediente técnico que atiende una solicitud de perito de Audiencias. Demanda y materia se obtienen de la solicitud.
-- CAMBIO PERITAJES: fecha_apertura (primera vez que el perito abre el expediente, RF 4.2), fecha_entrega (RF 4.4)
-- y fecha_cancelacion (RF 2.4). La fecha de solicitud es fecha_creacion.
CREATE TABLE peritaje_Per (
    id_peritaje INT NOT NULL AUTO_INCREMENT,
    id_solicitud_perito INT NOT NULL,
    demanda_adjunta TEXT NULL,
    estado_peritaje ENUM('Pendiente de Asignación','Asignado','En Proceso','Dictamen Entregado','Cancelado') NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_apertura DATETIME NULL,
    fecha_entrega DATETIME NULL,
    fecha_cancelacion DATETIME NULL,
    CONSTRAINT pk_peritaje_Per PRIMARY KEY (id_peritaje),
    CONSTRAINT uq_peritaje_Per_id_solicitud_perito UNIQUE (id_solicitud_perito),
    -- CORRECCIÓN: las fechas de cierre deben coincidir con el estado del peritaje.
    CONSTRAINT ck_peritaje_Per_cancelacion CHECK (fecha_cancelacion IS NULL OR estado_peritaje = 'Cancelado'),
    CONSTRAINT ck_peritaje_Per_entrega CHECK (fecha_entrega IS NULL OR estado_peritaje = 'Dictamen Entregado'),
    CONSTRAINT fk_peritaje_Per_id_solicitud_perito FOREIGN KEY (id_solicitud_perito) REFERENCES solicitud_perito_Aud(id_solicitud_perito)
) ENGINE=InnoDB;

-- Asignación activa del perito e historial de reasignaciones.
-- La asignación vigente de un peritaje es la de mayor id_asignacion.
-- VERSIÓN 3: sin perito saliente = asignación automática del sistema; con perito saliente = reasignación manual.
-- En la manual, id_usuario_cambio es quien la hizo; NULL = el administrador general (no está en user_Per).
CREATE TABLE asignacion_Per (
    id_asignacion INT NOT NULL AUTO_INCREMENT,
    id_peritaje INT NOT NULL,
    id_perito_asignado INT NOT NULL,
    id_perito_saliente INT NULL,
    id_usuario_cambio INT NULL,
    motivo_reasignacion TEXT NULL,
    fecha_hora_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_asignacion_Per PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_asignacion_Per_id_peritaje FOREIGN KEY (id_peritaje) REFERENCES peritaje_Per(id_peritaje),
    CONSTRAINT fk_asignacion_Per_id_perito_asignado FOREIGN KEY (id_perito_asignado) REFERENCES user_Per(id_usuario),
    CONSTRAINT fk_asignacion_Per_id_perito_saliente FOREIGN KEY (id_perito_saliente) REFERENCES user_Per(id_usuario),
    CONSTRAINT fk_asignacion_Per_id_usuario_cambio FOREIGN KEY (id_usuario_cambio) REFERENCES user_Per(id_usuario)
) ENGINE=InnoDB;

-- Conclusiones técnicas redactadas por el perito (uno por peritaje).
-- CAMBIO PERITAJES: version. Cada guardado hace "version = version + 1 WHERE version = ?"; si no cambia
-- ninguna fila, otra ventana guardó antes y se avisa "Hay cambios más recientes en otra ventana" (RF 4.3).
CREATE TABLE dictamen_Per (
    id_dictamen INT NOT NULL AUTO_INCREMENT,
    id_peritaje INT NOT NULL,
    contenido_texto LONGTEXT NULL,
    fecha_ultimo_guardado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    es_finalizado BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_dictamen_Per PRIMARY KEY (id_dictamen),
    CONSTRAINT uq_dictamen_Per_id_peritaje UNIQUE (id_peritaje),
    CONSTRAINT fk_dictamen_Per_id_peritaje FOREIGN KEY (id_peritaje) REFERENCES peritaje_Per(id_peritaje)
) ENGINE=InnoDB;

-- Archivos adjuntos del dictamen.
CREATE TABLE anexo_dictamen_Per (
    id_anexo INT NOT NULL AUTO_INCREMENT,
    id_dictamen INT NOT NULL,
    nombre_archivo VARCHAR(200) NOT NULL,
    formato_archivo VARCHAR(20) NOT NULL,
    tamano_mb DECIMAL(5,2) NOT NULL,
    archivo_contenido LONGBLOB NOT NULL,
    CONSTRAINT pk_anexo_dictamen_Per PRIMARY KEY (id_anexo),
    CONSTRAINT ck_anexo_dictamen_Per_tamano_mb CHECK (tamano_mb <= 25.00),
    CONSTRAINT fk_anexo_dictamen_Per_id_dictamen FOREIGN KEY (id_dictamen) REFERENCES dictamen_Per(id_dictamen)
) ENGINE=InnoDB;

-- Registro histórico inmutable de eventos del módulo de Peritajes.
-- CAMBIO PERITAJES: referencia (folio del peritaje o número de registro del perito) para filtrar (RF 5.2).
-- VERSIÓN 3: nombre_usuario y rol_usuario de quien hizo el movimiento. id_usuario NULL = el sistema
-- (asignación automática) o el administrador general (no está en user_Per).
CREATE TABLE bitacora_Per (
    id_bitacora INT NOT NULL AUTO_INCREMENT,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT NULL,
    nombre_usuario VARCHAR(150) NULL,
    rol_usuario VARCHAR(50) NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    referencia VARCHAR(50) NULL,
    detalle_movimiento TEXT NULL,
    CONSTRAINT pk_bitacora_Per PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Avisos enviados a los usuarios por dashboard o correo.
CREATE TABLE notificacion_Per (
    id_notificacion INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_peritaje INT NULL,
    evento_origen VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    enviado_correo BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_notificacion_Per PRIMARY KEY (id_notificacion),
    CONSTRAINT fk_notificacion_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario),
    CONSTRAINT fk_notificacion_Per_id_peritaje FOREIGN KEY (id_peritaje) REFERENCES peritaje_Per(id_peritaje)
) ENGINE=InnoDB;

-- Usuarios del módulo de Segunda Instancia (magistrados y administradores).
CREATE TABLE user_Seg (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    telefono VARCHAR(10) NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    Status BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_Seg PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Seg_correo UNIQUE (correo),
    CONSTRAINT fk_user_Seg_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
) ENGINE=InnoDB;

-- Magistrados; relación 1:1 con user_Seg.
CREATE TABLE magistrado_Seg (
    id_magistrado BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_sala BIGINT NULL,
    estatus BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_magistrado_Seg PRIMARY KEY (id_magistrado),
    CONSTRAINT uq_magistrado_Seg_id_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_magistrado_Seg_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Seg(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_magistrado_Seg_id_sala FOREIGN KEY (id_sala) REFERENCES sala_Seg(id_sala)
) ENGINE=InnoDB;

-- Expedientes de Segunda Instancia. Expediente original y materia se obtienen de segunda_instancia_Ofc.
CREATE TABLE toca_Seg (
    id_toca BIGINT NOT NULL AUTO_INCREMENT,
    numero_toca VARCHAR(40) NOT NULL,
    id_segunda_instancia INT NOT NULL,
    apelante VARCHAR(200) NULL,
    id_sala BIGINT NOT NULL,
    id_magistrado BIGINT NULL,
    estado ENUM('Nueva','En ponencia','Resuelto') NOT NULL DEFAULT 'Nueva',
    fecha_recepcion DATE NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_toca_Seg PRIMARY KEY (id_toca),
    CONSTRAINT uq_toca_Seg_numero_toca UNIQUE (numero_toca),
    CONSTRAINT uq_toca_Seg_id_segunda_instancia UNIQUE (id_segunda_instancia),
    CONSTRAINT fk_toca_Seg_id_segunda_instancia FOREIGN KEY (id_segunda_instancia) REFERENCES segunda_instancia_Ofc(id_segunda_instancia),
    CONSTRAINT fk_toca_Seg_id_sala FOREIGN KEY (id_sala) REFERENCES sala_Seg(id_sala),
    CONSTRAINT fk_toca_Seg_id_magistrado FOREIGN KEY (id_magistrado) REFERENCES magistrado_Seg(id_magistrado)
) ENGINE=InnoDB;

-- Documentos adjuntos del toca.
CREATE TABLE documento_Seg (
    id_documento BIGINT NOT NULL AUTO_INCREMENT,
    id_toca BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    paginas INT NULL,
    fecha_carga TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    documento LONGBLOB NOT NULL,
    CONSTRAINT pk_documento_Seg PRIMARY KEY (id_documento),
    CONSTRAINT fk_documento_Seg_id_toca FOREIGN KEY (id_toca) REFERENCES toca_Seg(id_toca) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Programación y sesiones de ponencia.
CREATE TABLE ponencia_Seg (
    id_ponencia BIGINT NOT NULL AUTO_INCREMENT,
    id_toca BIGINT NOT NULL,
    id_magistrado BIGINT NOT NULL,
    fecha_sesion DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    tipo_sesion VARCHAR(50) NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ponencia_Seg PRIMARY KEY (id_ponencia),
    CONSTRAINT fk_ponencia_Seg_id_toca FOREIGN KEY (id_toca) REFERENCES toca_Seg(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_ponencia_Seg_id_magistrado FOREIGN KEY (id_magistrado) REFERENCES magistrado_Seg(id_magistrado)
) ENGINE=InnoDB;

-- Resoluciones de Segunda Instancia.
CREATE TABLE resolucion_Seg (
    id_resolucion BIGINT NOT NULL AUTO_INCREMENT,
    id_toca BIGINT NOT NULL,
    id_magistrado BIGINT NOT NULL,
    sentido ENUM('Confirma','Revoca','Modifica') NOT NULL,
    cuerpo LONGTEXT NULL,
    observaciones LONGTEXT NULL,
    url_pdf VARCHAR(500) NULL,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_resolucion_Seg PRIMARY KEY (id_resolucion),
    CONSTRAINT fk_resolucion_Seg_id_toca FOREIGN KEY (id_toca) REFERENCES toca_Seg(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_resolucion_Seg_id_magistrado FOREIGN KEY (id_magistrado) REFERENCES magistrado_Seg(id_magistrado)
) ENGINE=InnoDB;

-- Solicitudes de reasignación de sala de un toca.
CREATE TABLE reasignacion_Seg (
    id_reasignacion BIGINT NOT NULL AUTO_INCREMENT,
    id_toca BIGINT NOT NULL,
    id_sala_origen BIGINT NOT NULL,
    id_sala_destino BIGINT NULL,
    motivo VARCHAR(300) NOT NULL,
    estado ENUM('Requiere validación','Aprobada','Rechazada') NOT NULL DEFAULT 'Requiere validación',
    solicitado_por BIGINT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reasignacion_Seg PRIMARY KEY (id_reasignacion),
    CONSTRAINT fk_reasignacion_Seg_id_toca FOREIGN KEY (id_toca) REFERENCES toca_Seg(id_toca) ON DELETE CASCADE,
    CONSTRAINT fk_reasignacion_Seg_id_sala_origen FOREIGN KEY (id_sala_origen) REFERENCES sala_Seg(id_sala),
    CONSTRAINT fk_reasignacion_Seg_id_sala_destino FOREIGN KEY (id_sala_destino) REFERENCES sala_Seg(id_sala),
    CONSTRAINT fk_reasignacion_Seg_solicitado_por FOREIGN KEY (solicitado_por) REFERENCES user_Seg(id_usuario)
) ENGINE=InnoDB;

-- Trazabilidad de eventos de cada toca.
CREATE TABLE historial_toca_Seg (
    id_historial BIGINT NOT NULL AUTO_INCREMENT,
    id_toca BIGINT NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_historial_toca_Seg PRIMARY KEY (id_historial),
    CONSTRAINT fk_historial_toca_Seg_id_toca FOREIGN KEY (id_toca) REFERENCES toca_Seg(id_toca) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Auditoría general de Segunda Instancia. Nombre y rol del actor se obtienen de user_Seg y Rol.
CREATE TABLE bitacora_Seg (
    id_bitacora BIGINT NOT NULL AUTO_INCREMENT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_fuente VARCHAR(50) NOT NULL,
    nombre_tabla VARCHAR(100) NULL,
    id_registro BIGINT NULL,
    accion VARCHAR(200) NOT NULL,
    id_usuario BIGINT NULL,
    valores_anteriores JSON NULL,
    valores_nuevos JSON NULL,
    detalle VARCHAR(500) NULL,
    tono ENUM('green','gold','blue','crimson') NOT NULL DEFAULT 'blue',
    CONSTRAINT pk_bitacora_Seg PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_Seg_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Seg(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Solicitudes de copias certificadas de un expediente. El solicitante es un usuario de Oficialía.
CREATE TABLE solicitud_copia_Cop (
    id_solicitud INT NOT NULL AUTO_INCREMENT,
    folio_solicitud VARCHAR(50) NOT NULL,
    num_expediente VARCHAR(20) NOT NULL,
    id_usuario_solicitante INT NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_solicitud ENUM('REGISTRADA','EN_REVISION','APROBADA','RECHAZADA','PAGADA') NOT NULL,
    CONSTRAINT pk_solicitud_copia_Cop PRIMARY KEY (id_solicitud),
    CONSTRAINT uq_solicitud_copia_Cop_folio_solicitud UNIQUE (folio_solicitud),
    CONSTRAINT fk_solicitud_copia_Cop_num_expediente FOREIGN KEY (num_expediente) REFERENCES expediente_Ofc(num_expediente),
    CONSTRAINT fk_solicitud_copia_Cop_id_usuario_solicitante FOREIGN KEY (id_usuario_solicitante) REFERENCES user_Ofc(id_usuario)
) ENGINE=InnoDB;

-- Dictamen de un funcionario sobre una solicitud de copia.
CREATE TABLE revision_solicitud_Cop (
    id_revision INT NOT NULL AUTO_INCREMENT,
    id_solicitud INT NOT NULL,
    id_usuario_revisor INT NOT NULL,
    fecha_revision DATETIME NOT NULL,
    resultado ENUM('APROBADA','RECHAZADA') NOT NULL,
    motivo_rechazo TEXT NULL,
    CONSTRAINT pk_revision_solicitud_Cop PRIMARY KEY (id_revision),
    CONSTRAINT ck_revision_motivo CHECK (resultado <> 'RECHAZADA' OR motivo_rechazo IS NOT NULL),
    CONSTRAINT fk_revision_solicitud_Cop_id_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud_copia_Cop(id_solicitud),
    CONSTRAINT fk_revision_solicitud_Cop_id_usuario_revisor FOREIGN KEY (id_usuario_revisor) REFERENCES user_Ofc(id_usuario)
) ENGINE=InnoDB;

-- Pagos de las solicitudes de copia.
CREATE TABLE pago_Cop (
    id_pago INT NOT NULL AUTO_INCREMENT,
    folio_pago VARCHAR(50) NULL,
    id_solicitud INT NOT NULL,
    id_usuario_pago INT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_pago DATETIME NULL,
    estado_pago ENUM('CONFIRMADO','FALLIDO') NOT NULL,
    CONSTRAINT pk_pago_Cop PRIMARY KEY (id_pago),
    CONSTRAINT uq_pago_Cop_folio_pago UNIQUE (folio_pago),
    CONSTRAINT ck_pago_Cop_monto CHECK (monto > 0),
    CONSTRAINT fk_pago_Cop_id_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud_copia_Cop(id_solicitud),
    CONSTRAINT fk_pago_Cop_id_usuario_pago FOREIGN KEY (id_usuario_pago) REFERENCES user_Ofc(id_usuario)
) ENGINE=InnoDB;

-- ==========================================================
-- Triggers: mantienen Login sincronizado con cada tabla user_*.
--  * AFTER INSERT: copia correo y contraseña a Login. Si el correo ya existe
--    en Login (en cualquier módulo), el INSERT del usuario se rechaza completo.
--  * AFTER UPDATE: si cambia el correo o la contraseña, actualiza Login
--    (busca por el correo anterior). Si el nuevo correo ya existe en Login,
--    el UPDATE del usuario se rechaza completo.
-- ==========================================================
DELIMITER $$
CREATE TRIGGER trg_user_Ofc_login_ai AFTER INSERT ON user_Ofc
FOR EACH ROW
BEGIN
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);
END$$

-- CORRECCIÓN: trigger nuevo para mantener Login sincronizado.
CREATE TRIGGER trg_user_Ofc_login_au AFTER UPDATE ON user_Ofc
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Aud_login_ai AFTER INSERT ON user_Aud
FOR EACH ROW
BEGIN
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);
END$$

-- CORRECCIÓN: trigger nuevo para mantener Login sincronizado.
CREATE TRIGGER trg_user_Aud_login_au AFTER UPDATE ON user_Aud
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Per_login_ai AFTER INSERT ON user_Per
FOR EACH ROW
BEGIN
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);
END$$

-- CAMBIO PERITAJES: al cambiar la contraseña de un usuario de Peritajes (RF 1.2) también se cambia en Login.
-- CORRECCIÓN: busca por OLD.correo y también actualiza el correo.
CREATE TRIGGER trg_user_Per_login_au AFTER UPDATE ON user_Per
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Seg_login_ai AFTER INSERT ON user_Seg
FOR EACH ROW
BEGIN
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);
END$$

-- CORRECCIÓN: trigger nuevo para mantener Login sincronizado.
CREATE TRIGGER trg_user_Seg_login_au AFTER UPDATE ON user_Seg
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

DELIMITER ;
