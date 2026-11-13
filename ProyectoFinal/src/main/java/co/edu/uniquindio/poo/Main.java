package co.edu.uniquindio.poo;

import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.repositorio.TransaccionRepositorio;
import co.edu.uniquindio.poo.servicio.AlertasServicio;
import co.edu.uniquindio.poo.servicio.AnalisisServicio;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.servicio.ProgramadasServicio;
import co.edu.uniquindio.poo.servicio.PuntosServicio;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;

/**
 * Demo de consola de la Fase 4: programadas, atípicas, análisis de gasto y alertas.
 * Es solo para verificar los servicios; en la Fase 5B se reemplaza por la aplicación JavaFX.
 */
public class Main {

    public static void main(String[] args) throws MonederoException {
        // ---- Creación de objetos: cada servicio recibe sus dependencias por constructor ----
        Reloj reloj = new Reloj();
        ClienteRepositorio clientes = new ClienteRepositorio();
        TransaccionRepositorio transacciones = new TransaccionRepositorio();
        AlertasServicio alertas = new AlertasServicio(clientes, reloj);
        AnalisisServicio analisis = new AnalisisServicio(clientes, alertas);
        PuntosServicio puntos = new PuntosServicio(clientes, reloj);
        MonederoServicio monederos = new MonederoServicio(clientes, transacciones, puntos, analisis, alertas, reloj);
        ProgramadasServicio programadas = new ProgramadasServicio(clientes, monederos, alertas, reloj);

        titulo("1. Clientes y monederos (hoy es " + reloj.hoy() + ")");
        monederos.registrarCliente("1001", "Ana Gómez", "ana@correo.com", "3001112233", "clave1");
        monederos.registrarCliente("2002", "Luis Pérez", "luis@correo.com", "3004445566", "clave2");
        Monedero anaAhorros = monederos.crearMonedero("1001", "Ahorros", TipoMonedero.AHORROS);
        Monedero anaGastos = monederos.crearMonedero("1001", "Gastos", TipoMonedero.GASTOS_DIARIOS);
        Monedero luisGastos = monederos.crearMonedero("2002", "Gastos", TipoMonedero.GASTOS_DIARIOS);
        System.out.println(anaAhorros + "\n" + anaGastos + "\n" + luisGastos);

        titulo("2. Detección de atípicas");
        System.out.println("Ana deposita 100, 200, 100, 200, 100 (promedio 140, desviación ≈ 48,99, umbral ≈ 237,98)");
        for (String monto : new String[]{"100", "200", "100", "200", "100"}) {
            monederos.depositar(anaAhorros.getId(), new BigDecimal(monto), "Depósito");
        }
        Transaccion atipica = monederos.depositar(anaAhorros.getId(), new BigDecimal("300"), "Depósito grande");
        System.out.println("Depósito de 300 -> " + atipica);
        System.out.println("¿Sospechosa? " + atipica.isSospechosa());

        titulo("3. Gastos y alerta de saldo bajo");
        monederos.depositar(luisGastos.getId(), new BigDecimal("1000"), "Sueldo");
        monederos.retirar(anaAhorros.getId(), new BigDecimal("150"), Categoria.COMIDA, "Mercado");
        monederos.transferir(anaAhorros.getId(), luisGastos.getId(), new BigDecimal("200"), Categoria.TRANSPORTE, "Pago viaje");
        monederos.retirar(anaAhorros.getId(), new BigDecimal("80"), Categoria.COMIDA, "Almuerzo");
        monederos.transferir(anaAhorros.getId(), anaGastos.getId(), new BigDecimal("100"), Categoria.OTROS,
                "A mi monedero de gastos (no cuenta como gasto)");
        System.out.println(anaAhorros + "\n" + anaGastos + "\n" + luisGastos);

        titulo("4. Programadas (creadas en desorden)");
        programadas.programarDeposito(anaGastos.getId(), new BigDecimal("50"), Frecuencia.SEMANAL,
                reloj.hoy().plusDays(7), "Mesada semanal");
        programadas.programarTransferencia(luisGastos.getId(), anaAhorros.getId(), new BigDecimal("400"),
                Categoria.SERVICIOS, Frecuencia.MENSUAL, reloj.hoy().plusDays(2), "Arriendo");
        programadas.programarTransferencia(anaGastos.getId(), luisGastos.getId(), new BigDecimal("300"),
                Categoria.ENTRETENIMIENTO, Frecuencia.SEMANAL, reloj.hoy().plusDays(4), "Cuota (no le alcanza)");
        System.out.println("Programadas de Ana: " + programadas.listarProgramadas("1001"));
        System.out.println("Programadas de Luis: " + programadas.listarProgramadas("2002"));

        titulo("5. Avanzar 10 días y ejecutar pendientes");
        reloj.avanzarDias(10);
        System.out.println("Hoy es " + reloj.hoy());
        programadas.ejecutarPendientes().forEach(System.out::println);

        titulo("6. Avanzar 25 días más (varias fechas vencidas de golpe)");
        reloj.avanzarDias(25);
        System.out.println("Hoy es " + reloj.hoy());
        programadas.ejecutarPendientes().forEach(System.out::println);
        monederos.retirar(anaAhorros.getId(), new BigDecimal("120"), Categoria.SALUD, "Farmacia (diciembre)");
        System.out.println(anaAhorros + "\n" + anaGastos + "\n" + luisGastos);

        titulo("7. Reporte de gastos");
        System.out.print(analisis.generarReporte("1001"));
        System.out.print(analisis.generarReporte("2002"));

        titulo("8. Alertas");
        System.out.println("Ana:");
        alertas.alertasDe("1001").forEach(a -> System.out.println("  " + a));
        System.out.println("Luis:");
        alertas.alertasDe("2002").forEach(a -> System.out.println("  " + a));
    }

    private static void titulo(String texto) {
        System.out.println("\n===== " + texto + " =====");
    }
}
