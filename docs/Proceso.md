# Proyecto Final: Programación Funcional y Concurrente

## Informe de Proceso

---

# 1. Introducción

Este informe presenta el **proceso interno de ejecución** de las funciones implementadas para la solución del **Problema del Riego Óptimo**. El objetivo es encontrar una secuencia de riego que minimice los costos asociados al sufrimiento del cultivo y al desplazamiento de la maquinaria.

El análisis aborda:
* Generación de espacios de búsqueda (permutaciones) mediante recursión.
* Cálculo acumulativo de tiempos de inicio de riego ($t_{ini}$).
* Evaluación de costos mediante funciones de asignación y reducción.
* Estrategia de selección del óptimo global.

---

# 2. Fundamento Teórico

## 2.1. Representación del Problema

El problema se modela utilizando tipos de datos inmutables y algebraicos:
* **Finca ($F$):** Vector de tablones $\langle ts, tr, p \rangle$.
* **Programación ($\Pi$):** Permutación de índices $\{0, \dots, n-1\}$.

## 2.2. Cálculo de Tiempos (Recursión Acumulativa)

El tiempo de inicio de riego es dependiente del tablón anterior. Dado un orden $\Pi$, el tiempo se define recursivamente:

$$t_{\pi_0}^{\Pi} = 0$$
$$t_{\pi_j}^{\Pi} = t_{\pi_{j-1}}^{\Pi} + tr_{\pi_{j-1}}^{F}$$

## 2.3. Funciones de Costo

Se definen dos costos que deben minimizarse conjuntamente:

### Costo de Riego ($CR$)
Penalización basada en si el tiempo de supervivencia es excedido.

$$CR_{F}^{\Pi}[i] = \begin{cases} ts_i - (t_i + tr_i) & \text{si } ts_i \ge t_i + tr_i \\ p_i \cdot |(t_i + tr_i) - ts_i| & \text{de lo contrario} \end{cases}$$

### Costo de Movilidad ($CM$)
Suma de los pesos de las aristas recorridas en el grafo de la finca (matriz de adyacencia).

$$CM_{F}^{\Pi} = \sum_{j=0}^{n-2} D[\pi_j, \pi_{j+1}]$$

---

# 3. Implementación

## 3.1. Clase `Riego`

```scala
package taller
import taller.Tipos._

class Riego {

  // ... (Funciones auxiliares de acceso a tuplas)

  def calculatiempoderiego(valores: Vector[Int], pi: ProgRiego, vector_final: Vector[Int], suma: Int): Vector[Int] = {
    if (pi.isEmpty) vector_final
    else calculatiempoderiego(valores, pi.tail, vector_final :+ suma, suma + valores(pi.head))
  }

  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val valores_tr = (for { i <- f.indices } yield treg(f , i) ).toVector
    val tiemposInicio = calculatiempoderiego(valores_tr, pi, Vector.empty, 0)
    mostrar_ordenreal((0 until f.length).toVector, pi, tiemposInicio)
  }

  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
     // Lógica condicional del costo de sufrimiento
     val costo = tsup(f,i) - tIR(f, pi)(i) - treg(f,i)
     if (costo >= 0) costo else prio(f,i) * Math.abs(costo)
  }

  def permutaciones(l: Vector[Int]): Vector[Vector[Int]] = l match {
    case Vector() => Vector(Vector.empty)
    case _ =>
      for {
        i <- l
        resto = l.filter(_ != i)
        p <- permutaciones(resto)
      } yield i +: p
  }

  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
      val programaciones = generarProgramacionesRiego(f)
      programaciones
          .map { pi => (pi, costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)) }
          .minBy(_._2)
  }
}
```
Estructura:
- `calculartiempoderiego`:
    - Es una función recursiva de cola.
    - Acumula el tiempo actual en `suma` y construye `vector_final` paso a paso.
- `tIR`:
    - Guía el cálculo. Extrae los tiempos de regado, llama a la recursión y luego reordena los resultados (utilizando `mostrar_ordenreal`) para que coincidan con los índices originales de la finca.
- `permutaciones`:
    - Genera el espacio de búsqueda completo.
    - Utiliza la recursión de árbol: por cada elemento, genera las permutaciones del resto.
- `ProgramacionRiegoOptimo`:
    - Función de alto orden. Genera el espacio de estados, "mapea" cada estado a un costo total y "reduce" buscando el mínimo.

## 3.2. Clase `Generador_aleatorio`

```scala
package taller
import taller.Tipos._
import scala.util.Random

class Generador_aleatorio {

  val random = new Random()

  def fincaAlAzar(long: Int): Finca = {
    // Crea una finca de long tablones,
    // con valores aleatorios entre 1 y long * 2 para el tiempo
    // de supervivencia, entre 1 y long para el tiempo
    // de regado y entre 1 y 4 para la prioridad
    val v = Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1,
        random.nextInt(4) + 1)
    )
    v
  }

  def distanciaAlAzar(long: Int): Distancia = {
    // Crea una matriz de distancias para una finca
    // de long tablones, con valores aleatorios entre
    // 1 y long * 3
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i)
    )
  }
}
```

Estructura:
- `random:`
    - Instancia de la clase `Random` de Scala para generar números aleatorios.
- `fincaAlAzar:`
    - Utiliza `Vector.fill(long)` para crear un vector de tamaño `long`.
    - Cada elemento es una tupla `(ts, tr, p)` que representa un tablón.
    - Los valores de la tupla se generan usando `random.nextInt(Límite) + 1` para asegurar que el valor mínimo sea 1.
    - Generar de forma pura un conjunto de datos inmutable (`Finca`) para simular el problema de optimización.
