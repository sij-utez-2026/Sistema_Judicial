-- Script del módulo de Copias Certificadas (Equipo 5 · prefijo cop_)
-- Ejecutar después de 00_nucleo.sql y 10_oficialia.sql sobre la base de datos sij.

CREATE TABLE IF NOT EXISTS cop_tarifa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto DECIMAL(10, 2) NOT NULL,
    vigente_desde DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS cop_solicitud (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio_solicitud VARCHAR(30) NOT NULL UNIQUE,
    numero_expediente VARCHAR(40) NOT NULL,
    juzgado_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    costo DECIMAL(10, 2) NOT NULL,
    solicitante_nombre VARCHAR(150) NULL,
    solicitante_rfc VARCHAR(20) NULL,
    ine_archivo_id BIGINT NULL,
    validado_por BIGINT NULL,
    motivo_cancelacion VARCHAR(255) NULL,
    fecha_solicitud DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS cop_pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id BIGINT NOT NULL UNIQUE,
    folio_pago VARCHAR(30) NOT NULL UNIQUE,
    monto DECIMAL(10, 2) NOT NULL,
    moneda VARCHAR(10) NOT NULL DEFAULT 'MXN',
    stripe_session_id VARCHAR(255) NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255) NULL,
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'PAGADO',
    metodo_pago VARCHAR(80) NOT NULL DEFAULT 'Stripe Checkout (Tarjeta)',
    fecha DATETIME NOT NULL,
    simulado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_cop_pago_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud(id)
);

CREATE TABLE IF NOT EXISTS cop_solicitud_historial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    estado_anterior VARCHAR(30) NULL,
    estado_nuevo VARCHAR(30) NOT NULL,
    usuario_id BIGINT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_cop_hist_solicitud FOREIGN KEY (solicitud_id) REFERENCES cop_solicitud(id)
);

-- Datos de prueba iniciales (RF 4.1)
INSERT IGNORE INTO cop_tarifa (id, monto, vigente_desde)
VALUES (1, 150.00, '2026-01-01');

INSERT IGNORE INTO cop_solicitud (
    folio_solicitud, numero_expediente, juzgado_id, materia_id, tipo, estado, costo, fecha_solicitud
) VALUES
    ('CC-2026-A7K9M2', 'EXP-2026-00104', 1, 1, 'PUBLICA', 'LISTO_PARA_PAGO', 150.00, NOW()),
    ('CC-2026-B3P8Q5', 'EXP-2026-00219', 2, 2, 'PRIVADA', 'APROBADO', 250.00, NOW()),
    ('CC-2026-C9W4R1', 'EXP-2026-00308', 3, 3, 'PRIVADA', 'EN_ESPERA_VALIDACION', 180.00, NOW()),
    ('CC-2026-D2N6T8', 'EXP-2026-00087', 4, 4, 'PRIVADA', 'CANCELADO', 200.00, NOW());
