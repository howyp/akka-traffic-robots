package io.github.howyp

import io.github.howyp.test.parsers.ParseResultMatchers
import org.scalatest.{FreeSpec, Matchers}

class TubeStationSpec extends FreeSpec with Matchers with ParseResultMatchers {
  "Tube Stations" - {
    "can be parsed from a CSV line" - {
      "given a valid example then it should extract name and location" in {
        TubeStation.parseLine("\"Blackhorse Road\",51.585777,-0.039626") should parseTo (
          TubeStation(name = "Blackhorse Road",location = Location(51.585777, -0.039626))
        )
      }
      "given a line with a missing quote then it should not parse" in {
        TubeStation.parseLine("\"Blackhorse Road,51.585777,-0.039626") should failParsing
      }
      "given a line with a missing field then it should not parse" in {
        TubeStation.parseLine("\"Blackhorse Road\",-0.039626") should failParsing
      }
      "given a line with an incorrectly typed field then it should not parse" in {
        TubeStation.parseLine("\"Blackhorse Road\",test,-0.039626") should failParsing
      }
    }
    "should be loadable as a set from a file" - {
      "given a sample CSV string then it should return a list of tube stations" in {
        val csvLines =
          """|"Beckton Park (DLR)",51.509994,0.055534
             |"Becontree",51.539585,0.12688
             |"Belsize Park",51.550191,-0.163974""".stripMargin
        TubeStation.parseLines(csvLines) should parseTo (
          List(
            TubeStation(name = "Beckton Park (DLR)", location = Location(51.509994, 0.055534)),
            TubeStation(name = "Becontree",          location = Location(51.539585, 0.12688)),
            TubeStation(name = "Belsize Park",       location = Location(51.550191, -0.163974))
          )
        )
      }
    }
  }
}
