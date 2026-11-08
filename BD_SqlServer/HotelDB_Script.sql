/* =====================================================================
   SISTEMA DE GESTIÓN DE HOSPEDAJE - HotelDB  (SQL Server)
   Script completo: estructura + datos de prueba + triggers + vistas.
   OJO: elimina y vuelve a crear la base HotelDB si ya existe.
   Ejecutar completo en SSMS (New Query -> F5).
   ===================================================================== */

USE master;
GO
IF DB_ID('HotelDB') IS NOT NULL
BEGIN
    ALTER DATABASE HotelDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE HotelDB;
END
GO
CREATE DATABASE HotelDB;
GO
USE HotelDB;
GO

/* =====================================================================
   1. SEGURIDAD
   ===================================================================== */
CREATE TABLE Rol (
    idRol   INT IDENTITY(1,1) PRIMARY KEY,
    nombre  NVARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE Usuario (
    idUsuario      INT IDENTITY(1,1) PRIMARY KEY,
    username       NVARCHAR(30)  NOT NULL UNIQUE,
    passwordHash   CHAR(64)      NOT NULL,          -- SHA-256 en hexadecimal (mayúsculas)
    nombreCompleto NVARCHAR(100) NOT NULL,
    idRol          INT           NOT NULL REFERENCES Rol(idRol),
    activo         BIT           NOT NULL DEFAULT 1,
    fechaCreacion  DATETIME      NOT NULL DEFAULT GETDATE()
);

/* =====================================================================
   2. CLIENTES, FIDELIZACIÓN Y CONVENIOS CORPORATIVOS
   ===================================================================== */
CREATE TABLE NivelFidelidad (
    idNivel              INT IDENTITY(1,1) PRIMARY KEY,
    nombre               NVARCHAR(30)  NOT NULL UNIQUE,
    puntosMinimos        INT           NOT NULL DEFAULT 0,
    descuentoPorcentaje  DECIMAL(5,2)  NOT NULL DEFAULT 0
        CHECK (descuentoPorcentaje BETWEEN 0 AND 100)
);

CREATE TABLE Empresa (
    idEmpresa              INT IDENTITY(1,1) PRIMARY KEY,
    ruc                    CHAR(11)      NOT NULL UNIQUE,
    razonSocial            NVARCHAR(120) NOT NULL,
    contacto               NVARCHAR(100) NULL,
    telefono               NVARCHAR(20)  NULL,
    correo                 NVARCHAR(100) NULL,
    descuentoPorcentaje    DECIMAL(5,2)  NOT NULL DEFAULT 0
        CHECK (descuentoPorcentaje BETWEEN 0 AND 100),
    facturacionDiferenciada BIT          NOT NULL DEFAULT 0,
    diasCredito            INT           NOT NULL DEFAULT 0,
    activo                 BIT           NOT NULL DEFAULT 1
);

CREATE TABLE Cliente (
    idCliente      INT IDENTITY(1,1) PRIMARY KEY,
    tipoDocumento  VARCHAR(12)   NOT NULL DEFAULT 'DNI'
        CHECK (tipoDocumento IN ('DNI','CE','PASAPORTE','RUC')),
    documento      VARCHAR(15)   NOT NULL UNIQUE,
    nombres        NVARCHAR(80)  NOT NULL,
    apellidos      NVARCHAR(80)  NOT NULL,
    telefono       VARCHAR(20)   NULL,
    correo         NVARCHAR(100) NULL,
    nacionalidad   NVARCHAR(40)  NULL,
    idEmpresa      INT           NULL REFERENCES Empresa(idEmpresa),
    idNivel        INT           NULL REFERENCES NivelFidelidad(idNivel),
    puntos         INT           NOT NULL DEFAULT 0,
    activo         BIT           NOT NULL DEFAULT 1,
    fechaRegistro  DATETIME      NOT NULL DEFAULT GETDATE()
);

/* =====================================================================
   3. HABITACIONES Y TARIFAS
   ===================================================================== */
CREATE TABLE TipoHabitacion (
    idTipo      INT IDENTITY(1,1) PRIMARY KEY,
    nombre      NVARCHAR(40)  NOT NULL UNIQUE,   -- Simple, Doble, Matrimonial, Suite, Familiar
    descripcion NVARCHAR(200) NULL,
    capacidad   INT           NOT NULL CHECK (capacidad > 0),
    tarifaBase  DECIMAL(10,2) NOT NULL CHECK (tarifaBase >= 0)
);

CREATE TABLE Amenidad (
    idAmenidad INT IDENTITY(1,1) PRIMARY KEY,
    nombre     NVARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE TipoHabitacionAmenidad (
    idTipo     INT NOT NULL REFERENCES TipoHabitacion(idTipo),
    idAmenidad INT NOT NULL REFERENCES Amenidad(idAmenidad),
    PRIMARY KEY (idTipo, idAmenidad)
);

CREATE TABLE Habitacion (
    idHabitacion INT IDENTITY(1,1) PRIMARY KEY,
    numero       VARCHAR(10) NOT NULL UNIQUE,
    piso         INT         NOT NULL,
    idTipo       INT         NOT NULL REFERENCES TipoHabitacion(idTipo),
    estado       VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (estado IN ('DISPONIBLE','RESERVADA','OCUPADA','LIMPIEZA','MANTENIMIENTO')),
    activa       BIT         NOT NULL DEFAULT 1
);

CREATE TABLE Temporada (
    idTemporada  INT IDENTITY(1,1) PRIMARY KEY,
    nombre       NVARCHAR(50)  NOT NULL,            -- Baja, Media, Alta, Festiva, Evento
    fechaInicio  DATE          NOT NULL,
    fechaFin     DATE          NOT NULL,
    factor       DECIMAL(4,2)  NOT NULL CHECK (factor > 0),  -- multiplica la tarifa base
    CONSTRAINT CK_Temporada_Fechas CHECK (fechaFin >= fechaInicio)
);

CREATE TABLE Promocion (
    idPromocion    INT IDENTITY(1,1) PRIMARY KEY,
    codigo         VARCHAR(20)   NOT NULL UNIQUE,
    nombre         NVARCHAR(80)  NOT NULL,
    tipoDescuento  VARCHAR(12)   NOT NULL CHECK (tipoDescuento IN ('PORCENTAJE','MONTO')),
    valor          DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    fechaInicio    DATE          NOT NULL,
    fechaFin       DATE          NOT NULL,
    minNoches      INT           NOT NULL DEFAULT 1,
    idTipo         INT           NULL REFERENCES TipoHabitacion(idTipo),  -- NULL = aplica a todos
    activa         BIT           NOT NULL DEFAULT 1,
    CONSTRAINT CK_Promocion_Fechas CHECK (fechaFin >= fechaInicio)
);

CREATE TABLE EmpresaTarifa (
    idEmpresaTarifa INT IDENTITY(1,1) PRIMARY KEY,
    idEmpresa       INT NOT NULL REFERENCES Empresa(idEmpresa),
    idTipo          INT NOT NULL REFERENCES TipoHabitacion(idTipo),
    precioNoche     DECIMAL(10,2) NOT NULL CHECK (precioNoche >= 0),
    CONSTRAINT UQ_EmpresaTarifa UNIQUE (idEmpresa, idTipo)
);

/* =====================================================================
   4. SERVICIOS ADICIONALES Y PAQUETES TURÍSTICOS
   ===================================================================== */
CREATE TABLE ServicioAdicional (
    idServicio INT IDENTITY(1,1) PRIMARY KEY,
    nombre     NVARCHAR(80)  NOT NULL UNIQUE,
    area       VARCHAR(20)   NOT NULL
        CHECK (area IN ('RESTAURANTE','LAVANDERIA','SPA','TRANSPORTE','OTROS')),
    precio     DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    activo     BIT           NOT NULL DEFAULT 1
);

CREATE TABLE Paquete (
    idPaquete   INT IDENTITY(1,1) PRIMARY KEY,
    nombre      NVARCHAR(80)  NOT NULL UNIQUE,
    descripcion NVARCHAR(250) NULL,
    noches      INT           NOT NULL CHECK (noches > 0),
    precio      DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    idTipo      INT           NULL REFERENCES TipoHabitacion(idTipo),
    activo      BIT           NOT NULL DEFAULT 1
);

CREATE TABLE PaqueteServicio (
    idPaquete  INT NOT NULL REFERENCES Paquete(idPaquete),
    idServicio INT NOT NULL REFERENCES ServicioAdicional(idServicio),
    cantidad   INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    PRIMARY KEY (idPaquete, idServicio)
);

/* =====================================================================
   5. RESERVAS Y CANALES
   ===================================================================== */
CREATE TABLE CanalReserva (
    idCanal                 INT IDENTITY(1,1) PRIMARY KEY,
    nombre                  NVARCHAR(40) NOT NULL UNIQUE,
    requiereAnticipo        BIT          NOT NULL DEFAULT 0,
    porcentajeAnticipo      DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (porcentajeAnticipo BETWEEN 0 AND 100),
    diasCancelacionGratis   INT          NOT NULL DEFAULT 0,
    confirmacionAutomatica  BIT          NOT NULL DEFAULT 0,
    descuentoPorcentaje     DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (descuentoPorcentaje BETWEEN 0 AND 100),
    comisionPorcentaje      DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (comisionPorcentaje BETWEEN 0 AND 100)
);

CREATE TABLE Reserva (
    idReserva     INT IDENTITY(1,1) PRIMARY KEY,
    codigo        AS ('RES-' + RIGHT('000000' + CAST(idReserva AS VARCHAR(10)), 6)) PERSISTED,
    idCliente     INT NOT NULL REFERENCES Cliente(idCliente),
    idTipo        INT NOT NULL REFERENCES TipoHabitacion(idTipo),
    idHabitacion  INT NULL REFERENCES Habitacion(idHabitacion),   -- se puede asignar en el check-in
    idCanal       INT NOT NULL REFERENCES CanalReserva(idCanal),
    idPromocion   INT NULL REFERENCES Promocion(idPromocion),
    idPaquete     INT NULL REFERENCES Paquete(idPaquete),
    idUsuario     INT NULL REFERENCES Usuario(idUsuario),         -- quien registró
    fechaEntrada  DATE NOT NULL,
    fechaSalida   DATE NOT NULL,
    numAdultos    INT  NOT NULL DEFAULT 1 CHECK (numAdultos > 0),
    numNinos      INT  NOT NULL DEFAULT 0 CHECK (numNinos >= 0),
    tarifaNoche   DECIMAL(10,2) NOT NULL DEFAULT 0,
    descuento     DECIMAL(10,2) NOT NULL DEFAULT 0,
    total         DECIMAL(10,2) NOT NULL DEFAULT 0,
    anticipo      DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado        VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE','CONFIRMADA','CHECKIN','CHECKOUT','CANCELADA','NO_SHOW')),
    observaciones NVARCHAR(250) NULL,
    fechaCreacion DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_Reserva_Fechas CHECK (fechaSalida > fechaEntrada)
);

CREATE TABLE HistorialReserva (   -- apoyo para State / Memento / Observer
    idHistorial    INT IDENTITY(1,1) PRIMARY KEY,
    idReserva      INT NOT NULL REFERENCES Reserva(idReserva),
    estadoAnterior VARCHAR(20) NULL,
    estadoNuevo    VARCHAR(20) NOT NULL,
    fecha          DATETIME    NOT NULL DEFAULT GETDATE(),
    idUsuario      INT         NULL REFERENCES Usuario(idUsuario),
    detalle        NVARCHAR(200) NULL
);

/* =====================================================================
   6. ESTADÍA, CONSUMOS, SOLICITUDES, LIMPIEZA Y MANTENIMIENTO
   ===================================================================== */
CREATE TABLE Estadia (
    idEstadia      INT IDENTITY(1,1) PRIMARY KEY,
    idReserva      INT NOT NULL UNIQUE REFERENCES Reserva(idReserva),
    idHabitacion   INT NOT NULL REFERENCES Habitacion(idHabitacion),
    fechaCheckIn   DATETIME NOT NULL DEFAULT GETDATE(),
    fechaCheckOut  DATETIME NULL,
    estado         VARCHAR(15) NOT NULL DEFAULT 'EN_CURSO'
        CHECK (estado IN ('EN_CURSO','FINALIZADA')),
    idUsuarioCheckIn  INT NULL REFERENCES Usuario(idUsuario),
    idUsuarioCheckOut INT NULL REFERENCES Usuario(idUsuario)
);

CREATE TABLE ConsumoServicio (
    idConsumo      INT IDENTITY(1,1) PRIMARY KEY,
    idEstadia      INT NOT NULL REFERENCES Estadia(idEstadia),
    idServicio     INT NOT NULL REFERENCES ServicioAdicional(idServicio),
    cantidad       INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL CHECK (precioUnitario >= 0),
    fecha          DATETIME NOT NULL DEFAULT GETDATE(),
    idUsuario      INT NULL REFERENCES Usuario(idUsuario)
);

CREATE TABLE SolicitudEspecial (
    idSolicitud INT IDENTITY(1,1) PRIMARY KEY,
    idEstadia   INT NOT NULL REFERENCES Estadia(idEstadia),
    descripcion NVARCHAR(250) NOT NULL,
    estado      VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE','EN_PROCESO','ATENDIDA')),
    fecha       DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE TABLE TareaLimpieza (
    idTarea       INT IDENTITY(1,1) PRIMARY KEY,
    idHabitacion  INT NOT NULL REFERENCES Habitacion(idHabitacion),
    tipo          VARCHAR(15) NOT NULL DEFAULT 'CHECKOUT'
        CHECK (tipo IN ('CHECKOUT','DIARIA','PROFUNDA')),
    estado        VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE','EN_PROCESO','TERMINADA')),
    idAsignado    INT NULL REFERENCES Usuario(idUsuario),
    fechaCreacion DATETIME NOT NULL DEFAULT GETDATE(),
    fechaFin      DATETIME NULL
);

CREATE TABLE Mantenimiento (
    idMantenimiento INT IDENTITY(1,1) PRIMARY KEY,
    idHabitacion    INT NOT NULL REFERENCES Habitacion(idHabitacion),
    descripcion     NVARCHAR(250) NOT NULL,
    prioridad       VARCHAR(10) NOT NULL DEFAULT 'MEDIA'
        CHECK (prioridad IN ('BAJA','MEDIA','ALTA','URGENTE')),
    estado          VARCHAR(15) NOT NULL DEFAULT 'ABIERTO'
        CHECK (estado IN ('ABIERTO','EN_PROCESO','CERRADO')),
    idReporta       INT NULL REFERENCES Usuario(idUsuario),
    fechaReporte    DATETIME NOT NULL DEFAULT GETDATE(),
    fechaCierre     DATETIME NULL
);

/* =====================================================================
   7. FACTURACIÓN Y PAGOS
   ===================================================================== */
CREATE TABLE Factura (
    idFactura       INT IDENTITY(1,1) PRIMARY KEY,
    idEstadia       INT NOT NULL REFERENCES Estadia(idEstadia),
    idCliente       INT NOT NULL REFERENCES Cliente(idCliente),
    idEmpresa       INT NULL REFERENCES Empresa(idEmpresa),   -- facturación corporativa
    tipoComprobante VARCHAR(10) NOT NULL DEFAULT 'BOLETA'
        CHECK (tipoComprobante IN ('BOLETA','FACTURA')),
    serie           CHAR(4)  NOT NULL,
    numero          INT      NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0,
    igv             DECIMAL(10,2) NOT NULL DEFAULT 0,
    total           DECIMAL(10,2) NOT NULL DEFAULT 0,
    fechaEmision    DATETIME NOT NULL DEFAULT GETDATE(),
    estado          VARCHAR(10) NOT NULL DEFAULT 'EMITIDA'
        CHECK (estado IN ('EMITIDA','PAGADA','ANULADA')),
    idUsuario       INT NULL REFERENCES Usuario(idUsuario),
    CONSTRAINT UQ_Factura_SerieNumero UNIQUE (serie, numero)
);

CREATE TABLE DetalleFactura (
    idDetalle      INT IDENTITY(1,1) PRIMARY KEY,
    idFactura      INT NOT NULL REFERENCES Factura(idFactura),
    concepto       NVARCHAR(120) NOT NULL,
    cantidad       INT NOT NULL CHECK (cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL CHECK (precioUnitario >= 0),
    subtotal       AS (cantidad * precioUnitario) PERSISTED
);

CREATE TABLE Pago (
    idPago     INT IDENTITY(1,1) PRIMARY KEY,
    idReserva  INT NULL REFERENCES Reserva(idReserva),   -- anticipos
    idFactura  INT NULL REFERENCES Factura(idFactura),   -- pago final
    metodo     VARCHAR(15) NOT NULL
        CHECK (metodo IN ('EFECTIVO','TARJETA','TRANSFERENCIA','PASARELA','CREDITO')),
    monto      DECIMAL(10,2) NOT NULL CHECK (monto > 0),
    referencia NVARCHAR(60) NULL,
    fecha      DATETIME NOT NULL DEFAULT GETDATE(),
    idUsuario  INT NULL REFERENCES Usuario(idUsuario),
    CONSTRAINT CK_Pago_Destino CHECK (idReserva IS NOT NULL OR idFactura IS NOT NULL)
);

/* =====================================================================
   8. SATISFACCIÓN DEL HUÉSPED
   ===================================================================== */
CREATE TABLE EncuestaSatisfaccion (
    idEncuesta INT IDENTITY(1,1) PRIMARY KEY,
    idEstadia  INT NOT NULL UNIQUE REFERENCES Estadia(idEstadia),
    puntaje    INT NOT NULL CHECK (puntaje BETWEEN 1 AND 5),
    comentario NVARCHAR(300) NULL,
    fecha      DATETIME NOT NULL DEFAULT GETDATE()
);
GO

/* =====================================================================
   9. ÍNDICES
   ===================================================================== */
CREATE INDEX IX_Reserva_Fechas   ON Reserva (fechaEntrada, fechaSalida);
CREATE INDEX IX_Reserva_Cliente  ON Reserva (idCliente);
CREATE INDEX IX_Reserva_Hab      ON Reserva (idHabitacion, estado);
CREATE INDEX IX_Consumo_Estadia  ON ConsumoServicio (idEstadia);
CREATE INDEX IX_Habitacion_Tipo  ON Habitacion (idTipo, estado);
GO

/* =====================================================================
   10. TRIGGER ANTI-SOBREVENTA
   Impide dos reservas activas de la misma habitación con fechas cruzadas.
   ===================================================================== */
CREATE TRIGGER TR_Reserva_Sobreventa
ON Reserva
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN Reserva r
          ON r.idHabitacion = i.idHabitacion
         AND r.idReserva <> i.idReserva
        WHERE i.idHabitacion IS NOT NULL
          AND i.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
          AND r.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
          AND i.fechaEntrada < r.fechaSalida
          AND r.fechaEntrada < i.fechaSalida
    )
    BEGIN
        RAISERROR('La habitación ya está reservada en esas fechas (sobreventa).', 16, 1);
        ROLLBACK TRANSACTION;
    END
END
GO

/* =====================================================================
   11. VISTAS PARA REPORTES GERENCIALES
   ===================================================================== */
CREATE VIEW vw_OcupacionActual AS
SELECT COUNT(*) AS totalHabitaciones,
       SUM(CASE WHEN estado = 'OCUPADA' THEN 1 ELSE 0 END) AS ocupadas,
       CAST(100.0 * SUM(CASE WHEN estado = 'OCUPADA' THEN 1 ELSE 0 END) / COUNT(*) AS DECIMAL(5,2)) AS porcentajeOcupacion
FROM Habitacion
WHERE activa = 1;
GO

CREATE VIEW vw_IngresosPorTipoHabitacion AS
SELECT t.nombre AS tipoHabitacion,
       COUNT(f.idFactura) AS facturas,
       SUM(f.total)       AS ingresos
FROM Factura f
JOIN Estadia e         ON e.idEstadia = f.idEstadia
JOIN Habitacion h      ON h.idHabitacion = e.idHabitacion
JOIN TipoHabitacion t  ON t.idTipo = h.idTipo
WHERE f.estado = 'PAGADA'
GROUP BY t.nombre;
GO

CREATE VIEW vw_DemandaPorMes AS
SELECT YEAR(fechaEntrada) AS anio,
       MONTH(fechaEntrada) AS mes,
       COUNT(*) AS reservas
FROM Reserva
WHERE estado <> 'CANCELADA'
GROUP BY YEAR(fechaEntrada), MONTH(fechaEntrada);
GO

CREATE VIEW vw_ServiciosMasConsumidos AS
SELECT s.nombre AS servicio,
       s.area,
       SUM(c.cantidad) AS unidades,
       SUM(c.cantidad * c.precioUnitario) AS ingresos
FROM ConsumoServicio c
JOIN ServicioAdicional s ON s.idServicio = c.idServicio
GROUP BY s.nombre, s.area;
GO

CREATE VIEW vw_SatisfaccionPorTipo AS
SELECT t.nombre AS tipoHabitacion,
       COUNT(*) AS encuestas,
       CAST(AVG(CAST(q.puntaje AS DECIMAL(4,2))) AS DECIMAL(4,2)) AS puntajePromedio
FROM EncuestaSatisfaccion q
JOIN Estadia e        ON e.idEstadia = q.idEstadia
JOIN Habitacion h     ON h.idHabitacion = e.idHabitacion
JOIN TipoHabitacion t ON t.idTipo = h.idTipo
GROUP BY t.nombre;
GO

/* =====================================================================
   12. DATOS INICIALES Y DE PRUEBA
   ===================================================================== */
INSERT INTO Rol (nombre) VALUES
(N'Administrador'),(N'Recepción'),(N'Caja'),(N'Housekeeping'),(N'Gerencia');

-- Contraseñas de prueba (SHA-256): admin123, recep123, caja123, house123, gerente123
INSERT INTO Usuario (username, passwordHash, nombreCompleto, idRol) VALUES
('admin',    CONVERT(CHAR(64), HASHBYTES('SHA2_256','admin123'),   2), N'Administrador del Sistema', 1),
('recepcion',CONVERT(CHAR(64), HASHBYTES('SHA2_256','recep123'),   2), N'María Quispe',             2),
('caja',     CONVERT(CHAR(64), HASHBYTES('SHA2_256','caja123'),    2), N'Jorge Salas',              3),
('house',    CONVERT(CHAR(64), HASHBYTES('SHA2_256','house123'),   2), N'Rosa Medina',              4),
('gerente',  CONVERT(CHAR(64), HASHBYTES('SHA2_256','gerente123'), 2), N'Carlos Ramírez',           5);

INSERT INTO NivelFidelidad (nombre, puntosMinimos, descuentoPorcentaje) VALUES
(N'Bronce',0,0),(N'Plata',500,5),(N'Oro',1500,10),(N'Platino',3000,15);

INSERT INTO Empresa (ruc, razonSocial, contacto, telefono, correo, descuentoPorcentaje, facturacionDiferenciada, diasCredito) VALUES
('20123456789', N'Constructora Andina S.A.C.', N'Patricia Luna', '014567890', N'compras@andina.com', 12, 1, 30),
('20987654321', N'Minera del Sur S.A.',        N'Raúl Paredes',  '054321000', N'rrhh@mineradelsur.com', 15, 1, 15);

INSERT INTO Cliente (tipoDocumento, documento, nombres, apellidos, telefono, correo, nacionalidad, idEmpresa, idNivel, puntos) VALUES
('DNI','70123456',N'Ana',  N'Torres Ruiz', '987654321', N'ana@mail.com',  N'Peruana',  NULL, 2, 650),
('DNI','45678912',N'Luis', N'Gómez Vera',  '912345678', N'luis@mail.com', N'Peruana',  1,    1, 0),
('CE', '001234567',N'John',N'Miller',      '999111222', N'john@mail.com', N'Estadounidense', NULL, 1, 0),
('DNI','41236985',N'Elena',N'Castro Díaz', '955444333', N'elena@mail.com',N'Peruana',  2,    3, 1700);

INSERT INTO TipoHabitacion (nombre, descripcion, capacidad, tarifaBase) VALUES
(N'Simple',      N'Una cama individual, ideal para viajeros de negocios', 1, 120),
(N'Doble',       N'Dos camas individuales',                               2, 180),
(N'Matrimonial', N'Una cama king size',                                   2, 200),
(N'Suite',       N'Sala, dormitorio y baño con jacuzzi',                  3, 350),
(N'Familiar',    N'Dos ambientes para familias',                          5, 300);

INSERT INTO Amenidad (nombre) VALUES
(N'WiFi'),(N'TV cable'),(N'Aire acondicionado'),(N'Minibar'),
(N'Jacuzzi'),(N'Desayuno incluido'),(N'Cuna disponible'),(N'Escritorio de trabajo');

INSERT INTO TipoHabitacionAmenidad (idTipo, idAmenidad)
SELECT t.idTipo, a.idAmenidad
FROM (VALUES
 (N'Simple',N'WiFi'),(N'Simple',N'TV cable'),(N'Simple',N'Escritorio de trabajo'),
 (N'Doble',N'WiFi'),(N'Doble',N'TV cable'),(N'Doble',N'Aire acondicionado'),
 (N'Matrimonial',N'WiFi'),(N'Matrimonial',N'TV cable'),(N'Matrimonial',N'Aire acondicionado'),(N'Matrimonial',N'Minibar'),
 (N'Suite',N'WiFi'),(N'Suite',N'TV cable'),(N'Suite',N'Aire acondicionado'),(N'Suite',N'Minibar'),(N'Suite',N'Jacuzzi'),(N'Suite',N'Desayuno incluido'),
 (N'Familiar',N'WiFi'),(N'Familiar',N'TV cable'),(N'Familiar',N'Aire acondicionado'),(N'Familiar',N'Cuna disponible')
) v(tipo, amenidad)
JOIN TipoHabitacion t ON t.nombre = v.tipo
JOIN Amenidad a       ON a.nombre = v.amenidad;

INSERT INTO Habitacion (numero, piso, idTipo) VALUES
('101',1,1),('102',1,1),('103',1,2),('104',1,2),
('201',2,3),('202',2,3),('203',2,2),
('301',3,4),('302',3,4),('303',3,5),('304',3,5);

INSERT INTO Temporada (nombre, fechaInicio, fechaFin, factor) VALUES
(N'Temporada Baja',    '2026-03-01','2026-06-30', 0.90),
(N'Temporada Media',   '2026-07-01','2026-12-14', 1.00),
(N'Fiestas de Fin de Año','2026-12-15','2027-01-05', 1.40),
(N'Temporada Alta Verano','2027-01-06','2027-02-28', 1.25);

INSERT INTO Promocion (codigo, nombre, tipoDescuento, valor, fechaInicio, fechaFin, minNoches, idTipo) VALUES
('LARGA3',   N'3 noches o más: 10% de descuento', 'PORCENTAJE', 10, '2026-01-01','2027-12-31', 3, NULL),
('SUITE50',  N'Descuento fijo en Suite',          'MONTO',      50, '2026-01-01','2027-12-31', 1, 4),
('WEB5',     N'5% por reservar en la web',        'PORCENTAJE',  5, '2026-01-01','2027-12-31', 1, NULL);

INSERT INTO EmpresaTarifa (idEmpresa, idTipo, precioNoche) VALUES
(1,1,100),(1,2,150),(2,1,95),(2,3,170);

INSERT INTO ServicioAdicional (nombre, area, precio) VALUES
(N'Desayuno buffet',        'RESTAURANTE', 35),
(N'Almuerzo ejecutivo',     'RESTAURANTE', 45),
(N'Cena a la carta',        'RESTAURANTE', 70),
(N'Lavado de ropa (kg)',    'LAVANDERIA',  18),
(N'Planchado de prenda',    'LAVANDERIA',   8),
(N'Masaje relajante',       'SPA',        120),
(N'Traslado aeropuerto',    'TRANSPORTE',  60),
(N'Late check-out',         'OTROS',       50);

INSERT INTO Paquete (nombre, descripcion, noches, precio, idTipo) VALUES
(N'Escapada Romántica', N'2 noches en Matrimonial con desayuno y cena', 2, 520, 3),
(N'Ejecutivo Plus',     N'3 noches en Simple con lavandería y traslado', 3, 450, 1);

INSERT INTO PaqueteServicio (idPaquete, idServicio, cantidad) VALUES
(1,1,2),(1,3,1),(2,4,2),(2,7,1);

INSERT INTO CanalReserva (nombre, requiereAnticipo, porcentajeAnticipo, diasCancelacionGratis, confirmacionAutomatica, descuentoPorcentaje, comisionPorcentaje) VALUES
(N'Recepción',          0,  0, 0, 1, 0, 0),
(N'Teléfono',           1, 20, 2, 0, 0, 0),
(N'Página web',         1, 30, 3, 1, 5, 0),
(N'App móvil',          1, 30, 3, 1, 5, 0),
(N'Plataforma externa', 0,  0, 1, 1, 0, 15);

-- Reserva de ejemplo (Doble, 2 noches, canal Web, habitación 103)
INSERT INTO Reserva (idCliente, idTipo, idHabitacion, idCanal, idUsuario, fechaEntrada, fechaSalida,
                     numAdultos, tarifaNoche, descuento, total, anticipo, estado)
VALUES (1, 2, 3, 3, 2, DATEADD(DAY,3,CAST(GETDATE() AS DATE)), DATEADD(DAY,5,CAST(GETDATE() AS DATE)),
        2, 180, 18, 342, 102.60, 'CONFIRMADA');

INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
VALUES (1, NULL, 'CONFIRMADA', 2, N'Reserva creada desde datos de prueba');
GO

/* =====================================================================
   13. USUARIO DE APLICACIÓN (Java se conecta con este, no con 'sa')
   ===================================================================== */
USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'hotel_user')
    CREATE LOGIN hotel_user WITH PASSWORD = 'Hotel#2026', CHECK_POLICY = OFF;
GO
USE HotelDB;
GO
CREATE USER hotel_user FOR LOGIN hotel_user;
ALTER ROLE db_owner ADD MEMBER hotel_user;
GO

PRINT 'HotelDB creada correctamente.';
GO
