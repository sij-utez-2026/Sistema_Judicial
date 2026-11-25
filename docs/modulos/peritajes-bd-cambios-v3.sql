-- ==========================================================
-- Lleva una base sistema_judicial de la VERSIÓN 2 a la VERSIÓN 3 (peritajes-bd-script-corregido.sql).
-- Se corre UNA sola vez. Si la base está en el script original, primero se corre peritajes-bd-cambios.sql.
-- Si se creó la base con la versión 3 completa, no hace falta.
-- ==========================================================
USE sistema_judicial;

-- 9. El bloqueo por intentos pasa a Login (aplica a todos los módulos con el inicio de sesión del núcleo)
ALTER TABLE Login
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0 AFTER contrasena,
    ADD COLUMN bloqueado_hasta DATETIME NULL AFTER intentos_fallidos,
    ADD CONSTRAINT ck_Login_intentos CHECK (intentos_fallidos >= 0);

-- Primero se quita el CHECK y después las columnas que usa
ALTER TABLE user_Per DROP CHECK ck_user_Per_intentos;
ALTER TABLE user_Per
    DROP COLUMN intentos_fallidos,
    DROP COLUMN bloqueado_hasta;

-- 10. Nombre y rol de quien hace cada movimiento (el administrador general no está en user_Per)
ALTER TABLE bitacora_Per
    ADD COLUMN nombre_usuario VARCHAR(150) NULL AFTER id_usuario,
    ADD COLUMN rol_usuario VARCHAR(50) NULL AFTER nombre_usuario;

-- Los movimientos que ya existían: se llenan con los datos de user_Per (o "Sistema")
UPDATE bitacora_Per b
LEFT JOIN user_Per u ON u.id_usuario = b.id_usuario
LEFT JOIN Rol r ON r.id_rol = u.id_rol
SET b.nombre_usuario = COALESCE(NULLIF(CONCAT_WS(' ', u.nombre, u.apellido_paterno, u.apellido_materno), ''), 'Sistema'),
    b.rol_usuario = COALESCE(r.rol, 'Automático')
WHERE b.nombre_usuario IS NULL;

-- 11. asignacion_Per no cambia de columnas: solo cambia cómo se lee (ver el comentario de la tabla en el script).
