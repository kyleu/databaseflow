import sbt.Resolver

scalacOptions ++= Seq( "-unchecked", "-deprecation" )
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.url("jetbrains-bintray", url("http://dl.bintray.com/jetbrains/sbt-plugins/"))(Resolver.ivyStylePatterns)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

// SBT-Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Scala.js
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.25")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6" exclude("org.scala-js", "sbt-scalajs"))

// Source Control
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Publishing
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2") // show */*:pgpSecretRing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

// Benchmarking
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.4")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")

// App Packaging
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.9")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")

// Dependency Resolution
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0")

// Code Quality
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0") // scalastyle
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.7")
addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7") // stats
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0") // dependencyGraph
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4") // dependencyUpdates
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1") // scalariformFormat
addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1")

// Documentation
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.4.2")
addSbtPlugin("io.github.jonas" % "sbt-paradox-material-theme" % "0.5.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.2" exclude("com.lightbend.paradox", "sbt-paradox"))
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.2")
