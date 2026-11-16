-- ============================================================================
-- PRUEBAS DE TRANSFERENCIAS (Persona 2)  -  Base: banco_udb
-- NO hace falta crear tablas nuevas: se usan cuenta_bancaria y movimiento de Banco.sql.
-- IMPORTANTE: antes de probar, ejecute el Banco.sql del repositorio (recrea las tablas con
-- los datos de prueba). Si ejecutó el script anterior de "movimientos", Banco.sql lo reemplaza.
-- ============================================================================
USE banco_udb;

-- Cuentas de Banco.sql que se usan en las pruebas:
--   100000000002  Corriente  1525.50  Activa
--   100000000003  Ahorro      250.75  Activa
--   100000000005  Ahorro       57.99  Activa
--   100000000007  Ahorro      199.00  Activa
--   100000000001  Ahorro      500.00  BLOQUEADA
--   100000000008  Ahorro      300.00  INACTIVA

-- ---------------------------------------------------------------------------
-- CASOS (se hacen desde la pestaña "Transferir" del formulario TransferenciasForm)
-- ---------------------------------------------------------------------------
-- # | Origen        | Destino       | Monto   | Resultado esperado
-- 1 | 100000000002  | 100000000003  | 25.00   | OK  -> 1525.50 baja a 1500.50 y 250.75 sube a 275.75 (2 movimientos)
-- 2 | 100000000005  | 100000000003  | 100     | RECHAZA: "Fondos insuficientes: saldo disponible 57.99..."
-- 3 | 100000000002  | 100000000003  | 0 o -5  | RECHAZA: "El monto ... no es válido"
-- 4 | 100000000002  | 999999999999  | 10      | RECHAZA: "No existe ninguna cuenta con el número 999999999999"
-- 5 | 999999999999  | 100000000003  | 10      | RECHAZA: "No existe ninguna cuenta ..."
-- 6 | 100000000001  | 100000000003  | 10      | RECHAZA: la cuenta 100000000001 se encuentra Bloqueada
-- 7 | 100000000002  | 100000000008  | 10      | RECHAZA: la cuenta 100000000008 se encuentra Inactiva
-- 8 | 100000000002  | 100000000002  | 10      | RECHAZA: origen y destino no pueden ser la misma
-- 9 | 100000000002  | 100000000003  | 10.555  | RECHAZA: máximo 2 decimales
-- En TODOS los rechazos los saldos deben quedar igual y NO debe aparecer ningún movimiento nuevo.

-- ---------------------------------------------------------------------------
-- VERIFICACIONES (ejecutar en MySQL Workbench después de cada prueba)
-- ---------------------------------------------------------------------------

-- Saldos actuales de las cuentas de prueba
SELECT numero_cuenta, tipo_cuenta, saldo, estado
FROM cuenta_bancaria
WHERE numero_cuenta IN ('100000000002','100000000003','100000000005','100000000007')
ORDER BY numero_cuenta;

-- Últimos movimientos (después del caso 1 deben verse 2 filas "Transferencia")
SELECT m.id_movimiento, c.numero_cuenta, m.tipo_movimiento, m.monto, m.saldo_resultante,
       m.fecha_hora, m.descripcion
FROM movimiento m
JOIN cuenta_bancaria c ON c.id_cuenta = m.id_cuenta
ORDER BY m.id_movimiento DESC
LIMIT 10;

-- Control de consistencia: el saldo resultante del último movimiento de cada cuenta
-- debe coincidir con su saldo actual (las filas que aparezcan aquí tienen un problema)
SELECT c.numero_cuenta, c.saldo AS saldo_cuenta, m.saldo_resultante AS ultimo_movimiento
FROM cuenta_bancaria c
JOIN movimiento m ON m.id_movimiento = (SELECT MAX(id_movimiento) FROM movimiento WHERE id_cuenta = c.id_cuenta)
WHERE c.saldo <> m.saldo_resultante;

-- Volver a empezar: ejecute de nuevo el Banco.sql del repositorio.
