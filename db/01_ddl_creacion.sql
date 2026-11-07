-- =====================================================================
-- TALLERNET - Sistema de gestión multi-sede para talleres mecánicos
-- Script 01: definición de la base de datos (DDL) - versión 3 (AP2)
-- Sin cambios de estructura en el AP3: el prototipo Java se apoya en este modelo.
-- Motor objetivo: MySQL 8.0 (InnoDB, utf8mb4)
-- Autor: Diego Salvador - Seminario de Práctica de Informática (UES21)
-- =====================================================================
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS tallernet
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE tallernet;

-- Se eliminan en orden inverso de dependencias para permitir re-ejecutar el script
DROP TABLE IF EXISTS turno;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS vehiculo;
DROP TABLE IF EXISTS servicio;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS sede;

-- sede: ubicaciones físicas de la red Talleres Ruiz Díaz (RF-TN20)
CREATE TABLE sede (
    sede_id        INT          NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    direccion      VARCHAR(150) NOT NULL,
    hora_apertura  TIME         NOT NULL,
    hora_cierre    TIME         NOT NULL,
    activa         TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_sede PRIMARY KEY (sede_id),
    CONSTRAINT uq_sede_nombre UNIQUE (nombre),
    CONSTRAINT ck_sede_horario CHECK (hora_apertura < hora_cierre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cliente: registro único compartido por ambas sedes (RF-TN02)
CREATE TABLE cliente (
    cliente_id  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(60)  NOT NULL,
    apellido    VARCHAR(60)  NOT NULL,
    telefono    VARCHAR(20)  NOT NULL,
    email       VARCHAR(120) NULL,
    fecha_alta  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente PRIMARY KEY (cliente_id),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- vehiculo: automóviles asociados a un cliente, con el kilometraje
-- registrado al momento del alta (RF-TN03)
CREATE TABLE vehiculo (
    vehiculo_id  INT          NOT NULL AUTO_INCREMENT,
    cliente_id   INT          NOT NULL,
    patente      VARCHAR(10)  NOT NULL,
    marca        VARCHAR(40)  NOT NULL,
    modelo       VARCHAR(60)  NOT NULL,
    anio         SMALLINT     NOT NULL,
    km_alta      INT          NOT NULL DEFAULT 0 COMMENT 'Kilometraje del odómetro a la fecha de alta',
    fecha_alta   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_vehiculo PRIMARY KEY (vehiculo_id),
    CONSTRAINT uq_vehiculo_patente UNIQUE (patente),
    CONSTRAINT ck_vehiculo_anio CHECK (anio BETWEEN 1950 AND 2100),
    CONSTRAINT ck_vehiculo_km CHECK (km_alta BETWEEN 0 AND 2000000),
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (cliente_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- servicio: catálogo común a ambas sedes, con el intervalo de
-- kilometraje recomendado cuando corresponde (RF-TN17)
CREATE TABLE servicio (
    servicio_id   INT            NOT NULL AUTO_INCREMENT,
    descripcion   VARCHAR(120)   NOT NULL,
    precio_base   DECIMAL(10,2)  NOT NULL,
    duracion_min  INT            NOT NULL,
    km_servicio   INT            NULL COMMENT 'Intervalo de kilometraje recomendado para el servicio (NULL si no aplica)',
    CONSTRAINT pk_servicio PRIMARY KEY (servicio_id),
    CONSTRAINT uq_servicio_descripcion UNIQUE (descripcion),
    CONSTRAINT ck_servicio_precio CHECK (precio_base > 0),
    CONSTRAINT ck_servicio_duracion CHECK (duracion_min > 0),
    CONSTRAINT ck_servicio_km CHECK (km_servicio IS NULL OR km_servicio > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- empleado: personal de cada sede con su rol (RF-TN01, RF-TN18)
CREATE TABLE empleado (
    empleado_id  INT          NOT NULL AUTO_INCREMENT,
    sede_id      INT          NOT NULL,
    nombre       VARCHAR(60)  NOT NULL,
    apellido     VARCHAR(60)  NOT NULL,
    rol          ENUM('RECEPCIONISTA','MECANICO','ENCARGADO_REPUESTOS','ADMINISTRADOR') NOT NULL,
    activo       TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_empleado PRIMARY KEY (empleado_id),
    CONSTRAINT fk_empleado_sede FOREIGN KEY (sede_id)
        REFERENCES sede (sede_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- turno: cita de servicio para un vehículo en una sede (RF-TN04..06)
-- No almacena datos del cliente: se obtienen a través del vehículo (3FN)
CREATE TABLE turno (
    turno_id        INT          NOT NULL AUTO_INCREMENT,
    sede_id         INT          NOT NULL,
    vehiculo_id     INT          NOT NULL,
    servicio_id     INT          NOT NULL,
    empleado_id     INT          NOT NULL COMMENT 'Recepcionista que registra el turno',
    fecha_hora      DATETIME     NOT NULL,
    estado          ENUM('CONFIRMADO','CANCELADO','ATENDIDO') NOT NULL DEFAULT 'CONFIRMADO',
    observaciones   VARCHAR(255) NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_turno PRIMARY KEY (turno_id),
    CONSTRAINT fk_turno_sede FOREIGN KEY (sede_id)
        REFERENCES sede (sede_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_turno_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculo (vehiculo_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_turno_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicio (servicio_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_turno_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (empleado_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_turno_sede_fecha (sede_id, fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
