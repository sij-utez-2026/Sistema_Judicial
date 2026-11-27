-- ==========================================================
-- Base de datos integrada del sistema judicial — CREAR DESDE CERO (versión 6)
-- Módulos: Oficialía de Partes (Ofc), Audiencias (Aud), Peritajes (Per),
--          Segunda Instancia (Seg) y Copias Certificadas (Cop)
-- Motor: MySQL 8.0.16 o superior (requiere soporte de CHECK).
--
-- Basado en docs/modulos/peritajes-bd-script-corregido.sql (versión 6), pero LIMPIO:
--  * No tiene ALTER TABLE: los cambios de las versiones 5 y 6 (Copias y Audiencias) ya están dentro
--    de cada CREATE TABLE. Por eso funciona aunque se cree la base de nuevo y no depende de que las
--    tablas estén vacías.
--  * Las tablas se crean en orden de dependencia de llaves foráneas (tarifa_Cop antes que
--    solicitud_copia_Cop).
--  * No usa DELIMITER: los triggers son de una sola instrucción, sin BEGIN...END, para que corra en
--    MySQL Workbench, DBeaver o la consola.
--  * Solo crea la estructura, los triggers y la tarifa inicial de Copias. Los datos de prueba de
--    Peritajes están en docs/modulos/peritajes-bd-datos-prueba.sql (se corre después de este).
--
-- CÓMO USARLO
--  * Base nueva: correr el archivo completo.
--  * Si ya existe una base sistema_judicial, este script FALLA en el primer CREATE TABLE ("Table
--    already exists"), a propósito, para no mezclar versiones. Para recrearla:
--      1. RESPALDARLA (por ejemplo con mysqldump o "Data Export" de Workbench).
--      2. Quitar el comentario del DROP DATABASE de abajo.
--    CUIDADO: el DROP borra TODOS los datos de los 5 módulos. Nunca lo corras sobre la base
--    compartida del equipo (Aiven) sin ponerse de acuerdo con todos los equipos.
--
-- HISTORIAL (qué trae, por versión)
-- Versión 2 (Peritajes, Equipo 3):
--   1. peritaje_Per: fecha_apertura, fecha_entrega y fecha_cancelacion (RF 4.2, 4.4 y 2.4).
--   2. dictamen_Per: version (RF 4.3: aviso "Hay cambios más recientes en otra ventana").
--   3. bitacora_Per: referencia (folio del peritaje o número de registro del perito).
--   4. Triggers AFTER UPDATE: al cambiar correo o contraseña en user_* también cambian en Login (RF 1.2).
--   5. CHECK en peritaje_Per: fecha_cancelacion solo en 'Cancelado'; fecha_entrega solo en 'Dictamen Entregado'.
-- Versión 3 (inicio de sesión único del núcleo):
--   6. intentos_fallidos y bloqueado_hasta en Login (3 intentos fallidos bloquean 15 minutos).
--   7. bitacora_Per: nombre_usuario y rol_usuario (el administrador general no está en user_Per).
--   8. asignacion_Per: sin perito saliente = asignación automática.
-- Versión 3.1:
--   9. user_Per.carga_trabajo (DFR 2.2, asignación automática al perito de menor carga).
--  10. demanda_Ofc.tipo (pública o privada).
-- Versión 4 (Oficialía):
--  11. distrito_Ofc, juzgado_Ofc, oficialia_Ofc y materia_Ofc con llave INT AUTO_INCREMENT.
--  12. juzgado_Ofc.num_juzgado único por distrito; un juzgado, una oficialía; cada usuario en una oficialía.
--  13. demanda_Ofc: llave id_demanda; folio VARCHAR(16) UNIQUE; anio y consecutivo únicos por oficialía;
--      motivo_no_procedencia; CHECK de al menos un participante.
--  14. documento_Ofc se liga por id_demanda.
--  15. En tablas de otros módulos se conserva el nombre de la columna "clave_..." pero ahora son INT:
--      sala_Seg.clave_distrito, mesa_audiencia_Aud.clave_juzgado y clave_materia,
--      solicitud_perito_Aud.clave_materia, perito_materia_Per.clave_materia (y user_Aud.clave_materia).
-- Versión 5:
--  16. Triggers AFTER DELETE: al borrar un usuario de user_* también se borra su renglón de Login
--      (si no, el correo no se podía volver a registrar: error 1062).
--  17. Copias: tabla tarifa_Cop (tarifa inicial de $320.00) y en solicitud_copia_Cop los datos del
--      solicitante, la tarifa aplicada, el costo congelado, la entrega y los estados
--      LISTA_PARA_RECOGER y ENTREGADA.
-- Versión 6 (Audiencias):
--  18. user_Aud: telefono y clave_materia (INT, id de materia_Ofc).
--  19. mesa_audiencia_Aud: estado.
--  20. audiencia_Aud: abogado que la atiende, inicio y fin reales, acta, reprogramación y estado EN_PROCESO.
--  21. participante_audiencia_Aud: motivo_ausencia, es_obligatorio y roles TESTIGO y OTRO.
--
-- Contraseñas: todos los módulos las guardan cifradas con PBKDF2 (nucleo/acceso/Contrasena); el trigger
-- las copia a Login y el inicio de sesión del núcleo las revisa ahí.
-- ==========================================================

-- DROP DATABASE IF EXISTS sistema_judicial;   -- ¡Borra TODO! Leer "CÓMO USARLO" arriba.

CREATE DATABASE IF NOT EXISTS sistema_judicial
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sistema_judicial;


-- ==========================================================
-- NÚCLEO: roles y credenciales de todo el sistema
-- ==========================================================

-- Catálogo de roles. Lo referencian todas las tablas de usuario (user_*).
CREATE TABLE Rol (
    id_rol INT NOT NULL AUTO_INCREMENT,
    rol VARCHAR(50) NOT NULL,
    CONSTRAINT pk_Rol PRIMARY KEY (id_rol),
    CONSTRAINT uq_Rol_rol UNIQUE (rol)
) ENGINE=InnoDB;

-- Credenciales de acceso de todo el sistema. Se llena sola con los triggers de cada tabla user_*;
-- la PK garantiza que un correo no se repita entre módulos.
-- 3 intentos fallidos bloquean la cuenta 15 minutos (inicio de sesión del núcleo, /login).
CREATE TABLE Login (
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    CONSTRAINT pk_Login PRIMARY KEY (correo),
    CONSTRAINT ck_Login_intentos CHECK (intentos_fallidos >= 0)
) ENGINE=InnoDB;


-- ==========================================================
-- OFICIALÍA DE PARTES (Ofc) y catálogos compartidos
-- ==========================================================

-- Usuarios de Oficialía de Partes; también son los usuarios de Copias Certificadas.
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

-- Catálogo de distritos judiciales (compartido).
CREATE TABLE distrito_Ofc (
    id_distrito INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    cabecera VARCHAR(150) NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_distrito_Ofc PRIMARY KEY (id_distrito)
) ENGINE=InnoDB;

-- Catálogo de juzgados (compartido). num_juzgado es el número del juzgado dentro de su distrito.
CREATE TABLE juzgado_Ofc (
    id_juzgado INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    id_distrito INT NOT NULL,
    num_juzgado INT NOT NULL,
    CONSTRAINT pk_juzgado_Ofc PRIMARY KEY (id_juzgado),
    CONSTRAINT uq_juzgado_distrito_numero UNIQUE (id_distrito, num_juzgado),
    CONSTRAINT fk_juzgado_Ofc_id_distrito FOREIGN KEY (id_distrito) REFERENCES distrito_Ofc(id_distrito)
) ENGINE=InnoDB;

-- Oficialías de partes. Un juzgado tiene a lo más una oficialía.
CREATE TABLE oficialia_Ofc (
    id_oficialia INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    id_juzgado INT NOT NULL,
    CONSTRAINT pk_oficialia_Ofc PRIMARY KEY (id_oficialia),
    CONSTRAINT uq_oficialia_juzgado UNIQUE (id_juzgado),
    CONSTRAINT fk_oficialia_Ofc_id_juzgado FOREIGN KEY (id_juzgado) REFERENCES juzgado_Ofc(id_juzgado)
) ENGINE=InnoDB;

-- Oficialía en la que trabaja cada usuario (cada usuario en una sola).
CREATE TABLE oficialia_personal_Ofc (
    id_op_trabajador INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_oficialia INT NOT NULL,
    CONSTRAINT pk_oficialia_personal_Ofc PRIMARY KEY (id_op_trabajador),
    CONSTRAINT uq_oficialia_personal_usuario UNIQUE (id_usuario),
    CONSTRAINT fk_oficialia_personal_Ofc_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Ofc(id_usuario),
    CONSTRAINT fk_oficialia_personal_Ofc_id_oficialia FOREIGN KEY (id_oficialia) REFERENCES oficialia_Ofc(id_oficialia)
) ENGINE=InnoDB;

-- Catálogo de materias (compartido). Las columnas "clave_materia" de otros módulos guardan este id.
CREATE TABLE materia_Ofc (
    id_materia INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_materia_Ofc PRIMARY KEY (id_materia),
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

-- Demandas recibidas en Oficialía. La consultan Audiencias y Peritajes.
-- El folio (oficialía + año + consecutivo) es único: lo usan expediente, audiencia, solicitud de perito y resolución.
CREATE TABLE demanda_Ofc (
    id_demanda INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(16) NOT NULL,
    anio INT NOT NULL,
    consecutivo INT NOT NULL,
    id_materia INT NOT NULL,
    id_demandante INT NULL,
    id_demandado INT NULL,
    descripcion VARCHAR(255) NOT NULL,
    id_oficialia INT NOT NULL,
    fecha_recepcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE','PROCEDENTE','IMPROCEDENTE') NOT NULL DEFAULT 'PENDIENTE',
    tipo ENUM('PUBLICA','PRIVADA') NOT NULL,
    motivo_no_procedencia VARCHAR(255) NULL,
    CONSTRAINT pk_demanda_Ofc PRIMARY KEY (id_demanda),
    CONSTRAINT uq_demanda_folio UNIQUE (folio),
    CONSTRAINT uq_demanda_oficialia_anio_consecutivo UNIQUE (id_oficialia, anio, consecutivo),
    -- Debe existir al menos un participante
    CONSTRAINT chk_demanda_participantes CHECK (id_demandante IS NOT NULL OR id_demandado IS NOT NULL),
    CONSTRAINT fk_demanda_Ofc_id_materia FOREIGN KEY (id_materia) REFERENCES materia_Ofc(id_materia),
    CONSTRAINT fk_demanda_Ofc_id_demandante FOREIGN KEY (id_demandante) REFERENCES persona_Ofc(id_persona),
    CONSTRAINT fk_demanda_Ofc_id_demandado FOREIGN KEY (id_demandado) REFERENCES persona_Ofc(id_persona),
    CONSTRAINT fk_demanda_Ofc_id_oficialia FOREIGN KEY (id_oficialia) REFERENCES oficialia_Ofc(id_oficialia)
) ENGINE=InnoDB;

-- Documentos anexos a una demanda.
CREATE TABLE documento_Ofc (
    id_documento INT NOT NULL AUTO_INCREMENT,
    id_demanda INT NOT NULL,
    ruta_archivo BLOB NOT NULL,
    CONSTRAINT pk_documento_Ofc PRIMARY KEY (id_documento),
    CONSTRAINT fk_documento_Ofc_id_demanda FOREIGN KEY (id_demanda) REFERENCES demanda_Ofc(id_demanda)
) ENGINE=InnoDB;


-- ==========================================================
-- AUDIENCIAS (Aud) — primera parte (la necesita el expediente de Oficialía)
-- ==========================================================

-- Usuarios de Audiencias (administradores y abogados de audiencia).
-- Versión 6: telefono y clave_materia (id de materia_Ofc).
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
    telefono VARCHAR(10) NULL,
    clave_materia INT NULL,
    CONSTRAINT pk_user_Aud PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Aud_correo UNIQUE (correo),
    CONSTRAINT fk_user_Aud_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol),
    CONSTRAINT fk_user_Aud_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(id_materia)
) ENGINE=InnoDB;

