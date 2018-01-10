package dibl

import scala.scalajs.js.annotation.JSExport

@JSExport
class Config(urlQuery: String) {
  println(urlQuery)

  private val keyValueStrings: Seq[String] = urlQuery
    .split("&")
    .filter(_.matches(".+=.*"))

  private val fields: Map[String, String] = keyValueStrings
    .map { kv: String => (kv.replaceAll("=.*", ""), kv.replaceAll(".*=", "")) }
    .toMap

  private def getMatrix(key: String): Array[String] = {
    fields.getOrElse(key, "").toLowerCase.split("[^-a-z0-9]+")
  }

  // TODO defend against unequal rows lengths
  val leftMatrix: Array[String] = getMatrix("footside")
  val centerMatrix: Array[String] = getMatrix("tile")
  val rightMatrix: Array[String] = getMatrix("headside")

  // TODO defaults based on the dimensions of the above matrices
  @JSExport
  val totalRows: Int = fields.getOrElse("repeatHeight", "12").replaceAll("[^0-9-]", "").toInt
  val centerCols: Int = fields.getOrElse("repeatWidth", "12").replaceAll("[^0-9-]", "").toInt
  val shiftRowsSE: Int = fields.getOrElse("shiftRowsSE", "12").replaceAll("[^0-9-]", "").toInt
  val shiftRowsSW: Int = fields.getOrElse("shiftRowsSW", "12").replaceAll("[^0-9-]", "").toInt
  val shiftColsSE: Int = fields.getOrElse("shiftColsSE", "12").replaceAll("[^0-9-]", "").toInt
  val shiftColsSW: Int = fields.getOrElse("shiftColsSW", "12").replaceAll("[^0-9-]", "").toInt

  private val leftMarginWidth =
    if (leftMatrix.length > 0 && leftMatrix.head.trim.length > 0)
      2 + leftMatrix.head.length
    else 0
  private val offsetRightMargin =
    if (rightMatrix.length > 0 && rightMatrix.head.trim.length > 0)
      leftMarginWidth + centerCols
    else 0

  @JSExport
  val totalCols: Int = centerCols +
      leftMarginWidth +
      (if (offsetRightMargin == 0) 0
       else 2 + rightMatrix.head.length)

  case class Item(vectorCode: Char = '-', stitch: String = "ctc", isOpaque: Boolean = false)

  val itemMatrix: Array[Array[Item]] = Array.fill[Array[Item]](totalRows)(
    Array.fill[Item](totalCols)(Item())
  )

  if (leftMarginWidth > 0)
    for {r <- 0 until totalRows} {
      for {c <- 0 until leftMatrix.head.length} {
        val rSource = r % leftMatrix.length
        val id = Stitches.toID(rSource, c + 2)
        val stitch = fields.getOrElse(id, "ctc")
        itemMatrix(r)(c + 2) = Item(leftMatrix(rSource)(c), stitch, r < leftMatrix.length)
      }
    }
  if (offsetRightMargin > 0)
    for {r <- 0 until totalRows} {
      for {c <- 0 until rightMatrix.head.length} {
        val rSource = r % rightMatrix.length
        val id = Stitches.toID(rSource, c + offsetRightMargin)
        val stitch = fields.getOrElse(id, "ctc")
        itemMatrix(r)(c + offsetRightMargin) = Item(rightMatrix(rSource)(c), stitch, r < rightMatrix.length)
      }
    }

  // See https://github.com/d-bl/GroundForge/blob/7b1effb/docs/help/images/shift-directions.png
  //noinspection RangeToIndices
  for { // TODO reduce ranges to avoid if
    i <- 0 until totalRows
    j <- -centerCols until centerCols
    translateRow = (i * shiftRowsSE) + (j * shiftRowsSW)
    translateCol = (i * shiftColsSE) + (j * shiftColsSW)
    r <- 0 until centerMatrix.length
    c <- 0 until centerMatrix.head.length
  } {
    // t in rt/ct stands for target cell, r and c for row and col
    val rt = r + translateRow
    val ct = c + translateCol
    if (rt >= 0 && ct >= 0 && rt < totalRows && ct < centerCols) {
      val id = Stitches.toID(r, c + leftMarginWidth)
      val stitch = fields.getOrElse(id, "ctc")
      val vectorCode = centerMatrix(r)(c)
      itemMatrix(rt)(ct + leftMarginWidth) = Item(vectorCode, stitch, r == rt && c == ct)
    }
  }

  @JSExport
  val encodedMatrix: String = itemMatrix
    .map(_.map(_.vectorCode).mkString)
    .mkString(",")
    .toUpperCase
}

@JSExport
object Config {

  @JSExport
  def create(urlQuery: String): Config = new Config(urlQuery)
}