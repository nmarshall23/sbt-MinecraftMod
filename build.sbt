sbtPlugin := true

name := "Minecraft-Obfuscater-plugin"

organization := "com.gmail.nmarshall23"

version := "0.3"

EclipseKeys.withSource := true

resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.5")

