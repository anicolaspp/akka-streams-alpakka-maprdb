package com.github.anicolaspp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import com.github.anicolaspp.alpakka.maprdb.scaladsl.{MapRDBFlow, MapRDBSink, MapRDBSource}
import com.typesafe.config.ConfigFactory
import org.ojai.store.DriverManager

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem = ActorSystem()

    val connection = DriverManager.getConnection("ojai:mapr:")

    val source = MapRDBSource
      .fromTable("/tables/seq", MapRDBSession(connection))
      .map(doc => doc.set("value", doc.getIdString))

    val flow = MapRDBFlow
      .upsert(MapRDBSession(connection), "/tables/seq", 1)
      .filter(_.getIdString.toInt % 2 == 0)
      .map(_.getIdString)

    val sink = MapRDBSink.delete(MapRDBSession(connection), "/tables/seq", 1)

    val delete = source.via(flow).runWith(sink)

    Await.ready(delete, Duration.Inf)

    actorSystem.terminate()
  }
}
