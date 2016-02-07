package io.github.howyp

import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{Matchers, FreeSpec}

import scala.util.parsing.combinator.Parsers

class TubeStationSpec extends FreeSpec with Matchers {
  "Tube Stations" - {
    "should be loadable as a set from a file" in pending
    "should be able to be parsed from a CSV line" - {
      "given a valid example then it should extract name and location" in {
        val csvLine = "\"Blackhorse Road\",51.585777,-0.039626"
        TubeStation.parse(TubeStation.station, csvLine) should parseTo (TubeStation("Blackhorse Road",Location(51.585777,-0.039626)))
      }
    }
  }

  def parseTo[T](expected: T): Matcher[Parsers#ParseResult[T]] =
    be(expected) compose ( actual => actual.getOrElse(fail(s"Parsing did not succeed:\n$actual")))
}
