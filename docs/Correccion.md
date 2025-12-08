# 🧠 Demostración de Corrección – Funciones del Proyecto de Riego  

# 1. ✔️ Demostración de corrección de `calculatiempoderiego`

## 📘 Código analizado
```scala
def calculatiempoderiego(valores: Vector[Int], pi: ProgRiego, vector_final: Vector[Int], suma: Int): Vector[Int] = {
  if (pi.isEmpty) vector_final
  else calculatiempoderiego(valores, pi.tail, vector_final :+ suma, suma + valores(pi.head))
}
def mostrar_ordenreal(vector_orden: Vector[Int], orden_riego: ProgRiego, vector_desorganizado: Vector[Int]): Vector[Int] = {
  val orden_real = (for {
    i <- vector_orden
    j <- orden_riego
    if i == j
  } yield vector_desorganizado(orden_riego.indexOf(j))).toVector
  orden_real
}

def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
  // Dada una finca f y una programación de riego pi,
  // y f.length == n, tIR(f, pi) devuelve t: TiempoInicioRiego
  // tal que t(i) es el tiempo en que inicia el riego del
  // tablon i de la finca f según pi
  val valores_tr = (for {
    i <- f.indices
  } yield treg(f , i)).toVector

  val tiemposInicio = calculatiempoderiego(valores_tr, pi, Vector.empty, 0)

  val valores_organizados_tablones =
    mostrar_ordenreal((0 until f.length).toVector, pi, tiemposInicio)

  valores_organizados_tablones
}

```
La función `calculatiempoderiego(valores, pi, Vector(), 0)` debe devolver un vector (t) tal que:

$$
t(\pi_0) = 0, \qquad 
t(\pi_j) = \sum_{k=0}^{j-1} \text{valores}(\pi_k), \quad j = 1..n-1.
$$

Es decir, produce el tiempo acumulado en que inicia el riego cada tablón **siguiendo el orden dado por** $$(\pi)$$.

Sea:

$$
\pi = \langle \pi_0, \pi_1, \dots, \pi_{n-1} \rangle
$$

Definimos:

$$
T_j = \sum_{k=0}^{j-1} valores(\pi_k)
\qquad \text{con } T_0 = 0
$$

La función debe devolver:

$$
\langle T_0, T_1, \dots, T_{n-1} \rangle
$$

en el orden en que han sido generados.

## Demostración por inducción.

**Caso base:**\
`pi = Vector()`\
La función retorna

$$
vectorfinal
$$


Por construcción, cuando se llega al final:

$$
vectorfinal = \langle T_0,\ T_1,\ \dots,\ T_{n-1} \rangle.
$$

La especificación se cumple.
### **Hipótesis inductiva**

Suponemos que para `pi.tail` se cumple que:

> "`calculatiempoderiego(valores, pi.tail, vec, s)` produce correctamente  
> los tiempos acumulados a partir del valor inicial `s`."

Formalmente:

$$
IH:\; calculatiempoderiego(valores,\ \pi_{[1:]},\ V,\ s)
= V \mathbin{++} \langle s,\ s + valores(\pi_1),\ \dots \rangle.
$$

---

### **Paso inductivo**

Sea:

```scala
calculatiempoderiego(valores, pi, vector_final, suma)
```
donde:

pi.head = \(pi_0)

suma = T_0

vector_final contiene los tiempos previos correctamente

La función ejecuta:

$$
vectorfinal' = vectorfinal :+ suma
$$

y llama recursivamente con:

$$
suma' = suma + valores(\pi_0) = T_1
$$

Por la hipótesis inductiva, la llamada recursiva genera correctamente:

$$
\langle T_1,\; T_2,\; \dots \rangle
$$

Como antes de la llamada añadimos \(T_0\), el resultado final es:

$$
\langle T_0,\ T_1,\ \dots,\ T_{n-1} \rangle
$$

que coincide exactamente con la definición matemática.



### 🧠 Explicación intuitiva de `mostrar_ordenreal`

- Esta función reordena los tiempos generados por `calculatiempoderiego`.
- `vector_desorganizado` contiene los tiempos en el orden de aparición en la permutación `pi`.
- `vector_orden` es simplemente el orden natural de los tablones: `0, 1, 2, ..., n−1`.
- Para cada tablón `i`, se busca su posición dentro de la permutación `orden_riego`.
- Esa posición le indica qué tiempo le corresponde dentro de `vector_desorganizado`.
- El resultado final es un vector donde la posición `i` contiene el tiempo de inicio de riego del tablón `i`.

---

