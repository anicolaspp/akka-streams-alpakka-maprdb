package com.github.anicolaspp.alpakka.maprdb.scaladsl

import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.github.anicolaspp.alpakka.maprdb.MapRDBSession
import org.ojai.Document

import scala.concurrent.Future
import scala.util.Try

object MapRDBFlow {

  /**
   * Reads [[Document]]s from a [[org.ojai.store.DocumentStore]] based on given ids.
   */
  def fromId(session: MapRDBSession, tableName: String, parallelism: Int): Flow[String, Document, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>

      Flow[String]
        .mapAsync(parallelism) { id =>
          Future {
            session.getStore(tableName).findById(id)
          }(mat.system.dispatcher)
        }
        .filter(_ != null)

    }.mapMaterializedValue(_ => NotUsed)


  /**
   * Adds or replaces [[Document]]s to a [[org.ojai.store.DocumentStore]].
   */
  def upsert[T](session: MapRDBSession, tableName: String, parallelism: Int): Flow[Document, Document, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>

      Flow[Document]
        .mapAsync(parallelism) { doc =>
          Future {
            Try {
              session.getStore(tableName).insertOrReplace(doc)
            }.map { _ =>
              session.getStore(tableName).findById(doc.getId)
            }
          }(mat.system.dispatcher)
        }
        .filter(_.isSuccess)
        .map(_.get)

    }.mapMaterializedValue(_ => NotUsed)

  /**
   * Adds or replaces [[Document]]s after adding the given id to each object.
   *
   * Notice that [[T]] must be a Java Bean compliant type.
   */
  def upsertWithId[T](session: MapRDBSession, tableName: String, parallelism: Int): Flow[akka.japi.Pair[String, T], Document, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>

      val connection = session.connection

      val toDocWithId = Flow[akka.japi.Pair[String, T]]
        .mapAsync(parallelism) { p =>
          Future {
            connection
              .newDocument(p.second)
              .setId(p.first)
          }(mat.system.dispatcher)
        }

      toDocWithId.via(MapRDBFlow.upsert(session, tableName, parallelism))

    }.mapMaterializedValue(_ => NotUsed)

  def upsertMap(session: MapRDBSession, tableName: String, parallelism: Int): Flow[Map[String, Object], Document, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>
      val connection = session.connection

      Flow[Map[String, Object]]
        .mapAsync(parallelism) { map =>
          Future(connection.newDocument(map))(mat.system.dispatcher)
        }
        .via(MapRDBFlow.upsert(session, tableName, parallelism))

    }.mapMaterializedValue(_ => NotUsed)

  /**
   * Creates a [[Flow]] that deletes ids from the store and returned the same ids.
   */
  def delete(session: MapRDBSession, tableName: String, parallelism: Int): Flow[String, String, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>
      Flow[String]
        .mapAsync(parallelism) { id =>
          Future {
            session.getStore(tableName).delete(id)

            id
          }(mat.system.dispatcher)
        }
    }
      .mapMaterializedValue(_ => NotUsed)


  def deleteDoc(session: MapRDBSession, tableName: String, parallelism: Int): Flow[Document, String, NotUsed] =
    Flow.fromMaterializer { (mat, _) =>
      Flow[Document].mapAsync(parallelism) { doc => Future(doc.getIdString)(mat.system.dispatcher) }
        .via(delete(session, tableName, parallelism))
    }.mapMaterializedValue(_ => NotUsed)
}