package com.gmail.nmarshall23

import sbt._
import Keys._

object ObfuscaterPlugin extends Plugin
{

object ObfuscaterKeys {
val recalcInheritance = TaskKey[String]("Obfuscater-recalc")
val reobfuscate = TaskKey[String]("Obfuscater-reobfuscate")
val copyObfuscated = TaskKey[Unit]("Obfuscater-copyToModDir")

val minecraftVersion = SettingKey[String]("Obfuscater-minecraft-version", "Version of Minecraft")
val mcpDeobfuscatePath = SettingKey[String]("Obfuscater-mcpd-bin", "Path to Deobfuscate tool") 

//I keep my RG Config and the deobfuscated minecraft jar in a dir outside of the project
val baseConfigPath = SettingKey[String]("Obfuscater-baseconfig-path", "Base path of config files")
val retroGuardConfig = SettingKey[String]("Obfuscater-rg-config", "Config for Deobfuscate tool")
val deobfMinecraftJar = SettingKey[String]("Obfuscater-deobf-mcjar", "Path to Deobfuscated Minecraft Jar")

val minecraftHome = SettingKey[File]("Obfuscater-Minecraft-Home", "Path to Minecraft user data")

val minecraftInstallMod = TaskKey[Unit]("Minecraft-InstallMod")

}



import ObfuscaterKeys._



val AddMinecraftPluginDefaults = Seq(
    
    minecraftVersion := "1.4.5",
    recalcInheritance <<= (packageBin in Compile,  mcpDeobfuscatePath, retroGuardConfig, deobfMinecraftJar , target) map { (bin:File, deobf, config, mcjar,t) =>
val inheritanceFile = t + "/my.inh"
val results = (Process( deobf :: "--config" :: config :: "--inheritance" :: inheritanceFile :: "--invert" :: "--infiles" :: bin.toString :: mcjar.toString :: Nil, file("/") ) !
,inheritanceFile)
results._2
},

reobfuscate <<= (packageBin in Compile, recalcInheritance,  mcpDeobfuscatePath, retroGuardConfig, target) map { (bin:File, inh, deobf, config, t) =>
	val obfzip = t + "/obfuscated.zip"
	val inheritanceFile = t + "/my.inh"
val results = (Process( deobf :: "--config" :: config :: "--stored_inheritance" :: inh :: "--invert" :: "--infiles" :: bin.toString :: "--outfiles" :: obfzip :: Nil, file("/") ) !
 , obfzip)
results._2
},

minecraftHome := Path.userHome / ".minecraft",

copyObfuscated <<= (minecraftHome, reobfuscate, name, version) map { (mchome, modzip, n, v) =>
	val destName = n + "-" + v + ".zip"
	val destFile:File = mchome / "mods" / destName 
file(modzip) #> destFile !
},

minecraftInstallMod := {
  copyObfuscated
}



    )
    
}
