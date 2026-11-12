/* =====================================================================
   SISTEMA DE GESTIÓN DE HOSPEDAJE - HotelDB  (SQL Server)
   Script ÚNICO y definitivo: estructura + datos de prueba + trigger + procedimientos almacenados.
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
   11. DATOS INICIALES Y DE PRUEBA
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
   12. PROCEDIMIENTOS ALMACENADOS (acceso a datos de TODO el sistema)
   Convención: usp_<Entidad>_<Accion>. Las reglas de negocio viven en
   los servicios Java; aquí solo hay acceso a datos y las operaciones
   que deben ser atómicas (check-in, check-out, factura, pago).
   Errores de negocio de la base: THROW 50001..50030 (ver cada uno).
   ===================================================================== */

/* ---------------------------------------------------------------------
   14.1 SEGURIDAD
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Rol_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idRol, nombre FROM Rol ORDER BY idRol;
END
GO

CREATE PROCEDURE usp_Usuario_ObtenerPorUsername
    @username NVARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: idUsuario, username, passwordHash, nombreCompleto, activo, idRol, rol
    SELECT u.idUsuario, u.username, u.passwordHash, u.nombreCompleto, u.activo,
           r.idRol, r.nombre AS rol
    FROM Usuario u
    JOIN Rol r ON r.idRol = u.idRol
    WHERE u.username = @username;
END
GO

CREATE PROCEDURE usp_Usuario_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT u.idUsuario, u.username, u.nombreCompleto, u.activo, r.idRol, r.nombre AS rol
    FROM Usuario u
    JOIN Rol r ON r.idRol = u.idRol
    ORDER BY u.username;
END
GO

CREATE PROCEDURE usp_Usuario_ExisteUsername
    @username NVARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT CASE WHEN EXISTS (SELECT 1 FROM Usuario WHERE username = @username)
                THEN 1 ELSE 0 END AS existe;
END
GO

CREATE PROCEDURE usp_Usuario_Insertar
    @username       NVARCHAR(30),
    @passwordHash   CHAR(64),
    @nombreCompleto NVARCHAR(100),
    @idRol          INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Usuario (username, passwordHash, nombreCompleto, idRol)
    VALUES (@username, @passwordHash, @nombreCompleto, @idRol);
END
GO

CREATE PROCEDURE usp_Usuario_CambiarEstado
    @idUsuario INT,
    @activo    BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Usuario SET activo = @activo WHERE idUsuario = @idUsuario;
END
GO

CREATE PROCEDURE usp_Usuario_CambiarPassword
    @idUsuario    INT,
    @passwordHash CHAR(64)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Usuario SET passwordHash = @passwordHash WHERE idUsuario = @idUsuario;
END
GO

/* ---------------------------------------------------------------------
   14.2 CLIENTES, CONVENIOS Y FIDELIZACIÓN
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Cliente_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idCliente, tipoDocumento, documento, nombres, apellidos,
           telefono, correo, nacionalidad
    FROM Cliente
    WHERE activo = 1
    ORDER BY apellidos, nombres;
END
GO

CREATE PROCEDURE usp_Cliente_BuscarPorDocumento
    @documento VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idCliente, tipoDocumento, documento, nombres, apellidos,
           telefono, correo, nacionalidad, idEmpresa, idNivel, puntos
    FROM Cliente
    WHERE documento = @documento AND activo = 1;
END
GO

CREATE PROCEDURE usp_Cliente_ExisteDocumento
    @documento  VARCHAR(15),
    @idExcluido INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT CASE WHEN EXISTS (SELECT 1 FROM Cliente
                             WHERE documento = @documento AND idCliente <> @idExcluido)
                THEN 1 ELSE 0 END AS existe;
END
GO

CREATE PROCEDURE usp_Cliente_Insertar
    @tipoDocumento VARCHAR(12),
    @documento     VARCHAR(15),
    @nombres       NVARCHAR(80),
    @apellidos     NVARCHAR(80),
    @telefono      VARCHAR(20)   = NULL,
    @correo        NVARCHAR(100) = NULL,
    @nacionalidad  NVARCHAR(40)  = NULL
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Cliente (tipoDocumento, documento, nombres, apellidos,
                         telefono, correo, nacionalidad)
    VALUES (@tipoDocumento, @documento, @nombres, @apellidos,
            @telefono, @correo, @nacionalidad);
END
GO

CREATE PROCEDURE usp_Cliente_Actualizar
    @idCliente     INT,
    @tipoDocumento VARCHAR(12),
    @documento     VARCHAR(15),
    @nombres       NVARCHAR(80),
    @apellidos     NVARCHAR(80),
    @telefono      VARCHAR(20)   = NULL,
    @correo        NVARCHAR(100) = NULL,
    @nacionalidad  NVARCHAR(40)  = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Cliente
    SET tipoDocumento = @tipoDocumento,
        documento     = @documento,
        nombres       = @nombres,
        apellidos     = @apellidos,
        telefono      = @telefono,
        correo        = @correo,
        nacionalidad  = @nacionalidad
    WHERE idCliente = @idCliente;
END
GO

CREATE PROCEDURE usp_Cliente_Desactivar
    @idCliente INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Cliente SET activo = 0 WHERE idCliente = @idCliente;
END
GO

CREATE PROCEDURE usp_Cliente_ObtenerDescuentos
    @documento VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: razonSocial, descEmpresa, nivel, descNivel (0 filas si el cliente no existe)
    SELECT e.razonSocial,
           ISNULL(e.descuentoPorcentaje, 0) AS descEmpresa,
           n.nombre                         AS nivel,
           ISNULL(n.descuentoPorcentaje, 0) AS descNivel
    FROM Cliente c
    LEFT JOIN Empresa e        ON e.idEmpresa = c.idEmpresa
    LEFT JOIN NivelFidelidad n ON n.idNivel   = c.idNivel
    WHERE c.documento = @documento AND c.activo = 1;
END
GO

CREATE PROCEDURE usp_Cliente_AsignarConvenio
    @idCliente INT,
    @idEmpresa INT = NULL,
    @idNivel   INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Cliente SET idEmpresa = @idEmpresa, idNivel = @idNivel WHERE idCliente = @idCliente;
END
GO

CREATE PROCEDURE usp_Cliente_ActualizarPuntos
    @idCliente INT,
    @puntos    INT,
    @idNivel   INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Cliente SET puntos = @puntos, idNivel = @idNivel WHERE idCliente = @idCliente;
END
GO

CREATE PROCEDURE usp_Empresa_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idEmpresa, ruc, razonSocial, contacto, telefono, correo,
           descuentoPorcentaje, facturacionDiferenciada, diasCredito
    FROM Empresa
    WHERE activo = 1
    ORDER BY razonSocial;
END
GO

CREATE PROCEDURE usp_NivelFidelidad_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idNivel, nombre, puntosMinimos, descuentoPorcentaje
    FROM NivelFidelidad
    ORDER BY puntosMinimos;
END
GO

/* ---------------------------------------------------------------------
   14.3 HABITACIONES
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Habitacion_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT h.idHabitacion, h.numero, h.piso, h.estado,
           t.idTipo, t.nombre AS tipo, t.capacidad, t.tarifaBase
    FROM Habitacion h
    JOIN TipoHabitacion t ON t.idTipo = h.idTipo
    WHERE h.activa = 1
    ORDER BY h.numero;
END
GO

CREATE PROCEDURE usp_Amenidad_ListarPorTipo
AS
BEGIN
    SET NOCOUNT ON;
    SELECT ta.idTipo, a.nombre
    FROM TipoHabitacionAmenidad ta
    JOIN Amenidad a ON a.idAmenidad = ta.idAmenidad
    ORDER BY a.nombre;
END
GO

CREATE PROCEDURE usp_Habitacion_ActualizarEstado
    @idHabitacion INT,
    @estado       VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Habitacion SET estado = @estado WHERE idHabitacion = @idHabitacion;
END
GO

CREATE PROCEDURE usp_Habitacion_Insertar
    @numero  VARCHAR(10),
    @piso    INT,
    @idTipo  INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Habitacion (numero, piso, idTipo) VALUES (@numero, @piso, @idTipo);
END
GO

CREATE PROCEDURE usp_Habitacion_Desactivar
    @idHabitacion INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Habitacion SET activa = 0 WHERE idHabitacion = @idHabitacion;
END
GO

CREATE PROCEDURE usp_Habitacion_BuscarDisponibles
    @idTipo   INT,
    @entrada  DATE,
    @salida   DATE
AS
BEGIN
    SET NOCOUNT ON;
    -- Habitaciones del tipo sin reservas activas que se crucen con el rango y que no estén en mantenimiento
    SELECT h.idHabitacion, h.numero, h.piso
    FROM Habitacion h
    WHERE h.activa = 1
      AND h.idTipo = @idTipo
      AND h.estado <> 'MANTENIMIENTO'
      AND NOT EXISTS (SELECT 1 FROM Reserva r
                      WHERE r.idHabitacion = h.idHabitacion
                        AND r.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
                        AND @entrada < r.fechaSalida
                        AND r.fechaEntrada < @salida)
    ORDER BY h.numero;
END
GO

CREATE PROCEDURE usp_TipoHabitacion_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idTipo, nombre, capacidad, tarifaBase
    FROM TipoHabitacion
    ORDER BY tarifaBase;
END
GO

CREATE PROCEDURE usp_TipoHabitacion_ActualizarTarifa
    @idTipo     INT,
    @tarifaBase DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE TipoHabitacion SET tarifaBase = @tarifaBase WHERE idTipo = @idTipo;
END
GO

/* ---------------------------------------------------------------------
   14.4 TARIFAS, PROMOCIONES, CONVENIOS, SERVICIOS, PAQUETES Y CANALES
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Temporada_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idTemporada, nombre, fechaInicio, fechaFin, factor
    FROM Temporada
    ORDER BY fechaInicio;
END
GO

CREATE PROCEDURE usp_Temporada_Insertar
    @nombre      NVARCHAR(50),
    @fechaInicio DATE,
    @fechaFin    DATE,
    @factor      DECIMAL(4,2)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Temporada (nombre, fechaInicio, fechaFin, factor)
    VALUES (@nombre, @fechaInicio, @fechaFin, @factor);
END
GO

CREATE PROCEDURE usp_Temporada_Actualizar
    @idTemporada INT,
    @nombre      NVARCHAR(50),
    @fechaInicio DATE,
    @fechaFin    DATE,
    @factor      DECIMAL(4,2)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Temporada
    SET nombre = @nombre, fechaInicio = @fechaInicio, fechaFin = @fechaFin, factor = @factor
    WHERE idTemporada = @idTemporada;
END
GO

CREATE PROCEDURE usp_Temporada_Eliminar
    @idTemporada INT
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM Temporada WHERE idTemporada = @idTemporada;
END
GO

CREATE PROCEDURE usp_Promocion_BuscarVigente
    @codigo VARCHAR(20),
    @fecha  DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idPromocion, codigo, nombre, tipoDescuento, valor,
           fechaInicio, fechaFin, minNoches, idTipo
    FROM Promocion
    WHERE codigo = @codigo
      AND activa = 1
      AND @fecha BETWEEN fechaInicio AND fechaFin;
END
GO

CREATE PROCEDURE usp_Promocion_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT p.idPromocion, p.codigo, p.nombre, p.tipoDescuento, p.valor,
           p.fechaInicio, p.fechaFin, p.minNoches, p.idTipo,
           t.nombre AS tipo, p.activa
    FROM Promocion p
    LEFT JOIN TipoHabitacion t ON t.idTipo = p.idTipo
    ORDER BY p.codigo;
END
GO

CREATE PROCEDURE usp_Promocion_Insertar
    @codigo        VARCHAR(20),
    @nombre        NVARCHAR(80),
    @tipoDescuento VARCHAR(12),
    @valor         DECIMAL(10,2),
    @fechaInicio   DATE,
    @fechaFin      DATE,
    @minNoches     INT = 1,
    @idTipo        INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Promocion (codigo, nombre, tipoDescuento, valor, fechaInicio, fechaFin, minNoches, idTipo)
    VALUES (@codigo, @nombre, @tipoDescuento, @valor, @fechaInicio, @fechaFin, @minNoches, @idTipo);
END
GO

CREATE PROCEDURE usp_Promocion_Actualizar
    @idPromocion   INT,
    @codigo        VARCHAR(20),
    @nombre        NVARCHAR(80),
    @tipoDescuento VARCHAR(12),
    @valor         DECIMAL(10,2),
    @fechaInicio   DATE,
    @fechaFin      DATE,
    @minNoches     INT = 1,
    @idTipo        INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Promocion
    SET codigo = @codigo, nombre = @nombre, tipoDescuento = @tipoDescuento, valor = @valor,
        fechaInicio = @fechaInicio, fechaFin = @fechaFin, minNoches = @minNoches, idTipo = @idTipo
    WHERE idPromocion = @idPromocion;
END
GO

CREATE PROCEDURE usp_Promocion_CambiarEstado
    @idPromocion INT,
    @activa      BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Promocion SET activa = @activa WHERE idPromocion = @idPromocion;
END
GO

CREATE PROCEDURE usp_EmpresaTarifa_Obtener
    @idEmpresa INT,
    @idTipo    INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT precioNoche FROM EmpresaTarifa WHERE idEmpresa = @idEmpresa AND idTipo = @idTipo;
END
GO

CREATE PROCEDURE usp_Servicio_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idServicio, nombre, area, precio
    FROM ServicioAdicional
    WHERE activo = 1
    ORDER BY area, nombre;
END
GO

CREATE PROCEDURE usp_Paquete_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idPaquete, nombre, descripcion, noches, precio, idTipo
    FROM Paquete
    WHERE activo = 1
    ORDER BY nombre;
END
GO

CREATE PROCEDURE usp_PaqueteServicio_ListarPorPaquete
    @idPaquete INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT s.idServicio, s.nombre, s.area, s.precio, ps.cantidad
    FROM PaqueteServicio ps
    JOIN ServicioAdicional s ON s.idServicio = ps.idServicio
    WHERE ps.idPaquete = @idPaquete
    ORDER BY s.nombre;
END
GO

CREATE PROCEDURE usp_Canal_Listar
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idCanal, nombre, requiereAnticipo, porcentajeAnticipo, diasCancelacionGratis,
           confirmacionAutomatica, descuentoPorcentaje, comisionPorcentaje
    FROM CanalReserva
    ORDER BY idCanal;
END
GO

/* ---------------------------------------------------------------------
   14.5 RESERVAS
   Errores: 50001 sobreventa, 50002 reserva no modificable, 50003 reserva inexistente
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Reserva_Crear
    @idCliente     INT,
    @idTipo        INT,
    @idHabitacion  INT           = NULL,
    @idCanal       INT,
    @idPromocion   INT           = NULL,
    @idPaquete     INT           = NULL,
    @idUsuario     INT           = NULL,
    @fechaEntrada  DATE,
    @fechaSalida   DATE,
    @numAdultos    INT           = 1,
    @numNinos      INT           = 0,
    @tarifaNoche   DECIMAL(10,2),
    @descuento     DECIMAL(10,2) = 0,
    @total         DECIMAL(10,2),
    @anticipo      DECIMAL(10,2) = 0,
    @estado        VARCHAR(20)   = 'PENDIENTE',
    @observaciones NVARCHAR(250) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @idHabitacion IS NOT NULL
       AND EXISTS (SELECT 1 FROM Reserva r
                   WHERE r.idHabitacion = @idHabitacion
                     AND r.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
                     AND @fechaEntrada < r.fechaSalida
                     AND r.fechaEntrada < @fechaSalida)
    BEGIN
        THROW 50001, N'La habitación ya está reservada en esas fechas (sobreventa).', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Reserva (idCliente, idTipo, idHabitacion, idCanal, idPromocion, idPaquete, idUsuario,
                             fechaEntrada, fechaSalida, numAdultos, numNinos,
                             tarifaNoche, descuento, total, anticipo, estado, observaciones)
        VALUES (@idCliente, @idTipo, @idHabitacion, @idCanal, @idPromocion, @idPaquete, @idUsuario,
                @fechaEntrada, @fechaSalida, @numAdultos, @numNinos,
                @tarifaNoche, @descuento, @total, @anticipo, @estado, @observaciones);

        DECLARE @idNueva INT = SCOPE_IDENTITY();

        INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
        VALUES (@idNueva, NULL, @estado, @idUsuario, N'Reserva creada');

        COMMIT TRANSACTION;

        -- Devuelve: idReserva, codigo
        SELECT idReserva, codigo FROM Reserva WHERE idReserva = @idNueva;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Reserva_ObtenerPorId
    @idReserva INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT r.idReserva, r.codigo, r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.idTipo, t.nombre AS tipo, r.idHabitacion, h.numero AS habitacion,
           r.idCanal, ca.nombre AS canal, r.idPromocion, r.idPaquete,
           r.fechaEntrada, r.fechaSalida, r.numAdultos, r.numNinos,
           r.tarifaNoche, r.descuento, r.total, r.anticipo, r.estado, r.observaciones, r.fechaCreacion
    FROM Reserva r
    JOIN Cliente c          ON c.idCliente = r.idCliente
    JOIN TipoHabitacion t   ON t.idTipo = r.idTipo
    JOIN CanalReserva ca    ON ca.idCanal = r.idCanal
    LEFT JOIN Habitacion h  ON h.idHabitacion = r.idHabitacion
    WHERE r.idReserva = @idReserva;
END
GO

CREATE PROCEDURE usp_Reserva_Listar
    @estado VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT r.idReserva, r.codigo, r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.idTipo, t.nombre AS tipo, r.idHabitacion, h.numero AS habitacion,
           r.idCanal, ca.nombre AS canal, r.idPromocion, r.idPaquete,
           r.fechaEntrada, r.fechaSalida, r.numAdultos, r.numNinos,
           r.tarifaNoche, r.descuento, r.total, r.anticipo, r.estado, r.observaciones, r.fechaCreacion
    FROM Reserva r
    JOIN Cliente c          ON c.idCliente = r.idCliente
    JOIN TipoHabitacion t   ON t.idTipo = r.idTipo
    JOIN CanalReserva ca    ON ca.idCanal = r.idCanal
    LEFT JOIN Habitacion h  ON h.idHabitacion = r.idHabitacion
    WHERE (@estado IS NULL OR r.estado = @estado)
    ORDER BY r.fechaEntrada DESC, r.idReserva DESC;
END
GO

CREATE PROCEDURE usp_Reserva_ListarPorCliente
    @idCliente INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT r.idReserva, r.codigo, r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.idTipo, t.nombre AS tipo, r.idHabitacion, h.numero AS habitacion,
           r.idCanal, ca.nombre AS canal, r.idPromocion, r.idPaquete,
           r.fechaEntrada, r.fechaSalida, r.numAdultos, r.numNinos,
           r.tarifaNoche, r.descuento, r.total, r.anticipo, r.estado, r.observaciones, r.fechaCreacion
    FROM Reserva r
    JOIN Cliente c          ON c.idCliente = r.idCliente
    JOIN TipoHabitacion t   ON t.idTipo = r.idTipo
    JOIN CanalReserva ca    ON ca.idCanal = r.idCanal
    LEFT JOIN Habitacion h  ON h.idHabitacion = r.idHabitacion
    WHERE r.idCliente = @idCliente
    ORDER BY r.fechaEntrada DESC, r.idReserva DESC;
END
GO

CREATE PROCEDURE usp_Reserva_Actualizar
    @idReserva     INT,
    @idHabitacion  INT           = NULL,
    @fechaEntrada  DATE,
    @fechaSalida   DATE,
    @numAdultos    INT,
    @numNinos      INT,
    @tarifaNoche   DECIMAL(10,2),
    @descuento     DECIMAL(10,2),
    @total         DECIMAL(10,2),
    @observaciones NVARCHAR(250) = NULL,
    @idUsuario     INT           = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @estadoActual VARCHAR(20) = (SELECT estado FROM Reserva WHERE idReserva = @idReserva);

    IF @estadoActual IS NULL
    BEGIN
        THROW 50003, N'La reserva no existe.', 1;
    END
    IF @estadoActual NOT IN ('PENDIENTE','CONFIRMADA')
    BEGIN
        THROW 50002, N'La reserva no se puede modificar en su estado actual.', 1;
    END
    IF @idHabitacion IS NOT NULL
       AND EXISTS (SELECT 1 FROM Reserva r
                   WHERE r.idHabitacion = @idHabitacion
                     AND r.idReserva <> @idReserva
                     AND r.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
                     AND @fechaEntrada < r.fechaSalida
                     AND r.fechaEntrada < @fechaSalida)
    BEGIN
        THROW 50001, N'La habitación ya está reservada en esas fechas (sobreventa).', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Reserva
        SET idHabitacion  = @idHabitacion,
            fechaEntrada  = @fechaEntrada,
            fechaSalida   = @fechaSalida,
            numAdultos    = @numAdultos,
            numNinos      = @numNinos,
            tarifaNoche   = @tarifaNoche,
            descuento     = @descuento,
            total         = @total,
            observaciones = @observaciones
        WHERE idReserva = @idReserva;

        INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
        VALUES (@idReserva, @estadoActual, @estadoActual, @idUsuario, N'Reserva modificada');

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Reserva_CambiarEstado
    @idReserva   INT,
    @estadoNuevo VARCHAR(20),
    @idUsuario   INT           = NULL,
    @detalle     NVARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @anterior VARCHAR(20) = (SELECT estado FROM Reserva WHERE idReserva = @idReserva);

    IF @anterior IS NULL
    BEGIN
        THROW 50003, N'La reserva no existe.', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Reserva SET estado = @estadoNuevo WHERE idReserva = @idReserva;

        INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
        VALUES (@idReserva, @anterior, @estadoNuevo, @idUsuario, @detalle);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Reserva_Historial
    @idReserva INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT h.idHistorial, h.estadoAnterior, h.estadoNuevo, h.fecha, u.username, h.detalle
    FROM HistorialReserva h
    LEFT JOIN Usuario u ON u.idUsuario = h.idUsuario
    WHERE h.idReserva = @idReserva
    ORDER BY h.fecha, h.idHistorial;
END
GO

/* ---------------------------------------------------------------------
   14.6 ESTADÍA: CHECK-IN / CHECK-OUT (atómicos), CONSUMOS, SOLICITUDES
   Errores: 50010 reserva no confirmada, 50011 habitación no disponible,
            50012 estadía no en curso, 50013 estadía no en curso (consumo)
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Estadia_CheckIn
    @idReserva    INT,
    @idHabitacion INT,
    @idUsuario    INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @estadoReserva VARCHAR(20) = (SELECT estado FROM Reserva WHERE idReserva = @idReserva);
    DECLARE @estadoHab VARCHAR(20) = (SELECT estado FROM Habitacion WHERE idHabitacion = @idHabitacion AND activa = 1);
    DECLARE @entrada DATE = (SELECT fechaEntrada FROM Reserva WHERE idReserva = @idReserva);
    DECLARE @salida DATE = (SELECT fechaSalida FROM Reserva WHERE idReserva = @idReserva);

    IF @estadoReserva IS NULL OR @estadoReserva <> 'CONFIRMADA'
    BEGIN
        THROW 50010, N'Solo se puede hacer check-in de una reserva CONFIRMADA.', 1;
    END
    IF @estadoHab IS NULL OR @estadoHab <> 'DISPONIBLE'
    BEGIN
        THROW 50011, N'La habitación no está disponible para el check-in.', 1;
    END
    IF EXISTS (SELECT 1 FROM Reserva r
               WHERE r.idHabitacion = @idHabitacion
                 AND r.idReserva <> @idReserva
                 AND r.estado IN ('PENDIENTE','CONFIRMADA','CHECKIN')
                 AND @entrada < r.fechaSalida
                 AND r.fechaEntrada < @salida)
    BEGIN
        THROW 50001, N'La habitación ya está reservada en esas fechas (sobreventa).', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Estadia (idReserva, idHabitacion, idUsuarioCheckIn)
        VALUES (@idReserva, @idHabitacion, @idUsuario);

        DECLARE @idNuevaEstadia INT = SCOPE_IDENTITY();

        UPDATE Reserva SET estado = 'CHECKIN', idHabitacion = @idHabitacion WHERE idReserva = @idReserva;
        UPDATE Habitacion SET estado = 'OCUPADA' WHERE idHabitacion = @idHabitacion;

        INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
        VALUES (@idReserva, 'CONFIRMADA', 'CHECKIN', @idUsuario, N'Check-in realizado');

        COMMIT TRANSACTION;

        -- Devuelve: idEstadia
        SELECT @idNuevaEstadia AS idEstadia;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Estadia_CheckOut
    @idEstadia INT,
    @idUsuario INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @estadoEstadia VARCHAR(15) = (SELECT estado FROM Estadia WHERE idEstadia = @idEstadia);
    DECLARE @idReserva INT = (SELECT idReserva FROM Estadia WHERE idEstadia = @idEstadia);
    DECLARE @idHabitacion INT = (SELECT idHabitacion FROM Estadia WHERE idEstadia = @idEstadia);

    IF @estadoEstadia IS NULL OR @estadoEstadia <> 'EN_CURSO'
    BEGIN
        THROW 50012, N'La estadía no está en curso.', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Estadia
        SET estado = 'FINALIZADA', fechaCheckOut = GETDATE(), idUsuarioCheckOut = @idUsuario
        WHERE idEstadia = @idEstadia;

        UPDATE Reserva SET estado = 'CHECKOUT' WHERE idReserva = @idReserva;
        UPDATE Habitacion SET estado = 'LIMPIEZA' WHERE idHabitacion = @idHabitacion;

        INSERT INTO TareaLimpieza (idHabitacion, tipo, estado)
        VALUES (@idHabitacion, 'CHECKOUT', 'PENDIENTE');

        INSERT INTO HistorialReserva (idReserva, estadoAnterior, estadoNuevo, idUsuario, detalle)
        VALUES (@idReserva, 'CHECKIN', 'CHECKOUT', @idUsuario, N'Check-out realizado');

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Estadia_ObtenerPorId
    @idEstadia INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.idEstadia, e.idReserva, r.codigo, e.idHabitacion, h.numero AS habitacion,
           e.fechaCheckIn, e.fechaCheckOut, e.estado,
           r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.fechaEntrada, r.fechaSalida, r.tarifaNoche, r.total
    FROM Estadia e
    JOIN Reserva r    ON r.idReserva = e.idReserva
    JOIN Cliente c    ON c.idCliente = r.idCliente
    JOIN Habitacion h ON h.idHabitacion = e.idHabitacion
    WHERE e.idEstadia = @idEstadia;
END
GO

CREATE PROCEDURE usp_Estadia_ObtenerPorReserva
    @idReserva INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.idEstadia, e.idReserva, r.codigo, e.idHabitacion, h.numero AS habitacion,
           e.fechaCheckIn, e.fechaCheckOut, e.estado,
           r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.fechaEntrada, r.fechaSalida, r.tarifaNoche, r.total
    FROM Estadia e
    JOIN Reserva r    ON r.idReserva = e.idReserva
    JOIN Cliente c    ON c.idCliente = r.idCliente
    JOIN Habitacion h ON h.idHabitacion = e.idHabitacion
    WHERE e.idReserva = @idReserva;
END
GO

CREATE PROCEDURE usp_Estadia_ListarEnCurso
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.idEstadia, e.idReserva, r.codigo, e.idHabitacion, h.numero AS habitacion,
           e.fechaCheckIn, e.fechaCheckOut, e.estado,
           r.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           r.fechaEntrada, r.fechaSalida, r.tarifaNoche, r.total
    FROM Estadia e
    JOIN Reserva r    ON r.idReserva = e.idReserva
    JOIN Cliente c    ON c.idCliente = r.idCliente
    JOIN Habitacion h ON h.idHabitacion = e.idHabitacion
    WHERE e.estado = 'EN_CURSO'
    ORDER BY h.numero;
END
GO

CREATE PROCEDURE usp_Estadia_ResumenCuenta
    @idEstadia INT
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: tarifaNoche, noches, totalAlojamiento, totalConsumos, anticipos
    SELECT r.tarifaNoche,
           DATEDIFF(DAY, r.fechaEntrada, r.fechaSalida) AS noches,
           r.total AS totalAlojamiento,
           ISNULL((SELECT SUM(cs.cantidad * cs.precioUnitario)
                   FROM ConsumoServicio cs WHERE cs.idEstadia = e.idEstadia), 0) AS totalConsumos,
           ISNULL((SELECT SUM(p.monto) FROM Pago p WHERE p.idReserva = r.idReserva), 0) AS anticipos
    FROM Estadia e
    JOIN Reserva r ON r.idReserva = e.idReserva
    WHERE e.idEstadia = @idEstadia;
END
GO

CREATE PROCEDURE usp_Consumo_Registrar
    @idEstadia  INT,
    @idServicio INT,
    @cantidad   INT,
    @idUsuario  INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM Estadia WHERE idEstadia = @idEstadia AND estado = 'EN_CURSO')
    BEGIN
        THROW 50013, N'Solo se pueden registrar consumos en una estadía en curso.', 1;
    END

    INSERT INTO ConsumoServicio (idEstadia, idServicio, cantidad, precioUnitario, idUsuario)
    SELECT @idEstadia, s.idServicio, @cantidad, s.precio, @idUsuario
    FROM ServicioAdicional s
    WHERE s.idServicio = @idServicio AND s.activo = 1;

    -- Devuelve: idConsumo (0 filas si el servicio no existe o está inactivo)
    SELECT SCOPE_IDENTITY() AS idConsumo WHERE @@ROWCOUNT > 0;
END
GO

CREATE PROCEDURE usp_Consumo_ListarPorEstadia
    @idEstadia INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT cs.idConsumo, s.idServicio, s.nombre AS servicio, s.area,
           cs.cantidad, cs.precioUnitario, cs.cantidad * cs.precioUnitario AS subtotal, cs.fecha
    FROM ConsumoServicio cs
    JOIN ServicioAdicional s ON s.idServicio = cs.idServicio
    WHERE cs.idEstadia = @idEstadia
    ORDER BY cs.fecha, cs.idConsumo;
END
GO

CREATE PROCEDURE usp_Solicitud_Registrar
    @idEstadia   INT,
    @descripcion NVARCHAR(250)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO SolicitudEspecial (idEstadia, descripcion) VALUES (@idEstadia, @descripcion);
END
GO

CREATE PROCEDURE usp_Solicitud_ListarPorEstadia
    @idEstadia INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idSolicitud, descripcion, estado, fecha
    FROM SolicitudEspecial
    WHERE idEstadia = @idEstadia
    ORDER BY fecha;
END
GO

CREATE PROCEDURE usp_Solicitud_CambiarEstado
    @idSolicitud INT,
    @estado      VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE SolicitudEspecial SET estado = @estado WHERE idSolicitud = @idSolicitud;
END
GO

/* ---------------------------------------------------------------------
   14.7 LIMPIEZA Y MANTENIMIENTO
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_TareaLimpieza_Listar
    @estado VARCHAR(15) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT t.idTarea, t.idHabitacion, h.numero AS habitacion, t.tipo, t.estado,
           t.idAsignado, u.username AS asignado, t.fechaCreacion, t.fechaFin
    FROM TareaLimpieza t
    JOIN Habitacion h  ON h.idHabitacion = t.idHabitacion
    LEFT JOIN Usuario u ON u.idUsuario = t.idAsignado
    WHERE (@estado IS NULL OR t.estado = @estado)
    ORDER BY t.fechaCreacion, t.idTarea;
END
GO

CREATE PROCEDURE usp_TareaLimpieza_Crear
    @idHabitacion INT,
    @tipo         VARCHAR(15) = 'DIARIA'
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO TareaLimpieza (idHabitacion, tipo) VALUES (@idHabitacion, @tipo);
END
GO

CREATE PROCEDURE usp_TareaLimpieza_Asignar
    @idTarea   INT,
    @idUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE TareaLimpieza SET idAsignado = @idUsuario WHERE idTarea = @idTarea;
END
GO

CREATE PROCEDURE usp_TareaLimpieza_CambiarEstado
    @idTarea INT,
    @estado  VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE TareaLimpieza
        SET estado = @estado,
            fechaFin = CASE WHEN @estado = 'TERMINADA' THEN GETDATE() ELSE fechaFin END
        WHERE idTarea = @idTarea;

        -- Al terminar la limpieza, la habitación vuelve a estar disponible
        IF @estado = 'TERMINADA'
        BEGIN
            UPDATE Habitacion
            SET estado = 'DISPONIBLE'
            WHERE estado = 'LIMPIEZA'
              AND idHabitacion = (SELECT idHabitacion FROM TareaLimpieza WHERE idTarea = @idTarea);
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Mantenimiento_Reportar
    @idHabitacion INT,
    @descripcion  NVARCHAR(250),
    @prioridad    VARCHAR(10) = 'MEDIA',
    @idUsuario    INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Mantenimiento (idHabitacion, descripcion, prioridad, idReporta)
        VALUES (@idHabitacion, @descripcion, @prioridad, @idUsuario);

        -- Una habitación ocupada o reservada no se bloquea automáticamente
        UPDATE Habitacion
        SET estado = 'MANTENIMIENTO'
        WHERE idHabitacion = @idHabitacion AND estado IN ('DISPONIBLE','LIMPIEZA');

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Mantenimiento_Listar
    @estado VARCHAR(15) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT m.idMantenimiento, m.idHabitacion, h.numero AS habitacion, m.descripcion,
           m.prioridad, m.estado, m.fechaReporte, m.fechaCierre, u.username AS reporta
    FROM Mantenimiento m
    JOIN Habitacion h   ON h.idHabitacion = m.idHabitacion
    LEFT JOIN Usuario u ON u.idUsuario = m.idReporta
    WHERE (@estado IS NULL OR m.estado = @estado)
    ORDER BY m.fechaReporte DESC, m.idMantenimiento DESC;
END
GO

CREATE PROCEDURE usp_Mantenimiento_Cerrar
    @idMantenimiento INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @idHabitacion INT = (SELECT idHabitacion FROM Mantenimiento WHERE idMantenimiento = @idMantenimiento);

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Mantenimiento
        SET estado = 'CERRADO', fechaCierre = GETDATE()
        WHERE idMantenimiento = @idMantenimiento;

        -- Si ya no quedan pendientes en esa habitación, vuelve a estar disponible
        IF NOT EXISTS (SELECT 1 FROM Mantenimiento
                       WHERE idHabitacion = @idHabitacion AND estado <> 'CERRADO')
        BEGIN
            UPDATE Habitacion SET estado = 'DISPONIBLE'
            WHERE idHabitacion = @idHabitacion AND estado = 'MANTENIMIENTO';
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

/* ---------------------------------------------------------------------
   14.8 FACTURACIÓN Y PAGOS
   Errores: 50020 factura pagada no anulable, 50021 factura no admite pagos
   El detalle de la factura se envía como XML:
   <detalle><item concepto="Alojamiento" cantidad="2" precioUnitario="180.00"/></detalle>
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Factura_Emitir
    @idEstadia       INT,
    @idCliente       INT,
    @idEmpresa       INT           = NULL,
    @tipoComprobante VARCHAR(10)   = 'BOLETA',
    @serie           CHAR(4),
    @subtotal        DECIMAL(10,2),
    @igv             DECIMAL(10,2),
    @total           DECIMAL(10,2),
    @idUsuario       INT           = NULL,
    @detalle         XML
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @numero INT = ISNULL((SELECT MAX(numero) FROM Factura WITH (UPDLOCK, HOLDLOCK)
                                      WHERE serie = @serie), 0) + 1;

        INSERT INTO Factura (idEstadia, idCliente, idEmpresa, tipoComprobante, serie, numero,
                             subtotal, igv, total, idUsuario)
        VALUES (@idEstadia, @idCliente, @idEmpresa, @tipoComprobante, @serie, @numero,
                @subtotal, @igv, @total, @idUsuario);

        DECLARE @idNuevaFactura INT = SCOPE_IDENTITY();

        INSERT INTO DetalleFactura (idFactura, concepto, cantidad, precioUnitario)
        SELECT @idNuevaFactura,
               x.i.value('@concepto', 'NVARCHAR(120)'),
               x.i.value('@cantidad', 'INT'),
               x.i.value('@precioUnitario', 'DECIMAL(10,2)')
        FROM @detalle.nodes('/detalle/item') AS x(i);

        COMMIT TRANSACTION;

        -- Devuelve: idFactura, serie, numero
        SELECT @idNuevaFactura AS idFactura, @serie AS serie, @numero AS numero;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Factura_ObtenerPorId
    @idFactura INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT f.idFactura, f.idEstadia, f.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           f.idEmpresa, e.razonSocial AS empresa, e.ruc,
           f.tipoComprobante, f.serie, f.numero, f.subtotal, f.igv, f.total, f.fechaEmision, f.estado
    FROM Factura f
    JOIN Cliente c      ON c.idCliente = f.idCliente
    LEFT JOIN Empresa e ON e.idEmpresa = f.idEmpresa
    WHERE f.idFactura = @idFactura;
END
GO

CREATE PROCEDURE usp_Factura_Listar
    @estado VARCHAR(10) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT f.idFactura, f.idEstadia, f.idCliente, c.nombres + N' ' + c.apellidos AS cliente, c.documento,
           f.idEmpresa, e.razonSocial AS empresa, e.ruc,
           f.tipoComprobante, f.serie, f.numero, f.subtotal, f.igv, f.total, f.fechaEmision, f.estado
    FROM Factura f
    JOIN Cliente c      ON c.idCliente = f.idCliente
    LEFT JOIN Empresa e ON e.idEmpresa = f.idEmpresa
    WHERE (@estado IS NULL OR f.estado = @estado)
    ORDER BY f.fechaEmision DESC, f.idFactura DESC;
END
GO

CREATE PROCEDURE usp_Factura_ListarPorEstadia
    @idEstadia INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT f.idFactura, f.tipoComprobante, f.serie, f.numero, f.total, f.fechaEmision, f.estado
    FROM Factura f
    WHERE f.idEstadia = @idEstadia
    ORDER BY f.fechaEmision;
END
GO

CREATE PROCEDURE usp_DetalleFactura_ListarPorFactura
    @idFactura INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idDetalle, concepto, cantidad, precioUnitario, subtotal
    FROM DetalleFactura
    WHERE idFactura = @idFactura
    ORDER BY idDetalle;
END
GO

CREATE PROCEDURE usp_Factura_Anular
    @idFactura INT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM Factura WHERE idFactura = @idFactura AND estado = 'PAGADA')
    BEGIN
        THROW 50020, N'No se puede anular una factura pagada.', 1;
    END

    UPDATE Factura SET estado = 'ANULADA' WHERE idFactura = @idFactura;
END
GO

CREATE PROCEDURE usp_Pago_Registrar
    @idReserva  INT           = NULL,
    @idFactura  INT           = NULL,
    @metodo     VARCHAR(15),
    @monto      DECIMAL(10,2),
    @referencia NVARCHAR(60)  = NULL,
    @idUsuario  INT           = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @idFactura IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM Factura WHERE idFactura = @idFactura AND estado = 'EMITIDA')
    BEGIN
        THROW 50021, N'La factura no admite pagos (no existe, ya está pagada o está anulada).', 1;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Pago (idReserva, idFactura, metodo, monto, referencia, idUsuario)
        VALUES (@idReserva, @idFactura, @metodo, @monto, @referencia, @idUsuario);

        DECLARE @idNuevoPago INT = SCOPE_IDENTITY();

        -- Anticipos: Reserva.anticipo refleja lo pagado a la reserva
        IF @idReserva IS NOT NULL
        BEGIN
            UPDATE Reserva
            SET anticipo = (SELECT SUM(monto) FROM Pago WHERE idReserva = @idReserva)
            WHERE idReserva = @idReserva;
        END

        -- Cuando lo pagado (pagos de la factura + anticipos de la reserva) alcanza el total,
        -- la factura queda PAGADA
        IF @idFactura IS NOT NULL
           AND ( ISNULL((SELECT SUM(monto) FROM Pago WHERE idFactura = @idFactura), 0)
               + ISNULL((SELECT SUM(p.monto)
                         FROM Pago p
                         JOIN Estadia e ON e.idReserva = p.idReserva
                         JOIN Factura f ON f.idEstadia = e.idEstadia
                         WHERE f.idFactura = @idFactura AND p.idFactura IS NULL), 0) )
               >= (SELECT total FROM Factura WHERE idFactura = @idFactura)
        BEGIN
            UPDATE Factura SET estado = 'PAGADA' WHERE idFactura = @idFactura;
        END

        COMMIT TRANSACTION;

        -- Devuelve: idPago
        SELECT @idNuevoPago AS idPago;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

CREATE PROCEDURE usp_Pago_ListarPorFactura
    @idFactura INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idPago, metodo, monto, referencia, fecha
    FROM Pago
    WHERE idFactura = @idFactura
    ORDER BY fecha, idPago;
END
GO

CREATE PROCEDURE usp_Pago_ListarPorReserva
    @idReserva INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT idPago, metodo, monto, referencia, fecha
    FROM Pago
    WHERE idReserva = @idReserva
    ORDER BY fecha, idPago;
END
GO

/* ---------------------------------------------------------------------
   14.9 SATISFACCIÓN
   Error: 50030 la estadía no ha finalizado
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Encuesta_Registrar
    @idEstadia  INT,
    @puntaje    INT,
    @comentario NVARCHAR(300) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM Estadia WHERE idEstadia = @idEstadia AND estado = 'FINALIZADA')
    BEGIN
        THROW 50030, N'Solo se puede evaluar una estadía finalizada.', 1;
    END

    INSERT INTO EncuestaSatisfaccion (idEstadia, puntaje, comentario)
    VALUES (@idEstadia, @puntaje, @comentario);
END
GO

/* ---------------------------------------------------------------------
   14.10 REPORTES GERENCIALES
   --------------------------------------------------------------------- */
