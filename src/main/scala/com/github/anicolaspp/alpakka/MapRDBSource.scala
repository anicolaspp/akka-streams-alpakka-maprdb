package com.github.anicolaspp.alpakka

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic}
import akka.stream.{Attributes, FlowShape}
import akka.util.ByteString
import org.ojai.Document
import org.ojai.store.Query

import scala.concurrent.Future


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

      Source.fromIterator(() => store.find(query).asScala.iterator)
    }.mapMaterializedValue(_ => NotUsed)
}








