# akka-streams-alpakka-maprdb

This library is an Akka Streams library that can be used to read and write to [MapR Database](https://mapr.com/products/mapr-database/), via **reactive streams** with **asynchronous back pressure**.

**MapR-Database** is a high-performance NoSQL database management system built into the MapR Data Platform. It is a highly scalable multi-model database that brings together operations and analytics as well as real-time streaming and database workloads to enable a broader set of next-generation data-intensive applications in organizations.

MapR-Database performance is analized in this post [ESG Labs Confirms MapR Outperforms Cassandra and HBase by 10x in the Cloud](https://mapr.com/company/press-releases/esg-labs-confirms-mapr-outperforms/).


**akka-streams-alpakka-maprdb** brings reactive to MapR-Datase. Interacting with MapR-Datase can be done in many ways, this post, [Interacting with MapR-Database](https://medium.com/hackernoon/interacting-with-mapr-db-58c4f482efa1) is explained all different available options. However, in the rise of microservices, the use of reactive components with native non-blocking and integrated back preassure is a must which ultimately inspired us to create this library, **akka-streams-alpakka-maprdb**.

The library can be used in both, Java and Scala through the corresponding APIs. These APIs has been designed to work Akka Strams and follow similar approaches to those presented by Akka. 

--

