# Scala Project Export

This undocumented feature of Database Flow lets you generate a Scala project from your database schema.

You can see an example of the project at [chinook.databaseflow.com](https://chinook.databaseflow.com).

This is alpha-quality at best. It's never been used by anyone but me, and I've only tried a few schemas.


## The Pitch

You've got a PostgreSQL schema somewhere. You *make a local copy of that schema that can be destroyed without consequence if something breaks*. 
You load that schema in [Database Flow](https://databaseflow.com), the funkiest database client. 
You then hit the export option, making sure it sees your tables and picked the right names, then hit a big blue export button.
You enjoy the newly-generated SBT project with loads of features, based on [Boilerplay](https://github.com/KyleU/boilerplay). 


## Features

Holy shit it does *too* much. For each of your tables (and their relations), the export generates:

* A case class representing your table row, available in Scala.js, along with JSON (via circe) and binary (via BooPickle) serialization macros.

* Async queries to retrieve your model by id or foreign key, insert or update models, and search. Batch and sequence queries are also available.

* Slick tables and column types, and doobie queries including strong support for Json, enums, and `java.time` classes.

* A search result model with binary and json serialization that allows paging, sorting, and filtering with strongly-typed parameters.

* Dependency-injected service that provides (optionally audited) methods to search or change your models.

* A high-performance GraphQL schema (via Sangria) that provides methods to concurrently search, query, and mutate using GraphQL.

* Twirl view templates for administration, including system-wide search, model forms, and foreign key relations.

* Play Framework controllers, integrated with authentication and admin security, providing results in HTML, JSON, or even freakin' CSV. 

* Deep integration with Prometheus, OpenTracing, system-wide auditing, authentication, a full admin site, docker publishing, and just a whole bunch of stuff. 


## Do it yourself

* If you want to use a sample Postgres schema, grab one [here](https://github.com/lerocha/chinook-database/blob/master/ChinookDatabase/DataSources/Chinook_PostgreSql.sql). 

* Download [Boilerplay](https://github.com/KyleU/boilerplay), configure it to use your schema, and run it once. 
This creates the tables needed to support auditing, notes, and authentication. 

* Download and run Database Flow, and add a connection to your PostgreSQL schema.

* Add a "Project Location" in the connection settings.

* On the home screen, select the "Generate" button for your connection.

* Ok, this next page is important. It shows the full details of your database, with options for each table. Take some time to review the options available.
  * The top section lets you choose the output directory, which defaults to `./tmp/{projectId}`.
  * Each table has a "package" setting, which helps you organize large projects with many tables.
  * The table can also be exported to Scala.js (or, by default, just the JVM), or ignored completely.
  * Each column can be renamed, included in search results, added to the summary, or ignored.

* Once you're comfortable with the settings (make sure you set an output directory in the main database options), select "Export Project".

* The results page shows all of the generated code, as well as a summary of the decisions made.

* Head to the output directory you configured, run "sbt", try to compile the project, then open a github issue telling me where it all went wrong.

* Start the SBT task `run`, and point your browser to [localhost:9000](http://localhost:9000). 

* Hit the admin section, and start exploring! You can re-run the export at any time, and it will preserve the changes you've made.


## Work with an existing generated project

* Clone https://github.com/KyleU/databaseflow
* Launch sbt and type "run"
* Wait for the world to compile, then hit http://localhost:4260
* Once it loads, you'll need to create a local Database Flow account
* Create a new connection, pointing to the database you wish to use, and adding a project location.
* Add your stuff, make your database changes, then hit "Refresh Schema" from the query interface
* Open your project on the home screen, then hit "Generate"
* Find your newly created stuff, make sure it's in the right package, and all info is correct
* Export that thing.
