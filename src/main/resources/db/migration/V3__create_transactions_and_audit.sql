-- ════════════════════════════════════════════════════════════
-- V3: Read-model tables for transactions.
-- FKs to cuenta are dropped: transaction events can arrive before
-- the corresponding AccountOpened event materialises the cuenta row.
-- saldo_anterior / saldo_posterior are nullable because TransactionCompletedEvent
-- does not carry pre/post balances; the read model can populate them later
-- if a richer event schema is introduced.
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
    cuenta_origen_id    UUID,
    cuenta_destino_id   UUID,
    tipo_id             SMALLINT        REFERENCES tipo_transaccion(id),
    monto               NUMERIC(18, 2),
    saldo_anterior      NUMERIC(18, 2),
    saldo_posterior     NUMERIC(18, 2),
    descripcion         VARCHAR(255),
    referencia          VARCHAR(50),
    estado              VARCHAR(20)     NOT NULL DEFAULT 'COMPLETADA',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX IF NOT EXISTS idx_trans_cuenta_origen  ON transaccion (cuenta_origen_id);
CREATE INDEX IF NOT EXISTS idx_trans_cuenta_destino ON transaccion (cuenta_destino_id);
CREATE INDEX IF NOT EXISTS idx_trans_created_at     ON transaccion (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_trans_tipo           ON transaccion (tipo_id);

GRANT SELECT, INSERT ON transaccion TO app_user;
