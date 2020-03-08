# akka-streams-alpakka-maprdb

This library is an Akka Streams library that can be used to read and write to [MapR Database](https://mapr.com/products/mapr-database/), via **reactive streams** with **asynchronous back pressure**.

**MapR-Database** is a high-performance NoSQL database management system built into the MapR Data Platform. It is a highly scalable multi-model database that brings together operations and analytics as well as real-time streaming and database workloads to enable a broader set of next-generation data-intensive applications in organizations.

MapR-Database performance is analized in this post [ESG Labs Confirms MapR Outperforms Cassandra and HBase by 10x in the Cloud](https://mapr.com/company/press-releases/esg-labs-confirms-mapr-outperforms/).


**akka-streams-alpakka-maprdb** brings reactive to MapR-Datase. Interacting with MapR-Datase can be done in many ways, this post, [Interacting with MapR-Database](https://medium.com/hackernoon/interacting-with-mapr-db-58c4f482efa1) is explained all different available options. However, in the rise of microservices, the use of reactive components with native non-blocking and integrated back preassure is a must which ultimately inspired us to create this library, **akka-streams-alpakka-maprdb**.

The library can be used in both, Java and Scala through the corresponding APIs. These APIs has been designed to work Akka Strams and follow similar approaches to those presented by Akka. 

## Reading / Streaming from MapR-Database

The following is a simple example showing how to use the library in Scala. A table that holds people information is read, parsed, and filtered based on people age.

```scala

val connection = DriverManager.getConnection("ojai:mapr:")

val source = MapRDBSource
  .fromTable("/tables/people", MapRDBSession(connection))
  .map(doc => Person.from(doc))
  .filter(_.age >= 18)
  .runForeach(person => println(person.name))
```

Notice that in this example, we are creating a **connection** outside the library itself, then we create a `MapRDBSession` based on the **connection** to be used by the library. 

We can create different kind of connections. In this example we are connecting through the MapR-Client that must be installed and configured the host running this application. See [How to connect to MapR cluster](https://mapr.com/docs/61/MapROverview/establishing_connections_to_mapr_clusters.html).

Connecting to MapR can also be done through the [MapR Data Access Gateway](https://mapr.com/docs/61/MapROverview/MapRDataAccessGateway.html?hl=data%2Caccess%2Cgateway) and in this case, we could use the OJAI Java thin client to create the connection and pass it into the `MapRDBSession` that this library uses. 

```scala
val connection = DriverManager.getConnection("ojai:mapr:thin")

val sesssion = MapRDBSession(connection)
```
Notice that the protocol for the connection has changed from `ojai:mapr` to `ojai:mapr:thin` to indicate what type of connection should be stablished, yet **akka-streams-alpakka-maprdb** will use the provided **connection** regardless of how it was created. in our testing, we are using a special connection protocol, `"ojai:anicolaspp:mem"` which is a in memory MapR-Database representation using the [OJAI Testing](https://github.com/anicolaspp/ojai-testing) project.

In the provided example, we are reading the entire `/tables/people` table and then filtering in memory. However, we can created sources using specific queries. This is a tremendous advantage since no all data will be read, only the one satifying the given query. 

The following example creates a `Source` using an `OJAI Query` and then print the results.

```scala
val cond = connection
  .newCondition()
  .is("age", QueryCondition.Op.GREATER_OR_EQUAL, 18)
  .build()

val query = connection
  .newQuery()
  .where(cond)
  .select("name")
  .build()
  
MapRDBSource
  .fromQuery(query, "/tables/people", MapRDBSession(connection))
  .runForeach(println)
```
Notice that output will be the same that in the first example. The difference is how the data was filter and projected. In the first example, the condition is evaluated for each record as it fetched. In the second example, the query is sent to MapR-Database and only those records that satified the condition are then fetched. 

## Streaming into MapR-Database

**akka-streams-alpakka-maprdb** provides `Sink`s for MapR-Database so upstreams can be sinked into MapR-Database with ease.

The following example streams data from Kafka and sinks it into MapR-Database using the provided Akka `Sink`.

```scala
val source: Source[Person, NotUsed] = getKafkaStream()
  .map(Person.fromKafkaObject)

val getDocFlow: Flow[Person, Document, NotUsed] = 
  Flow.map(person => connection.newDocument(person))

val toMapRDBSink = MapRDBSink.upset(MapRDBSession(connection),"/tables/people", 10)

source
  .via(getDocFlow)
  .runWith(toMapRDBSink)
```
Notice that `toMapRDBSink` is an Akka `Sink` that is able to write to MapR-Database the given `Document`s.

In the same way we have `Sink`s for delete, updates, and others. 

The `MapRDBFlow` class provides some useful `Flow`s. 

`MapRDBFlow.fromId` provides a way to load MapR-Database `Document`s given the corresponding ids (`_id`) and `MapRDBFlow.delete` is a flow that requires an input stream of ids to be deleted while returning the same ids via a pass through. 

***This is not an extensive list, but a small way to show some features***.

