/*
 Copyright 2015 Jo Pol
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see http://www.gnu.org/licenses/gpl.html dibl
*/
package dibl

import dibl.Force.Point

import scala.collection.immutable.HashMap
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object Matrix {

  /** @param lines for example: <pre>
    *              ab
    *              cd
    *              </pre>
    * @return <pre>
    *         --------
    *         --------
    *         --abab--
    *         --cdcd--
    *         --abab--
    *         --cdcd--
    *         --------
    *         --------
    *         </pre>
    */
  def extend(lines: Array[String], absRows: Int, absCols: Int): Array[String] = {
    def repeatRows(rows: Array[String]): Array[String] =
      Array.fill((absRows + rows.length) / rows.length)(rows).flatten.take(absRows)
    def repeatCols(row: String): String =
      (row * ((absCols + row.length) / row.length)).take(absCols)
    val marginRows = Array.fill(2)("-" * (absCols + 4))
    marginRows ++ repeatRows(lines.map("--" + repeatCols(_) + "--")) ++ marginRows
  }

  def shift[T: ClassTag](xs: Array[T], n: Int): Array[T] = {
    val modN = n % xs.length
    xs.takeRight(xs.length - modN) ++ xs.take(modN)
  }

  def shiftChars(xs: String, n: Int): String = {
    val modN = n % xs.length
    xs.takeRight(xs.length - modN) ++ xs.take(modN)
  }

  /** Converts a matrix with multiple tuples per cell pointing to source cells
    *
    * @param m tuples have relative positions
    * @return tuples have absolute position
    */
  def toAbsolute(m: M): M = {
    Array.tabulate(m.length)(targetRow =>
      Array.tabulate(m(0).length)(targetCol =>
        for ((relSrcRow, relSrcCol) <- m(targetRow)(targetCol))
          // not allowing zero helps creating footsides, should be done there
          yield (Math.max(1, targetRow + relSrcRow), Math.max(1, targetCol + relSrcCol))
      )
    )
  }

  /** Counts the numbers of links arriving at or leaving from a cell
    *
    * @param m tuples have absolute positions
    * @return a matrix with the same dimensions as m
    */
  def countLinks(m: M): Array[Array[Int]] = {
    val links = Array.fill(m.length,m(0).length)(0)
    for {
      row <- m.indices
      col <- m(row).indices
      _  = links(row)(col) += m(row)(col).length
      (srcRow,srcCol) <- m(row)(col)
      _ = links(srcRow)(srcCol) += 1
    } {}
    links
  }

  /** Translates a character in a matrix string into relative links with two source nodes.
    * The source nodes are defined with relative (row,column) numbers.
    * A node can be connected in eight directions, but source nodes are not found downwards.
    */
  def toRelativeSources(c: Char): SrcNodes = c.toUpper match {
    // ascii art of incoming links for a node
    case '0' => SrcNodes(Cell(-1, 1), Cell(0, 1)) // .../_
    case '1' => SrcNodes(Cell(-1, 0), Cell(0, 1)) // ..|._
    case '2' => SrcNodes(Cell(-1, -1), Cell(0, 1)) // .\.._
    case '3' => SrcNodes(Cell(0, -1), Cell(0, 1)) // _..._
    case '4' => SrcNodes(Cell(-1, 0), Cell(-1, 1)) // ..|/.
    case '5' => SrcNodes(Cell(-1, -1), Cell(-1, 1)) // .\./.
    case '6' => SrcNodes(Cell(0, -1), Cell(-1, 1)) // _../.
    case '7' => SrcNodes(Cell(-1, -1), Cell(-1, 0)) // .\|..
    case '8' => SrcNodes(Cell(0, -1), Cell(-1, 0)) // _.|..
    case '9' => SrcNodes(Cell(0, -1), Cell(-1, -1)) // _\...
    // double length for vertical link only
    case 'A' => SrcNodes(Cell(-2, 0), Cell(0, 1)) // ..|._
    case 'B' => SrcNodes(Cell(-2, 0), Cell(-1, 1)) // ..|/.
    case 'C' => SrcNodes(Cell(-1, -1), Cell(-2, 0)) // .\|..
    case 'D' => SrcNodes(Cell(0, -1), Cell(-2, 0)) // _.|..
    // double length for horizontal links to
    case 'E' => SrcNodes(Cell(-1, 1), Cell(0, 2)) // .../_
    case 'F' => SrcNodes(Cell(-1, 0), Cell(0, 2)) // ..|._
    case 'G' => SrcNodes(Cell(-2, 0), Cell(0, 2)) // ..|._
    case 'H' => SrcNodes(Cell(-1, -1), Cell(0, 2)) // .\.._
    case 'I' => SrcNodes(Cell(0, -1), Cell(0, 2)) // _..._
    case 'J' => SrcNodes(Cell(0, -2), Cell(0, 1)) // _..._
    case 'K' => SrcNodes(Cell(0, -2), Cell(0, 2)) // _..._
    case 'L' => SrcNodes(Cell(0, -2), Cell(-1, 1)) // _../.
    case 'M' => SrcNodes(Cell(0, -2), Cell(-1, 0)) // _.|..
    case 'N' => SrcNodes(Cell(0, -2), Cell(-2, 0)) // _.|..
    case 'O' => SrcNodes(Cell(0, -2), Cell(-1, -1)) // _\...
    case _ => SrcNodes() // not used/valid node
  }

  /** Matches any sequence of characters that are not a key of [[toRelativeSources]] */
  val separator: String = "[^-0-9A-O]+"

  /** Split on sequences of characters that are not a key of [[toRelativeSources]].
    *
    * @param str compact matrix specifying a 2-in-2out-directed graph
    * @return Failure if resulting lines do not have equal length,
    *         or are longer than 26 as stitch ID's tag columns with a single capital letter.
    */
  def toValidMatrixLines(str: String): Try[Array[String]] = {
    val lines = str.split(separator)
    if (lines.map(_.length).sortBy(n => n).distinct.length != 1)
      Failure(new scala.Exception(s"Matrix lines have varying lengths: $str ==> ${lines.map(s => s"$s(${s.length})").mkString(", ")}"))
    else if (lines(0).length > 26)
      Failure(new scala.Exception(s"Matrix lines exceeds maximum length of 26: $str ==> ${lines.mkString(", ")}"))
    else Success(lines)
  }
}