CREATE PROCEDURE usp_Reporte_OcupacionActual
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: estado, cantidad, porcentaje
    SELECT estado,
           COUNT(*) AS cantidad,
           CAST(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM Habitacion WHERE activa = 1), 0)
                AS DECIMAL(5,2)) AS porcentaje
    FROM Habitacion
    WHERE activa = 1
    GROUP BY estado
    ORDER BY estado;
END
GO

CREATE PROCEDURE usp_Reporte_IngresosPorTipo
    @desde DATE = NULL,
    @hasta DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: tipo, facturas, ingresos (solo facturas PAGADAS)
    SELECT t.nombre AS tipo,
           COUNT(f.idFactura) AS facturas,
           ISNULL(SUM(f.total), 0) AS ingresos
    FROM TipoHabitacion t
    LEFT JOIN Habitacion h ON h.idTipo = t.idTipo
    LEFT JOIN Estadia e    ON e.idHabitacion = h.idHabitacion
    LEFT JOIN Factura f    ON f.idEstadia = e.idEstadia
                          AND f.estado = 'PAGADA'
                          AND (@desde IS NULL OR CAST(f.fechaEmision AS DATE) >= @desde)
                          AND (@hasta IS NULL OR CAST(f.fechaEmision AS DATE) <= @hasta)
    GROUP BY t.nombre
    ORDER BY ingresos DESC, t.nombre;
