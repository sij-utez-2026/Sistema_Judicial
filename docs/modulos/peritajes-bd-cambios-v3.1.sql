-- ==========================================================
-- Lleva una base sistema_judicial de la VERSIÓN 3 a la VERSIÓN 3.1 (peritajes-bd-script-corregido.sql).
-- Si la base se creó con la versión 3.1 completa, no hace falta.
--
-- Paso 1: se corre UNA sola vez. Si ya agregaste las columnas, sáltatelo (marcaría "Duplicate column").
-- Paso 2: se puede correr las veces que quieras; deja la carga de cada perito igual a sus peritajes.
-- ==========================================================
USE sistema_judicial;

-- Paso 1. Columnas nuevas
ALTER TABLE user_Per ADD carga_trabajo INT NOT NULL DEFAULT 0;
ALTER TABLE demanda_Ofc ADD tipo ENUM('Pública','Privada') NOT NULL;

-- Paso 2. Carga inicial de cada perito: peritajes "Asignado" o "En Proceso" en su asignación vigente
-- (la de mayor id_asignacion). Es la misma cuenta que hace PeritajeDao.actualizarCarga.
UPDATE user_Per u
SET u.carga_trabajo = (
    SELECT COUNT(*)
    FROM peritaje_Per p
    JOIN asignacion_Per a ON a.id_asignacion =
        (SELECT MAX(a2.id_asignacion) FROM asignacion_Per a2 WHERE a2.id_peritaje = p.id_peritaje)
    WHERE a.id_perito_asignado = u.id_usuario
      AND p.estado_peritaje IN ('Asignado', 'En Proceso')
)
WHERE u.id_usuario > 0;   -- WHERE con la llave para que MySQL Workbench (safe updates) lo permita
