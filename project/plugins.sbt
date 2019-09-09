import sbt.Resolver

scalacOptions ++= Seq( "-unchecked", "-deprecation" )

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")

// SBT-Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Scala.js
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.28")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.9-0.6" exclude("org.scala-js", "sbt-scalajs"))

// Source Control
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Publishing
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2") // show */*:pgpSecretRing
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

// Benchmarking
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")

// App Packaging
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

// Code Quality
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0") // scalastyle
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1") // scalariformFormat

// Documentation
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.6.4")
addSbtPlugin("io.github.jonas" % "sbt-paradox-material-theme" % "0.6.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.3" exclude("com.lightbend.paradox", "sbt-paradox"))
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