END
GO

CREATE PROCEDURE usp_Reporte_DemandaPorMes
    @anio INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: anio, mes, reservas, noches
    SELECT YEAR(fechaEntrada) AS anio,
           MONTH(fechaEntrada) AS mes,
           COUNT(*) AS reservas,
           SUM(DATEDIFF(DAY, fechaEntrada, fechaSalida)) AS noches
    FROM Reserva
    WHERE estado <> 'CANCELADA'
      AND (@anio IS NULL OR YEAR(fechaEntrada) = @anio)
    GROUP BY YEAR(fechaEntrada), MONTH(fechaEntrada)
    ORDER BY anio, mes;
END
GO

CREATE PROCEDURE usp_Reporte_ServiciosMasConsumidos
    @desde DATE = NULL,
    @hasta DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: servicio, area, unidades, ingresos
    SELECT s.nombre AS servicio,
           s.area,
           SUM(c.cantidad) AS unidades,
           SUM(c.cantidad * c.precioUnitario) AS ingresos
    FROM ConsumoServicio c
    JOIN ServicioAdicional s ON s.idServicio = c.idServicio
    WHERE (@desde IS NULL OR CAST(c.fecha AS DATE) >= @desde)
      AND (@hasta IS NULL OR CAST(c.fecha AS DATE) <= @hasta)
    GROUP BY s.nombre, s.area
    ORDER BY unidades DESC, s.nombre;
