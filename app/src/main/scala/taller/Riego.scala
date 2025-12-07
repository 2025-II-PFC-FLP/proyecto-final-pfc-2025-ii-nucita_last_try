package taller
import taller.Tipos._

class Riego {

  def treg(f: Finca , i: Int): Int = {
    f(i)._2
  }

  def calculatiempoderiego(valores: Vector[Int], pi: ProgRiego, vector_final: Vector[Int], suma: Int): Vector[Int] = {
    if (pi.isEmpty) vector_final
    else calculatiempoderiego(valores, pi.tail, vector_final :+ suma, suma + valores(pi.head))

  }

  def mostrar_ordenreal(vector_orden: Vector[Int],orden_riego: ProgRiego, vector_desorganizado: Vector[Int]): Vector[Int] = {
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
    } yield treg(f , i) ).toVector
    val tiemposInicio = calculatiempoderiego(valores_tr, pi, Vector.empty, 0)
    val valores_organizados_tablones = mostrar_ordenreal((0 until f.length).toVector, pi, tiemposInicio)
    valores_organizados_tablones
  }
//
//  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
//    // Calcula el costo de riego del tablón i de la finca f
//    // según la programación de riego pi
//
//  }
//
//  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
//    // Calcula el costo de riego total de la finca f
//    // bajo la programación de riego pi
//
//  }
//
//  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
//    // Calcula el costo de movilidad para regar todos los tablones
//    // según la programación pi y la matriz de distancias d
//
//  }

  def permutaciones(l: Vector[Int]): Vector[Vector[Int]] = l match {
    case Vector() => Vector(Vector.empty)
    case _ =>
      for {
        i <- l
        resto = l.filter(_ != i)
        p <- permutaciones(resto)
      } yield i +: p
  }

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    // Dada una finca de n tablones, devuelve todas las
    // posibles programaciones de riego de la finca
    val n = f.length
    val indices = (0 until n).toVector
    val perms = permutaciones(indices)
    perms
  }

//  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
//    // Devuelve el costo total de regar una finca f dada una
//    // programación de riego pi, calculando en paralelo
//  }
//
//  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
//    // Calcula el costo de movilidad de manera paralela
//  }
//
//  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
//    // Dada una finca devuelve la programación
//    // de riego óptima
//  }
}
