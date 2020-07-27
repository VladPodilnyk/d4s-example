val V = new {
  val distage         = "0.10.16"
  val logstage        = "0.10.16"
  val scalatest       = "3.1.1"
  val scalacheck      = "1.14.3"
  val http4s          = "0.21.6"
  val doobie          = "0.8.8"
  val zio             = "1.0.0-RC21-2"
  val zioCats         = "2.0.0.0-RC13"
  val kindProjector   = "0.11.0"
  val circeDerivation = "0.12.0-M7"
  val awsJavaSdk2     = "2.13.61"
  val scanamo         = "1.0.0-M12"
  val akka            = "2.0.0"
  val d4s             = "1.0.10"
}

val Deps = new {
  val scalatest  = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck

  val distageCore    = "io.7mind.izumi" %% "distage-core" % V.distage
  val distageConfig  = "io.7mind.izumi" %% "distage-extension-config" % V.distage
  val distageRoles   = "io.7mind.izumi" %% "distage-framework" % V.distage
  val distageDocker  = "io.7mind.izumi" %% "distage-framework-docker" % V.distage
  val distageTestkit = "io.7mind.izumi" %% "distage-testkit-scalatest" % V.distage
  val logstageSlf4j  = "io.7mind.izumi" %% "logstage-adapter-slf4j" % V.logstage

  val http4sDsl    = "org.http4s" %% "http4s-dsl" % V.http4s
  val http4sServer = "org.http4s" %% "http4s-blaze-server" % V.http4s
  val http4sClient = "org.http4s" %% "http4s-blaze-client" % V.http4s
  val http4sCirce  = "org.http4s" %% "http4s-circe" % V.http4s

  val circeDerivation = "io.circe" %% "circe-derivation" % V.circeDerivation

  val kindProjector = "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full

  val zio     = "dev.zio" %% "zio" % V.zio
  val zioCats = "dev.zio" %% "zio-interop-cats" % V.zioCats

  val awsDynamo = "software.amazon.awssdk" % "dynamodb" % V.awsJavaSdk2 exclude ("log4j", "log4j")

  val awsImplApache = "software.amazon.awssdk" % "apache-client" % V.awsJavaSdk2 exclude ("log4j", "log4j")

  val scanamo        = "org.scanamo" %% "scanamo" % V.scanamo
  val scanamoAlpakka = "org.scanamo" %% "scanamo-alpakka" % V.scanamo

  val akka = "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % V.akka

  val d4s       = "net.playq" %% "d4s" % V.d4s
  val d4s_circe = "net.playq" %% "d4s-circe" % V.d4s
  val d4s_test  = "net.playq" %% "d4s-test" % V.d4s
}

inThisBuild(
  Seq(
    scalaVersion := "2.13.1",
    version := "1.0.0-SNAPSHOT",
  )
)

lazy val leaderboard = project
  .in(file("."))
  .settings(
    name := "LeaderBoard",
    scalacOptions in Compile += s"-Xmacro-settings:metricsDir=${(classDirectory in Compile).value}",
    scalacOptions in Test += s"-Xmacro-settings:metricsDir=${(classDirectory in Test).value}",
    scalacOptions in Compile += s"-Xmacro-settings:metricsRole=${(name in Compile).value};${(moduleName in Compile).value}",
    scalacOptions in Test += s"-Xmacro-settings:metricsRole=${(name in Test).value};${(moduleName in Test).value}",
    scalacOptions --= Seq("-Werror", "-Xfatal-warnings"),
    //resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      Deps.distageCore,
      Deps.distageRoles,
      Deps.distageConfig,
      Deps.logstageSlf4j,
      Deps.distageDocker % Test,
      Deps.distageTestkit % Test,
      Deps.scalatest % Test,
      Deps.scalacheck % Test,
      Deps.http4sDsl,
      Deps.http4sServer,
      Deps.http4sClient % Test,
      Deps.http4sCirce,
      Deps.circeDerivation,
      Deps.zio,
      Deps.zioCats,
      Deps.awsDynamo,
      Deps.awsImplApache,
      Deps.scanamo,
      Deps.scanamoAlpakka,
      Deps.akka,
      Deps.d4s,
      Deps.d4s_circe,
      Deps.d4s_test % Test,
    ),
    addCompilerPlugin(Deps.kindProjector),
  )