### 🧠 Explicación intuitiva de `tIR`

- Primero se extrae el tiempo de riego de cada tablón:  
  `valores_tr(i) = treg(f, i)`.
- Luego `calculatiempoderiego` calcula los tiempos acumulados  
  **siguiendo la secuencia de riego `pi`**.  
  Esto genera los tiempos en “orden de riego”.
- Pero esa lista no está asociada al índice real del tablón,  
  sino al orden de la permutación.
- `mostrar_ordenreal` se encarga de devolverlos al orden natural:  
  tiempo para el tablón 0, para el 1, para el 2, etc.
- El vector final tiene la forma correcta:  
  `t(i) = tiempo en que se empieza a regar el tablón i`.

---

### ✔️ Conclusión intuitiva

- `calculatiempoderiego` calcula tiempos acumulados según el orden de riego.  
- `mostrar_ordenreal` los mapea al índice correcto del tablón.  
- `tIR` solo **conecta ambas funciones** para producir el vector solicitado por la definición formal:  
  tiempo de inicio de riego por tablón.

## 2. Costo de Riego: Análisis y Correspondencia con la Especificación

### Código analizado

```scala
def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
  val costoRiegoParcial = tsup(f,i) - tIR(f, pi)(i) - treg(f,i)
  if (costoRiegoParcial >= 0) costoRiegoParcial
  else prio(f,i) * math.abs(costoRiegoParcial)
}

def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
  val costoRiegoTotal = pi.map(i => costoRiegoTablon(i, f, pi))
  costoRiegoTotal.sum
}
```
### Especificación Matemática

El problema define el costo de riego de un tablón individual $T_i$ mediante la siguiente **función a trozos**:

$$
CR_{F}^{\Pi}[i] = 
\begin{cases} 
ts_{i}^{F} - (t_{i}^{\Pi} + tr_{i}^{F}), & \text{si } ts_{i}^{F} - tr_{i}^{F} \ge t_{i}^{\Pi} \\ 
p_{i}^{F} \cdot ((t_{i}^{\Pi} + tr_{i}^{F}) - ts_{i}^{F}), & \text{de lo contrario.}
\end{cases}
$$

Donde:
* $ts_{i}^{F}$: Tiempo de supervivencia (`tsup`).
* $tr_{i}^{F}$: Tiempo de regado (`treg`).
* $t_{i}^{\Pi}$: Tiempo de inicio de riego (`tIR`).
* $p_{i}^{F}$: Prioridad del tablón (`prio`).

### Demostración de equivalencia lógica

Para demostrar que la implementación es correcta, analizamos la variable auxiliar `costoRiegoParcial` definida en el código:

$$
\text{costoRiegoParcial} = ts_{i}^{F} - t_{i}^{\Pi} - tr_{i}^{F}
$$

Esta expresión es algebraicamente equivalente a agrupar los tiempos de consumo:

$$
\text{costoRiegoParcial} = ts_{i}^{F} - (t_{i}^{\Pi} + tr_{i}^{F})
$$

Analizamos ahora la estructura de control `if/else` frente a los dos casos de la especificación:

#### **Caso A: El cultivo no sufre (Conservación hídrica)**

La condición matemática para este caso es $ts_{i}^{F} - tr_{i}^{F} \ge t_{i}^{\Pi}$. Si restamos $t_{i}^{\Pi}$ a ambos lados obtenemos:

$$
ts_{i}^{F} - tr_{i}^{F} - t_{i}^{\Pi} \ge 0 \implies \text{costoRiegoParcial} \ge 0
$$

Esto coincide exactamente con la condición del código `if (costoRiegoParcial >= 0)`.
En este escenario, la función retorna `costoRiegoParcial`, lo cual es exactamente:

$$
ts_{i}^{F} - (t_{i}^{\Pi} + tr_{i}^{F})
$$

> **Conclusión:** La rama `if` implementa correctamente el primer caso de la función a trozos.

#### **Caso B: El cultivo sufre (Penalización por prioridad)**

Si la condición anterior no se cumple, implica que $\text{costoRiegoParcial} < 0$ (es decir, el tiempo de riego excedió el tiempo de supervivencia).

La especificación matemática pide calcular:

$$
\text{Costo} = p_{i}^{F} \cdot ((t_{i}^{\Pi} + tr_{i}^{F}) - ts_{i}^{F})
$$

Observamos que el término entre paréntesis es el negativo de nuestra variable auxiliar:

