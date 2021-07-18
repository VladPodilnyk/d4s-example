
val V = new {
  val distage         = "1.0.5"
  val logstage        = distage
  val scalatest       = "3.2.7"
  val scalacheck      = "1.15.3"
  val http4s          = "0.21.25"
  val zio             = "1.0.5"
  val zioCats         = "2.4.0.0"
  val kindProjector   = "0.11.3"
  val circeDerivation = "0.13.0-M5"
  val d4s             = "1.0.18"
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

  val d4s       = "net.playq" %% "d4s" % V.d4s
  val d4s_circe = "net.playq" %% "d4s-circe" % V.d4s
  val d4s_test  = "net.playq" %% "d4s-test" % V.d4s
}

inThisBuild(
  Seq(
    scalaVersion := "2.13.5",
    version := "1.0.0-SNAPSHOT",
  )
)

lazy val leaderboard = project
  .in(file("."))
  .settings(
    name := "LeaderBoard",
    scalacOptions -= "-Xfatal-warnings",
    scalacOptions += "-Wconf:msg=kind-projector:silent",
    scalacOptions += "-Wmacros:after",
    scalacOptions in Compile += s"-Xmacro-settings:metricsDir=${(classDirectory in Compile).value}",
    scalacOptions in Test += s"-Xmacro-settings:metricsDir=${(classDirectory in Test).value}",
    scalacOptions in Compile += s"-Xmacro-settings:metricsRole=${(name in Compile).value};${(moduleName in Compile).value}",
    scalacOptions in Test += s"-Xmacro-settings:metricsRole=${(name in Test).value};${(moduleName in Test).value}",
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}",
      s"-Xmacro-settings:sbt-version=${sbtVersion.value}",
      s"-Xmacro-settings:git-repo-clean=${git.gitUncommittedChanges.value}",
      s"-Xmacro-settings:git-branch=${git.gitCurrentBranch.value}",
      s"-Xmacro-settings:git-described-version=${git.gitDescribedVersion.value.getOrElse("")}",
      s"-Xmacro-settings:git-head-commit=${git.gitHeadCommit.value.getOrElse("")}",
    ),
    libraryDependencies ++= Seq(
      Deps.distageCore,
      Deps.distageRoles,
      Deps.distageConfig,
      Deps.logstageSlf4j,
      Deps.distageDocker,
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
      Deps.d4s,
      Deps.d4s_circe,
      Deps.d4s_test,
    ),
    addCompilerPlugin(Deps.kindProjector),
  )
