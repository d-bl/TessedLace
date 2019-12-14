/*
 Copyright 2016 Jo Pol
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
package fte

import java.io.File

import fte.data.GraphCreator
import fte.ui.SVGRender

import scala.util.{Failure, Success, Try}

object Demo {
  def main(args: Array[String]): Unit = {
    val dir = new File("target/test/fte-demo")
    dir.mkdirs()
    dir.listFiles().foreach(_.delete())
    Seq(
      "bandage&tileStitch=ctc&patchWidth=3&patchHeight=4&tile=1,8&tileStitch=ctc&shiftColsSW=0&shiftRowsSW=2&shiftColsSE=1&shiftRowsSE=2",
      "sheered&tileStitch=ct&patchWidth=6&patchHeight=4&tile=l-,-h&shiftColsSW=0&shiftRowsSW=2&shiftColsSE=2&shiftRowsSE=2",
      // the patterns above even fail as pair diagrams, increasing the patch size doesn't help
      "torchon&tileStitch=ctc&patchWidth=6&patchHeight=4&tile=5-,-5&shiftColsSW=0&shiftRowsSW=2&shiftColsSE=2&shiftRowsSE=2",
      "rose&tileStitch=ctc&patchWidth=12&patchHeight=4&tile=5831,-4-7&shiftColsSW=-2&shiftRowsSW=2&shiftColsSE=2&shiftRowsSE=2",
      "pinwheel&tileStitch=ct&patchWidth=12&patchHeight=8&tile=586-,-4-5,5-21,-5-7&shiftColsSE=4&shiftRowsSE=4&shiftColsSW=0&shiftRowsSW=4&",
      "whiting=F14_P193&tileStitch=ct&patchWidth=24&patchHeight=28&tile=-XX-XX-5,C-X-X-B-,-C---B-5,5-C-B-5-,-5X-X5-5,5XX-XX5-,-XX-XX-5,C-----B-,-CD-AB--,A11588D-,-78-14--,A11588D-,-78-14--,A11588D-&shiftColsSW=0&shiftRowsSW=14&shiftColsSE=8&shiftRowsSE=14",
      // for now prefixed id's with X in the next pattern to just apply the tileStitch everywhere
      "braid&patchWidth=18&tileStitch=ctct&patchHeight=8&tile=-B-C-y,B---cx,xC-B-x,m-5-b-&shiftColsSW=0&shiftRowsSW=4&shiftColsSE=6&shiftRowsSE=4&Xa4=llcttct&Xe4=rrcttctrr",
    ).zipWithIndex.foreach { case (query, i) =>
      Try(GraphCreator.fromDiagram(query)) match {
        case Success(None) => println(s"$i has no solution")
        case Failure(e) => e.printStackTrace()
        case Success(Some(graph)) =>
          new SVGRender().draw(graph, s"$dir/$i.svg")
      }
    }
  }
}