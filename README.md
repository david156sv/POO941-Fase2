# POO941-Fase2

Sistema bancario de escritorio con Java Swing, NetBeans, JDBC y MySQL.

Incluye gestión de clientes y cuentas, depósitos, retiros, transferencias, consulta de movimientos y ocho reportes financieros y de cuentas.

## Reportes — Persona 3

Abrir **Reportes > Reportes financieros y de cuentas** desde Gestión de Clientes.

El botón **Ver PDF** genera el archivo y lo abre automáticamente con la aplicación asociada de Windows. Los archivos se guardan con nombres únicos en `BancoUDB/Reportes` dentro de la carpeta de usuario.

El formulario se puede editar visualmente desde `ReportesForm.java` y `ReportesForm.form` en NetBeans. La generación de PDF usa OpenPDF 1.3.43, incluido en `lib` y `dist/lib`.

## Base de datos

La configuración de conexión está en `ConexionDB`: localhost:3306, base `banco_udb`, usuario `root` y contraseña vacía.

`actualizar_reportes.sql` agrega la tabla `movimiento` si falta y conserva los datos existentes. `Banco.sql` crea la base con datos de ejemplo y elimina y recrea las tablas.
