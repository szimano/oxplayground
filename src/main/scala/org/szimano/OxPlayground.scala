package org.szimano

import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.Logger
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode}

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec
import scala.util.Random

class OxPlayground {
  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def simplePlayerBenchmark(): Unit = {
    OxPlayground.simplePlayer(OxPlayground.teams)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def oxPlayerBenchmarkSlide10k(): Unit = {
    OxPlayground.oxPlayer(OxPlayground.teams, 10000)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def oxPlayerBenchmarkSlide20k(): Unit = {
    OxPlayground.oxPlayer(OxPlayground.teams, 20000)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def oxPlayerBenchmarkSlide50k(): Unit = {
    OxPlayground.oxPlayer(OxPlayground.teams, 50000)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def oxPlayerBenchmarkSlide5k(): Unit = {
    OxPlayground.oxPlayer(OxPlayground.teams, 5000)
  }
  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def oxPlayerBenchmarkNoSlide(): Unit = {
    OxPlayground.oxPlayer(OxPlayground.teams, -1)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def catsPlayerBenchmark(): Unit = {
    OxPlayground.catsPlayer(OxPlayground.teams)
  }

}


object OxPlayground {

  val seed = System.currentTimeMillis()

  val log = Logger("OxPlayground")

  val n = 21
  val teamCount = Math.pow(2, n).toInt

  import Team.*

  val teams = List.fill(teamCount)(newTeam)

  def main(args: Array[String]): Unit = {
    log.info (s"ScalaCup Starts! The seed is ${seed}")

    log.info(s"Playing ${teams.length} teams")
    log.debug(s"${teams}")

    val startSimple = System.currentTimeMillis()

    val theResult = simplePlayer(teams)

    val stopSimple = System.currentTimeMillis()

    log.info(s"We have a simple winner! ${theResult}. Finished in ${stopSimple - startSimple} ms")

    log.info("Playing with ox!")

    val startOx = System.currentTimeMillis()

    val oxResult = oxPlayer(teams, 10000)

    val stopOx = System.currentTimeMillis()

    log.info(s"We have an ox winner! ${oxResult}. Finished in ${stopOx - startOx} ms")

    log.info("Playing with cats!")

    val startCats = System.currentTimeMillis()

    val catsResult = catsPlayer(teams)

    val stopCats = System.currentTimeMillis()

    log.info(s"We have a cats winner! ${catsResult}. Finished in ${stopCats - startCats} ms")
  }

  @tailrec
  def simplePlayer(teams: List[Team]) : List[Team] = {
    val teamsResults = teams.sliding(2, 2).collect{case List(a, b) => (a, b)}.toList.collect(Match.play)

    if (teamsResults.length == 1) teamsResults
    else {
      simplePlayer(teamsResults)
    }
  }

  def oxPlayer(teams: List[Team], slider: Int) : List[Team] = {
    import ox.par

    val teamPairs = teams.sliding(2, 2).collect{case List(a, b) => (a, b)}.toList

    val matches = teamPairs.map{(teamA, teamB) => () => Match.play(teamA, teamB)}

    val nextRound = if slider > 0 then
      matches.sliding(slider, slider).flatMap(m => par(m)).toList
    else par(matches).toList

    if (nextRound.length == 1) then nextRound else oxPlayer(nextRound, slider)
  }

  def catsPlayer(teams: List[Team]): List[Team] = {
    import cats.effect.IO
    import cats.syntax.all._

    val teamPairs = teams.sliding(2, 2).collect{case List(a, b) => (a, b)}.toList

    val matchesProgram = teamPairs.parTraverse { (teamA, teamB) =>
      IO(Match.play(teamA, teamB))
    }

    import cats.effect.unsafe.implicits._

    val teamsResults = matchesProgram.unsafeRunSync()

    if (teamsResults.length == 1) teamsResults
    else {
      catsPlayer(teamsResults)
    }
  }

}

object Team {
  val numGen = AtomicLong(0L)
  val randomizer = Random(OxPlayground.seed)
  def newTeam: Team = Team(numGen.getAndAdd(1L), randomizer.nextInt(100),
    randomizer.nextInt(100), randomizer.nextInt(100))
}

case class Team(number: Long, strength: Int, charisma: Int, luck: Int) {
  def power(otherLuck: Int): Int =
    if ((luck - otherLuck) >= 0) then math.max(strength, charisma)
    else math.min(strength, charisma)

  val sumPower: Int = strength + charisma + luck
}

object Match {

  private val log = Logger("Match")

  def playL(teams: List[Team]) : Team = {
    if (teams.length != 2) throw new IllegalArgumentException(s"You need to privide exactly two team but got: ${teams}")

    play(teams.head, teams(1))
  }

  def play(teamA: Team, teamB: Team): Team = {

    log.debug("Playing:")
    log.debug(s"TeamA: ${teamA}")
    log.debug(s"TeamB: ${teamB}")

    val powerA = teamA.power(teamB.luck)
    val powerB = teamB.power(teamA.luck)

    log.debug(s"PowerA = ${powerA}; PowerB = ${powerB}")

    val resA = calculateFromPower(powerA)
    val resB = calculateFromPower(powerB)

    log.debug(s"ResB = ${resA}; ResA = ${resB}")

    if (resA > resB) then teamA
    else if (resA < resB) then teamB
    else if (teamA.sumPower > teamB.sumPower) then teamA
    else if (teamA.sumPower < teamB.sumPower) then teamB
    else olderTeam(teamA, teamB)
  }

  private def calculateFromPower(power: Int): Double =
    Range(0, power).map{i => math.log(i)}.sum

  private def olderTeam(teamA: Team, teamB: Team) : Team =
    if (teamA.number > teamB.number) then teamA else teamB
}