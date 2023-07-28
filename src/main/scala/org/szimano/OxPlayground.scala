package org.szimano

import com.typesafe.scalalogging.Logger
import org.openjdk.jmh.annotations.Benchmark

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec
import scala.util.Random

class OxPlayground {
  @Benchmark
  def simplePlayerBenchmark(): Unit = {
  }

  @Benchmark
  def oxPlayerBenchmark(): Unit = {
  }
}


object OxPlayground {

  val seed = System.currentTimeMillis()

  val log = Logger("OxPlayground")

  val n = 20
  val teamCount = Math.pow(2, n).toInt

  def main(args: Array[String]): Unit = {
    log.info (s"ScalaCup Starts! The seed is ${seed}")

    import Team.*

    val teams = List.fill(teamCount)(newTeam)

    log.info(s"Playing ${teams.length} teams")
    log.debug(s"${teams}")

    val startSimple = System.currentTimeMillis()

    val theResult = simplePlayer(teams)

    val stopSimple = System.currentTimeMillis()

    log.info(s"We have a winner! ${theResult}. Finished in ${stopSimple - startSimple} ms")

    log.info("Playing with ox!")

    val startOx = System.currentTimeMillis()

    val oxResult = oxPlayer(teams)

    val stopOx = System.currentTimeMillis()

    log.info(s"We have a winner! ${oxResult}. Finished in ${stopOx - startOx} ms")
  }

  @tailrec
  def simplePlayer(teams: List[Team]) : List[Team] = {
    val teamsResults = teams.sliding(2, 2).collect{case List(a, b) => (a, b)}.toList.collect(Match.play)

    if (teamsResults.length == 1) teamsResults
    else {
      simplePlayer(teamsResults)
    }
  }

  def oxPlayer(teams: List[Team]) : List[Team] = {
    import ox.par

    val teamPairs = teams.sliding(2, 2).collect{case List(a, b) => (a, b)}.toList

    val matches = teamPairs.map{(teamA, teamB) => () => Match.play(teamA, teamB)}

    val slider = 30000

    val nextRound = matches.sliding(slider, slider).flatMap(m => par(m)).toList

    if (nextRound.length == 1) then nextRound else oxPlayer(nextRound)
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
    Range(0, power).map{i => (math.log(i) + math.log1p(i) + math.log10(i))/3}.sum

  private def olderTeam(teamA: Team, teamB: Team) : Team =
    if (teamA.number > teamB.number) then teamA else teamB
}