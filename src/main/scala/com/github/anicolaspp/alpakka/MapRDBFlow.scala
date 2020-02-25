package com.github.anicolaspp.alpakka

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.ojai.Document

import scala.concurrent.Future

object MapRDBFlow {
  def fromId(session: MapRDBSession, tableName: String): Flow[String, Document, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>

      implicit val dispatcher = mat.system.dispatcher

      Flow[String]
        .mapAsync(1)(id => Future(session.getStore(tableName).findById(id)))

    }.mapMaterializedValue(_ => NotUsed)
}
