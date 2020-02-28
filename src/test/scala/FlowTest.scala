import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow
import com.github.anicolaspp.ojai.ScalaOjaiTesting
import org.ojai.scala.Document
import org.scalatest.{FlatSpec, Matchers}

import scala.beans.BeanProperty
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class FlowTest extends FlatSpec with ScalaOjaiTesting with Matchers {
  import scala.collection.JavaConverters._

  it should "read from table by id" in {
    val store = getConnection().getStore("table")

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(store.insert)

    implicit val system = ActorSystem("test")
    //    implicit val mat = system.mat

    val source = Source.fromIterator(() => (1 to 100).map(_.toString).iterator)

    val result = source
      .via(MapRDBFlow.fromId(MapRDBSession(getConnection()), "table", 1))
      .map(_.getIdString.toInt)
      .reduce(_ + _)

    result.runForeach(n => {
      n should be((1 to 100).sum)
    })
  }

  it should "should add objects" in {
    class Dog(@BeanProperty val name: String, @BeanProperty val age: Int)

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

  it should "replace" in {
    class Dog(@BeanProperty val name: String, @BeanProperty val age: Int)

    implicit val system = ActorSystem("test")

    val source = Source
      .fromIterator(() =>
        (1 to 100).map(n => akka.japi.Pair(n.toString, new Dog(n.toString, n))).toIterator)

    source
      .via(MapRDBFlow.upsertWithId(MapRDBSession(getConnection()), "t2", 1))
      .runWith(Sink.ignore)

    getConnection().getStore("t2").find().asScala.size should be (100)


  }
}
