package com.github.anicolaspp.alpakka.maprdb.javadsl

import akka.NotUsed
import akka.stream.javadsl.Source
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document
import org.ojai.store.Query

object MapRDBSource {

  def fromTable(tableName: String, session: MapRDBSession): Source[Document, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBSource.fromTable(tableName, session).asJava

  def fromQuery(query: Query, tableName: String, session: MapRDBSession): Source[Document, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBSource.fromQuery(query, tableName, session).asJava
}

