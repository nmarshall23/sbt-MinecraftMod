import AssemblyKeys._

assemblySettings

sbtPlugin := true

name := "Minecraft-Obfuscater-plugin"

organization := "com.gmail.nmarshall23"

version := "0.3.1"

//scalaVersion := "2.10.2"
scalaVersion := "2.9.2"

EclipseKeys.withSource := true

assembleArtifact in packageScala := false

artifact in (Compile, assembly) ~= { art =>
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