$$
(t_{i}^{\Pi} + tr_{i}^{F}) - ts_{i}^{F} = -1 \cdot (ts_{i}^{F} - (t_{i}^{\Pi} + tr_{i}^{F})) = - (\text{costoRiegoParcial})
$$

Dado que estamos en la rama `else`, sabemos que `costoRiegoParcial` es un número negativo. Por definición de valor absoluto, si $x < 0$, entonces $|x| = -x$. Por lo tanto:

$$
- (\text{costoRiegoParcial}) = |\text{costoRiegoParcial}|
$$

Sustituyendo esto en la ecuación de costo:

$$
\text{Costo} = p_{i}^{F} \cdot |\text{costoRiegoParcial}|
$$

Esto corresponde exactamente a la instrucción del código: `prio(f,i) * math.abs(costoRiegoParcial)`.

> **Conclusión:** La rama `else` implementa correctamente el segundo caso de la función a trozos.

---

## 2.2. Análisis de `costoRiegoFinca`

### Especificación Matemática

El costo total de riego de la finca se define como la sumatoria de los costos individuales de todos los tablones:

$$
CR_{F}^{\Pi} = \sum_{i=0}^{n-1} CR_{F}^{\Pi}[i]
$$

### Argumentación de corrección

La función `costoRiegoFinca` utiliza el paradigma de programación funcional mediante funciones de alto orden (`map` y `sum`) sobre la colección `pi`.

1.  **Mapeo (`map`):**
    La instrucción `pi.map(i => costoRiegoTablon(i, f, pi))` genera un vector donde cada elemento corresponde a $CR_{F}^{\Pi}[i]$.
    
    Dado que $\Pi$ (`pi`) es una permutación de los índices $\{0, 1, \dots, n-1\}$ (como se define en la formalización del problema), aplicar la función a cada elemento de `pi` garantiza que se calcula el costo para **cada uno de los tablones** de la finca exactamente una vez.

2.  **Reducción (`sum`):**
    La instrucción `.sum` reduce la colección aplicando la operación de adición a todos sus elementos.
    
    $$
    \text{resultado} = \sum_{k \in \Pi} \text{costoRiegoTablon}(k, f, \Pi)
    $$
    
    Debido a la propiedad conmutativa y asociativa de la suma ($\sum_{k \in \Pi} x_k = \sum_{i=0}^{n-1} x_i$), el orden en el que aparecen los índices en la permutación `pi` no altera el resultado final de la suma total.

> **Conclusión:** La función implementa correctamente la sumatoria definida formalmente como $CR_{F}^{\Pi}$.

Aquí tienes la demostración para costoMovilidad siguiendo estrictamente el mismo formato académico y estructura Markdown lista para GitHub que usamos en los puntos anteriores.

## 3. ✔️ Demostración de corrección de costoMovilidad
📘 Código analizado
```scala
def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
  // Calcula el costo de movilidad para regar todos los tablones
  // según la programación pi y la matriz de distancias d
  def auxCostoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val resultado_parcial = for {
      i <- pi.indices
      if (i + 1) - f.length < 0
    } yield d(pi(i))(pi(i+1))
    resultado_parcial.sum
  }
  auxCostoMovilidad(f, pi, d)
}
```
### 3.1. Especificación Matemática

El costo de movilidad se define como la suma de las distancias recorridas para desplazarse entre los tablones en el orden establecido por la programación de riego $\Pi$.

Si la programación es la secuencia $\Pi = \langle \pi_0, \pi_1, \dots, \pi_{n-1} \rangle$, el costo total de movilidad $CM(\Pi)$ está dado por:

$$
CM(\Pi) = \sum_{i=0}^{n-2} \text{distancia}(\pi_i, \pi_{i+1})
$$

Donde:
* $n$: es el número de tablones (la longitud de la finca $F$ o la programación $\Pi$).
* $\text{distancia}(u, v)$: es el costo de ir del tablón $u$ al tablón $v$, dado por la matriz de distancias $d$.

> **Nota:** La sumatoria va hasta $n-2$ porque el último tablón $\pi_{n-1}$ es el punto final y no requiere desplazarse a un "siguiente" tablón.
> ### 3.2. Demostración de equivalencia lógica

Para demostrar la corrección, analizamos la *for-comprehension* dentro de la función auxiliar `auxCostoMovilidad`.

#### **Generación de índices y filtrado**

El código inicia iterando sobre todos los índices de la programación:
1.  `i <- pi.indices`: Genera la secuencia $i \in \{0, 1, \dots, n-1\}$.

Luego, se aplica un filtro (guardia) en la iteración:
2.  `if (i + 1) - f.length < 0`

