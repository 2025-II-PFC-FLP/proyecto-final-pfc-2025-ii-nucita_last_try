package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
// Asegúrate de que Tipos y la clase Riego están en el paquete 'taller'
import taller.Tipos._

@RunWith(classOf[JUnitRunner])
class PruebaRiego extends AnyFunSuite {

  val riego = new Riego()

  // ========================================================
  // 1. DEFINICIÓN DE DATOS DE ENTRADA
  // ========================================================

  // --- EJEMPLO 1 DEL ENUNCIADO (N=5) ---
  // F1 = <(10, 3, 4), (5, 3, 3), (2, 2, 1), (8, 1, 1), (6, 4, 2)>
  val f1: Finca = Vector((10, 3, 4), (5, 3, 3), (2, 2, 1), (8, 1, 1), (6, 4, 2))

  /* Df1 (Matriz de distancias 5x5) */
  val d1: Distancia = Vector(
    Vector(0, 2, 4, 4, 4),
    Vector(2, 0, 4, 2, 6),
    Vector(2, 4, 0, 2, 2),
    Vector(4, 2, 2, 0, 4),
    Vector(4, 6, 2, 4, 0)
  )

  // Programación 1: Pi1. Costo Total=45.
  val pi1: ProgRiego = Vector(0, 1, 4, 2, 3)
  // Programación 2: Pi2. Costo Total=38 (Optimo para E1).
  val pi2: ProgRiego = Vector(2, 1, 4, 3, 0)

  // --- EJEMPLO 2 DEL ENUNCIADO (N=5) ---
  // F2 = <(9, 3, 4), (5, 3, 3), (2, 2, 1), (8, 1, 1), (6, 4, 2)>
  // Solo se usa para pruebas de Costo de Riego, ya que tiene ts diferentes a F1.
  val f2: Finca = Vector((9, 3, 4), (5, 3, 3), (2, 2, 1), (8, 1, 1), (6, 4, 2))


  // --- EJEMPLO C3: ALTA COMPLEJIDAD (N=5) ---
  // F_C3 fuerza un conflicto entre alto costo de riego (Tablón 3) y alta movilidad (a/desde 3).
  val f_c3: Finca = Vector((10, 2, 5), (25, 3, 1), (10, 2, 5), (5, 1, 10), (20, 2, 1))
  val d_c3: Distancia = Vector(
    Vector(0, 2, 10, 50, 5),
    Vector(2, 0, 2, 10, 5),
    Vector(10, 2, 0, 10, 5),
    Vector(50, 10, 10, 0, 2),
    Vector(5, 5, 5, 2, 0)
  )
  // Pi_C3_Óptimo: <3, 2, 0, 1, 4>. CR=43, CM=27. Total=70.
  val pi_c3_optimo: ProgRiego = Vector(3, 2, 0, 1, 4)
  // Pi_C3 Subóptimo de movilidad: <3, 0, 2, 1, 4>. CR=43, CM=67. Total=110.
  val pi_c3_suboptimo: ProgRiego = Vector(3, 0, 2, 1, 4)


  // ========================================================
  // 2. PRUEBAS PARA tIR (Tiempo de Inicio de Riego)
  // ========================================================

  test("tIR — E1: Programación Pi1 (Caso Base)") {
    // Expected: <0, 3, 10, 12, 6>
    val expected: TiempoInicioRiego = Vector(0, 3, 10, 12, 6)
    assert(riego.tIR(f1, pi1) == expected)
  }

  test("tIR — E1: Programación Pi2 (Otro Orden)") {
    // Expected: <10, 2, 0, 9, 5>
    val expected: TiempoInicioRiego = Vector(10, 2, 0, 9, 5)
    assert(riego.tIR(f1, pi2) == expected)
  }

  test("tIR — E3: Complejidad C3, Pi Óptimo") {
    // Pi_optimo = <3, 2, 0, 1, 4>. Tiempos por índice: <3, 5, 1, 0, 8>
    val expected: TiempoInicioRiego = Vector(3, 5, 1, 0, 8)
    assert(riego.tIR(f_c3, pi_c3_optimo) == expected)
  }

