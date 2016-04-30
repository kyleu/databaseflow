import sbt._

object Dependencies {
  object Cache {
    val ehCache = "net.sf.ehcache" % "ehcache-core" % "2.6.11"
  }

  object Logging {
    val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.21"
  }

  object Play {
    private[this] val version = "2.5.1" // 2.5.2 has a logging error
    val playFilters = play.sbt.PlayImport.filters
    val playWs = play.sbt.PlayImport.ws
    val playTest = "com.typesafe.play" %% "play-test" % version
    val playMailer = "com.typesafe.play" %% "play-mailer" % "4.0.0"
  }

  object Akka {
    private[this] val version = "2.4.4"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version
  }

  object Authentication {
    private[this] val version = "4.0.0-BETA4"
    val silhouette = "com.mohiva" %% "play-silhouette" % version
    val hasher = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
    val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
  }

  object Jdbc {
    val hikariCp = "com.zaxxer" % "HikariCP" % "2.4.5"

    //val db2 = dblibs/lib/???
    val h2 = "com.h2database" % "h2" % "1.4.191"
    //val informix = dblibs/lib/???
    val mysql = "mysql" % "mysql-connector-java" % "5.1.38" // 6.0.2 has NPE on load
    val postgres = "org.postgresql" % "postgresql" % "9.4.1208"
    //val oracle = dblibs/lib/oracle-ojdbc7.jar
    //val sqlServer = dblibs/lib/sqlserver-sqljdbc42.jar
  }

  object Hibernate {
    val core = "org.hibernate" % "hibernate-core" % "5.1.0.Final"
  }

  object Serialization {
    val version = "0.3.9" // 0.4.0 has a linking error
    val uPickle = "com.lihaoyi" %% "upickle" % version
  }

  object Licensing {
    val version = "1.1.0"
    val core = "net.nicholaswilliams.java.licensing" % "licensing-core" % version
    val licensor = "net.nicholaswilliams.java.licensing" % "licensing-licensor-base" % version
  }

  object WebJars {
    val requireJs = "org.webjars" % "requirejs" % "2.2.0"
    val jquery = "org.webjars" % "jquery" % "2.2.3"
    val materialize = "org.webjars" % "materializecss" % "0.97.5"
    val fontAwesome = "org.webjars" % "font-awesome" % "4.6.1"
  }

  object Ui {
    val swing = "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"
  }

  object Metrics {
    val metrics = "nl.grons" %% "metrics-scala" % "3.5.4"
    val jvm = "io.dropwizard.metrics" % "metrics-jvm" % "3.1.2"
    val ehcache = "io.dropwizard.metrics" % "metrics-ehcache" % "3.1.2" intransitive()
    val healthChecks = "io.dropwizard.metrics" % "metrics-healthchecks" % "3.1.2" intransitive()

    val json = "io.dropwizard.metrics" % "metrics-json" % "3.1.2"

    val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "9.3.9.M1"
    val servlets = "io.dropwizard.metrics" % "metrics-servlets" % "3.1.2" intransitive()
    val graphite = "io.dropwizard.metrics" % "metrics-graphite" % "3.1.2" intransitive()
  }

  object Utils {
    val scapegoatVersion = "1.2.1"
    val enumeratumVersion = "1.4.1"
    val enumeratum = "com.beachape" %% "enumeratum-upickle" % enumeratumVersion
    val commonsIo = "commons-io" % "commons-io" % "2.4"
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.0.1"
  }

  object Testing {
    val uTestVersion = "0.4.3"
    val utest = "com.lihaoyi" %% "utest" % uTestVersion % "test"
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  }
}
