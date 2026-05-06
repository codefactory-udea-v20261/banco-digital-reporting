DROP TRIGGER IF EXISTS trg_auditoria_transaccion ON transaccion;
DROP TRIGGER IF EXISTS trg_auditoria_cuenta ON cuenta;
DROP TRIGGER IF EXISTS trg_auditoria_cliente ON cliente;

DROP FUNCTION IF EXISTS fn_auditar_cambios();

DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS token_revocado;
DROP TABLE IF EXISTS usuario_rol;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;
