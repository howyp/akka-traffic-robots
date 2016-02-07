package io.github.howyp.test.parsers

import org.scalatest.Matchers
import org.scalatest.matchers.Matcher

import scala.util.parsing.combinator.Parsers

trait ParseResultMatchers { this: Matchers =>

  def parseTo[T](expected: T): Matcher[Parsers#ParseResult[T]] =
    be(expected) compose ( actual => actual.getOrElse(fail(s"Parsing did not succeed:\n$actual")))

  def failParsing = be (a [Parsers#Failure])
}
