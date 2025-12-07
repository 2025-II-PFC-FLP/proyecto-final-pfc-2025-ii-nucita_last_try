package taller

object Tipos {
  // Un tablon es una tripleta con el tiempo de supervivencia,
  // el tiempo de riego y la prioridad del tablon
  type Tablon = (Int, Int, Int)

  // Una finca es un vector de tablones
  type Finca = Vector[Tablon]
  // Si f : Finca, f(i) = (tsi, tri, pi)

  // La distancia entre dos tablones se representa por
  // una matriz
  type Distancia = Vector[Vector[Int]]

  // Una programación de riego es un vector que asocia
  // cada tablon i con su turno de riego (0 es el primer turno,
  // n-1 es el último turno)
  type ProgRiego = Vector[Int]
  // Si v : ProgRiego, y v.length == n, v es una permutación
  // de {0, ..., n-1} y v(i) es el turno de riego del tablon i
  // para 0 <= i < n

  // El tiempo de inicio de riego es un vector que asocia
  // cada tablon i con el momento del tiempo en que se riega
  type TiempoInicioRiego = Vector[Int]
  // Si t : TiempoInicioRiego y t.length == n, t(i) es la hora a
  // la que inicia a regarse el tablon i
}