-- Mesas de audiencia: cada una de un juzgado y una materia, con un abogado fijo (1:1).
-- clave_juzgado y clave_materia guardan id_juzgado e id_materia. Versión 6: estado.
CREATE TABLE mesa_audiencia_Aud (
    id_mesa INT NOT NULL AUTO_INCREMENT,
    nombre_mesa VARCHAR(100) NOT NULL,
    clave_juzgado INT NOT NULL,
    clave_materia INT NOT NULL,
    id_abogado_asignado INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_mesa_audiencia_Aud PRIMARY KEY (id_mesa),
    CONSTRAINT uq_mesa_audiencia_Aud_id_abogado_asignado UNIQUE (id_abogado_asignado),
    CONSTRAINT uq_mesa_juzgado UNIQUE (nombre_mesa, clave_juzgado),
    CONSTRAINT fk_mesa_audiencia_Aud_clave_juzgado FOREIGN KEY (clave_juzgado) REFERENCES juzgado_Ofc(id_juzgado),
    CONSTRAINT fk_mesa_audiencia_Aud_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(id_materia),
    CONSTRAINT fk_mesa_audiencia_Aud_id_abogado_asignado FOREIGN KEY (id_abogado_asignado) REFERENCES user_Aud(id_usuario)
) ENGINE=InnoDB;


