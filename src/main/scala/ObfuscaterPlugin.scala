package com.gmail.nmarshall23

import sbt._
import sbt.Keys._

import scala.collection.JavaConverters._
import sbt.ConfigKey.configurationToKey
import sbt.Scoped.t2ToTable2
import sbt.Scoped.t4ToTable4
import scala.Array.canBuildFrom


object ObfuscaterPlugin extends Plugin
{

  

object MinecraftReobfuscater {

val reobfuscate = TaskKey[java.io.File]("Obfuscater-reobfuscate")
val copyObfuscated = TaskKey[Unit]("Obfuscater-copyToModDir")

val makeFatJar = TaskKey[Unit]("Obfuscater-makeFatJar")



val minecraftInstallMod = TaskKey[Unit]("Minecraft-InstallMod")


}


import MinecraftReobfuscater._


lazy val minecraftLibrary = SettingKey[ModuleID]("Obfuscater-Managed-Minecraft-Library", "The Managed Minecraft Library")

lazy val modDirectoryPath = SettingKey[File]("Obfuscater-mod-directory-path", "Directory for mods to be copyed to")
lazy val retroGuardConfig = SettingKey[File]("Obfuscater-rg-config", "Config for Deobfuscate tool")
lazy val deobfMinecraftJar = SettingKey[File]("Obfuscater-deobf-mcjar", "Path to Deobfuscated Minecraft Jar")


lazy val retriveMCFromIvy = SettingKey[(File,File)]("Obfuscater-retriveMCFromIvy")
lazy val retriveMCFromIvySetting = retriveMCFromIvy <<= minecraftLibrary.apply( IvyHandler.RetrieveModule(_) )

lazy val reobfuscateTask = reobfuscate <<= (packageBin in Compile, deobfMinecraftJar, retroGuardConfig, target) map {
    (bin:File, mcjar, srgFile, t) =>
	
    val obfuscatedJar:File = t / (bin.base + "_ob.jar")
	
	ObfuscationHandler.TempDir = t
	println("Calculating inheritance...")
	val inh = ObfuscationHandler.CreateInheritanceTable(List(bin,mcjar))
	
	println("Reobfuscating...")
	ObfuscationHandler.ReobfuscateJar(srgFile, inh, List(bin), List(obfuscatedJar))
	
	obfuscatedJar
}

val copyObfuscatedJarTask = copyObfuscated <<= (modDirectoryPath, reobfuscate) map { (modDir, modjar) =>
	
	val destFile:File = new File(modDir, modjar.getName()) 
	modjar #> destFile !
}



lazy val obfuscatorTasks = Seq(
reobfuscateTask,
copyObfuscatedJarTask,
retriveMCFromIvySetting
)



lazy val MinecraftReobfuscaterSettings = obfuscatorTasks ++ Seq[sbt.Project.Setting[_]](
    retroGuardConfig <<= retriveMCFromIvy.apply(_._1),
    deobfMinecraftJar <<= retriveMCFromIvy.apply(_._2),
    minecraftLibrary := "de.ocean-labs" % "mcp" % "1.5.2",
    modDirectoryPath  := Path.userHome / ".minecraft" / "mods",
    minecraftInstallMod <<= copyObfuscated

) 
    
}
