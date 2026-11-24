-- ==========================================================
-- Datos de prueba del módulo de Peritajes. Se corre después del script de la base en su VERSIÓN 3
-- (peritajes-bd-script-corregido.sql o los archivos de cambios). Usa INSERT IGNORE: si un dato ya existe, lo salta.
-- Se entra en http://localhost:8080/sij/login (inicio de sesión único del núcleo).
--
-- Usuarios (todos con la contraseña de prueba  Peritajes2026 ; correos de example.com, dominio reservado para pruebas):
--   admin.general@example.com       Administrador general    (SISTEMA_ADMIN, en user_Ofc): entra a los 5 módulos
--   admin.peritajes@example.com     Administrador            (PERITAJES_ADMIN)
--   rafael.cruz@example.com         Perito Administrador     (PERITAJES_PERITO_ADMIN)
--   elena.salgado@example.com       Perito                   (PERITAJES_PERITO)   ... y otros 5 peritos
-- La contraseña se guarda cifrada con PBKDF2 (clase nucleo/acceso/Contrasena). No usar en producción.
--
-- Las solicitudes de Audiencias (solicitud_perito_Aud) NO tienen peritaje todavía: el módulo las convierte en
-- peritajes y les asigna perito automáticamente la primera vez que alguien abre el inicio de Peritajes (RF 2.1 y 2.2).
--
-- [Confirmar con Oficialía] Si su script de datos ya trae materias con otras claves (por ejemplo 'CIVIL'),
-- hay que cambiar aquí 'CIV', 'FAM', 'MER' y 'PEN' por las suyas.
-- ==========================================================
USE sistema_judicial;

-- ---------- Roles: administrador general y los de Peritajes ----------
INSERT IGNORE INTO Rol (rol) VALUES ('SISTEMA_ADMIN'), ('PERITAJES_ADMIN'), ('PERITAJES_PERITO_ADMIN'), ('PERITAJES_PERITO');

-- ---------- Catálogos de Oficialía que necesitan las demandas ----------
INSERT IGNORE INTO materia_Ofc (clave_materia, nombre, descripcion) VALUES
    ('CIV', 'Civil', 'Materia civil'),
    ('FAM', 'Familiar', 'Materia familiar'),
    ('MER', 'Mercantil', 'Materia mercantil'),
    ('PEN', 'Penal', 'Materia penal');

INSERT IGNORE INTO distrito_Ofc (clave_distrito, nombre, cabecera) VALUES
    ('D01', 'Primer Distrito Judicial', 'Cuernavaca');

INSERT IGNORE INTO juzgado_Ofc (clave_juzgado, nombre, clave_distrito) VALUES
    ('J2CIV', 'Juzgado Segundo Civil de Cuernavaca', 'D01'),
    ('J1FAM', 'Juzgado Primero Familiar de Cuernavaca', 'D01'),
    ('J1PEN', 'Juzgado Primero Penal de Cuernavaca', 'D01'),
    ('J1MER', 'Juzgado Mercantil de Cuernavaca', 'D01');

INSERT IGNORE INTO oficialia_Ofc (clave_oficialia, nombre, clave_juzgado) VALUES
    ('OP2CIV', 'Oficialía del Juzgado Segundo Civil', 'J2CIV'),
    ('OP1FAM', 'Oficialía del Juzgado Primero Familiar', 'J1FAM'),
    ('OP1PEN', 'Oficialía del Juzgado Primero Penal', 'J1PEN'),
    ('OP1MER', 'Oficialía del Juzgado Mercantil', 'J1MER');

-- ---------- Demandas y solicitudes de peritaje (como las enviaría Audiencias) ----------
INSERT IGNORE INTO demanda_Ofc (folio, clave_materia, descripcion, clave_oficialia, fecha_recepcion, estado) VALUES
    ('PER-PRB-0438', 'CIV', 'Determinar el valor comercial de un inmueble urbano para una prueba pericial en materia civil.', 'OP2CIV', '2026-10-19 09:00:00', 'PROCEDENTE'),
    ('PER-PRB-0391', 'FAM', 'Valoración psicológica de las partes en un juicio de guarda y custodia.', 'OP1FAM', '2026-10-16 12:30:00', 'PROCEDENTE'),
    ('PER-PRB-0184', 'MER', 'Revisión contable de los estados financieros de la empresa demandada.', 'OP1MER', '2026-10-17 10:00:00', 'PROCEDENTE'),
    ('PER-PRB-0207', 'PEN', 'Dictamen de criminalística sobre los indicios del lugar de los hechos.', 'OP1PEN', '2026-10-17 13:00:00', 'PROCEDENTE'),
    ('PER-PRB-0512', 'CIV', 'Avalúo de daños en una construcción por defectos de obra.', 'OP2CIV', '2026-10-18 11:00:00', 'PROCEDENTE'),
    ('PER-PRB-0365', 'CIV', 'Peritaje en documentoscopía sobre la firma de un contrato de arrendamiento.', 'OP2CIV', '2026-10-13 09:00:00', 'PROCEDENTE');

-- Solo se insertan si esa demanda todavía no tiene solicitud (así el archivo se puede correr dos veces)
INSERT INTO solicitud_perito_Aud (folio, clave_materia)
SELECT d.folio, d.clave_materia FROM demanda_Ofc d
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

INSERT IGNORE INTO perito_materia_Per (id_usuario, clave_materia)
SELECT u.id_usuario, m.clave
FROM user_Per u
JOIN (SELECT 'rafael.cruz@example.com' AS correo, 'PEN' AS clave
      UNION ALL SELECT 'elena.salgado@example.com', 'CIV'
      UNION ALL SELECT 'elena.salgado@example.com', 'FAM'
      UNION ALL SELECT 'daniel.rios@example.com', 'CIV'
      UNION ALL SELECT 'daniel.rios@example.com', 'MER'
      UNION ALL SELECT 'lucia.torres@example.com', 'CIV'
      UNION ALL SELECT 'lucia.torres@example.com', 'PEN'
      UNION ALL SELECT 'oscar.medina@example.com', 'MER'
      UNION ALL SELECT 'marina.leon@example.com', 'FAM'
      UNION ALL SELECT 'arturo.luna@example.com', 'CIV'
      UNION ALL SELECT 'arturo.luna@example.com', 'PEN') m ON m.correo = u.correo;