-- ==========================================================
-- OFICIALÍA (Ofc) — expediente, envío a segunda instancia y bitácora
-- ==========================================================

-- Expediente que se genera cuando una demanda procede. Lo consultan Audiencias, Segunda Instancia y Copias.
CREATE TABLE expediente_Ofc (
    num_expediente VARCHAR(20) NOT NULL,
    folio VARCHAR(16) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVA','CONCLUIDA') NOT NULL DEFAULT 'ACTIVA',
    clasificacion ENUM('PUBLICO','PRIVADO') NOT NULL,
    id_mesa INT NOT NULL,
    CONSTRAINT pk_expediente_Ofc PRIMARY KEY (num_expediente),
    CONSTRAINT uq_expediente_Ofc_folio UNIQUE (folio),
    CONSTRAINT fk_expediente_Ofc_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_expediente_Ofc_id_mesa FOREIGN KEY (id_mesa) REFERENCES mesa_audiencia_Aud(id_mesa)
) ENGINE=InnoDB;

-- Salas de Segunda Instancia (se crea aquí porque la referencia segunda_instancia_Ofc).
-- clave_distrito guarda el id_distrito.
CREATE TABLE sala_Seg (
    id_sala BIGINT NOT NULL AUTO_INCREMENT,
    clave_distrito INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    estatus BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sala_Seg PRIMARY KEY (id_sala),
    CONSTRAINT fk_sala_Seg_clave_distrito FOREIGN KEY (clave_distrito) REFERENCES distrito_Ofc(id_distrito)
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

-- Bitácora de Oficialía de Partes y de Copias Certificadas.
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


-- ==========================================================
-- AUDIENCIAS (Aud) — audiencias, participantes, solicitudes de perito y resoluciones
-- ==========================================================

-- Audiencias de una demanda.
-- Versión 6: abogado que la atiende, inicio y fin reales, acta, reprogramación y estado EN_PROCESO.
-- Los valores nuevos del ENUM van al final para no cambiar los que ya existían.
CREATE TABLE audiencia_Aud (
    id_audiencia INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(16) NOT NULL,
    fecha_hora_inicio DATETIME NOT NULL,
    estado_audiencia ENUM('PROGRAMADA','SUSPENDIDA_POR_INASISTENCIA','CELEBRADA','REPROGRAMADA',
                          'EN_PROCESO') NOT NULL DEFAULT 'PROGRAMADA',
    declaracion_demandante TEXT NULL,
    declaracion_demandado TEXT NULL,
    declaracion_rep_legales TEXT NULL,
    acta_firmada_url VARCHAR(500) NULL,
    id_abogado_audiencia INT NOT NULL,              -- abogado que atiende la audiencia
    fecha_hora_inicio_real DATETIME NULL,           -- momento real de inicio
    fecha_hora_fin_real DATETIME NULL,              -- momento real de conclusión
    acta_audiencia LONGTEXT NULL,                   -- contenido del acta redactada
    motivo_reprogramacion TEXT NULL,                -- motivo del cambio de fecha
    id_audiencia_anterior INT NULL,                 -- audiencia que se reprogramó
    CONSTRAINT pk_audiencia_Aud PRIMARY KEY (id_audiencia),
    CONSTRAINT fk_audiencia_Aud_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_audiencia_Aud_id_abogado_audiencia FOREIGN KEY (id_abogado_audiencia) REFERENCES user_Aud(id_usuario),
    CONSTRAINT fk_audiencia_Aud_id_audiencia_anterior FOREIGN KEY (id_audiencia_anterior) REFERENCES audiencia_Aud(id_audiencia)
) ENGINE=InnoDB;

-- Pase de lista de los participantes de una audiencia.
-- Versión 6: motivo_ausencia, es_obligatorio y roles TESTIGO y OTRO (al final del ENUM).
CREATE TABLE participante_audiencia_Aud (
    id_participante INT NOT NULL AUTO_INCREMENT,
    id_audiencia INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    rol_participante ENUM('DEMANDANTE','DEMANDADO','REP_LEGAL','PERITO',
                          'TESTIGO','OTRO') NOT NULL,
    asistencia BOOLEAN NULL,
    motivo_ausencia VARCHAR(500) NULL,              -- motivo de la inasistencia
    es_obligatorio BOOLEAN NOT NULL DEFAULT TRUE,   -- su presencia es indispensable
    CONSTRAINT pk_participante_audiencia_Aud PRIMARY KEY (id_participante),
    CONSTRAINT fk_participante_audiencia_Aud_id_audiencia FOREIGN KEY (id_audiencia) REFERENCES audiencia_Aud(id_audiencia)
) ENGINE=InnoDB;

-- Solicitudes de peritaje que Audiencias envía a Peritajes (una materia por solicitud).
-- El perito asignado se obtiene de asignacion_Per. clave_materia guarda el id_materia.
CREATE TABLE solicitud_perito_Aud (
    id_solicitud_perito INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(16) NOT NULL,
    clave_materia INT NOT NULL,
    CONSTRAINT pk_solicitud_perito_Aud PRIMARY KEY (id_solicitud_perito),
    CONSTRAINT fk_solicitud_perito_Aud_folio FOREIGN KEY (folio) REFERENCES demanda_Ofc(folio),
    CONSTRAINT fk_solicitud_perito_Aud_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(id_materia)
) ENGINE=InnoDB;

-- Resolución de la demanda emitida en Audiencias (una por demanda).
CREATE TABLE resolucion_demanda_Aud (
    id_resolucion INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(16) NOT NULL,
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


-- ==========================================================
-- PERITAJES (Per)
-- ==========================================================

-- Usuarios de Peritajes (administradores y peritos). El perito ES el usuario: su id_usuario es su número
-- de registro (PER-0005).
CREATE TABLE user_Per (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    Status BOOLEAN NOT NULL DEFAULT TRUE,
    -- Carga de trabajo (DFR 2.2): cuántos peritajes tiene "Asignado" o "En Proceso" en su asignación vigente.
    -- La recalcula el código (PeritajeDao.actualizarCarga) al asignar, reasignar y entregar. No se edita a mano.
    carga_trabajo INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_Per PRIMARY KEY (id_usuario),
    CONSTRAINT uq_user_Per_correo UNIQUE (correo),
    CONSTRAINT fk_user_Per_id_rol FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
) ENGINE=InnoDB;

-- Especialidades del perito (área o estudios; texto libre, al menos una).
CREATE TABLE educacion_especialidad_Per (
    id_especialidad INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    descripcion_especialidad VARCHAR(200) NOT NULL,
    CONSTRAINT pk_educacion_especialidad_Per PRIMARY KEY (id_especialidad),
    CONSTRAINT fk_educacion_especialidad_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario)
) ENGINE=InnoDB;

-- Materias en las que interviene cada perito (las usa la asignación automática). clave_materia = id_materia.
CREATE TABLE perito_materia_Per (
    id_usuario INT NOT NULL,
    clave_materia INT NOT NULL,
    CONSTRAINT pk_perito_materia_Per PRIMARY KEY (id_usuario, clave_materia),
    CONSTRAINT fk_perito_materia_Per_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Per(id_usuario),
    CONSTRAINT fk_perito_materia_Per_clave_materia FOREIGN KEY (clave_materia) REFERENCES materia_Ofc(id_materia)
) ENGINE=InnoDB;

-- Peritaje de cada solicitud de Audiencias. Su id es el folio del peritaje; fecha_creacion es la fecha de solicitud.
-- fecha_apertura: primera vez que el perito abre el expediente (RF 4.2); fecha_entrega (RF 4.4); fecha_cancelacion (RF 2.4).
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
    -- Las fechas de cierre deben coincidir con el estado
    CONSTRAINT ck_peritaje_Per_cancelacion CHECK (fecha_cancelacion IS NULL OR estado_peritaje = 'Cancelado'),
    CONSTRAINT ck_peritaje_Per_entrega CHECK (fecha_entrega IS NULL OR estado_peritaje = 'Dictamen Entregado'),
    CONSTRAINT fk_peritaje_Per_id_solicitud_perito FOREIGN KEY (id_solicitud_perito) REFERENCES solicitud_perito_Aud(id_solicitud_perito)
) ENGINE=InnoDB;

