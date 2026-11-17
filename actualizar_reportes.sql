-- Actualizacion NO destructiva para instalaciones que aun no tienen movimiento.
-- Conserva clientes, cuentas, saldos y movimientos existentes.
USE banco_udb;
CREATE TABLE IF NOT EXISTS movimiento (
  id_movimiento INT NOT NULL AUTO_INCREMENT,
  id_cuenta INT NOT NULL,
  tipo_movimiento ENUM('Depósito','Retiro','Transferencia') NOT NULL,
  monto DECIMAL(12,2) NOT NULL,
  fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descripcion VARCHAR(200) DEFAULT NULL,
  saldo_resultante DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_movimiento),
  KEY idx_movimiento_cuenta_fecha (id_cuenta,fecha_hora),
  CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (id_cuenta)
    REFERENCES cuenta_bancaria(id_cuenta) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