  test("tIR — Caso Borde: Finca vacía") {
    assert(riego.tIR(Vector.empty, Vector.empty) == Vector.empty)
  }

  test("tIR — Caso Borde: Finca de un solo tablón") {
    val f_single: Finca = Vector((10, 5, 1))
    assert(riego.tIR(f_single, Vector(0)) == Vector(0))
  }

  // ========================================================
  // 3. PRUEBAS PARA costoRiegoTablon
  // ========================================================

  test("costoRiegoTablon — E1: Caso Beneficio (Tablón 0, Pi1)") {
    // ts=10, t+tr=3. Beneficio: 10 - 3 = 7.
    assert(riego.costoRiegoTablon(0, f1, pi1) == 7)
  }

  test("costoRiegoTablon — E1: Caso Penalización (Tablón 1, Pi1)") {
    // ts=5, t+tr=6. Sufre 1. Costo: p*|sufrimiento| = 3 * 1 = 3.
    assert(riego.costoRiegoTablon(1, f1, pi1) == 3)
  }

  test("costoRiegoTablon — E1: Caso Penalización con p=1 (Tablón 3, Pi1)") {
    // ts=8, t+tr=13. Sufre 5. Costo: 1 * 5 = 5.
    assert(riego.costoRiegoTablon(3, f1, pi1) == 5)
  }

  test("costoRiegoTablon — C3: Caso Crítico (Tablón 3, Pi_Óptimo)") {
    // ts=5, t+tr=1. Beneficio: 5 - 1 = 4.
    assert(riego.costoRiegoTablon(3, f_c3, pi_c3_optimo) == 4)
  }

  test("costoRiegoTablon — Caso Borde: Justo a tiempo (Costo 0)") {
    // Fd = <(3, 3, 1)>. t_0=0. t+tr=3. Costo: 3 - 3 = 0.
    val f_justo: Finca = Vector((3, 3, 1))
    assert(riego.costoRiegoTablon(0, f_justo, Vector(0)) == 0)
  }

  // ========================================================
  // 4. PRUEBAS PARA costoRiegoFinca
  // ========================================================

  test("costoRiegoFinca — E1: Programación Pi1") {
    // Expected: 33.
    assert(riego.costoRiegoFinca(f1, pi1) == 33)
  }

  test("costoRiegoFinca — E1: Programación Pi2") {
    // Expected: 20.
    assert(riego.costoRiegoFinca(f1, pi2) == 20)
  }

  test("costoRiegoFinca — E2: Costo con Finca f2 (CR=24)") {
    // F2 con Pi2: Total 24. (Caso complejo de revalidación)
    val pi_e2_1: ProgRiego = Vector(2, 1, 4, 3, 0)
    assert(riego.costoRiegoFinca(f2, pi_e2_1) == 24)
  }

  test("costoRiegoFinca — C3: Pi Óptimo (CR=43)") {
    assert(riego.costoRiegoFinca(f_c3, pi_c3_optimo) == 43)
  }

  test("costoRiegoFinca — Caso Borde: Finca vacía") {
    assert(riego.costoRiegoFinca(Vector.empty, Vector.empty) == 0)
  }

  // ========================================================
  // 5. PRUEBAS PARA costoMovilidad
  // ========================================================

  test("costoMovilidad — E1: Programación Pi1") {
    // D[0,1] + D[1,4] + D[4,2] + D[2,3] = 2 + 6 + 2 + 2 = 12.
    assert(riego.costoMovilidad(f1, pi1, d1) == 12)
  }

  test("costoMovilidad — E1: Programación Pi2") {
    // D[2,1] + D[1,4] + D[4,3] + D[3,0] = 4 + 6 + 4 + 4 = 18.
    assert(riego.costoMovilidad(f1, pi2, d1) == 18)
  }

  test("costoMovilidad — C3: Pi Óptimo (CM=27)") {
    // D[3,2] + D[2,0] + D[0,1] + D[1,4] = 10 + 10 + 2 + 5 = 27.
    assert(riego.costoMovilidad(f_c3, pi_c3_optimo, d_c3) == 27)
  }

