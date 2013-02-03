package com.gmail.nmarshall23

import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ObfuscaterPlugin extends Plugin
{
  

object MinecraftReobfuscater {

val recalcInheritance = TaskKey[java.io.File]("Obfuscater-recalc")
val reobfuscate = TaskKey[java.io.File]("Obfuscater-reobfuscate")
val copyObfuscated = TaskKey[Unit]("Obfuscater-copyToModDir")

val makeFatJar = TaskKey[Unit]("Obfuscater-makeFatJar")


//I keep my RG Config and the deobfuscated minecraft jar in a dir outside of the project


val minecraftHome = SettingKey[java.io.File]("Obfuscater-Minecraft-Home", "Path to Minecraft user data")

val minecraftInstallMod = TaskKey[Unit]("Minecraft-InstallMod")

val lookupMC = TaskKey[Unit]("Minecraft-Lib")
}




import MinecraftReobfuscater._

val lookupMCTask = lookupMC <<= (libraryDependencies) map { (libs) =>

  //IvyRetrieve.toArtifact(art)
}

val minecraftLibrary = SettingKey[ModuleID]("Obfuscater-Managed-Minecraft-Library", "The Managed Minecraft Library")
val minecraftVersion = SettingKey[String]("Obfuscater-minecraft-version", "Version of Minecraft")
val mcpDeobfuscatePath = SettingKey[String]("Obfuscater-mcpd-bin", "Path to Deobfuscate tool") 
val baseConfigPath = SettingKey[String]("Obfuscater-baseconfig-path", "Base path of config files")
val retroGuardConfig = SettingKey[String]("Obfuscater-rg-config", "Config for Deobfuscate tool")
val deobfMinecraftJar = SettingKey[String]("Obfuscater-deobf-mcjar", "Path to Deobfuscated Minecraft Jar")

val defaultminecraftLib = minecraftLibrary := "de.ocean-labs" % "mcp" % "1.4.7+"

val recalcInheritanceTask = recalcInheritance <<= (packageBin in Compile,  mcpDeobfuscatePath, retroGuardConfig, deobfMinecraftJar , target) map { (bin:File, deobf, config, mcjar,t) =>

  val inheritanceFile:File = t / "my.inh"
  
  val results = Process( deobf :: "--config" :: config :: 
      "--inheritance" :: inheritanceFile.absolutePath :: "--invert" :: 
      "--infiles" :: bin.toString :: mcjar.toString :: Nil, file("/") ) !
      
inheritanceFile
}


val reobfuscateTask = reobfuscate <<= (packageBin in Compile, recalcInheritance,  mcpDeobfuscatePath, retroGuardConfig, target) map { (bin:File, inh, deobf, config, t) =>
	val obfzip:File = t / "obfuscated.zip"
	
	val results = Process( deobf :: "--config" :: config :: 
	    "--stored_inheritance" :: inh.absolutePath :: "--invert" :: 
	    "--infiles" :: bin.toString :: "--outfiles" :: obfzip.absolutePath :: Nil, file("/") ) !

obfzip
}

val copyObfuscatedJarTask = copyObfuscated <<= (minecraftHome, reobfuscate, name, version) map { (mchome, modzip, n, v) =>
	val destName = n + "-" + v + ".zip"
	val destFile:File = mchome / "mods" / destName 
modzip #> destFile !
}

private def assemblyTask(out: File, po: Seq[PackageOption], mappings: File => Seq[(File, String)],
      strats: String => MergeStrategy, tempDir: File, cacheDir: File, log: Logger): File =
    Assembly(out, po, mappings, strats, tempDir, cacheDir, log)

val makeFatJarTask = makeFatJar <<= (reobfuscate, packageOptions in assembly,
        assembledMappings in packageDependency, mergeStrategy in assembly,
        assemblyDirectory in assembly, cacheDirectory, streams) map {
      (out, po, am, ms, tempDir, cacheDir, s) => assemblyTask(out, po, am, ms, tempDir, cacheDir, s.log) }

val obfuscatorTasks = Seq(
recalcInheritanceTask,
reobfuscateTask,
makeFatJarTask,
copyObfuscatedJarTask

) ++ assemblySettings


lazy val MinecraftReobfuscaterSettings = obfuscatorTasks ++ Seq[sbt.Project.Setting[_]](
    defaultminecraftLib,
    minecraftVersion := "1.4.7",
    minecraftHome := Path.userHome / ".minecraft",
    minecraftInstallMod <<= copyObfuscated

) 
    
}
