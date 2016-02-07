package io.github.howyp.parse

import scala.util.parsing.combinator.JavaTokenParsers

trait CsvParser[T] extends JavaTokenParsers {
  override protected val whiteSpace = """[ \t]""".r

  def double: Parser[Double] = floatingPointNumber map (_.toDouble)
  def integer: Parser[Int] = "\\d*".r map (_.toInt)

  def quotedString: Parser[String] = '"' ~> "[^\"]*".r <~ '"'
  def quotedDouble: Parser[Double] = '"' ~> double     <~ '"'

  def record: Parser[T]
  def lines: Parser[List[T]] = repsep(record, "\n")

  val parseLine = parse(record, _: String)
  val parseLines = parse(lines, _: String)
}
