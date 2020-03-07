package com.github.anicolaspp.alpakka.maprdb.javadsl

import akka.NotUsed
import akka.stream.javadsl.Flow
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document

object MapRDBFlow {

  /**
   * Reads [[Document]]s from a [[org.ojai.store.DocumentStore]] based on given ids.
   */
  def fromId(session: MapRDBSession, tableName: String, parallelism: Int): Flow[String, Document, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow.fromId(session, tableName, parallelism).asJava


  /**
   * Adds or replaces [[Document]]s to a [[org.ojai.store.DocumentStore]].
   */
  def upsert[T](session: MapRDBSession, tableName: String, parallelism: Int): Flow[Document, Document, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow.upsert(session, tableName, parallelism).asJava

  /**
   * Adds or replaces [[Document]]s after adding the given id to each object.
   *
   * Notice that [[T]] must be a Java Bean compliant type.
   */
  def upsertWithId[T](session: MapRDBSession, tableName: String, parallelism: Int): Flow[akka.japi.Pair[String, T], Document, NotUsed] = {
    val flow: akka.stream.scaladsl.Flow[akka.japi.Pair[String, T], Document, NotUsed] =
      com.github.anicolaspp.alpakka.maprdb.scaladsl
        .MapRDBFlow.upsertWithId(session, tableName, parallelism)

    flow.asJava
  }

  def upsertMap(session: MapRDBSession, tableName: String, parallelism: Int): Flow[Map[String, Object], Document, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow.upsertMap(session, tableName, parallelism).asJava

  /**
   * Creates a [[Flow]] that deletes ids from the store and returned the same ids.
   */
  def delete(session: MapRDBSession, tableName: String, parallelism: Int): Flow[String, String, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow.delete(session, tableName, parallelism).asJava


  def deleteDoc(session: MapRDBSession, tableName: String, parallelism: Int): Flow[Document, String, NotUsed] =
    com.github.anicolaspp.alpakka.maprdb.scaladsl.MapRDBFlow.deleteDoc(session, tableName, parallelism).asJava
}