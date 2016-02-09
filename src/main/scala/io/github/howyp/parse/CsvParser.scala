package io.github.howyp.parse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.parsing.combinator.JavaTokenParsers

trait CsvParser[T] extends JavaTokenParsers {
  override protected val whiteSpace = """[ \t]""".r

  def string: Parser[String] = "[^\"]*".r
  def double: Parser[Double] = floatingPointNumber map (_.toDouble)
  def integer: Parser[Int]   = "\\d*".r map (_.toInt)

  def timestamp: Parser[LocalDateTime] = {
    """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".r map (_.replace(' ', 'T')) map (LocalDateTime.parse)
  }

  def quoted[T](parser: Parser[T]) = '"' ~> parser <~ '"'

  def record: Parser[T]

  val parseLine = parse(record, _: String)
}
