-- ════════════════════════════════════════════════════════════
-- V2: Tablas cliente y cuenta (DDL inicial)
-- Módulos: customers, accounts
-- ════════════════════════════════════════════════════════════

-- ── Tabla: cliente ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cliente (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_cedula   VARCHAR(20)     NOT NULL UNIQUE,
    primer_nombre   VARCHAR(100)    NOT NULL,
    segundo_nombre  VARCHAR(100),
    primer_apellido VARCHAR(100)    NOT NULL,
    segundo_apellido VARCHAR(100),
    email           VARCHAR(255)    NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    fecha_nacimiento DATE           NOT NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

COMMENT ON TABLE cliente IS 'Registro de clientes del banco digital';
COMMENT ON COLUMN cliente.numero_cedula IS 'Campo inmutable — no puede actualizarse vía API';

CREATE INDEX IF NOT EXISTS idx_cliente_email       ON cliente (email);
CREATE INDEX IF NOT EXISTS idx_cliente_cedula      ON cliente (numero_cedula);
CREATE INDEX IF NOT EXISTS idx_cliente_activo      ON cliente (activo) WHERE activo = TRUE;

-- ── Tabla: tipo_cuenta ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS tipo_cuenta (
    id          SMALLINT    PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);

INSERT INTO tipo_cuenta (id, nombre, descripcion) VALUES
    (1, 'AHORRO',   'Cuenta de ahorros estándar'),
    (2, 'CORRIENTE','Cuenta corriente empresarial')
ON CONFLICT DO NOTHING;

-- ── Tabla: cuenta ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cuenta (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_cuenta   VARCHAR(20)     NOT NULL UNIQUE,
    cliente_id      UUID            NOT NULL REFERENCES cliente(id),
    tipo_cuenta_id  SMALLINT        NOT NULL REFERENCES tipo_cuenta(id),
    saldo           NUMERIC(18, 2)  NOT NULL DEFAULT 0.00 CHECK (saldo >= 0),
    estado          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA','BLOQUEADA')),
    fecha_apertura  DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

COMMENT ON COLUMN cuenta.saldo IS 'Saldo transaccional — se gestiona en la capa de aplicación';

CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id   ON cuenta (cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_estado       ON cuenta (estado);
CREATE INDEX IF NOT EXISTS idx_cuenta_numero       ON cuenta (numero_cuenta);

-- ── Grants (Principio de Menor Privilegio) ───────────────────
GRANT SELECT ON cliente, cuenta, tipo_cuenta TO app_readonly;
GRANT SELECT, INSERT, UPDATE ON cliente, cuenta TO app_user;
GRANT SELECT ON tipo_cuenta TO app_user;
