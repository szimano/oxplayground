package org.szimano

import com.typesafe.scalalogging.Logger

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec
import scala.util.Random

object OxPlayground {
  import ox.par

  val log = Logger("OxPlayground")

  def main(args: Array[String]): Unit = {
    println ("Szimano rules")


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

    import Team._

    val teamA = newTeam
    val teamB = newTeam

    import Match._

    val teamWon = play(teamA, teamB)

    log.info(s"${teamWon} WINS! Congrats")

    val n = 15
    val teamCount = Math.pow(2, n).toInt

    val teams = List.fill(teamCount)(newTeam)

    log.info(s"${teams}")

    val theResult = simplePlayer(teams)

    log.info(s"We have a winner! ${theResult}")
  }

  @tailrec
  def simplePlayer(teams: List[Team]) : List[Team] = {
    if ((teams.length & (teams.length - 1)) == 0) then {
      // the length is a power of 2, a new round is played!
      log.info("New Round!")
      log.debug(s"${teams.map(_.number).mkString(" ")}")
    }

    if (teams.length == 1) teams
    else {
      val (thisMatch, theRest) = teams.splitAt(2)
      val teamWon = Match.playL(thisMatch)

      simplePlayer((teamWon :: theRest.reverse).reverse)
    }
  }
}

object Team {
  val numGen = AtomicLong(0L)
  val randomizer = Random(System.currentTimeMillis())
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

    val resA = math.log(powerA)
    val resB = math.log(powerB)

    log.debug(s"ResB = ${resA}; ResA = ${resB}")

    if (resA > resB) then teamA
    else if (resA < resB) then teamB
    else if (teamA.sumPower > teamB.sumPower) then teamA
    else if (teamA.sumPower < teamB.sumPower) then teamB
    else olderTeam(teamA, teamB)
  }

  private def olderTeam(teamA: Team, teamB: Team) : Team =
    if (teamA.number > teamB.number) then teamA else teamB
}