-- TALLERNET - Script 04: consultas de reporte Q1 a Q7 (AP2)
USE tallernet;
SET NAMES utf8mb4;

-- Q1: turnos confirmados de cada sede en un día (RF-TN04, RF-TN21)
SELECT s.nombre AS sede,
       TIME_FORMAT(t.fecha_hora, '%H:%i') AS hora,
       v.patente AS patente,
       CONCAT(v.marca, ' ', v.modelo) AS vehiculo,
       CONCAT(c.apellido, ', ', c.nombre) AS cliente,
       c.telefono AS telefono,
       sv.descripcion AS servicio,
       sv.duracion_min AS duracion_min
FROM turno t
INNER JOIN sede s      ON s.sede_id = t.sede_id
INNER JOIN vehiculo v  ON v.vehiculo_id = t.vehiculo_id
INNER JOIN cliente c   ON c.cliente_id = v.cliente_id
INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id
WHERE DATE(t.fecha_hora) = '2026-10-05'
  AND t.estado = 'CONFIRMADO'
ORDER BY s.nombre, t.fecha_hora;

-- Q2: historial de un vehículo en ambas sedes (RF-TN22)
SELECT v.patente AS patente,
       v.km_alta AS km_alta,
       DATE_FORMAT(t.fecha_hora, '%d/%m/%Y %H:%i') AS fecha_turno,
       s.nombre AS sede,
       sv.descripcion AS servicio,
       sv.km_servicio AS km_servicio,
       t.estado AS estado
FROM turno t
INNER JOIN vehiculo v  ON v.vehiculo_id = t.vehiculo_id
INNER JOIN sede s      ON s.sede_id = t.sede_id
INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id
WHERE v.patente = 'AE123BC'
ORDER BY t.fecha_hora DESC;

-- Q3: vista consolidada por sede (titular)
SELECT s.nombre AS sede,
       COUNT(t.turno_id) AS turnos_confirmados,
       SUM(sv.duracion_min) AS minutos_agendados,
       SUM(sv.precio_base) AS ingreso_estimado
FROM sede s
INNER JOIN turno t     ON t.sede_id = s.sede_id
INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id
WHERE t.estado = 'CONFIRMADO'
GROUP BY s.sede_id, s.nombre
ORDER BY turnos_confirmados DESC, s.nombre;

-- Q4: superposición de un turno nuevo (RF-TN05)
SELECT t.turno_id AS turno_existente,
       TIME_FORMAT(t.fecha_hora, '%H:%i') AS inicio,
       TIME_FORMAT(DATE_ADD(t.fecha_hora, INTERVAL sv.duracion_min MINUTE), '%H:%i') AS fin
FROM turno t
INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id
WHERE t.sede_id = 1
  AND t.estado <> 'CANCELADO'
  AND t.fecha_hora < '2026-10-05 11:30:00'
  AND DATE_ADD(t.fecha_hora, INTERVAL sv.duracion_min MINUTE) > '2026-10-05 10:30:00'
ORDER BY t.fecha_hora;

-- Q5: clientes con más de un vehículo
SELECT CONCAT(c.apellido, ', ', c.nombre) AS cliente,
       COUNT(v.vehiculo_id) AS cantidad_vehiculos
FROM cliente c
INNER JOIN vehiculo v ON v.cliente_id = c.cliente_id
GROUP BY c.cliente_id, c.apellido, c.nombre
HAVING COUNT(v.vehiculo_id) > 1
ORDER BY cantidad_vehiculos DESC, cliente;

-- Q6: kilometraje promedio al alta por marca (marcas con 3 o más vehículos)
SELECT v.marca AS marca,
       COUNT(*) AS vehiculos,
       ROUND(AVG(v.km_alta)) AS km_promedio_alta,
       MAX(v.km_alta) AS km_maximo_alta
FROM vehiculo v
GROUP BY v.marca
HAVING COUNT(*) >= 3
ORDER BY km_promedio_alta DESC;

-- Q7: próximo service estimado por kilometraje
SELECT v.patente AS patente,
       CONCAT(c.apellido, ', ', c.nombre) AS cliente,
       sv.descripcion AS servicio_realizado,
       DATE_FORMAT(t.fecha_hora, '%d/%m/%Y') AS fecha,
       v.km_alta AS km_referencia,
       v.km_alta + sv.km_servicio AS proximo_service_km
FROM turno t
INNER JOIN vehiculo v  ON v.vehiculo_id = t.vehiculo_id
INNER JOIN cliente c   ON c.cliente_id = v.cliente_id
INNER JOIN servicio sv ON sv.servicio_id = t.servicio_id
WHERE t.estado = 'ATENDIDO'
  AND sv.km_servicio IS NOT NULL
ORDER BY proximo_service_km
LIMIT 10;
