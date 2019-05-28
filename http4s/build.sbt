import EndpointsSettings._

val `algebra-jvm` = LocalProject("algebraJVM")
val `algebra-circe-jvm` = LocalProject("algebra-circeJVM")

val http4sVersion = "0.20.1"

val `http4s-client` =
  project.in(file("client"))
    .settings(
      publishSettings,
      `scala 2.11 to 2.12`,
      name := "endpoints-http4s-client",
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-testing" % http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion
      )
    )
    .dependsOn(`algebra-jvm` % "compile->compile;test->test", `algebra-circe-jvm` % "test->test")
