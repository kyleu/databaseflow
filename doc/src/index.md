@@@ index

* [Features](feature/index.md)
* [Contributing](contribute/index.md)
* [Todo](todo.md)

@@@

# Database Flow

Running locally or on a shared server, Database Flow helps you discover and search your database using SQL and GraphQL.

You can also generate a full-featured Scala web project based on your database.

## Features

* Supports MySQL, PostgreSQL, SQLite, Oracle, SQL Server, H2, DB2, and Informix.
* Rich SQL editor with auto-complete and live syntax checking based on your schema.
* Explore tables, views, and stored procedures. Share saved results or sql queries.
* A sophisticated GraphQL server, providing a detailed and efficient graph for your schema and data.
* Supports local single-user app installations as well as a shared server mode for your whole team.   

## Installation

* Download the latest `databaseflow.jar` file from [Github](https://github.com/KyleU/databaseflow/releases).
* Run `java -jar databaseflow.jar`; a new browser tab pointed to `http://localhost:4260` will open automatically. 

## Configuration

* If you're on Windows, config files are stored in `%APPDATA%\Database Flow`. For macOS and Linux, the configuration folder may be found in `~/.databaseflow`/
* The main configuation file is named `databaseflow.conf`.
* You may change the configuration for file path, mail setup, and storage locations.

## License

This project is owned by Kyle Unverferth. All rights reserved for now.
