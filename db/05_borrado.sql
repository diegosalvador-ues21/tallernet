-- TALLERNET - Script 05: borrado y verificación (AP2)
-- Se ejecuta con un usuario administrador: el usuario de la aplicación no tiene permiso DELETE.
USE tallernet;
SET NAMES utf8mb4;

-- 5.1 Depuración puntual: turnos cancelados (no aportan al historial).
DELETE FROM turno WHERE estado = 'CANCELADO';
SELECT COUNT(*) AS turnos_cancelados_restantes FROM turno WHERE estado = 'CANCELADO';

-- 5.2 Demostración de integridad referencial: este DELETE FALLA con el
-- error 1451 (Cannot delete or update a parent row) porque el cliente 2
-- tiene vehículos asociados. Se deja comentado para no cortar el script.
-- DELETE FROM cliente WHERE cliente_id = 2;

-- 5.3 Borrado completo de los datos de prueba, respetando dependencias.
START TRANSACTION;
DELETE FROM turno;      -- depende de sede, vehiculo, servicio y empleado
DELETE FROM empleado;   -- depende de sede
DELETE FROM vehiculo;   -- depende de cliente
DELETE FROM servicio;
DELETE FROM cliente;
DELETE FROM sede;
COMMIT;

-- 5.4 Verificación: todas las tablas deben informar 0 registros.
SELECT 'sede' AS tabla, COUNT(*) AS registros FROM sede
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'vehiculo', COUNT(*) FROM vehiculo
UNION ALL SELECT 'servicio', COUNT(*) FROM servicio
UNION ALL SELECT 'empleado', COUNT(*) FROM empleado
UNION ALL SELECT 'turno', COUNT(*) FROM turno;
