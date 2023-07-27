package org.szimano

import com.typesafe.scalalogging.Logger

object OxPlayground {
  import ox.par

  def main(args: Array[String]): Unit = {
    println ("Szimano rules")

    val log = Logger("OxPlayground")

    log.info("Starting!")

    def computation1: Int =
      Thread.sleep(2000)
      log.info("1 finished")
      1

    def computation2: String =
      Thread.sleep(1000)
      log.info("2 finished")
      "2"

    val result: (Int, String) = par(computation1)(computation2)
    // (1, "2")
    log.info("All done!")

    println(result)
  }
}
