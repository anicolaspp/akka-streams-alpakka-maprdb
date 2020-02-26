import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.alpakka.maprdb.scaladsl.{MapRDBFlow, MapRDBSource}
import com.github.anicolaspp.ojai.ScalaOjaiTesting
import org.ojai.store.QueryCondition
import org.scalatest.{FlatSpec, Matchers}

import scala.beans.BeanProperty
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class TestRead extends FlatSpec with ScalaOjaiTesting with Matchers {


  it should "read from table" in {
    val store = getConnection().getStore("table")

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(store.insert)

    implicit val system = ActorSystem("test")

    val source = MapRDBSource
      .fromTable("table", MapRDBSession(getConnection()))
      .map(_.getInt("value"))
      .runFold(0)((acc, _) => acc + 1)

    Await.result(source, Duration.Inf) should be(100)
  }

  it should "read from table by query" in {
    val store = getConnection().getStore("table")

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(store.insert)

    implicit val system = ActorSystem("test")

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

class FlowTest extends FlatSpec with ScalaOjaiTesting with Matchers {
  it should "read from table by id" in {
    val store = getConnection().getStore("table")

    (1 to 100)
      .map(n => getConnection().newDocument().setId(n.toString).set("value", n))
      .foreach(store.insert)

    implicit val system = ActorSystem("test")
    //    implicit val mat = system.mat

    val source = Source.fromIterator(() => (1 to 100).map(_.toString).iterator)

    val result = source
      .via(MapRDBFlow.fromId(MapRDBSession(getConnection()), "table"))
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
      .addWithId[Dog](MapRDBSession(getConnection()), "table", 1)

    val result = source
      .via(flow)
      .map(_.getIdString.toInt)
      .runReduce(_ + _)

    val sum = Await.result(result, Duration.Inf)

    sum should be((1 to 100).sum)
  }

  it should "no replace when inserting" in {

  }
}






























