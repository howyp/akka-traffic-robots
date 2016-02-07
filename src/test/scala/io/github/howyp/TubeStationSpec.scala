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
  }
}