Analicemos algebraicamente esta condición, sabiendo que `f.length` es $n$:

$$
(i + 1) - n < 0
$$

Sumando $n$ a ambos lados de la inecuación:

$$
i + 1 < n
$$

Restando 1 a ambos lados:

$$
i < n - 1
$$

Dado que trabajamos con números enteros, la condición $i < n-1$ es equivalente a decir que el valor máximo que puede tomar $i$ es $n-2$.
Por lo tanto, el conjunto de índices efectivos sobre los que opera el bucle es:

$$
I_{validos} = \{0, 1, \dots, n-2\}
$$

Esto coincide exactamente con los límites de la sumatoria especificados en la definición matemática ($\sum_{i=0}^{n-2}$).

#### **Cálculo del término**

Dentro del bloque `yield`, el código ejecuta:
`d(pi(i))(pi(i+1))`

Esto corresponde directamente al acceso a la matriz de distancias:

$$
d[\pi_i][\pi_{i+1}] \equiv \text{distancia}(\pi_i, \pi_{i+1})
$$

#### **Agregación**

Finalmente, se invoca `.sum` sobre la colección resultante `resultado_parcial`.
Esto transforma la secuencia generada en una sumatoria total:


$$
\texttt{resultadoparcial.sum} = \sum_{i \in I_{validos}} d(\(pi(i)), \(pi(i+1))) = \sum_{i=0}^{n-2} \text{distancia}(\(pi(i)), \(pi(i+1)))
$$


---

### 3.3. Conclusión

La función `costoMovilidad`:

1.  Restringe correctamente el rango de iteración para evitar un `IndexOutOfBoundsException` en el último elemento (el tablón final no tiene sucesor).
2.  Acumula las distancias entre elementos adyacentes $(\pi_i \to \pi_{i+1})$.
3.  Calcula exactamente la definición formal de $CM(\Pi)$.

> **Por lo tanto, la implementación es correcta.**

# 4. ✔️ Demostración de Algoritmos de Generación y Optimización

## 4.1. Demostración de `permutaciones`

### 📘 Código analizado

