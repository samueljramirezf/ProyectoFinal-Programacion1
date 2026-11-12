# Monedero Virtual con Sistema de Puntos (Proyecto final, Programación 1)

Simulador de un monedero virtual en Java. Todo corre en un solo programa (app JavaFX), sin servidor ni base de datos. La persistencia es **solo en memoria**.

Entrega: **15 de noviembre, 11:59 p. m.** Equipo de 2 a 3 personas.

> Los valores marcados como **(supuesto)** son decisiones nuestras porque el enunciado no las define. Se pueden cambiar en un solo lugar (ver `util/Config.java`).

---

## Stack

- Java 17 o superior
- Maven (con `javafx-maven-plugin`, se ejecuta con `mvn javafx:run`)
- JavaFX para la interfaz
- `BigDecimal` para todo el dinero (nunca `double`)
- Sin `module-info.java`
- Paquete base: `co.edu.uniquindio.poo`

## Estructura de paquetes

```
co.edu.uniquindio.poo
├── Main.java
├── modelo/        entidades y enums (casi solo datos y reglas propias de cada entidad)
├── servicio/      lógica de negocio
├── repositorio/   acceso a datos en memoria
├── notificacion/  notificadores (simulados)
├── excepcion/     excepciones personalizadas
├── util/          Config, validaciones, formato de dinero, Reloj
└── ui/            controladores JavaFX
```

**Regla de dependencias:** `ui → servicio → modelo`. El modelo no conoce a nadie. La interfaz gráfica nunca toca los datos directamente: solo llama a los servicios. Los cálculos (promedios, desviación, ordenamientos, reportes) van en `servicio`, no en `modelo`.

---

## Requisitos académicos obligatorios

Deben quedar **integrados en la lógica**, no puestos solo para cumplir:

- POO: herencia, interfaces, polimorfismo, encapsulamiento (atributos `private`, getters necesarios), abstracción (clases abstractas).
- Programación declarativa: lambdas y Stream API en búsquedas, filtros, ordenamientos, agrupaciones, cálculos y reportes.
- Mínimo 3 excepciones personalizadas ligadas a reglas de negocio.
- Validaciones de los datos ingresados.
- Interfaz gráfica con JavaFX.
- Informe final (lo hacemos aparte): pensamiento computacional, pruebas y análisis de POO. Por eso **cada decisión de diseño debe quedar explicada en comentarios breves** (por qué es abstracta una clase, qué método se sobrescribe, etc.).

---

## Reglas de negocio

### Puntos (enteros, redondeados hacia abajo)
- Depósito: 1 punto por cada 100 unidades.
- Retiro: 2 puntos por cada 100 unidades.
- Transferencia: 3 puntos por cada 100 unidades.
- Los puntos se recalculan si se revierte una transacción.
- Cada cliente guarda su historial de movimientos de puntos (ganó, canjeó, revirtió).

### Rangos (según puntos acumulados)
- Bronce: 0–500
- Plata: 501–1000
- Oro: 1001–5000
- Platino: más de 5000

El enum `Rango` contiene los límites y un método `desdePuntos(int)`.

**(supuesto)** Beneficios por rango: descuento adicional en la comisión de transferencias de 0 % (Bronce), 5 % (Plata), 10 % (Oro), 20 % (Platino).

### Canje de puntos
- 100 puntos: 10 % menos de comisión en transferencias.
- 500 puntos: un mes sin cargos por retiros.
- 1000 puntos: bono de 50 unidades en el monedero.

### Comisiones **(supuesto)**
- Depósito: 0 %.
- Retiro: 1 %.
- Transferencia: 2 %.

### Reversión de transacciones **(supuesto)**
- Se puede revertir cualquier transacción en estado `COMPLETADA`, una sola vez.
- El reverso es total: devuelve el saldo (incluida la comisión) y descuenta los puntos ganados.
- Una transferencia se revierte en ambos monederos. Si el destinatario ya no tiene saldo suficiente, se lanza excepción.
- La transacción queda en estado `REVERTIDA`; no se borra del historial.

### Multimonedero
Un cliente puede tener varios monederos (por ejemplo AHORROS y GASTOS_DIARIOS), cada uno con su saldo.

### Transacciones programadas **(supuesto)**
- Frecuencias: DIARIA, SEMANAL, MENSUAL.
- Se procesan **ordenadas por fecha de ejecución** (cola de prioridad o `Comparator`).
- No usar la hora real del sistema: existe una clase `Reloj` con "avanzar días" para poder demostrarlo al instante.

### Detección de transacciones atípicas
- Una transacción es atípica si su monto > **promedio + 2 × desviación estándar** del historial del cliente.
- **(supuesto)** Desviación estándar poblacional; se exige un mínimo de 5 transacciones históricas para analizar.
- Al detectarla se marca como potencialmente sospechosa y se genera una alerta.
- Documentar la complejidad temporal: O(n) para promedio y desviación, O(n) para marcar.

