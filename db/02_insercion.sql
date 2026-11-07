-- TALLERNET - Script 02: datos base (DML) - versión 3 (AP2)
-- Ejecutar después de 01_ddl_creacion.sql. Las pruebas de sistema CPS05 corren sobre esta carga.
USE tallernet;
SET NAMES utf8mb4;

INSERT INTO sede (nombre, direccion, hora_apertura, hora_cierre) VALUES
('Sede Echeandía', 'Echeandía 3042, CABA', '08:00:00', '20:00:00'),
('Sede Flores', 'Av. Rivadavia 8450, Flores, CABA', '08:00:00', '20:00:00');

INSERT INTO cliente (nombre, apellido, telefono, email) VALUES
('Laura', 'Gómez', '11-4567-8901', 'laura.gomez@mail.com'),
('Martín', 'Pereyra', '11-5234-1122', 'mpereyra@mail.com'),
('Sofía', 'Benítez', '11-6012-3344', NULL),
('Ricardo', 'Fernández', '11-3890-7788', 'rfernandez@mail.com');

INSERT INTO vehiculo (cliente_id, patente, marca, modelo, anio, km_alta, fecha_alta) VALUES
(1, 'AE123BC', 'Volkswagen', 'Gol Trend', 2018, 86500, '2026-09-01 09:15:00'),
(2, 'AC456DE', 'Toyota', 'Etios', 2019, 61200, '2026-09-02 10:40:00'),
(2, 'OKL482', 'Ford', 'Ranger', 2014, 198750, '2026-09-02 10:45:00'),
(3, 'AF789GH', 'Renault', 'Sandero', 2022, 24300, '2026-09-10 16:05:00'),
(4, 'AD321JK', 'Fiat', 'Cronos', 2020, 52900, '2026-09-15 11:30:00');

INSERT INTO servicio (descripcion, precio_base, duracion_min, km_servicio) VALUES
('Cambio de aceite y filtros', 45000.00, 60, 10000),
('Alineación y balanceo', 38000.00, 60, 10000),
('Service completo 10.000 km', 120000.00, 180, 10000),
('Diagnóstico de frenos', 25000.00, 90, NULL),
('Diagnóstico por escáner (OBD-II)', 30000.00, 30, NULL);

INSERT INTO empleado (sede_id, nombre, apellido, rol) VALUES
(1, 'Mario', 'Ruiz Díaz', 'ADMINISTRADOR'),
(1, 'Carla', 'Medina', 'RECEPCIONISTA'),
(2, 'Julián', 'Acosta', 'RECEPCIONISTA'),
(1, 'Esteban', 'Sosa', 'MECANICO'),
(2, 'Nahuel', 'Ortiz', 'MECANICO');

INSERT INTO turno (sede_id, vehiculo_id, servicio_id, empleado_id, fecha_hora, estado, observaciones) VALUES
(1, 1, 1, 2, '2026-10-05 09:00:00', 'CONFIRMADO', 'Cliente solicita revisar ruido en suspensión'),
(1, 2, 3, 2, '2026-10-05 10:00:00', 'CONFIRMADO', NULL),
(2, 3, 4, 3, '2026-10-05 09:00:00', 'CONFIRMADO', 'Pedal de freno esponjoso'),
(2, 4, 2, 3, '2026-10-05 14:00:00', 'CONFIRMADO', NULL),
(1, 5, 5, 2, '2026-10-06 08:00:00', 'CANCELADO', 'Cancelado por el cliente vía WhatsApp'),
(2, 1, 5, 3, '2026-09-21 11:00:00', 'ATENDIDO', 'Testigo de motor encendido');

-- Verificación de la carga base
SELECT 'sede' AS tabla, COUNT(*) AS registros FROM sede
UNION ALL SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL SELECT 'vehiculo', COUNT(*) FROM vehiculo
UNION ALL SELECT 'servicio', COUNT(*) FROM servicio
UNION ALL SELECT 'empleado', COUNT(*) FROM empleado
UNION ALL SELECT 'turno', COUNT(*) FROM turno;