END
GO

CREATE PROCEDURE usp_Reporte_SatisfaccionPorTipo
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: tipo, encuestas, promedio
    SELECT t.nombre AS tipo,
           COUNT(q.idEncuesta) AS encuestas,
           CAST(AVG(CAST(q.puntaje AS DECIMAL(4,2))) AS DECIMAL(4,2)) AS promedio
    FROM EncuestaSatisfaccion q
    JOIN Estadia e        ON e.idEstadia = q.idEstadia
    JOIN Habitacion h     ON h.idHabitacion = e.idHabitacion
    JOIN TipoHabitacion t ON t.idTipo = h.idTipo
    GROUP BY t.nombre
    ORDER BY promedio DESC;
END
GO

CREATE PROCEDURE usp_Reporte_ReservasPorCanal
    @desde DATE = NULL,
    @hasta DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: canal, reservas, total
    SELECT ca.nombre AS canal,
           COUNT(r.idReserva) AS reservas,
           ISNULL(SUM(r.total), 0) AS total
    FROM CanalReserva ca
    LEFT JOIN Reserva r ON r.idCanal = ca.idCanal
                       AND r.estado <> 'CANCELADA'
                       AND (@desde IS NULL OR r.fechaEntrada >= @desde)
                       AND (@hasta IS NULL OR r.fechaEntrada <= @hasta)
    GROUP BY ca.nombre
    ORDER BY reservas DESC, ca.nombre;
END
GO

CREATE PROCEDURE usp_Reporte_ReservasPorEstado
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: estado, cantidad
    SELECT estado, COUNT(*) AS cantidad
    FROM Reserva
    GROUP BY estado
    ORDER BY estado;
END
GO

CREATE PROCEDURE usp_Reporte_ClientesFrecuentes
    @top INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    -- Devuelve: documento, cliente, reservas, puntos, nivel
    SELECT TOP (@top)
           c.documento,
           c.nombres + N' ' + c.apellidos AS cliente,
           COUNT(r.idReserva) AS reservas,
           c.puntos,
           ISNULL(n.nombre, N'Sin nivel') AS nivel
    FROM Cliente c
    JOIN Reserva r ON r.idCliente = c.idCliente
                  AND r.estado IN ('CONFIRMADA','CHECKIN','CHECKOUT')
    LEFT JOIN NivelFidelidad n ON n.idNivel = c.idNivel
    GROUP BY c.documento, c.nombres, c.apellidos, c.puntos, n.nombre
    ORDER BY reservas DESC, c.puntos DESC;
END
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

PRINT 'HotelDB creada correctamente (tablas, datos, trigger y procedimientos almacenados).';
GO
