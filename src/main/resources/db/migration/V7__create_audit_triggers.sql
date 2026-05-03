-- ════════════════════════════════════════════════════════════
-- V7: Trigger de auditoría automática
-- Propósito: Registrar automáticamente en la tabla 'auditoria'
-- cualquier INSERT, UPDATE o DELETE sobre las tablas clave
-- del sistema (cuenta, cliente, transaccion).
-- ════════════════════════════════════════════════════════════

-- ── Función del trigger ─────────────────────────────────────
CREATE OR REPLACE FUNCTION fn_auditar_cambios()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria (tabla, operacion, registro_id, datos_antes, datos_despues, usuario_bd)
        VALUES (TG_TABLE_NAME, 'INSERT', NEW.id::VARCHAR, NULL, to_jsonb(NEW), CURRENT_USER);
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO auditoria (tabla, operacion, registro_id, datos_antes, datos_despues, usuario_bd)
        VALUES (TG_TABLE_NAME, 'UPDATE', NEW.id::VARCHAR, to_jsonb(OLD), to_jsonb(NEW), CURRENT_USER);
        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO auditoria (tabla, operacion, registro_id, datos_antes, datos_despues, usuario_bd)
        VALUES (TG_TABLE_NAME, 'DELETE', OLD.id::VARCHAR, to_jsonb(OLD), NULL, CURRENT_USER);
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- ── Triggers sobre tabla CUENTA ─────────────────────────────
CREATE OR REPLACE TRIGGER trg_auditoria_cuenta
    AFTER INSERT OR UPDATE OR DELETE ON cuenta
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_cambios();

-- ── Triggers sobre tabla CLIENTE ────────────────────────────
CREATE OR REPLACE TRIGGER trg_auditoria_cliente
    AFTER INSERT OR UPDATE OR DELETE ON cliente
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_cambios();

-- ── Triggers sobre tabla TRANSACCION ────────────────────────
CREATE OR REPLACE TRIGGER trg_auditoria_transaccion
    AFTER INSERT ON transaccion
    FOR EACH ROW EXECUTE FUNCTION fn_auditar_cambios();

-- ════════════════════════════════════════════════════════════
-- Función auxiliar: Resumen de movimientos por período
-- Uso: SELECT * FROM resumen_movimientos_cuenta(id, '2026-01-01', '2026-12-31');
-- ════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION resumen_movimientos_cuenta(
    p_cuenta_id UUID,
    p_fecha_inicio DATE,
    p_fecha_fin DATE
)
RETURNS TABLE (
    total_depositos     NUMERIC,
    total_retiros       NUMERIC,
    cantidad_operaciones BIGINT,
    saldo_actual        NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(SUM(CASE WHEN tt.nombre IN ('DEPOSITO','TRANSFERENCIA_CREDITO') THEN t.monto ELSE 0 END), 0) AS total_depositos,
        COALESCE(SUM(CASE WHEN tt.nombre IN ('RETIRO','TRANSFERENCIA_DEBITO')    THEN t.monto ELSE 0 END), 0) AS total_retiros,
        COUNT(*)::BIGINT AS cantidad_operaciones,
        (SELECT c.saldo FROM cuenta c WHERE c.id = p_cuenta_id) AS saldo_actual
    FROM transaccion t
    JOIN tipo_transaccion tt ON t.tipo_id = tt.id
    WHERE (t.cuenta_origen_id = p_cuenta_id OR t.cuenta_destino_id = p_cuenta_id)
      AND t.created_at >= p_fecha_inicio::TIMESTAMPTZ
      AND t.created_at < (p_fecha_fin + INTERVAL '1 day')::TIMESTAMPTZ;
END;
$$ LANGUAGE plpgsql;
