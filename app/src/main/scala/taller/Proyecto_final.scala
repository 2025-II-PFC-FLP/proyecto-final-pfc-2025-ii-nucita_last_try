package taller
import taller.BenchMarking._
object Proyecto_final {
  def main(args: Array[String]): Unit = {

    println("===== BENCHMARK Riego optimo =====")

    // ejecutar el benchmarking
      BenchMarking.runAll()

    println("===== FIN DEL BENCHMARK =====")
  }
}