```scala
def permutaciones(l: Vector[Int]): Vector[Vector[Int]] = l match {
  case Vector() => Vector(Vector.empty)
  case _ =>
    for {
      i <- l
      resto = l.filter(_ != i)
      p <- permutaciones(resto)
    } yield i +: p
}

Aquí tienes el contenido convertido a formato Markdown optimizado para GitHub.He aplicado las siguientes mejoras para que se vea profesional en tu repositorio:Syntax Highlighting: Bloques de código con la etiqueta scala para coloreado de sintaxis.LaTeX: Fórmulas matemáticas formateadas con $ y $$ (GitHub renderiza esto nativamente).Jerarquía: Uso correcto de encabezados y listas para facilitar la lectura.Puedes copiar y pegar el siguiente bloque directamente en tu archivo .md.Markdown# 4. ✔️ Demostración de Algoritmos de Generación y Optimización

## 4.1. Demostración de `permutaciones`

### 📘 Código analizado

```scala
def permutaciones(l: Vector[Int]): Vector[Vector[Int]] = l match {
  case Vector() => Vector(Vector.empty)
  case _ =>
    for {
      i <- l
      resto = l.filter(_ != i)
      p <- permutaciones(resto)
    } yield i +: p
}
```
Especificación Matemática  
Sea $L$ un conjunto de elementos distintos. La función debe retornar $P(L)$, que es el conjunto de todas las ordenaciones posibles de los elementos de $L$.  
Si la cardinalidad $|L| = n$, entonces la cantidad de permutaciones es $|P(L)| = n!$.

## Demostración por Inducción Estructural

### Caso Base:
Sea $L$ un vector vacío (longitud $n=0$).  
El patrón `case Vector()` se activa y retorna `Vector(Vector.empty)`.  

Matemáticamente, la única permutación de un conjunto vacío es el conjunto que contiene al conjunto vacío:

$$P(\emptyset) = \{\emptyset\}$$

La cardinalidad es $0! = 1$. El código retorna un vector que contiene un vector vacío.

**Conclusión:** El caso base se cumple.

### Paso Inductivo:
**Hipótesis Inductiva (HI):** Asumimos que `permutaciones(resto)` genera correctamente todas las permutaciones para un vector de tamaño $n-1$.

Sea $L$ un vector de tamaño $n$. El código realiza la siguiente lógica en la for-comprehension:

- **Selección (i <- l):** Itera sobre cada elemento $x \in L$ para colocarlo como la cabeza (primer elemento) de la permutación.  
- **Filtrado (resto):** Genera un nuevo vector $L' = L - \{x\}$, el cual tiene tamaño $n-1$.  
- **Recursión (p <- permutaciones(resto)):** Por la HI, esta llamada retorna todas las permutaciones posibles de $L'$.  
- **Construcción (yield i +: p):** Prepende (añade al inicio) el elemento $x$ a cada una de las permutaciones de $L'$.  

La unión de todas las permutaciones que comienzan con cada uno de los elementos de $L$ conforma el conjunto total de permutaciones:

$$P(L) = \bigcup_{x \in L} \{ \langle x \rangle \mathbin{++} \pi \mid \pi \in P(L - \{x\}) \}$$

Esta estructura coincide con la definición recursiva formal de las permutaciones.

**Conclusión:** La función es correcta para todo $n \ge 0$.

---

## 4.2. Análisis de `generarProgramacionesRiego`

### 📘 Código analizado (Scala)
```scala
def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
  val n = f.length
  val indices = (0 until n).toVector
  val perms = permutaciones(indices)
  perms
}
```
### Justificación  
Esta función actúa como un adaptador para el problema específico de riego:

- Define el dominio de los tablones transformando la finca en una secuencia de índices numéricos:  
  $$I = \{0, 1, \dots, n-1\}$$

- Invoca a la función genérica probada anteriormente: `permutaciones(indices)`.

- Dado que una Programación de Riego ($\Pi$) se define formalmente en el problema como una permutación de los índices de los tablones de la finca, esta función satisface directamente la especificación al generar el espacio de búsqueda completo $S_n$.

---

## 4.3. Demostración de ProgramacionRiegoOptimo

### 📘 Código analizado (Scala)
```scala
def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
  
  def minimo(p:Vector[(ProgRiego,Int)], min:Int, pi : Vector[(ProgRiego,Int)]):(ProgRiego, Int) ={
    if(p.isEmpty) pi.filter(_._2 == min).head
    else minimo(p.tail, if (p.head._2 < min) p.head._2 else min, pi )
  }

  val programaciones = generarProgramacionesRiego(f)

  if (programaciones.isEmpty) (Vector.empty[Int], 0)
  else {
    val p = programaciones.map { pi =>
      val cr = costoRiegoFinca(f, pi)
      val cm = costoMovilidad(f, pi, d)
      val total = cr + cm
      (pi, total)
    }
    minimo(p.tail, p.head._2, p)
  }
}
```
## Especificación Matemática del Óptimo

El objetivo es encontrar una programación $\Pi^* \in S_n$ tal que minimice el costo total combinado.

Sea la función de costo total:

$$\text{CostoTotal}(\Pi) = CR_{F}^{\Pi} + CM(\Pi)$$

Se busca:

$$\Pi^* = \arg\min_{\Pi \in S_n}(\text{CostoTotal}(\Pi))$$

---

## Demostración de corrección

### 1. Cálculo de Costos (Mapeo)

El bloque `map` transforma cada permutación posible `pi` generada en una tupla `(pi, total)`.

Para ello utiliza `costoRiegoFinca` y `costoMovilidad`, cuya corrección ya fue demostrada en las secciones anteriores.

Por lo tanto, la lista `p` contiene pares válidos:

$$p = \{ (\Pi_k, \text{CostoTotal}(\Pi_k)) \mid \forall \Pi_k \in S_n \}$$

---

### 2. Función auxiliar `minimo` (Reducción)

Esta función implementa una búsqueda lineal recursiva para encontrar el valor mínimo.

**Invariante:**  
El parámetro `min` almacena el menor costo numérico encontrado hasta ese momento.

**Paso recursivo:**  
Se compara el costo de `p.head._2` con el valor almacenado en `min`.  
Si es menor, se actualiza; si no, se conserva.

**Caso base (`p.isEmpty`):**  
Cuando se termina la lista, el mínimo absoluto ya está identificado.  
`pi.filter(_._2 == min).head` recupera la programación asociada a ese mínimo.

---

### 3. Integración

La función principal maneja el caso borde de lista vacía y, en el caso general:

- Genera todas las programaciones posibles.  
- Calcula su costo total.  
- Recorre todos los valores usando `minimo` para seleccionar la programación óptima.  

Dado que evalúa exhaustivamente todas las permutaciones ($S_n$) y selecciona la de costo mínimo, encuentra el óptimo global.

---

## Conclusión

La función encuentra correctamente la programación que minimiza la suma de costos de riego y movilidad, cumpliendo la especificación del proyecto.


