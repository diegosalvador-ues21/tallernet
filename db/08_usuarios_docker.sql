-- TALLERNET - Script 08: usuarios para MySQL en Docker (SOLO ENTORNO DE DESARROLLO)
-- Con MySQL en un contenedor, el equipo anfitrión se conecta a través de la red de Docker
-- y no como 'localhost': los usuarios de los scripts 06 y 07 no coinciden. Este script crea
-- los mismos usuarios, con las mismas claves de ejemplo, para cualquier origen ('%').
-- El puerto del contenedor se publica solo en 127.0.0.1, por lo que no es accesible desde la red.
-- No usar en producción: allí rige el script 06 (usuario restringido a la red VPN 10.8.0.%).
SET NAMES utf8mb4;

DROP USER IF EXISTS 'tallernet_app'@'%';
CREATE USER 'tallernet_app'@'%' IDENTIFIED BY 'Tallernet.App.2026' REQUIRE SSL;
GRANT SELECT, INSERT, UPDATE ON tallernet.* TO 'tallernet_app'@'%';

DROP USER IF EXISTS 'tallernet_admin'@'%';
CREATE USER 'tallernet_admin'@'%' IDENTIFIED BY 'Tallernet.Admin.2026';
GRANT ALL PRIVILEGES ON tallernet.* TO 'tallernet_admin'@'%';

FLUSH PRIVILEGES;
