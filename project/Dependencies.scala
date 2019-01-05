import sbt._

object Dependencies {
  object Play {
    private[this] val version = "2.6.20"
    val lib = "com.typesafe.play" %% "play" % version
    val filters = play.sbt.PlayImport.filters
    val ws = play.sbt.PlayImport.ws
    val guice = play.sbt.PlayImport.guice
    val cache = play.sbt.PlayImport.ehcache
    val json = "com.typesafe.play" %% "play-json" % "2.6.10"
    val test = "com.typesafe.play" %% "play-test" % version % "test"

    val mailer = "com.typesafe.play" %% "play-mailer" % "6.0.0"
    val mailerGuice = "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"
  }

  object Authentication {
    private[this] val version = "5.0.6"
    val silhouette = "com.mohiva" %% "play-silhouette" % version
    val hasher = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
    val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
    val crypto = "com.mohiva" %% "play-silhouette-crypto-jca" % version
  }

  object Jdbc {
    val hikariCp = "com.zaxxer" % "HikariCP" % "3.2.0"

    val db2 = "dblibs/lib/db2-db2jcc4.jar"
    val h2 = "com.h2database" % "h2" % "1.4.197"
    val informix = "dblibs/lib/informix-ifxjdbc.jar"
    val mysql = "mysql" % "mysql-connector-java" % "5.1.47" // 6.0 is all different
    val postgres = "org.postgresql" % "postgresql" % "42.2.4"
    val oracle = "dblibs/lib/oracle-ojdbc7.jar"
    val sqlite = "org.xerial" % "sqlite-jdbc" % "3.23.1"
    val sqlServer = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.2.2.jre8"
  }

  object Hibernate {
    val core = "org.hibernate" % "hibernate-core" % "5.1.0.Final"
  }

  object Export {
    val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
  }

  object Serialization {
    val circeVersion = "0.10.1"
    val circeProjects = Seq("circe-core", "circe-generic", "circe-generic-extras", "circe-parser", "circe-java8")
  }

  object ScalaJS {
    val version = "0.6.25"

    val scalaTagsVersion = "0.6.7"
    val jQueryVersion = "0.9.4"
  }

  object GraphQL {
    val sangria = "org.sangria-graphql" %% "sangria" % "1.4.2"
    val playJson = "org.sangria-graphql" %% "sangria-play-json" % "1.0.5"
    val circe = "org.sangria-graphql" %% "sangria-circe" % "1.2.1"
  }

  object Utils {
    val scapegoatVersion = "1.3.8"
    val enumeratumVersion = "1.5.13"
    val enumeratumCirceVersion = "1.5.18"

    val betterFiles = "com.github.pathikrit" %% "better-files" % "3.6.0"
    val commonsIo = "commons-io" % "commons-io" % "2.6"
    val commonsLang = "org.apache.commons" % "commons-lang3" % "3.8.1"
    val enumeratumCirce = "com.beachape" %% "enumeratum-circe" % enumeratumCirceVersion
    val fastparse = "com.lihaoyi" %% "fastparse" % "1.0.0"
    val guava = "com.google.guava" % "guava" % "23.0"
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.2.1"
    val scopts = "com.github.scopt" %% "scopt" % "3.7.0"
    val thriftParser = "com.facebook.swift" % "swift-idl-parser" % "0.23.1"

    val scribeVersion = "1.4.6"
  }

  object Testing {
    val gatlingVersion = "2.2.3"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    val gatlingCore = "io.gatling" % "gatling-test-framework" % gatlingVersion % "test"
    val gatlingCharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test"
  }
}
