import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.alpakka.maprdb.scaladsl.{MapRDBFlow, MapRDBSource}
import com.github.anicolaspp.ojai.ScalaOjaiTesting
import org.ojai.store.QueryCondition
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class SourceTest extends FlatSpec with ScalaOjaiTesting with Matchers with BeforeAndAfterEach {
  import scala.collection.JavaConverters._


  ignore "Source" should "read from table" in {

    implicit val system = ActorSystem("test")

//    val connection = getConnection()

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(d => getConnection().getStore("s").insert(d))

    getConnection.getStore("s").find().asScala.size should be (100)

    println("here...")

    val source = MapRDBSource
      .fromTable("s", MapRDBSession(getConnection))
      .map(_.getInt("value"))
      .runFold(0)((acc, _) => {
        println(acc)

        acc + 1
      })

    Await.result(source, Duration.Inf) should be(100)
  }

  ignore it should "read from table by query" in {
    implicit val system = ActorSystem("test")

    val ss = Source.fromIterator(() =>
      (1 to 100)
        .map(n => getConnection().newDocument().setId(n.toString).set("value", n)).iterator)

    ss.via(MapRDBFlow.upsert(MapRDBSession(getConnection()), "table", 1))
      .runWith(Sink.ignore)

    val query = getConnection()
      .newQuery()
      .where(getConnection()
        .newCondition()
        .is("value", QueryCondition.Op.EQUAL, 50)
        .build()
      ).build()

    val source = MapRDBSource
      .fromQuery(query, "table", MapRDBSession(getConnection()))
      .map(_.getInt("value"))
      .runFold(0)((acc, _) => acc + 1)

    Await.result(source, Duration.Inf) should be(1)
  }
}