package com.github.anicolaspp.alpakka.maprdb.scaladsl

import akka.Done
import akka.stream.scaladsl.{Keep, Sink}
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document

import scala.concurrent.Future

object MapRDBSink {
  def upsert[T](session: MapRDBSession, tableName: String, parallelism: Int): Sink[Document, Future[Done]] =
    MapRDBFlow.upsert(session, tableName, parallelism).toMat(Sink.ignore)(Keep.right)

  def delete(session: MapRDBSession, tableName: String, parallelism: Int): Sink[String, Future[Done]] =
    MapRDBFlow.delete(session, tableName, parallelism).toMat(Sink.ignore)(Keep.right)

  def deleteDoc(session: MapRDBSession, tableName: String, parallelism: Int): Sink[Document, Future[Done]] =
    MapRDBFlow.deleteDoc(session, tableName, parallelism).toMat(Sink.ignore)(Keep.right)
}
