-- TALLERNET - Script 06: usuario de la aplicación (AP3)
-- Ejecutar como administrador de MySQL después de 01_ddl_creacion.sql.
-- La aplicación no se conecta como administradora: solo lee, inserta y modifica (AP2, sección 3.2).
-- La contraseña es de ejemplo para el entorno de desarrollo; reemplazarla en producción.
SET NAMES utf8mb4;

-- Entorno de desarrollo: conexión local con cifrado obligatorio.
DROP USER IF EXISTS 'tallernet_app'@'localhost';
CREATE USER 'tallernet_app'@'localhost' IDENTIFIED BY 'Tallernet.App.2026' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE ON tallernet.* TO 'tallernet_app'@'localhost';

-- Producción (AP2, sección 10.6): solo desde la red VPN y con TLS.
-- CREATE USER 'tallernet_app'@'10.8.0.%' IDENTIFIED BY '<contraseña segura>' REQUIRE SSL;
-- GRANT SELECT, INSERT, UPDATE ON tallernet.* TO 'tallernet_app'@'10.8.0.%';

FLUSH PRIVILEGES;
