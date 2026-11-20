-- =====================================================================================
-- Copias Certificadas (EQUIPO 5) · tablas cop_*
-- Se ejecuta después de 00_nucleo.sql y 10_oficialia.sql (docs/ARQUITECTURA.md, sección 11).
-- Detalle de cada columna: docs/modulos/copias-diccionario-datos.md
--
-- TODO núcleo: cuando existan core_usuario y core_archivo, agregar sus FOREIGN KEY
--              (columnas *_por, usuario_id e ine_archivo_id).
-- =====================================================================================

USE sij;

-- Tarifa de la copia (pendiente 17: quién la configura). La vigente es la más reciente que ya empezó.
CREATE TABLE IF NOT EXISTS cop_tarifa (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto         DECIMAL(10, 2) NOT NULL,
    vigente_desde DATE           NOT NULL,
    creada_por    BIGINT         NULL
);

-- Solicitud de copia certificada. Del expediente de Oficialía solo se guardan los datos esenciales (sin FK).
CREATE TABLE IF NOT EXISTS cop_solicitud (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio              VARCHAR(20)    NOT NULL UNIQUE,
    numero_expediente  VARCHAR(30)    NOT NULL,
    tipo               VARCHAR(10)    NOT NULL,              -- PUBLICO | PRIVADO
    juzgado_id         BIGINT         NOT NULL,
    juzgado            VARCHAR(150)   NOT NULL,
    materia_id         BIGINT         NOT NULL,
    materia            VARCHAR(80)    NOT NULL,
    solicitante_nombre VARCHAR(150)   NOT NULL,
    solicitante_rfc    VARCHAR(13)    NOT NULL,
    solicitante_correo VARCHAR(150)   NOT NULL,
    ine_archivo_id     BIGINT         NULL,                  -- core_archivo, solo privadas
    estado             VARCHAR(30)    NOT NULL,              -- ver EstadoSolicitud
    tarifa_id          BIGINT         NOT NULL,
    costo              DECIMAL(10, 2) NOT NULL,              -- congelado al crear
    motivo_rechazo     VARCHAR(500)   NULL,
    capturado_por      BIGINT         NOT NULL,
    validado_por       BIGINT         NULL,
    fecha_solicitud    DATETIME       NOT NULL,
    fecha_validacion   DATETIME       NULL,
    actualizado_en     DATETIME       NULL,
    CONSTRAINT fk_cop_solicitud_tarifa FOREIGN KEY (tarifa_id) REFERENCES cop_tarifa (id),
    INDEX idx_cop_solicitud_expediente (numero_expediente),
    INDEX idx_cop_solicitud_estado (estado)
);

-- Pago simulado de la solicitud (RF 4.1). Una solicitud se paga una sola vez.
CREATE TABLE IF NOT EXISTS cop_pago (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id   BIGINT         NOT NULL UNIQUE,
    folio_pago     VARCHAR(20)    NOT NULL UNIQUE,           -- CP-AAAA-XXXXXX
    monto          DECIMAL(10, 2) NOT NULL,
    simulado       BOOLEAN        NOT NULL DEFAULT TRUE,
    registrado_por BIGINT         NOT NULL,
    fecha          DATETIME       NOT NULL,
    CONSTRAINT fk_cop_pago_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud (id)
);

-- Entrega física. Una por solicitud; después el folio queda inhabilitado (RN-03).
CREATE TABLE IF NOT EXISTS cop_entrega (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id  BIGINT   NOT NULL UNIQUE,
    entregado_por BIGINT   NOT NULL,
    fecha         DATETIME NOT NULL,
    CONSTRAINT fk_cop_entrega_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud (id)
);

-- Correos enviados al solicitante (comprobante y aviso de "lista para recoger").
CREATE TABLE IF NOT EXISTS cop_correo (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id BIGINT       NOT NULL,
    tipo         VARCHAR(20)  NOT NULL,                     -- COMPROBANTE | LISTA_PARA_RECOGER
    destinatario VARCHAR(150) NOT NULL,
    enviado_por  BIGINT       NOT NULL,
    enviado_en   DATETIME     NOT NULL,
    exito        BOOLEAN      NOT NULL,
    CONSTRAINT fk_cop_correo_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud (id)
);

-- Cada cambio de estado de una solicitud: quién y cuándo.
CREATE TABLE IF NOT EXISTS cop_solicitud_historial (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id    BIGINT       NOT NULL,
    estado_anterior VARCHAR(30)  NULL,                      -- nulo al crear
    estado_nuevo    VARCHAR(30)  NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    comentario      VARCHAR(500) NULL,
    fecha           DATETIME     NOT NULL,
    CONSTRAINT fk_cop_historial_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud (id)
);

-- Colores de las pantallas de Copias (COPIAS_ADMIN).
CREATE TABLE IF NOT EXISTS cop_color (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    variable        VARCHAR(40) NOT NULL UNIQUE,
    valor           VARCHAR(7)  NOT NULL,                   -- #RRGGBB
    actualizado_por BIGINT      NULL,
    actualizado_en  DATETIME    NULL
);

-- Datos iniciales: tarifa de prueba (pendiente 17)
INSERT INTO cop_tarifa (monto, vigente_desde)
SELECT 320.00, '2026-01-01'
WHERE NOT EXISTS (SELECT 1 FROM cop_tarifa);
