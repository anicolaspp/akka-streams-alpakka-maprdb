package com.github.anicolaspp.alpakka.maprdb.scaladsl

import org.ojai.store.{Connection, DocumentStore}

import scala.collection.mutable

class MapRDBSession(connection: Connection) {

  private val registry: mutable.Map[String, DocumentStore] = mutable.Map.empty

  def getStore(storeName: String): DocumentStore = synchronized {
    registry.getOrElseUpdate(storeName, connection.getStore(storeName))
  }
}

object MapRDBSession {
  def apply(connection: Connection): MapRDBSession = new MapRDBSession(connection)
}