-- ==========================================================
-- Datos de prueba del módulo de Peritajes — ADAPTADO A LA VERSIÓN 4 de la base
-- (sistema_judicial_v4.sql). Se puede correr varias veces: lo que ya existe se salta
-- (INSERT IGNORE sobre llaves únicas o NOT EXISTS).
-- Se entra en http://localhost:8080/sij/login (inicio de sesión único del núcleo).
--
-- Usuarios (todos con la contraseña de prueba  Peritajes2026  y correos de example.com, dominio reservado para pruebas):
--   admin.general@example.com       Administrador general    (SISTEMA_ADMIN, en user_Ofc): entra a los 5 módulos
--   admin.peritajes@example.com     Administrador            (PERITAJES_ADMIN)
--   rafael.cruz@example.com         Perito Administrador     (PERITAJES_PERITO_ADMIN)
--   elena.salgado@example.com       Perito                   (PERITAJES_PERITO)   ... y otros 5 peritos
-- La contraseña se guarda cifrada con PBKDF2 (clase nucleo/acceso/Contrasena). No usar en producción.
--
-- Las solicitudes de Audiencias (solicitud_perito_Aud) NO tienen peritaje todavía: el módulo las convierte en
-- peritajes y les asigna perito automáticamente la primera vez que alguien abre el inicio de Peritajes (RF 2.1 y 2.2).
--
-- CAMBIOS PARA LA VERSIÓN 4:
--  * Materias, distrito, juzgados y oficialías ya no tienen clave de texto ('CIV', 'D01', 'J2CIV', 'OP2CIV'):
--    su llave es un id numérico automático. Aquí se buscan por nombre.
--  * juzgado_Ofc.num_juzgado es único por distrito (no por materia), así que los 4 juzgados del
--    Primer Distrito llevan 1, 2, 3 y 4.
--  * demanda_Ofc pide anio, consecutivo (único por oficialía y año), tipo ('PUBLICA'/'PRIVADA') y al
--    menos un participante: se crean 2 personas de prueba (demandante y demandado).
--    Los folios PER-PRB-xxxx se conservan (caben en VARCHAR(16)).
--  * solicitud_perito_Aud.clave_materia y perito_materia_Per.clave_materia guardan el id de la materia.
--  * SET NAMES al inicio: evita el error 1267 (mezcla de collations) al comparar textos con las tablas.
-- [Confirmar con Oficialía] si su script de datos ya crea estas materias, distrito o juzgados con otros nombres.
-- ==========================================================
USE sistema_judicial;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ---------- Roles: administrador general y los de Peritajes ----------
INSERT IGNORE INTO Rol (rol) VALUES ('SISTEMA_ADMIN'), ('PERITAJES_ADMIN'), ('PERITAJES_PERITO_ADMIN'), ('PERITAJES_PERITO');

-- ---------- Catálogos de Oficialía que necesitan las demandas ----------
-- materia_Ofc.nombre es UNIQUE, por eso INSERT IGNORE basta.
INSERT IGNORE INTO materia_Ofc (nombre, descripcion) VALUES
    ('Civil', 'Materia civil'),
    ('Familiar', 'Materia familiar'),
    ('Mercantil', 'Materia mercantil'),
    ('Penal', 'Materia penal');

-- distrito_Ofc no tiene UNIQUE en nombre: se revisa con NOT EXISTS.
INSERT INTO distrito_Ofc (nombre, cabecera)
SELECT 'Primer Distrito Judicial', 'Cuernavaca' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM distrito_Ofc WHERE nombre = 'Primer Distrito Judicial');

SET @distrito = (SELECT MIN(id_distrito) FROM distrito_Ofc WHERE nombre = 'Primer Distrito Judicial');

-- UNIQUE (id_distrito, num_juzgado)
INSERT IGNORE INTO juzgado_Ofc (nombre, id_distrito, num_juzgado) VALUES
    ('Juzgado Primero Familiar de Cuernavaca', @distrito, 1),
    ('Juzgado Segundo Civil de Cuernavaca', @distrito, 2),
    ('Juzgado Primero Penal de Cuernavaca', @distrito, 3),
    ('Juzgado Mercantil de Cuernavaca', @distrito, 4);

-- UNIQUE (id_juzgado): un juzgado, una oficialía
INSERT IGNORE INTO oficialia_Ofc (nombre, id_juzgado)
SELECT o.nombre, j.id_juzgado
FROM (SELECT 'Oficialía del Juzgado Segundo Civil' AS nombre, 'Juzgado Segundo Civil de Cuernavaca' AS juzgado
      UNION ALL SELECT 'Oficialía del Juzgado Primero Familiar', 'Juzgado Primero Familiar de Cuernavaca'
      UNION ALL SELECT 'Oficialía del Juzgado Primero Penal', 'Juzgado Primero Penal de Cuernavaca'
      UNION ALL SELECT 'Oficialía del Juzgado Mercantil', 'Juzgado Mercantil de Cuernavaca') o