  test("costoMovilidad — C3: Pi Subóptimo (CM=67)") {
    // D[3,0] + D[0,2] + D[2,1] + D[1,4] = 50 + 10 + 2 + 5 = 67.
    assert(riego.costoMovilidad(f_c3, pi_c3_suboptimo, d_c3) == 67)
  }

  test("costoMovilidad — Caso Borde: Finca de un solo tablón") {
    // n-1 movimientos = 0 movimientos.
    val f_single: Finca = Vector((10, 5, 1))
    val d_single: Distancia = Vector(Vector(0))
    assert(riego.costoMovilidad(f_single, Vector(0), d_single) == 0)
  }

  // ========================================================
  // 6. PRUEBAS PARA ProgramacionRiegoOptimo (Secuencial)
  // ========================================================

  test("ProgramacionRiegoOptimo — E1: Debe encontrar el costo mínimo (31)") {
    val (piOptimo, costoOptimo) = riego.ProgramacionRiegoOptimo(f1, d1)

    // CORRECCIÓN: Validamos SOLO el costo.
    // Si tu código encuentra otra permutación diferente a la del ejemplo
    // pero con el mismo costo de 38, el test pasará correctamente.
    assert(costoOptimo == 31, s"El costo óptimo debería ser 31, pero dio $costoOptimo. Revisar lógica de costos.")

    // Opcional: Imprimir qué solución encontró para verificar visualmente
    println(s"Solución E1 encontrada: $piOptimo con costo $costoOptimo")
  }

  test("ProgramacionRiegoOptimo — C3 (Complejo): Debe encontrar el costo mínimo (54)") {
    // Este caso tiene 120 permutaciones. El óptimo matemático es 54.
    val (piOptimo, costoOptimo) = riego.ProgramacionRiegoOptimo(f_c3, d_c3)

    assert(costoOptimo == 54, s"El costo óptimo para C3 debería ser 54, pero dio $costoOptimo")
  }

  test("ProgramacionRiegoOptimo — Caso 2 Tablones: CR vs CM") {
    // Caso pequeño de control para verificar si prioriza bien entre riego y movilidad
    val fd: Finca = Vector((2, 5, 1), (10, 1, 1))
    val dd: Distancia = Vector(Vector(0, 1), Vector(10, 0))
    // PiA = <0, 1>: CR=7, CM=1. Total=8. (Óptimo)
    // PiB = <1, 0>: CR=13, CM=10. Total=23.
    val (piOptimo, costoOptimo) = riego.ProgramacionRiegoOptimo(fd, dd)

    assert(costoOptimo == 8, s"Falló en caso pequeño. Dio $costoOptimo, esperaba 8")
  }

  // ========================================================
  // 7. PRUEBAS PARA ProgramacionRiegoOptimoPar (Paralelo)
  // ========================================================

  test("ProgramacionRiegoOptimoPar — E1: Debe coincidir con el resultado esperado (31)") {
    // Calculamos el paralelo
    val (piParalelo, costoParalelo) = riego.ProgramacionRiegoOptimoPar(f1, d1)

    println(s"Costo Paralelo E1 encontrado: $costoParalelo")

    // Validamos que llegue al mismo óptimo
    assert(costoParalelo == 31, "El algoritmo paralelo no encontró el mínimo global de 31")
  }

  test("ProgramacionRiegoOptimoPar — C3: Comparación Secuencial vs Paralelo") {
    // Ejecutamos ambos para asegurar que la lógica paralela no rompe nada
    val (_, costoSecuencial) = riego.ProgramacionRiegoOptimo(f_c3, d_c3)
    val (_, costoParalelo) = riego.ProgramacionRiegoOptimoPar(f_c3, d_c3)

    println(s"Secuencial C3: $costoSecuencial | Paralelo C3: $costoParalelo")

    assert(costoParalelo == costoSecuencial, "La versión paralela arrojó un costo diferente a la secuencial")
    assert(costoParalelo == 54, "El costo paralelo debería ser 54")
  }
}