-- Asignación del perito e historial de reasignaciones. La vigente es la de mayor id_asignacion.
-- Sin perito saliente = asignación automática; con perito saliente = reasignación manual
-- (id_usuario_cambio = quien la hizo; NULL = el administrador general, que no está en user_Per).
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

-- Dictamen del perito (uno por peritaje). version: cada guardado hace "version = version + 1 WHERE version = ?";
-- si no cambia ninguna fila, otra ventana guardó antes (RF 4.3).
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

-- Archivos adjuntos del dictamen (máximo 25 MB cada uno, guardados en la base).
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

-- Bitácora inmutable de Peritajes (RF 5.2). referencia = folio del peritaje o número de registro del perito.
-- id_usuario NULL = el sistema (asignación automática) o el administrador general; su nombre y rol quedan
-- en nombre_usuario y rol_usuario.
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

-- Avisos a los usuarios de Peritajes (se muestran en su inicio).
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


-- ==========================================================
-- SEGUNDA INSTANCIA (Seg)
-- ==========================================================

-- Usuarios de Segunda Instancia (magistrados y administradores).
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

-- Expedientes de Segunda Instancia (tocas). Expediente original y materia se obtienen de segunda_instancia_Ofc.
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


-- ==========================================================
-- COPIAS CERTIFICADAS (Cop). Sus usuarios viven en user_Ofc.
-- ==========================================================

