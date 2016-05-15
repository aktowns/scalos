addSbtPlugin("com.geirsson" %% "sbt-scalafmt" % "0.2.3")

libraryDependencies ++= Seq(
    "org.scala-native" %% "tools" % "0.1-SNAPSHOT"
)

unmanagedSourceDirectories in Compile ++= {
  val root = baseDirectory.value.getParentFile
  Seq(
    root / "osbuildplugin/src/main/scala"
  )
}

