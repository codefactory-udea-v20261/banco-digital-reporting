-- ════════════════════════════════════════════════════════════
-- V6: Función/Procedimiento almacenado
-- Propósito: Calcular el saldo total de todas las cuentas activas de un cliente.
-- ════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION obtener_saldo_total_cliente(p_cliente_id UUID)
RETURNS NUMERIC AS $$
DECLARE
    v_saldo_total NUMERIC := 0;
BEGIN
    SELECT COALESCE(SUM(saldo), 0) INTO v_saldo_total
    FROM cuenta
    WHERE cliente_id = p_cliente_id
      AND estado = 'ACTIVA';
      
    RETURN v_saldo_total;
END;
$$ LANGUAGE plpgsql;
