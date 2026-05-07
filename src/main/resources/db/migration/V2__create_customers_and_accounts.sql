-- ════════════════════════════════════════════════════════════
-- V2: Read-model tables for customers and accounts.
-- Constraints are intentionally relaxed compared to Core's schema:
-- this is an eventually-consistent read model, so foreign keys and
-- unique constraints that fight reordered or partial events are dropped.
-- ════════════════════════════════════════════════════════════

-- ── Tabla: cliente ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cliente (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_cedula   VARCHAR(20),
    primer_nombre   VARCHAR(100),
    segundo_nombre  VARCHAR(100),
    primer_apellido VARCHAR(100),
    segundo_apellido VARCHAR(100),
    email           VARCHAR(255),
    telefono        VARCHAR(20),
    fecha_nacimiento DATE,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_cliente_email   ON cliente (email);
CREATE INDEX IF NOT EXISTS idx_cliente_cedula  ON cliente (numero_cedula);
CREATE INDEX IF NOT EXISTS idx_cliente_activo  ON cliente (activo) WHERE activo = TRUE;

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
-- No FK on cliente_id: events may arrive in any order, so the cuenta
-- row can land before its owner cliente row. Filtering by cliente_id
-- still works once both have been materialised.
CREATE TABLE IF NOT EXISTS cuenta (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_cuenta   VARCHAR(20),
    cliente_id      UUID,
    tipo_cuenta_id  SMALLINT        REFERENCES tipo_cuenta(id),
    saldo           NUMERIC(18, 2)  NOT NULL DEFAULT 0.00,
    estado          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura  DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id ON cuenta (cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_estado     ON cuenta (estado);
CREATE INDEX IF NOT EXISTS idx_cuenta_numero     ON cuenta (numero_cuenta);

-- ── Grants ───────────────────────────────────────────────────
GRANT SELECT ON cliente, cuenta, tipo_cuenta TO app_readonly;
GRANT SELECT, INSERT, UPDATE ON cliente, cuenta TO app_user;
GRANT SELECT ON tipo_cuenta TO app_user;
