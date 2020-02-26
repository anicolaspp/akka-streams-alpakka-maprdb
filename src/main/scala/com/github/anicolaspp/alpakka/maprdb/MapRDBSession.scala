package com.github.anicolaspp.alpakka.maprdb

import akka.japi
import org.ojai.store.{Connection, DocumentStore}

import scala.collection.mutable


trait MapRDBSession extends AutoCloseable {
  def getStore(storeName: String): DocumentStore

  def openStores(): Seq[akka.japi.Pair[String, DocumentStore]]

  def connection: Connection
}

object MapRDBSession {
  private var session: Option[MapRDBSession] = None

  def apply(connection: Connection): MapRDBSession = synchronized {
    session match {
      case Some(value) => value
      case None =>
        session = Some(createSession(connection))
        apply(connection)
    }
  }

  private def createSession(theConnection: Connection): MapRDBSession = new MapRDBSession {
    private val registry: mutable.Map[String, DocumentStore] = mutable.Map.empty

    override def getStore(storeName: String): DocumentStore = synchronized {
      registry.getOrElseUpdate(storeName, connection.getStore(storeName))
    }

    override def openStores(): Seq[japi.Pair[String, DocumentStore]] =
      registry.map { case (name, store) => japi.Pair(name, store) }.toSeq

    override def close(): Unit = {
      registry.foreach { case (_, store) => store.close() }
      registry.clear()
    }

    override def connection: Connection = theConnection
  }
}