import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.ojai.ScalaOjaiTesting
import org.scalatest.{FlatSpec, Matchers}

class MapRDBSessionTest extends FlatSpec with Matchers with ScalaOjaiTesting {

  import scala.collection.JavaConverters._

   "MapRDBSession" should "always return the same store" in {
    val session = MapRDBSession(getConnection())

    val storeT1 = session.getStore("table")

    storeT1.insert(getConnection().newDocument().setId("5").set("name", "Joe"))

    session.getStore("table").find().asScala.size should be(1)
  }

   it should "be able to keep track of multiple stores" in {
    val session = MapRDBSession(getConnection())

    (1 to 100)
      .map(n => session.getStore(n.toString))
      .foreach(_.insert(getConnection().newDocument().setId("5").set("name", "Joe")))

    (1 to 100)
      .map(n => session.getStore(n.toString))
      .flatMap(_.find().asScala)
      .size should be(100)
  }

   it should "close all open stores" in {
    val session = MapRDBSession(getConnection())

    (1 to 100)
      .map(n => session.getStore(n.toString))
      .foreach(_.insert(getConnection().newDocument().setId("5").set("name", "Joe")))

    session.close()

    getConnection().getStore("1").find().asScala.size should be(0)
  }

   it should "be a single instance available all the time" in {
    MapRDBSession(getConnection()) should be(MapRDBSession(getConnection()))
  }
}
