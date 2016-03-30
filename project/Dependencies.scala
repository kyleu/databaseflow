import sbt._

object Dependencies {
  object Cache {
    val ehCache = "net.sf.ehcache" % "ehcache-core" % "2.6.11"
  }

  object Logging {
    val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.19"
  }

  object Hibernate {
    val core = "org.hibernate" % "hibernate-core" % "5.1.0.Final"
  }

  object Jdbc {
    val hikariCp = "com.zaxxer" % "HikariCP" % "2.4.5"

    val db2 = "?" % "?" % "?"
    val h2 = "com.h2database" % "h2" % "1.4.191"
    val mysql = "mysql" % "mysql-connector-java" % "5.1.38"
    val postgres = "org.postgresql" % "postgresql" % "9.4.1208"
    val oracle = "?" % "?" % "?"
    val sqlServer = "?" % "?" % "?"
  }

  object Play {
    private[this] val version = "2.4.6"
    val playFilters = play.sbt.PlayImport.filters
    val playWs = play.sbt.PlayImport.ws
    val playTest = "com.typesafe.play" %% "play-test" % version
  }

  object Serialization {
    val version = "0.3.9"
    val uPickle = "com.lihaoyi" %% "upickle" % version
  }

  object Templating {
    val version = "0.5.4"
    val scalaTags = "com.lihaoyi" %% "scalatags" % version
  }

  object Akka {
    private[this] val version = "2.4.2"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version
  }

  object Authentication {
    val silhouette = "com.mohiva" %% "play-silhouette" % "3.0.4"
  }

  object WebJars {
    val requireJs = "org.webjars" % "requirejs" % "2.2.0"
    val jquery = "org.webjars" % "jquery" % "2.2.2"
    val materialize = "org.webjars" % "materializecss" % "0.97.5"
    val fontAwesome = "org.webjars" % "font-awesome" % "4.5.0"
  }

  object Mail {
    val mailer = "com.typesafe.play" %% "play-mailer" % "4.0.0"
  }

  object Ui {
    val swing = "org.scala-lang" % "scala-swing" % "2.11.0-M7"
  }

  object Metrics {
    val metrics = "nl.grons" %% "metrics-scala" % "3.5.3"
    val jvm = "io.dropwizard.metrics" % "metrics-jvm" % "3.1.2"
    val ehcache = "io.dropwizard.metrics" % "metrics-ehcache" % "3.1.2" intransitive()
    val healthChecks = "io.dropwizard.metrics" % "metrics-healthchecks" % "3.1.2" intransitive()

    val json = "io.dropwizard.metrics" % "metrics-json" % "3.1.2"

    val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "9.3.8.v20160314"
    val servlets = "io.dropwizard.metrics" % "metrics-servlets" % "3.1.2" intransitive()
    val graphite = "io.dropwizard.metrics" % "metrics-graphite" % "3.1.2" intransitive()
  }

  object Utils {
    val scapegoatVersion = "1.2.1"
    val enumeratumVersion = "1.3.7"
    val enumeratum = "com.beachape" %% "enumeratum-upickle" % enumeratumVersion
    val commonsIo = "commons-io" % "commons-io" % "2.4"
  }

  object Testing {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  }
}