JOIN juzgado_Ofc j ON j.nombre = o.juzgado AND j.id_distrito = @distrito;

-- ---------- Participantes de prueba (la demanda exige al menos uno) ----------
INSERT INTO persona_Ofc (tipo_persona, nombre, apellido_paterno, apellido_materno)
SELECT p.tipo, p.nombre, p.ap, p.am
FROM (SELECT 'FISICA' AS tipo, 'Demandante' AS nombre, 'Prueba' AS ap, 'Peritajes' AS am
      UNION ALL SELECT 'FISICA', 'Demandado', 'Prueba', 'Peritajes') p
WHERE NOT EXISTS (SELECT 1 FROM persona_Ofc x
                  WHERE x.nombre = p.nombre AND x.apellido_paterno = p.ap AND x.apellido_materno = p.am);

SET @demandante = (SELECT MIN(id_persona) FROM persona_Ofc WHERE nombre = 'Demandante' AND apellido_paterno = 'Prueba' AND apellido_materno = 'Peritajes');
SET @demandado  = (SELECT MIN(id_persona) FROM persona_Ofc WHERE nombre = 'Demandado'  AND apellido_paterno = 'Prueba' AND apellido_materno = 'Peritajes');

-- ---------- Demandas y solicitudes de peritaje (como las enviaría Audiencias) ----------
-- consecutivo: orden de llegada dentro de cada oficialía en 2026.
INSERT IGNORE INTO demanda_Ofc (folio, anio, consecutivo, id_materia, id_demandante, id_demandado,
                                descripcion, id_oficialia, fecha_recepcion, estado, tipo)
SELECT d.folio, 2026, d.consecutivo, m.id_materia, @demandante, @demandado,
       d.descripcion, o.id_oficialia, d.fecha, 'PROCEDENTE', d.tipo
FROM (SELECT 'PER-PRB-0438' AS folio, 3 AS consecutivo, 'Civil' AS materia,
             'Determinar el valor comercial de un inmueble urbano para una prueba pericial en materia civil.' AS descripcion,
             'Oficialía del Juzgado Segundo Civil' AS oficialia, '2026-10-19 09:00:00' AS fecha, 'PUBLICA' AS tipo
      UNION ALL SELECT 'PER-PRB-0391', 1, 'Familiar',
             'Valoración psicológica de las partes en un juicio de guarda y custodia.',
             'Oficialía del Juzgado Primero Familiar', '2026-10-16 12:30:00', 'PRIVADA'
      UNION ALL SELECT 'PER-PRB-0184', 1, 'Mercantil',
             'Revisión contable de los estados financieros de la empresa demandada.',
             'Oficialía del Juzgado Mercantil', '2026-10-17 10:00:00', 'PUBLICA'
      UNION ALL SELECT 'PER-PRB-0207', 1, 'Penal',
             'Dictamen de criminalística sobre los indicios del lugar de los hechos.',
             'Oficialía del Juzgado Primero Penal', '2026-10-17 13:00:00', 'PUBLICA'
      UNION ALL SELECT 'PER-PRB-0512', 2, 'Civil',
             'Avalúo de daños en una construcción por defectos de obra.',
             'Oficialía del Juzgado Segundo Civil', '2026-10-18 11:00:00', 'PUBLICA'
      UNION ALL SELECT 'PER-PRB-0365', 1, 'Civil',
             'Peritaje en documentoscopía sobre la firma de un contrato de arrendamiento.',
             'Oficialía del Juzgado Segundo Civil', '2026-10-13 09:00:00', 'PUBLICA') d
JOIN materia_Ofc m ON m.nombre = d.materia
JOIN oficialia_Ofc o ON o.nombre = d.oficialia;

-- Solo se insertan si esa demanda todavía no tiene solicitud (así el archivo se puede correr dos veces).
-- clave_materia guarda el id de la materia (VERSIÓN 4).
INSERT INTO solicitud_perito_Aud (folio, clave_materia)
SELECT d.folio, d.id_materia FROM demanda_Ofc d
WHERE d.folio LIKE 'PER-PRB-%'
  AND NOT EXISTS (SELECT 1 FROM solicitud_perito_Aud s WHERE s.folio = d.folio)
ORDER BY d.fecha_recepcion;

-- Contraseña de prueba de todos: Peritajes2026
SET @clave = 'pbkdf2$600000$6jVG9ThQpMzDdY4RzNozuw==$Zb6NDBbjgPaWUcTdw2Khpra36UF9WuHKZ0wvd6jLndY=';

