-- Ejecutar EN EL SERVIDOR como root:  sudo mysql < permisos-y-triggers.sql
-- 1. Da a dev_judicial el permiso TRIGGER.
-- 2. Crea los 12 triggers de Login (de docs/sistema-judicial-desde-cero.sql) a nombre de dev_judicial.
-- Se puede correr varias veces (DROP TRIGGER IF EXISTS).

GRANT TRIGGER ON sistema_judicial.* TO 'dev_judicial'@'%';
USE sistema_judicial;

DROP TRIGGER IF EXISTS trg_user_Ofc_login_ai;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Ofc_login_ai AFTER INSERT ON user_Ofc
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

DROP TRIGGER IF EXISTS trg_user_Ofc_login_au;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Ofc_login_au AFTER UPDATE ON user_Ofc
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

DROP TRIGGER IF EXISTS trg_user_Ofc_login_ad;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Ofc_login_ad AFTER DELETE ON user_Ofc
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

DROP TRIGGER IF EXISTS trg_user_Aud_login_ai;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Aud_login_ai AFTER INSERT ON user_Aud
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

DROP TRIGGER IF EXISTS trg_user_Aud_login_au;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Aud_login_au AFTER UPDATE ON user_Aud
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

DROP TRIGGER IF EXISTS trg_user_Aud_login_ad;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Aud_login_ad AFTER DELETE ON user_Aud
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

DROP TRIGGER IF EXISTS trg_user_Per_login_ai;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Per_login_ai AFTER INSERT ON user_Per
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

DROP TRIGGER IF EXISTS trg_user_Per_login_au;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Per_login_au AFTER UPDATE ON user_Per
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

DROP TRIGGER IF EXISTS trg_user_Per_login_ad;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Per_login_ad AFTER DELETE ON user_Per
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

DROP TRIGGER IF EXISTS trg_user_Seg_login_ai;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Seg_login_ai AFTER INSERT ON user_Seg
    FOR EACH ROW
    INSERT INTO Login (correo, contrasena) VALUES (NEW.correo, NEW.contrasena);

DROP TRIGGER IF EXISTS trg_user_Seg_login_au;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Seg_login_au AFTER UPDATE ON user_Seg
    FOR EACH ROW
    UPDATE Login SET correo = NEW.correo, contrasena = NEW.contrasena
    WHERE correo = OLD.correo
      AND (NEW.correo <> OLD.correo OR NEW.contrasena <> OLD.contrasena);

DROP TRIGGER IF EXISTS trg_user_Seg_login_ad;
CREATE DEFINER=`dev_judicial`@`%` TRIGGER trg_user_Seg_login_ad AFTER DELETE ON user_Seg
    FOR EACH ROW
    DELETE FROM Login WHERE correo = OLD.correo;

-- Revisión: deben salir 12
SELECT COUNT(*) AS triggers FROM information_schema.triggers WHERE trigger_schema = 'sistema_judicial';
SHOW GRANTS FOR 'dev_judicial'@'%';
