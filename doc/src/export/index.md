@@@ index

* [Database](databaseExport.md)
* [GraphQL](graphqlExport.md)
* [Thrift](thriftExport.md)

@@@

# Scala Export

Database Flow allows you to export your database schema, Thrift definitions, and GraphQL queries to idiomatic Scala. 
Three different export types are available:

* @ref:[Database Export](databaseExport.md) - Exports your schema to a full-featured Scala web application, including an efficient GraphQL schema, admin UI, Slick/doobie mappings, and more.  
* @ref:[GraphQL Export](graphqlExport.md) - Turns a set of GraphQL queries into Scala classes with Circe serialization.
* @ref:[Thrift Export](thriftExport.md) - Given a set of Thrift definitions, generates Scala wrappers around Scrooge-generated code, adding support for Scala Futures, UUIDs, and `java.time` classes