### Categorías de gasto **(supuesto)**
COMIDA, TRANSPORTE, SERVICIOS, ENTRETENIMIENTO, SALUD, EDUCACION, OTROS. Los retiros y transferencias llevan categoría. El análisis de patrones agrupa con `Collectors.groupingBy`.

### Alertas y notificaciones
- Tipos: SALDO_BAJO, RECORDATORIO, ATIPICA.
- Los envíos (WhatsApp, correo, SMS) son **simulados**. No integrar APIs externas por ahora.

### Roles **(supuesto)**
Cliente y Administrador (heredan de `Usuario`, clase abstracta). Inicio de sesión simple en memoria.

### Excepciones personalizadas (mínimo 3, ideal 5)
`SaldoInsuficienteException`, `MontoInvalidoException`, `TransaccionNoReversibleException`, `PuntosInsuficientesException`, `ClienteNoEncontradoException`.

---

## Alcance por fases

| Fase | Contenido |
|------|-----------|
| 1 | Proyecto Maven + paquetes `modelo` y `excepcion` |
| 2 | Servicios núcleo: depósito, retiro, transferencia, puntos, rangos, reversión |
| 3 | `Main` de consola que ejercita todo (sirve de verificación) |
| 4 | Avanzado: programadas, atípicas, análisis de gasto, alertas |
| 5 | Interfaces de Java (`Notificador`, `Repositorio`, `Reversible`) y JavaFX |
| 6 | (Después) pruebas con JUnit y notificaciones reales |

**Por ahora (fases 1 a 3): sin JavaFX, sin interfaces de Java, sin JUnit y sin APIs externas.**

---

## Cómo trabajar

1. Antes de escribir código en cada fase, **proponer un plan** y esperar confirmación.
2. Escribir solo lo que pide la fase actual. No adelantar fases.
3. Nombres de clases, métodos y variables en español, claros y consistentes.
4. Comentarios breves que expliquen el porqué del diseño, sobre todo en herencia, polimorfismo y abstracción.
5. Al terminar cada fase: compilar, ejecutar lo que corresponda y resumir qué se hizo y qué decisiones se tomaron.
6. Si el enunciado o este archivo no definen una regla, **preguntar** en lugar de inventarla.
7. Hacer commit al cerrar cada fase.

---

## Decisiones confirmadas (Fase 1)

- Puntos por bloques completos: `floor(monto / 100) × factor` (retiro de 250 → 4 puntos).
- Dos contadores: `puntosDisponibles` (bajan al canjear) y `puntosAcumulados` (solo bajan al revertir). El rango usa los acumulados.
- Revertir puntos ya canjeados lanza `PuntosInsuficientesException` (evita el abuso de canjear y luego revertir).
- La comisión se cobra encima del monto; los puntos se calculan sobre el monto sin comisión.
- En transferencias solo gana puntos el emisor.
- Transferencia entre monederos propios: permitida, sin comisión y sin puntos.
- Beneficio de 100 pts: una sola transferencia; se combina con el descuento por rango de forma multiplicativa. 500 pts: 30 días del `Reloj`. 1000 pts: bono al monedero que elija el cliente, sin generar puntos.
- 6.ª excepción `DatoInvalidoException` para datos no monetarios. Todas son checked y heredan de `MonederoException` (abstracta).
- Saldo bajo: saldo ≤ 500. Es solo un aviso (alerta), no bloquea movimientos.
- Dinero: 2 decimales con `RoundingMode.HALF_EVEN`.
- Fase 5: evaluar una interfaz `Categorizable` (Retiro, Transferencia) para el análisis de gasto.

## Decisiones confirmadas (Fase 2)

- `Reloj` es un objeto inyectado por constructor; arranca en `Config.FECHA_INICIO_RELOJ` (2026-11-01 08:00) y solo avanza.
- El id de una transacción se asigna al completarse: las operaciones fallidas y las cotizaciones no consumen ids. `Transaccion` usa igualdad por identidad.
- Bono de 1000 pts: se acredita directo al monedero (no es un `Deposito`, no se revierte ni da puntos); el rastro queda en el historial de puntos.
- Canjear de nuevo "retiros sin comisión" mientras está vigente suma 30 días al vencimiento actual.
- El beneficio de 10 % usado en una transferencia no se devuelve si esa transferencia se revierte.
- Monedero o transacción inexistente → `DatoInvalidoException`. Cliente inexistente → `ClienteNoEncontradoException`.
- Registrar un cliente no crea monederos; se crean con `crearMonedero`.
- La reversión está en `MonederoServicio` (switch por tipo, temporal hasta la interfaz `Reversible` de la Fase 5A).
