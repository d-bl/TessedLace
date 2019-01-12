/*
 Copyright 2018 Jo Pol
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

import dibl.Stitches.defaultColorValue

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("TilesConfig") case class TilesConfig(urlQuery: String) {
  println(urlQuery)

  private val keyValueStrings: Seq[String] = urlQuery
    .split("&")
    .filter(_.matches(".+=.*"))

  private val fields: Map[String, String] = keyValueStrings
    .map { kv: String => (kv.replaceAll("=.*", ""), kv.replaceAll(".*=", "")) }
    .toMap

  private def getMatrix(key: String): Seq[String] = {
    fields.getOrElse(key, "").toLowerCase.split("[^-a-z0-9]+").map(_.trim)
  }

  // TODO defend against unequal rows lengths
  val leftMatrix: Seq[String] = getMatrix("footside")
  val rightMatrix: Seq[String] = getMatrix("headside")
  private val centerMatrix: Seq[String] = getMatrix("tile")

  private val leftMatrixStitch: String = fields.getOrElse("footsideStitch", "ctctt")
  private val rightMatrixStitch: String = fields.getOrElse("headsideStitch", "ctctt")
  private val centerMatrixStitch: String = fields.getOrElse("tileStitch", "ctc")

  @JSExport
  val leftMatrixCols: Int = Option(leftMatrix.head).map(_.length).getOrElse(2)
  @JSExport
  val centerMatrixCols: Int = Option(centerMatrix.head).map(_.length).getOrElse(5)
  @JSExport
  val rightMatrixCols: Int = Option(rightMatrix.head).map(_.length).getOrElse(2)

  private val centerMatrixRows: Int = centerMatrix.length

  @JSExport
  val maxTileRows: Int = Math.max(centerMatrixRows, Math.max(leftMatrix.length, rightMatrix.length))

  // TODO defaults based on the dimensions of the above matrices
  @JSExport
  val totalRows: Int = fields.getOrElse("patchHeight", "12").safeToInt
  private val centerCols: Int = fields.getOrElse("patchWidth", "12").safeToInt
  val shiftRowsSE: Int = fields.getOrElse("shiftRowsSE", "12").safeToInt
  val shiftRowsSW: Int = fields.getOrElse("shiftRowsSW", "12").safeToInt
  val shiftColsSE: Int = fields.getOrElse("shiftColsSE", "12").safeToInt
  val shiftColsSW: Int = fields.getOrElse("shiftColsSW", "12").safeToInt

  private val leftMarginWidth = leftMatrix.head.trim.length
  private val offsetRightMargin = leftMarginWidth + centerCols

  @JSExport
  val totalCols: Int = centerCols +
    leftMarginWidth +
    (if (offsetRightMargin == 0) 0
     else rightMatrix.head.length)

  case class Item(id: String,
                  vectorCode: Char = '-',
                  stitch: String = "",
                  isOpaque: Boolean = false,
                  relativeSources: SrcNodes
                 ) {
    lazy val noStitch: Boolean = stitch.isEmpty || stitch == "-"
    lazy val color: Option[String] = Option(defaultColorValue(stitch))
      .filter(_.nonEmpty)
  }

  private val itemMatrix: Array[Array[Item]] = Array.fill[Array[Item]](totalRows)(
    Array.fill[Item](totalCols)(Item("", relativeSources = Array.empty))
  )
  def getItemMatrix: Seq[Seq[Item]] = itemMatrix.map(_.toSeq)

  lazy val nrOfPairsOut: Seq[Seq[Int]] = {
    val rows: Int = itemMatrix.length
    val cols: Int = itemMatrix.head.length
    val pairsOut = Array.fill[Array[Int]](rows)(Array.fill[Int](cols)(0))
    for {
      r <- itemMatrix.indices
      c <- itemMatrix(r).indices
    } {
      itemMatrix(r)(c).relativeSources
        .foreach { case (relativeSourceRow, relativeSourceCol) =>
          val row: Int = r + relativeSourceRow
          val col: Int = c + relativeSourceCol
          if (row >= 0 && col >= 0 && col < cols && row < rows) {
            pairsOut(row)(col) += 1
          }
        }
    }
    pairsOut.toSeq.map(_.toSeq)
  }

  // repeat foot-side / head-side

  if (leftMarginWidth > 0) replaceItems(leftMatrix, 0, leftMatrixStitch)
  if (offsetRightMargin > 0) replaceItems(rightMatrix, offsetRightMargin, rightMatrixStitch)

  private def replaceItems(inputMatrix: Seq[String], offset: Int, defaultStitch: String): Unit = {
    for {
      row <- itemMatrix.indices
      rSource = row % inputMatrix.length
      col <- inputMatrix(rSource).indices
    } {
      val id = Stitches.toID(rSource, col + offset)
      val vectorCode = inputMatrix(rSource)(col)
      val stitch = if (vectorCode == '-') ""
                   else fields.getOrElse(id, defaultStitch)
      itemMatrix(row)(col + offset) = Item(
        id,
        vectorCode,
        stitch,
        row < inputMatrix.length,
        relativeSources = Matrix.toRelativeSources(vectorCode)
      )
    }
  }

  // repeat tiles, see: https://github.com/d-bl/GroundForge/blob/2e96d8b5/docs/help/images/shift-directions.png

  for { // TODO reduce ranges to avoid if
    i <- itemMatrix.indices
    j <- -centerCols until centerCols
    translateRow = (i * shiftRowsSE) + (j * shiftRowsSW)
    translateCol = (i * shiftColsSE) + (j * shiftColsSW)
    r <- centerMatrix.indices
    c <- centerMatrix(r).indices
  } {
    // t in rt/ct stands for target cell, r and c for row and col
    val rt = r + translateRow
    val ct = c + translateCol
    if (rt >= 0 && ct >= 0 && rt < totalRows && ct < centerCols) {
      val id = Stitches.toID(r, c + leftMarginWidth)
      val vectorCode = centerMatrix(r)(c)
      val stitch = if (vectorCode == '-') ""
                   else fields.getOrElse(id, centerMatrixStitch)
      itemMatrix(rt)(ct + leftMarginWidth) = Item(id, vectorCode, stitch, r == rt && c == ct,
        relativeSources = Matrix.toRelativeSources(vectorCode))
    }
  }

  // rejoin links to ignored stitches

  private def isFringe(row: Int, col: Int) = {
    row < 0 || col < 0 || col >= itemMatrix(row).length
  }

  for {
    row <- itemMatrix.indices
    col <- itemMatrix(row).indices
  } {
    // println(s"============== $row, $col")
    def indirectSource(relativeSource: (Int, Int),
                       firstOrLast: Array[(Int, Int)] => Option[(Int, Int)]
                      ): (Int, Int) = {
      val (relativeSourceRow, relativeSourceCol) = relativeSource
      val absSrcRow = row + relativeSourceRow
      val absSrcCol = col + relativeSourceCol
      if (isFringe(absSrcRow, absSrcCol)) relativeSource
      else {
        lazy val srcItem = itemMatrix(absSrcRow)(absSrcCol)
        if (!srcItem.noStitch) relativeSource
        else {
          // srcItem is the point of "<" or ">"; change it into "|" (into the top)
          val (srcRow, srcCol) = firstOrLast(srcItem.relativeSources).getOrElse((0, 0))
          if (row + srcRow < 0) relativeSource
          else (relativeSourceRow + srcRow, relativeSourceCol + srcCol)
        }
      }
    }
    def replace(item: Item): Boolean = {
      if (item.relativeSources.isEmpty || item.noStitch) false
      else {
        val Array(directLeft, directRight) = item.relativeSources
        val indirectLeft = indirectSource(directLeft, _.lastOption)
        val indirectRight = indirectSource(directRight, _.headOption)
        val replacement = SrcNodes(indirectLeft,indirectRight)
        if (item.relativeSources sameElements replacement) false
        else {
          //println(s"replacing ${item.id} ${item.relativeSources.mkString} ${replacement.mkString}")
          itemMatrix(row)(col) = item.copy(relativeSources = replacement)
          true
        }
      }
    }
    while (replace(itemMatrix(row)(col))){}
  }

  private def logRow(row: Int) = {
    itemMatrix(row).map(item => s"${ item.id }${ item.relativeSources.mkString } ").mkString
  }

  for {
    row <- itemMatrix.indices
    col <- itemMatrix(row).indices
  } {
    // now everything is reconnected around ignored stitches
    val item = itemMatrix(row)(col)
    if(item.noStitch) {
      // drop links into ignored stitches
      itemMatrix(row)(col) = item.copy(relativeSources = SrcNodes())
    }
    else if (item.relativeSources.nonEmpty) {
      // TODO a fringe like "vv" with an ignored stitch
      //  caused two pairs starting with a single node, causing trouble for droste steps
      val Array(left,right) = item.relativeSources
      if (left == right) {
        // replace Y with V; the tail of the Y are two links connecting the same two nodes
        val (sharedRow, sharedCol) = left
        val srcRow = row + sharedRow
        val srcCol = col + sharedCol
        if (!isFringe(srcRow, srcCol)) {
          val srcItem = itemMatrix(srcRow)(srcCol)
          val Array((leftRow, leftCol), (rightRow, rightCol)) = srcItem.relativeSources
          val newSrcNodes = SrcNodes((sharedRow + leftRow, sharedCol + leftCol), (sharedRow + rightRow, sharedCol + rightCol))
          itemMatrix(row)(col) = itemMatrix(row)(col).copy(relativeSources = newSrcNodes)
          itemMatrix(srcRow)(srcCol) = srcItem.copy(relativeSources = SrcNodes())
        }
      }
    }
  }

  /**
   * Get links for one tile.
   *
   * @param diagram A diagram created from this object.
   *                Use diagrams with the original nodes for transformation from pairs to threads to pairs etc.
   *                The result is not defined when using nodes with changed values for the x/y properties
   *                for any of the transformation steps. Plaits with more than 12 half stitches (ct)
   *                might cause a problem with duplicate ids in transformed diagrams.
   * @param scale   Use value one for the initial pair diagram,
   *                multiply by 2 for each transition from pair to thread diagram.
   *
   * Requirements:
   * - The values for totalRows alias patchHeight respective totalCols alias patchWidth
   *   must add at least 4 rows/cols to the dimensions of the centerMatrix alias tile.
   * - No gaps between tiles.
   * - As for now: the leftMatrix and rightMatrix must be empty.
   *
   * @return An empty array on some types of invalid arguments, the type of error is logged to standard-out.
   *
   *         Changes to the diagram won't affect previously returned results, nor the other way around.
   *
   *         Node objects inside the tile are different from those outside the tile.
   *         Nodes outside the tile will have an id property shared by a node inside the tile on the
   *         opposite side. Where along the opposite side is defined by the four shift properties.
   *
   *         Each transformation from pairs to threads puts more nodes at the same x/y positions.
   *         The start of their id-s will be identical, the tail of their id-s will be different.
   */
  @JSExport
  def linksOfCenterTile(diagram: Diagram, scale: Int): Array[LinkedNode] = {
    val links: Seq[LinkedNode] = {

      lazy val minWidthForBricks = centerMatrixCols + 4
      lazy val minHeightForBricks = centerMatrixRows + 4
      lazy val minWidth = shiftColsSE + centerMatrixCols + 2
      lazy val minHeight = shiftRowsSE + centerMatrixRows + 2
      lazy val isHBrick =
        shiftRowsSE == shiftRowsSW &&
          shiftRowsSE == centerMatrixRows &&
          shiftColsSE - shiftColsSW == centerMatrixCols
      lazy val isVBrick =
        shiftColsSE == shiftColsSW &&
          shiftColsSE == centerMatrixCols &&
          shiftRowsSE - shiftRowsSW == centerMatrixRows

      def invalidMin(dimension: String, value: Int): Seq[LinkedNode] = {
        invalid(s"patch $dimension should be at least $value")
      }
      def invalid(msg: String): Seq[LinkedNode] = {
        println(msg)
        Seq.empty
      }
      // Offsets and distances between the nodes on the initial square grid:
      // https://github.com/d-bl/GroundForge/blob/94342eb/src/main/scala/dibl/NewPairDiagram.scala#L20
      // https://github.com/d-bl/GroundForge/blob/268b2e2/src/main/scala/dibl/ThreadDiagram.scala#L105-L107
      // In other words: 15 between rows/cols, 2 rows/cols allowance for the fringe.
      //                 Another 2 rows/cols allowance to have four links on all nodes.
      if (!leftMatrix.mkString.isEmpty || !rightMatrix.mkString.isEmpty)
        invalid("foot sides not supported")
      else if (totalCols < minWidthForBricks) invalidMin("width", minWidthForBricks)
      else if (totalRows < minHeightForBricks) invalidMin("height", minHeightForBricks)
      else if (isHBrick || isVBrick) diagram.tileLinks(
        scale * 52.5,
        scale * (52.5 + 15 * centerMatrixRows),
        scale * (52.5 + 15 * centerMatrixCols),
        scale * 52.5,
      ) // TODO find the first tile closest to NE but at least 2 rows/cols to the SW
      else if (shiftColsSE < 2 && shiftRowsSE < 2) invalid("type of tiling is not suported")
      else if (minWidth > totalCols) invalidMin("patch width", minWidth)
      else if (minHeight > totalRows) invalidMin("height", minHeight)
      else {
        val offsetCols = (1.5 + shiftColsSE) * 15
        val offsetRows = (1.5 + shiftRowsSE) * 15
        diagram.tileLinks(
          scale * offsetCols,
          scale * (offsetRows + 15 * centerMatrixRows),
            scale * (offsetCols + 15 * centerMatrixCols),
            scale * offsetRows,
        )
      }
    }
    if (links.exists(l => l.core.id.isEmpty || l.sources.keys.exists(_.id.isEmpty) || l.targets.keys.exists(_.id.isEmpty)))
      Seq.empty
    else {
      links
    }
  }.toArray
}
