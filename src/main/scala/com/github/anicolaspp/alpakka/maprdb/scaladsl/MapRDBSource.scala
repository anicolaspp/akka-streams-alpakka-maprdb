package com.github.anicolaspp.alpakka.maprdb.scaladsl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document
import org.ojai.store.Query

object MapRDBSource {

  import scala.collection.JavaConverters._

  def fromTable(tableName: String, session: MapRDBSession): Source[Document, NotUsed] =
    Source.fromMaterializer { (mat, _) =>
      val store = session.getStore(tableName)

      Source.fromIterator(() => store.find().asScala.iterator)
    }.mapMaterializedValue(_ => NotUsed)

  def fromQuery(query: Query, tableName: String, session: MapRDBSession): Source[Document, NotUsed] =
    Source.fromMaterializer { (mat, _) =>
      val store = session.getStore(tableName)

      val it = store.find(query).asScala.iterator

      Source.fromIterator(() => it)
    }.mapMaterializedValue(_ => NotUsed)
}