- `distanciaAlAzar:`
    - Crea una matriz cuadrada `v` de `long x long` llenada con valores aleatorios.
    - Usa `Vector.tabulate(long, long)` para construir la matriz final, garantizando las propiedades de una matriz de distancias:
        - **Diagonal:** `(i == j) => 0` (la distancia de un tablón a si mismo es cero).
        - **Simetría:** `(i > j) => v(j)(i)` (la distancia de $j$ a $i$ es igual a la distancia de $i$ a $j$).
        - **Triángulo superior:** `(i < j) => v(i)(j)` (se usan los valores generados aleatoriamente).
    - Crea una matriz inmutable y simétrica para modelar el costo de movilidad.
---
# 4. Informe de Proceso

## 4.1. Pila de llamados - calculatiempoderiego

```mermaid
flowchart TD
A0["calculatiempoderiego(val, pi=[0,1], acc=[], sum=0)"]
A0 -->|Procesa pi.head=0, nuevo sum=0+10| A1["calculatiempoderiego(val, pi=[1], acc=[0], sum=10)"]
A1 -->|Procesa pi.head=1, nuevo sum=10+20| A2["calculatiempoderiego(val, pi=[], acc=[0,10], sum=30)"]
A2 -->|Caso base pi.isEmpty| A3["Retorna acc=[0, 10]"]
A3 -.-> A1
A1 -.-> A0
```

## 4.2. Flujo de datos - tIR (Tiempo Inicio Riego)

```mermaid
flowchart TD
B0["tIR(f, pi=[1,0])"]
B0 --> B1["Extraer treg: [tr0, tr1]"]
B0 --> B2["calculatiempoderiego(..., pi=[1,0]...)"]
B2 --> B3["Retorna tiempos en orden de ejecución: [0, tr1]"]
B3 --> B4["mostrar_ordenreal: Mapea (Tablón 1 -> 0), (Tablón 0 -> tr1)"]
B4 --> B5["Resultado Final: [tr1, 0]"]
```

## 4.3. Pila de llamados - permutaciones

```mermaid
flowchart TD
C0["permutaciones([1, 2])"]

C0 --> C1["Ramificación i = 1"]
C1 --> C2["permutaciones([2])"]
C2 --> C3["Ramificación i = 2"]
C3 --> C4["permutaciones([]) -> [[]]"]
C3 --> C5["yield 2 +: [] = [2]"]
C2 --> C6["Retorna [[2]]"]
C1 --> C7["yield 1 +: [2] = [1, 2]"]

C0 --> C8["Ramificación i = 2"]
C8 --> C9["permutaciones([1])"]
C9 --> C10["Ramificación i = 1"]
C10 --> C11["permutaciones([]) -> [[]]"]
C10 --> C12["yield 1 +: [] = [1]"]
C9 --> C13["Retorna [[1]]"]
C8 --> C14["yield 2 +: [1] = [2, 1]"]

C0 --> C15["Resultado Final: [[1, 2], [2, 1]]"]
```

## 4.4. Flujo de Proceso - costoMovilidad

```mermaid
flowchart TD
D0["costoMovilidad(f, pi=[0,2,1], d)"]
D0 --> D1["Llama auxCostoMovilidad (bucle for/yield)"]
D1 --> D2["Iteración 1 (i=0): par (pi[0], pi[1]) -> (0, 2)"]
D2 --> D3["Busca D[0][2] -> costo A"]
D1 --> D4["Iteración 2 (i=1): par (pi[1], pi[2]) -> (2, 1)"]
D4 --> D5["Busca D[2][1] -> costo B"]
D1 --> D6["Fin iteración (i=2): No hay i+1"]
D1 --> D7["Sumar resultados parciales: A + B"]
D7 --> D8["Retorna Total"]
```

## 4.5. Proceso General - ProgramacionRiegoOptimo

```mermaid
flowchart TD
E0["ProgramacionRiegoOptimo(f, d)"]
E0 --> E1["generarProgramacionesRiego(f)"]
E1 --> E2["Obtiene Vector de Permutaciones: [P1, P2, ..., Pn]"]

E0 --> E3["Map: Para cada Pi en Permutaciones"]
E3 --> E4["Calcula CostoRiego(Pi)"]
E3 --> E5["Calcula CostoMovilidad(Pi)"]
E3 --> E6["Suma Total = CR + CM"]

E0 --> E7["minBy: Comparar Totales"]
E7 --> E8["Seleccionar Pi con menor costo"]
E8 --> E9["Retornar (Pi_optima, Costo_min)"]
```

---
# 5. Conclusiones
- **Recursión de cola:** La implementación de `calculatiempoderiego` utiliza recursión de cola, lo que optimiza el uso de la pila (stack) al no dejar operaciones pendientes en cada llamada recursiva.
- **Complejidad Factorial:** La función `permutaciones` genera un árbol de llamadas que crece factorialmente ($n!$). Esto indica que para fincas grandes, el tiempo de procesamiento secuencial crecerá exponencialmente.
- **Independencia de tareas:** El cálculo de costos (`costoRiegoFinca` y `costoMovilidad`) para cada permutación es independiente entre sí. Esto sugiere que la etapa de `map` dentro de `ProgramacionRiegoOptimo` es el candidato ideal para la paralelización en la siguiente fase del proyecto.