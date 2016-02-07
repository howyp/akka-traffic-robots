package io.github.howyp

import org.scalatest.matchers.Matcher
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source
import scala.util.parsing.combinator.Parsers

class TubeStationSpec extends FreeSpec with Matchers {
  "Tube Stations" - {
    "should be able to be parsed from a CSV line" - {
      "given a valid example then it should extract name and location" in {
        val csvLine = "\"Blackhorse Road\",51.585777,-0.039626"
        TubeStation.parse(TubeStation.station, csvLine) should parseTo (TubeStation("Blackhorse Road",Location(51.585777,-0.039626)))
      }
      "given a invalid line then it should not parse" in {
        val csvLine = "\"Blackhorse Road,51.585777,-0.039626"
        TubeStation.parse(TubeStation.station, csvLine) should failParsing
      }
    }
    "should be loadable as a set from a file" - {
      "given a sample CSV string then it should return a list of TubeStations" in {
        val csvLines =
          """|"Beckton Park (DLR)",51.509994,0.055534
             |"Becontree",51.539585,0.12688
             |"Belsize Park",51.550191,-0.163974""".stripMargin
        TubeStation.parse(TubeStation.stations, csvLines) should parseTo (
          List(
            TubeStation("Beckton Park (DLR)",Location(51.509994,0.055534)),
            TubeStation("Becontree",Location(51.539585,0.12688)),
            TubeStation("Belsize Park",Location(51.550191,-0.163974))
          )
        )
      }
    }
  }

  def parseTo[T](expected: T): Matcher[Parsers#ParseResult[T]] =
    be(expected) compose ( actual => actual.getOrElse(fail(s"Parsing did not succeed:\n$actual")))

  def failParsing = be (a [Parsers#Failure])
}
