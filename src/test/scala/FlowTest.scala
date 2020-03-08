import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.alpakka.maprdb.scaladsl.{MapRDBFlow, MapRDBSource}
import com.github.anicolaspp.ojai.ScalaOjaiTesting
import org.ojai.store.QueryCondition
import org.scalatest.{FlatSpec, Ignore, Matchers}

import scala.beans.BeanProperty
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class FlowTest extends FlatSpec with ScalaOjaiTesting with Matchers {

  import scala.collection.JavaConverters._

  ignore it should "read from table by id" in {
    val store = getConnection().getStore("n1")

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(store.insert)

    implicit val system = ActorSystem("test")
    //    implicit val mat = system.mat

    val source = Source.fromIterator(() => (1 to 100).map(_.toString).iterator)

    val result = source
      .via(MapRDBFlow.fromId(MapRDBSession(getConnection()), "n1", 1))
      .map(_.getIdString.toInt)
      .reduce(_ + _)

    result.runForeach(n => {
      n should be((1 to 100).sum)
    })
  }

  ignore it should "should add objects" in {

    implicit val system = ActorSystem("test")

    val source = Source
      .fromIterator(() =>
        (1 to 100).map(n => akka.japi.Pair(n.toString, new Dog(n.toString, n))).toIterator)

    val flow = MapRDBFlow
      .upsertWithId[Dog](MapRDBSession(getConnection()), "table", 1)

    val result = source
      .via(flow)
      .map(_.getIdString.toInt)
      .runReduce(_ + _)

    val sum = Await.result(result, Duration.Inf)

    sum should be((1 to 100).sum)
  }

  class Dog(@BeanProperty val name: String, @BeanProperty val age: Int)

  ignore it should "replace" in {

    implicit val system = ActorSystem("test")

    val source = Source
      .fromIterator(() =>
        (1 to 10).map(n => akka.japi.Pair(n.toString, new Dog(n.toString, n))).toIterator)

    val task = source
      .via(MapRDBFlow.upsertWithId(MapRDBSession(getConnection()), "t2", 1))
      .runWith(Sink.ignore)

    Await.ready(task, Duration.Inf)

    getConnection().getStore("t2").find().asScala.size should be(10)

    val replace = MapRDBSource.fromTable("t2", MapRDBSession(getConnection()))
      .map(_.set("name", "pepe"))
      .via(MapRDBFlow.upsert(MapRDBSession(getConnection()), "t2", 1))
      .runWith(Sink.ignore)

    Await.ready(replace, Duration.Inf)

    val query = getConnection().newCondition().is("name", QueryCondition.Op.EQUAL, "pepe").build()

    getConnection().getStore("t2").find(query).asScala.size should be(10)
  }

  ignore it should "delete" in {
    implicit val system = ActorSystem("test")

    val connection = getConnection()

        (1 to 10).map(n => akka.japi.Pair(n.toString, new Dog(n.toString, n)))
            .foreach(p => connection.getStore("t3").insert(p.first, connection.newDocument(p.second)))

    val delete = Source.fromIterator(() => List("2", "4", "6", "8", "10").iterator)
      .via(MapRDBFlow.delete(MapRDBSession(connection), "t3", 1))
      .runWith(Sink.ignore)

    Await.ready(delete, Duration.Inf)

    connection.getStore("t3").find().asScala.size should be(5)
  }
}
