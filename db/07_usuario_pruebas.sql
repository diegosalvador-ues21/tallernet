-- TALLERNET - Script 07: usuario para pruebas automatizadas (solo entorno de desarrollo)
-- Las pruebas de integración CPI04 recrean la base con los scripts 01 y 02,
-- por lo que necesitan un usuario con permisos de definición de datos.
-- No crear este usuario en el servidor de producción.
SET NAMES utf8mb4;
DROP USER IF EXISTS 'tallernet_admin'@'localhost';
CREATE USER 'tallernet_admin'@'localhost' IDENTIFIED BY 'Tallernet.Admin.2026';
GRANT ALL PRIVILEGES ON tallernet.* TO 'tallernet_admin'@'localhost';
FLUSH PRIVILEGES;
