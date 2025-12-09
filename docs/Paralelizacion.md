# Proyecto Final: Programación Funcional y Concurrente
## Informe de Paralelización

---
# 1. Introducción

Este informe documenta la estrategia de paralelización aplicada al proyecto *Riego*, explica qué partes del código fueron paralelizadas, analiza su desempeño, justifica matemáticamente los resultados y presenta los benchmarks obtenidos.

---
# 2. Identificación de problemas paralelizables

El problema requiere evaluar **todas las permutaciones** de los tablones de la finca. Para cada permutación `pi` se calcula:

- Costo total de riego: `costoRiegoFinca(f, pi)`
- Costo total de movilidad: `costoMovilidad(f, pi, d)`
- Se combina en un costo total: `cr + cm`

Cada permutación es **independiente**. Por lo tanto:

- El cálculo del costo total para cada `pi` es completamente paralelizable.
- La suma de costos por tablón dentro de `costoRiegoFinca` también es paralelizable.
- La suma de distancias consecutivas en `costoMovilidad` también es paralelizable.

La estructura del problema es entonces **fuertemente paralelizable** por tareas independientes.

---
# 3. Estrategia de paralelización empleada
Se implementaron tres formas principales:

### 3.1. Paralelización por división del trabajo
Se dividieron listas en dos mitades:

```scala
val (a, b) = parallel(tarea1, tarea2)
```

Esto se aplicó a:
- `costoRiegoFincaPar`
- `costoMovilidadPar`
- `ProgramacionRiegoOptimoPar`

### 3.2. Uso de colecciones paralelas
Para generar permutaciones:
```scala
for {
  i <- l.par
  resto = l.filter(_ != i)
  p <- permutacionespar(resto)
} yield i +: p
```

### 3.3. Paralelización del procesamiento de conjuntos de permutaciones
```scala
val (p1, p2) = programaciones.splitAt(...)
parallel(
  p1.map(...),
  p2.map(...)
)
```

---
# 4. Análisis detallado por función

---

### 4.1. `costoRiegoFincaPar`
Se divide la lista `pi` en dos partes y se procesa en paralelo:
```scala
val (pi1,pi2) = pi.splitAt(Math.ceil(pi.length/2).toInt)
val (c1,c2) = parallel(
  pi1.map(i => costoRiegoTablon(i, f, pi)).sum,
  pi2.map(i => costoRiegoTablon(i, f, pi)).sum
)
```

Resultados combinados:
```scala
c1 + c2
```

### 4.2. `costoMovilidadPar`
Similar a la anterior, pero sumando además el salto entre mitades:
```scala
val (pi1,pi2) = pi.splitAt(Math.ceil(pi.length/2).toInt)
val (c1, c2) = parallel(
  auxCostoMovilidad(f, pi1, d),
  auxCostoMovilidad(f, pi2, d)
)
c1 + c2 + d(pi1(pi1.length-1))(pi2(0))
```

### 4.3. `permutacionesPar`
Se usa `.par` para paralelizar la elección del elemento fijo en cada nivel de recursión:
```scala
for {
  i <- l.par
  resto = l.filter(_ != i)
  p <- permutacionespar(resto)
} yield i +: p
```

### 4.4. `ProgramacionRiegoOptimoPar`
Divide el conjunto de permutaciones en dos partes grandes y procesa cada una en paralelo:
```scala
val (p1,p2) = parallel(
  programacion1.map(...),
  programacion2.map(...)
)
```

Cada mapa llama internamente a:
- `costoRiegoFincaPar`
- `costoMovilidadPar`

---
# 5. Fundamento teórico - Ley de Amdahl
Si:
- `p` = fracción paralelizable
- `n` = núcleos disponibles

La aceleración máxima teórica es:
$S(n) = \dfrac{1}{(1-p) + \dfrac{p}{n}}$

Observando los resultados obtenidos, la fracción paralelizable efectiva es moderada, debido a:
- Recomputaciones costosas (por ejemplo `tIR` se repite muchas veces)
- Overhead por creación de tareas pequeñas
- Costos factoriales del número de permutaciones

Estos factores explican por qué el speedup no escala linealmente.

---
# 6. Resultados de Benchmarking

| Tamaño de la expresión | Secuencial (ms) | Paralelo (ms) | Speedup |
|------------------------|-----------------|---------------|---------|
| Finca pequeña          | 97,9587         | 58,9780       | 1,66×   |
| Finca mediana          | 1884,8087       | 1583,9006     | 1,19×   |
| Finca grande           | 211211,1286     | 174011,1513   | 1,21×   |
