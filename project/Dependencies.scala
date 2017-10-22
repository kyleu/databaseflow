import sbt._

object Dependencies {
  object Play {
    private[this] val version = "2.6.6"
    val lib = "com.typesafe.play" %% "play" % version
    val filters = play.sbt.PlayImport.filters
    val ws = play.sbt.PlayImport.ws
    val guice = play.sbt.PlayImport.guice
    val cache = play.sbt.PlayImport.ehcache
    val json = "com.typesafe.play" %% "play-json" % "2.6.6"
    val test = "com.typesafe.play" %% "play-test" % version % "test"

    val mailer = "com.typesafe.play" %% "play-mailer" % "6.0.0"
    val mailerGuice = "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"
  }

  object Akka {
    private[this] val version = "2.5.6"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
  }

  object Authentication {
    private[this] val version = "5.0.2"
    val silhouette = "com.mohiva" %% "play-silhouette" % version
    val hasher = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
    val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
    val crypto = "com.mohiva" %% "play-silhouette-crypto-jca" % version
  }

  object Jdbc {
    val hikariCp = "com.zaxxer" % "HikariCP" % "2.6.3"

    val db2 = "dblibs/lib/db2-db2jcc4.jar"
    val h2 = "com.h2database" % "h2" % "1.4.196"
    val informix = "dblibs/lib/informix-ifxjdbc.jar"
    val mysql = "mysql" % "mysql-connector-java" % "5.1.43" // 6.0 is all different
    val postgres = "org.postgresql" % "postgresql" % "9.4.1212"
    val oracle = "dblibs/lib/oracle-ojdbc7.jar"
    val sqlite = "org.xerial" % "sqlite-jdbc" % "3.20.0"
    val sqlServer = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.2.1.jre8"
  }

  object Hibernate {
    val core = "org.hibernate" % "hibernate-core" % "5.1.0.Final"
  }

  object Export {
    val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
  }

  object Serialization {
    val circeVersion = "0.8.0"
    val uPickleVersion = "0.4.4"
  }

  object Metrics {
    val version = "3.2.5"
    val metrics = "nl.grons" %% "metrics-scala" % "3.5.9"
    val jvm = "io.dropwizard.metrics" % "metrics-jvm" % version
    val ehcache = "io.dropwizard.metrics" % "metrics-ehcache" % version intransitive()
    val healthChecks = "io.dropwizard.metrics" % "metrics-healthchecks" % version intransitive()
    val json = "io.dropwizard.metrics" % "metrics-json" % version exclude("com.fasterxml.jackson.core", "jackson-databind")
    val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "9.4.7.v20170914"
    val servlets = "io.dropwizard.metrics" % "metrics-servlets" % version intransitive()
    val graphite = "io.dropwizard.metrics" % "metrics-graphite" % version intransitive()
  }

  object ScalaJS {
    val scalaTagsVersion = "0.6.5"
    val jQueryVersion = "0.9.2"
  }

  object GraphQL {
    val sangria = "org.sangria-graphql" %% "sangria" % "1.3.0"
    val playJson = "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"
    val circe = "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
  }

  object Utils {
    val scapegoatVersion = "1.3.3"
    val enumeratumVersion = "1.5.11"
    val enumeratumCirceVersion = "1.5.14"

    val commonsIo = "commons-io" % "commons-io" % "2.6"
    val enumeratum = "com.beachape" %% "enumeratum-upickle" % enumeratumVersion
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.1.0"
    val fastparse = "com.lihaoyi" %% "fastparse" % "1.0.0"
    val betterFiles = "com.github.pathikrit" %% "better-files" % "3.2.0"
    val guava = "com.google.guava" % "guava" % "23.0"

    val scribeVersion = "1.4.5"
  }

  object Testing {
    val gatlingVersion = "2.2.3"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
    val gatlingCore = "io.gatling" % "gatling-test-framework" % gatlingVersion % "test"
    val gatlingCharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test"
  }
}
