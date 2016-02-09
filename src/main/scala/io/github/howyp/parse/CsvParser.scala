package io.github.howyp.parse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.parsing.combinator.JavaTokenParsers

trait CsvParser[T] extends JavaTokenParsers {
  override protected val whiteSpace = """[ \t]""".r

  def double: Parser[Double] = floatingPointNumber map (_.toDouble)
  def integer: Parser[Int] = "\\d*".r map (_.toInt)
  def timestamp: Parser[LocalDateTime] = {
    """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".r map (_.replace(' ', 'T')) map (LocalDateTime.parse)
  }

  def quotedString: Parser[String]           = '"' ~> "[^\"]*".r    <~ '"'
  def quotedDouble: Parser[Double]           = '"' ~> double        <~ '"'
  def quotedTimestamp: Parser[LocalDateTime] = '"' ~> timestamp     <~ '"'

  def record: Parser[T]
  def lines: Parser[List[T]] = repsep(record, "\n")

  val parseLine = parse(record, _: String)
}
