-- ════════════════════════════════════════════════════════════
-- V3: Tablas de transacciones y auditoría
-- Módulos: transactions, audit
-- ════════════════════════════════════════════════════════════

-- ── Tabla: tipo_transaccion ──────────────────────────────────
CREATE TABLE IF NOT EXISTS tipo_transaccion (
    id      SMALLINT    PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO tipo_transaccion (id, nombre) VALUES
    (1, 'DEPOSITO'),
    (2, 'RETIRO'),
    (3, 'TRANSFERENCIA_DEBITO'),
    (4, 'TRANSFERENCIA_CREDITO')
ON CONFLICT DO NOTHING;

-- ── Tabla: transaccion ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS transaccion (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_origen_id    UUID            REFERENCES cuenta(id),
    cuenta_destino_id   UUID            REFERENCES cuenta(id),
    tipo_id             SMALLINT        NOT NULL REFERENCES tipo_transaccion(id),
    monto               NUMERIC(18, 2)  NOT NULL CHECK (monto > 0),
    saldo_anterior      NUMERIC(18, 2)  NOT NULL,
    saldo_posterior     NUMERIC(18, 2)  NOT NULL,
    descripcion         VARCHAR(255),
    referencia          VARCHAR(50)     UNIQUE,
    estado              VARCHAR(20)     NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA','FALLIDA','REVERTIDA')),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

COMMENT ON TABLE transaccion IS 'Registro inmutable de operaciones — solo INSERT permitido';
COMMENT ON COLUMN transaccion.saldo_anterior IS 'Snapshot de saldo para trazabilidad total sin recálculo';

CREATE INDEX IF NOT EXISTS idx_trans_cuenta_origen  ON transaccion (cuenta_origen_id);
CREATE INDEX IF NOT EXISTS idx_trans_cuenta_destino ON transaccion (cuenta_destino_id);
CREATE INDEX IF NOT EXISTS idx_trans_created_at     ON transaccion (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_trans_tipo           ON transaccion (tipo_id);

-- ── Tabla: auditoria ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auditoria (
    id          BIGSERIAL       PRIMARY KEY,
    tabla       VARCHAR(50)     NOT NULL,
    operacion   VARCHAR(10)     NOT NULL CHECK (operacion IN ('INSERT','UPDATE','DELETE')),
    registro_id VARCHAR(100)    NOT NULL,
    datos_antes JSONB,
    datos_despues JSONB,
    usuario_bd  VARCHAR(100)    NOT NULL DEFAULT CURRENT_USER,
    ip_origen   VARCHAR(45),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auditoria_tabla      ON auditoria (tabla);
CREATE INDEX IF NOT EXISTS idx_auditoria_registro   ON auditoria (registro_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_created_at ON auditoria (created_at DESC);

GRANT SELECT, INSERT ON auditoria TO app_user;
GRANT USAGE, SELECT ON SEQUENCE auditoria_id_seq TO app_user;
GRANT SELECT, INSERT ON transaccion TO app_user;
