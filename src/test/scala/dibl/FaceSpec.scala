package dibl

import dibl.Face.facesFrom
import dibl.proto.TilesConfig
import fte.data.GraphCreator.inCenterBottomTile
import org.scalatest.{FlatSpec, Matchers}

class FaceSpec extends FlatSpec with Matchers {
  "facesFrom" should "create proper thread link sequences" in {
    val s = "torchon&tileStitch=ct&patchWidth=6&patchHeight=4&tile=5-,-5&shiftColsSW=0&shiftRowsSW=2&shiftColsSE=2&shiftRowsSE=2"
    implicit val config: TilesConfig = TilesConfig(s)
    implicit val diagram: Diagram = ThreadDiagram(NewPairDiagram.create(config))
    implicit val scale: Int = 2
    val topoLinks = TopoLink.simplify(diagram.links.filter(inCenterBottomTile))
    topoLinks.mkString(";") shouldBe
      "b20,b22,t,f;a11,b22,f,f;a12,b21,t,t;b20,b21,f,t;a12,b20,t,f;a11,b20,f,t;a10,a12,t,f;b21,a12,f,f;b22,a11,t,t;a10,a11,f,t;b22,a10,t,f;b21,a10,f,t"
    facesFrom(topoLinks).mkString("\n") shouldBe
      """b20->b21,b21->a12,a12->b20 ; b20->b22,b22->a11,a11->b20
        |a12->b21 ; a12->b20,b20->b21
        |a11->b20,b20->b22 ; a11->b22
        |b22->a11 ; b22->a10,a10->a11
        |a10->a11,a11->b22,b22->a10 ; a10->a12,a12->b21,b21->a10
        |b21->a10,a10->a12 ; b21->a12""".stripMargin
  }
  it should "create proper pair link sequences for brick pattern" in {
    val s = "rose&tileStitch=ctc&patchWidth=12&patchHeight=4&tile=5831,-4-7,&shiftColsSW=-2&shiftRowsSW=2&shiftColsSE=2&shiftRowsSE=2"
    implicit val config: TilesConfig = TilesConfig(s)
    implicit val diagram: Diagram = NewPairDiagram.create(config)
    implicit val scale: Int = 1
    val topoLinks = TopoLink.simplify(diagram.links.filter(inCenterBottomTile))
    facesFrom(topoLinks).mkString("\n") shouldBe
      """b1->b2 ; b1->c1,c1->b2
        |d2->a1,a1->b1 ; d2->b1
        |b1->c1 ; b1->b2,b2->d1,d1->c1
        |d1->c1,c1->d2 ; d1->d2
        |c1->b2,b2->a1 ; c1->d2,d2->a1
        |b2->d1 ; b2->a1,a1->d1""".stripMargin
    topoLinks.mkString(";") shouldBe
      "? b1,c1,t,t;d1,c1,f,t;b2,d1,t,t;a1,d1,f,t;b2,a1,t,f;d2,a1,f,t;a1,b1,t,f;d2,b1,f,f;c1,d2,t,t;d1,d2,f,f;b1,b2,t,f;c1,b2,f,f"
  }
}