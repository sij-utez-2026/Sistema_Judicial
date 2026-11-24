-- ==========================================================
-- Lleva una base sistema_judicial YA CREADA a la VERSIÓN 2 del script (peritajes-bd-script-corregido.sql).
-- Si se creó la base con la versión 2 completa, no hace falta correr este archivo.
--
-- PARTE 1: solo si la base se creó con el script ORIGINAL y nunca se corrió la versión anterior de este archivo.
--          (Si ya existen las columnas, MySQL marca "Duplicate column name": salte a la PARTE 2.)
-- PARTE 2: siempre. Se puede correr más de una vez: primero borra los triggers y luego los vuelve a crear.
-- ==========================================================
USE sistema_judicial;

-- ===================== PARTE 1: columnas de Peritajes =====================

-- 1. RF 1.1: bloqueo de 15 minutos después de 3 intentos fallidos
ALTER TABLE user_Per
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0 AFTER Status,
    ADD COLUMN bloqueado_hasta DATETIME NULL AFTER intentos_fallidos;

-- 2. RF 4.2, 4.4 y 2.4: fechas de apertura, entrega y cancelación del peritaje
ALTER TABLE peritaje_Per
    ADD COLUMN fecha_apertura DATETIME NULL AFTER fecha_creacion,
    ADD COLUMN fecha_entrega DATETIME NULL AFTER fecha_apertura,
    ADD COLUMN fecha_cancelacion DATETIME NULL AFTER fecha_entrega;

-- 3. RF 4.3: versión del dictamen para el autoguardado
ALTER TABLE dictamen_Per
    ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER es_finalizado;

-- 4. RF 5.2: referencia de cada movimiento de la bitácora (folio del peritaje o número de registro del perito)
ALTER TABLE bitacora_Per
    ADD COLUMN referencia VARCHAR(50) NULL AFTER tipo_evento;

-- ===================== PARTE 2: correcciones de la revisión =====================

-- 8. CHECK: intentos no negativos y fechas de cierre que coincidan con el estado.
--    (Si ya existen, MySQL marca "Duplicate check constraint name": se pueden saltar.)
ALTER TABLE user_Per
    ADD CONSTRAINT ck_user_Per_intentos CHECK (intentos_fallidos >= 0);
ALTER TABLE peritaje_Per
    ADD CONSTRAINT ck_peritaje_Per_cancelacion CHECK (fecha_cancelacion IS NULL OR estado_peritaje = 'Cancelado'),
    ADD CONSTRAINT ck_peritaje_Per_entrega CHECK (fecha_entrega IS NULL OR estado_peritaje = 'Dictamen Entregado');

-- 5, 6 y 7. Triggers AFTER UPDATE: si cambia el correo o la contraseña, se actualiza Login
-- buscando por el correo anterior. En los cuatro módulos con usuarios.
DROP TRIGGER IF EXISTS trg_user_Ofc_login_au;
DROP TRIGGER IF EXISTS trg_user_Aud_login_au;
DROP TRIGGER IF EXISTS trg_user_Per_login_au;
DROP TRIGGER IF EXISTS trg_user_Seg_login_au;

DELIMITER $$
CREATE TRIGGER trg_user_Ofc_login_au AFTER UPDATE ON user_Ofc
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Aud_login_au AFTER UPDATE ON user_Aud
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Per_login_au AFTER UPDATE ON user_Per
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$

CREATE TRIGGER trg_user_Seg_login_au AFTER UPDATE ON user_Seg
FOR EACH ROW
BEGIN
    IF NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena THEN
        UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
        WHERE correo = OLD.correo;
    END IF;
END$$
DELIMITER ;
