package com.github.anicolaspp.alpakka.maprdb.javadsl

import akka.Done
import akka.stream.javadsl.Sink
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document

import scala.concurrent.Future

object MapRDBSink {
  def upsert[T](session: MapRDBSession, tableName: String, parallelism: Int): Sink[Document, Future[Done]] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBSink.upsert(session, tableName, parallelism).asJava

  def delete(session: MapRDBSession, tableName: String, parallelism: Int): Sink[String, Future[Done]] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBSink.delete(session, tableName, parallelism).asJava

  def deleteDoc(session: MapRDBSession, tableName: String, parallelism: Int): Sink[Document, Future[Done]] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBSink.deleteDoc(session, tableName, parallelism).asJava
}
