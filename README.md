This is a showcase of ox (direct style programming) versus other Scala code writing paradigms.

Solved problem:

We need a piece of program that will carry out a Scala Digital Football championship.

There are 2^n teams. Each team has couple properties:

* team number (0-2^n)
* strength (0-99)
* charisma (0-99)
* luck (0-99)

To run a match between team A and B, we check compare lucks on each team.

If they are different, the team with higher luck (team H) picks powerH = max(charisma, strength),
while the team with lower luck (team L), picks powerL = min(charisma, strength)

Then both teams count log(power), and the higher mark wins. 
If the results are equals, then the team with higher sum of strength, charisma and luck win, 
and if thise are still equal, a team with a higher team number wins.

If the lucks are equal, both teams pick max(strength, charisma) and the winner is picked in the same way as above.

The teams are picked randomly in pairs and then they form a typical cup tree, while at the on the the 
championship there is only one winner.