-- ---------- Administrador general (SISTEMA_ADMIN) ----------
-- Vive en user_Ofc (docs/ARQUITECTURA.md 7.2: "Administrador Mayor de Oficialía"). Entra a los cinco módulos.
-- [Confirmar con Oficialía e integradores] si ellos ya crean este usuario en su script.
INSERT IGNORE INTO user_Ofc (nombre, apellido_paterno, apellido_materno, correo, contrasena, id_rol, Status)
SELECT 'Administrador', 'General', NULL, 'admin.general@example.com', @clave, id_rol, TRUE
FROM Rol WHERE rol = 'SISTEMA_ADMIN';

-- ---------- Usuarios de Peritajes ----------
SET @admin = (SELECT id_rol FROM Rol WHERE rol = 'PERITAJES_ADMIN');
SET @peritoAdmin = (SELECT id_rol FROM Rol WHERE rol = 'PERITAJES_PERITO_ADMIN');
SET @perito = (SELECT id_rol FROM Rol WHERE rol = 'PERITAJES_PERITO');

INSERT IGNORE INTO user_Per (nombre, apellido_paterno, apellido_materno, correo, contrasena, id_rol, Status) VALUES
    ('Adriana', 'Montes', 'Gil', 'admin.peritajes@example.com', @clave, @admin, TRUE),
    ('Rafael', 'Cruz', 'Lara', 'rafael.cruz@example.com', @clave, @peritoAdmin, TRUE),
    ('Elena', 'Salgado', 'Ruiz', 'elena.salgado@example.com', @clave, @perito, TRUE),
    ('Daniel', 'Ríos', 'Vega', 'daniel.rios@example.com', @clave, @perito, TRUE),
    ('Lucía', 'Torres', 'Mora', 'lucia.torres@example.com', @clave, @perito, TRUE),
    ('Óscar', 'Medina', 'Sol', 'oscar.medina@example.com', @clave, @perito, TRUE),
    ('Marina', 'León', 'Soto', 'marina.leon@example.com', @clave, @perito, TRUE),
    ('Arturo', 'Luna', 'Gil', 'arturo.luna@example.com', @clave, @perito, FALSE);

-- Especialidades (texto libre) y materias de cada perito
INSERT INTO educacion_especialidad_Per (id_usuario, descripcion_especialidad)
SELECT u.id_usuario, e.especialidad
FROM user_Per u
JOIN (SELECT 'rafael.cruz@example.com' AS correo, 'Criminalística' AS especialidad
      UNION ALL SELECT 'elena.salgado@example.com', 'Valuación inmobiliaria'
      UNION ALL SELECT 'daniel.rios@example.com', 'Valuación inmobiliaria'
      UNION ALL SELECT 'daniel.rios@example.com', 'Arquitectura'
      UNION ALL SELECT 'lucia.torres@example.com', 'Criminalística'
      UNION ALL SELECT 'lucia.torres@example.com', 'Balística'
      UNION ALL SELECT 'oscar.medina@example.com', 'Contabilidad'
      UNION ALL SELECT 'marina.leon@example.com', 'Psicología forense'
      UNION ALL SELECT 'arturo.luna@example.com', 'Documentoscopía') e ON e.correo = u.correo
WHERE NOT EXISTS (SELECT 1 FROM educacion_especialidad_Per x
                  WHERE x.id_usuario = u.id_usuario AND x.descripcion_especialidad = e.especialidad);

-- clave_materia guarda el id de la materia (VERSIÓN 4); se busca por nombre.
INSERT IGNORE INTO perito_materia_Per (id_usuario, clave_materia)
SELECT u.id_usuario, mat.id_materia
FROM user_Per u
JOIN (SELECT 'rafael.cruz@example.com' AS correo, 'Penal' AS materia
      UNION ALL SELECT 'elena.salgado@example.com', 'Civil'
      UNION ALL SELECT 'elena.salgado@example.com', 'Familiar'
      UNION ALL SELECT 'daniel.rios@example.com', 'Civil'
      UNION ALL SELECT 'daniel.rios@example.com', 'Mercantil'
      UNION ALL SELECT 'lucia.torres@example.com', 'Civil'
      UNION ALL SELECT 'lucia.torres@example.com', 'Penal'
      UNION ALL SELECT 'oscar.medina@example.com', 'Mercantil'
      UNION ALL SELECT 'marina.leon@example.com', 'Familiar'
      UNION ALL SELECT 'arturo.luna@example.com', 'Civil'
      UNION ALL SELECT 'arturo.luna@example.com', 'Penal') m ON m.correo = u.correo
JOIN materia_Ofc mat ON mat.nombre = m.materia;
