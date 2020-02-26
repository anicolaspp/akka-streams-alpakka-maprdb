package com.github.anicolaspp.alpakka.maprdb.scaladsl

import org.ojai.store.{Connection, DocumentStore}

import scala.collection.mutable

class MapRDBSession(connection: Connection) extends AutoCloseable {

  private val registry: mutable.Map[String, DocumentStore] = mutable.Map.empty

  def getStore(storeName: String): DocumentStore = synchronized {
    registry.getOrElseUpdate(storeName, connection.getStore(storeName))
  }

  override def close(): Unit = {
    registry.foreach { case (_, store) => store.close() }
    registry.clear()
  }
}

object MapRDBSession {
  private var session: Option[MapRDBSession] = None

  def apply(connection: Connection): MapRDBSession = synchronized {
    session match {
      case Some(value) => value
      case None =>
        session = Some(new MapRDBSession(connection))
        apply(connection)
    }
  }
}