-- Tarifas de la copia certificada (versión 5). Va antes de solicitud_copia_Cop, que la referencia.
CREATE TABLE tarifa_Cop (
    id_tarifa INT NOT NULL AUTO_INCREMENT,
    monto DECIMAL(10,2) NOT NULL,
    vigente_desde DATE NOT NULL,
    id_usuario INT NULL,                            -- quién la registró
    CONSTRAINT pk_tarifa_Cop PRIMARY KEY (id_tarifa),
    CONSTRAINT ck_tarifa_Cop_monto CHECK (monto > 0),
    CONSTRAINT fk_tarifa_Cop_id_usuario FOREIGN KEY (id_usuario) REFERENCES user_Ofc(id_usuario)
) ENGINE=InnoDB;

-- Solicitudes de copias certificadas de un expediente.
-- Versión 5: datos del solicitante, tarifa aplicada, costo congelado al registrar, entrega y los estados
-- LISTA_PARA_RECOGER y ENTREGADA (al final del ENUM).
CREATE TABLE solicitud_copia_Cop (
    id_solicitud INT NOT NULL AUTO_INCREMENT,
    folio_solicitud VARCHAR(50) NOT NULL,
    num_expediente VARCHAR(20) NOT NULL,
    id_usuario_solicitante INT NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_solicitud ENUM('REGISTRADA','EN_REVISION','APROBADA','RECHAZADA','PAGADA',
                          'LISTA_PARA_RECOGER','ENTREGADA') NOT NULL,
    solicitante_nombre VARCHAR(150) NOT NULL,
    solicitante_rfc VARCHAR(13) NOT NULL,
    solicitante_correo VARCHAR(150) NOT NULL,
    solicitante_ine MEDIUMBLOB NULL,                -- solo expedientes privados. PDF/JPG/PNG hasta 5 MB
    id_tarifa INT NOT NULL,                         -- tarifa aplicada
    costo DECIMAL(10,2) NOT NULL,                   -- precio congelado al registrar
    id_usuario_entrega INT NULL,                    -- trabajador que entregó
    fecha_entrega DATETIME NULL,                    -- cuándo se entregó
    CONSTRAINT pk_solicitud_copia_Cop PRIMARY KEY (id_solicitud),
    CONSTRAINT uq_solicitud_copia_Cop_folio_solicitud UNIQUE (folio_solicitud),
    CONSTRAINT ck_solicitud_copia_Cop_rfc CHECK (CHAR_LENGTH(solicitante_rfc) IN (12, 13)),
    CONSTRAINT ck_solicitud_copia_Cop_costo CHECK (costo > 0),
    CONSTRAINT ck_solicitud_copia_Cop_entrega
        CHECK (estado_solicitud <> 'ENTREGADA' OR (id_usuario_entrega IS NOT NULL AND fecha_entrega IS NOT NULL)),
    CONSTRAINT fk_solicitud_copia_Cop_num_expediente FOREIGN KEY (num_expediente) REFERENCES expediente_Ofc(num_expediente),
    CONSTRAINT fk_solicitud_copia_Cop_id_usuario_solicitante FOREIGN KEY (id_usuario_solicitante) REFERENCES user_Ofc(id_usuario),
    CONSTRAINT fk_solicitud_copia_Cop_id_tarifa FOREIGN KEY (id_tarifa) REFERENCES tarifa_Cop(id_tarifa),
    CONSTRAINT fk_solicitud_copia_Cop_id_usuario_entrega FOREIGN KEY (id_usuario_entrega) REFERENCES user_Ofc(id_usuario)
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
-- TRIGGERS: mantienen Login sincronizado con cada tabla user_* (12 en total)
--  * AFTER INSERT: copia correo y contraseña a Login. Si el correo ya existe en Login (en cualquier
--    módulo), el INSERT del usuario se rechaza completo.
--  * AFTER UPDATE: si cambia el correo o la contraseña, actualiza Login (busca por el correo anterior).
--    Si el correo nuevo ya existe en Login, el UPDATE del usuario se rechaza completo.
--  * AFTER DELETE: si se borra el usuario, se borra su renglón de Login (así el correo se puede volver
--    a registrar). La baja lógica (Status = FALSE) no necesita trigger: el núcleo revisa Status.
-- ==========================================================

-- Oficialía (y Copias)
CREATE TRIGGER trg_user_Ofc_login_ai AFTER INSERT ON user_Ofc
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

CREATE TRIGGER trg_user_Ofc_login_au AFTER UPDATE ON user_Ofc
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

CREATE TRIGGER trg_user_Ofc_login_ad AFTER DELETE ON user_Ofc
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

-- Audiencias
CREATE TRIGGER trg_user_Aud_login_ai AFTER INSERT ON user_Aud
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

CREATE TRIGGER trg_user_Aud_login_au AFTER UPDATE ON user_Aud
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

CREATE TRIGGER trg_user_Aud_login_ad AFTER DELETE ON user_Aud
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

-- Peritajes
CREATE TRIGGER trg_user_Per_login_ai AFTER INSERT ON user_Per
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

CREATE TRIGGER trg_user_Per_login_au AFTER UPDATE ON user_Per
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

CREATE TRIGGER trg_user_Per_login_ad AFTER DELETE ON user_Per
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

-- Segunda Instancia
CREATE TRIGGER trg_user_Seg_login_ai AFTER INSERT ON user_Seg
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

CREATE TRIGGER trg_user_Seg_login_au AFTER UPDATE ON user_Seg
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

CREATE TRIGGER trg_user_Seg_login_ad AFTER DELETE ON user_Seg
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;


-- ==========================================================
-- DATOS INICIALES
-- ==========================================================

-- Tarifa inicial de la copia certificada (versión 5)
INSERT INTO tarifa_Cop (monto, vigente_desde) VALUES (320.00, '2026-01-01');


-- ==========================================================
-- REVISIÓN (opcional): deben salir 44 tablas y 12 triggers
-- ==========================================================
-- SELECT COUNT(*) AS tablas FROM information_schema.tables WHERE table_schema = 'sistema_judicial';
-- SELECT event_object_table, action_timing, event_manipulation, trigger_name
-- FROM information_schema.triggers WHERE trigger_schema = 'sistema_judicial' ORDER BY 1, 3;
