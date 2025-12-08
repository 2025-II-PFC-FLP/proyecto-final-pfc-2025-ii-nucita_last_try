package taller
import org.scalameter._
import taller.Tipos._

object BenchMarking {
  val riego = new Riego()
  val generador = new Generador_aleatorio()
  val finca_pequeña = generador.fincaAlAzar(5)
  val finca_mediana = generador.fincaAlAzar(9)
  val finca_grande = generador.fincaAlAzar(11)
  val dist_pequeña = generador.distanciaAlAzar(5)
  val dist_mediana = generador.distanciaAlAzar(9)
  val dist_grande = generador.distanciaAlAzar(11)

  def bench(f: Finca, d: Distancia, name: String): Unit = {
    println(s"\n--- Benchmark: $name ---")

    val tSeq = measure {
      riego.ProgramacionRiegoOptimo(f, d)
    }.value

    val tPar = measure {
      riego.ProgramacionRiegoOptimoPar(f, d)
    }.value

    val speedup = tSeq / tPar

    println(f"Secuencial: $tSeq%.4f ms")
    println(f"Paralelo:   $tPar%.4f ms")
    println(f"Aceleración (speedup): $speedup%.2f x")
  }
  def runAll(): Unit = {
    bench(finca_pequeña,dist_pequeña, "Finca pequeña")
    bench(finca_mediana,dist_mediana, "Finca mediana")
    bench(finca_grande,dist_grande, "Finca grande")
  }
}
