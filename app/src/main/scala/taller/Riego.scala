package taller
import taller.Tipos.{ProgRiego, _}
import common._
import scala.collection.parallel.CollectionConverters._
import scala.annotation.tailrec

class Riego {

  def tsup ( f : Finca , i : Int ) : Int = {
    f ( i ) . _1
  }
  def treg ( f : Finca , i : Int ) : Int = {
    f ( i ) . _2
  }
  def prio( f : Finca , i : Int ) : Int = {
    f ( i ) . _3
  }
  @tailrec
  private def calculatiempoderiego(valores: Vector[Int], pi: ProgRiego, vector_final: Vector[Int], suma: Int): Vector[Int] = {
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

  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    // Calcula el costo de riego del tablón ival de la finca f
    // según la programación de riego pi
    val costoRiegoParcial = tsup(f,i) - tIR(f, pi)(i) - treg(f,i)
    if (costoRiegoParcial >= 0) costoRiegoParcial else prio(f,i) * Math.abs(costoRiegoParcial)
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    val costoRiegoTotal = pi.map(i => costoRiegoTablon(i, f, pi))
    costoRiegoTotal.sum
  }
//
def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
  // Calcula el costo de movilidad para regar todos los tablones
  // según la programación pi y la matriz de distancias d
  def auxCostoMovilidad(f: Finca, pi: ProgRiego, d: Distancia):Int ={
    val resultado_parcial = for {
      i <- pi.indices
      if (i + 1) - f.length < 0
    }yield d(pi(i))(pi(i+1))
    resultado_parcial.sum

  }
  auxCostoMovilidad(f, pi, d )

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

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    // Dada una finca de n tablones, devuelve todas las
    // posibles programaciones de riego de la finca
    val n = f.length
    val indices = (0 until n).toVector
    val perms = permutaciones(indices)
    perms
  }

  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    //    // Dada una finca devuelve la programación
    //    // de riego óptima
    @tailrec
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
      minimo(p.tail,p.head._2, p )
    }
  }

  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
    //    // Devuelve el costo total de regar una finca f dada una
    //    // programación de riego pi, calculando en paralelo
    val (pi1,pi2) = pi.splitAt(Math.ceil(pi.length/2).toInt)
    val (costoRiegoParcial1,costoRiegoParcial2) = parallel(pi1.map(i => costoRiegoTablon(i, f, pi)).sum,pi2.map(i => costoRiegoTablon(i, f, pi)).sum)
    val costoRiegoTotal = costoRiegoParcial1 + costoRiegoParcial2
    costoRiegoTotal
  }

  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    // Calcula el costo de movilidad de manera paralela
    // Calcula el costo de movilidad para regar todos los tablones
    // según la programación pi y la matriz de distancias d
    def auxCostoMovilidad(f: Finca, pi: ProgRiego, d: Distancia):Int ={
      val resultado_parcial = for {
        i <- pi.indices
        if (i + 1) - pi.length < 0
      }yield d(pi(i))(pi(i+1))
      resultado_parcial.sum

    }
    val (pi1,pi2) = pi.splitAt(Math.ceil(pi.length/2).toInt)
    val (costoParcial1, costoParcial2) = parallel(auxCostoMovilidad(f, pi1, d ), auxCostoMovilidad(f, pi2, d ))
    val costoMovilidadTotal  = costoParcial1 + costoParcial2 + d(pi1(pi1.length-1))(pi2(0))
    costoMovilidadTotal
  }
  def permutacionespar(l: Vector[Int]): Vector[Vector[Int]] = l match {
    case Vector() => Vector(Vector.empty)
    case _ =>
      (for {
        i <- l.par
        resto = l.filter(_ != i)
        p <- permutaciones(resto)
      } yield i +: p).toVector
  }
    def generarProgramacionesRiegopar(f: Finca): Vector[ProgRiego] = {
      // Dada una finca de n tablones, devuelve todas las
      // posibles programaciones de riego de la finca
      val n = f.length
      val indices = (0 until n).toVector
      val perms = permutacionespar(indices)
      perms
    }
  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia): (ProgRiego, Int) = {
    //    // Dada una finca devuelve la programación
    //    // de riego óptima
    @tailrec
    def minimo(p:Vector[(ProgRiego,Int)], min:Int, pi : Vector[(ProgRiego,Int)]):(ProgRiego, Int) ={
      if(p.isEmpty) pi.filter(_._2 == min).head
      else minimo(p.tail, if (p.head._2 < min) p.head._2 else min, pi )
    }
    val programaciones = generarProgramacionesRiego(f)
    val (programacion1, programacion2) = programaciones.splitAt(math.ceil(f.length / 2).toInt)


    if (programaciones.isEmpty) (Vector.empty[Int], 0)
    else {
      val (p1,p2) = parallel( programacion1.map { pi =>
        val cr = costoRiegoFincaPar(f, pi)
        val cm = costoMovilidadPar(f, pi, d)
        val total = cr + cm
        (pi, total)
      }, programacion2.map{ pi =>
        val cr = costoRiegoFincaPar(f, pi)
        val cm = costoMovilidadPar(f, pi, d)
        val total = cr + cm
        (pi, total)})
      val (minimo1,minimo2) = parallel(minimo(p1.tail,p1.head._2, p1 ),minimo(p2.tail,p2.head._2, p2 ) )
      if (minimo1._2 <= minimo2._2) minimo1 else minimo2
    }
  }


}
