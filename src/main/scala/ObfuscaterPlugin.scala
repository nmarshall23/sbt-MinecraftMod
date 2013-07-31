package com.gmail.nmarshall23

import sbt._
import Keys._
import org.ldg.mcpd.MCPDInheritanceVisitor
import org.ldg.mcpd.MCPDFileHandler
import java.io.IOException
import org.ldg.mcpd.MCPDInheritanceGraph
import org.ldg.mcpd.MCPDRemapper
import java.util.ArrayList
import org.apache.ivy.Ivy
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.resolve.ResolveOptions



object ObfuscaterPlugin extends Plugin
{

  

object MinecraftReobfuscater {

val recalcInheritance = TaskKey[java.io.File]("Obfuscater-recalc")
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
lazy val retriveMCFromIvySetting = retriveMCFromIvy <<= minecraftLibrary.apply( RetrieveMCFromIvy(_) )



def RetrieveMCFromIvy(config:ModuleID) = {
  
  val ivy = Ivy.newInstance()
  ivy.configureDefault()
  val mrid = ModuleRevisionId.newInstance(config.organization, config.name, config.revision)
  var options = new ResolveOptions()
  options.setOutputReport(false)
  options.setRefresh(false)
  options.setCheckIfChanged(false)
  options.setUseCacheOnly(true)
  
  val report = ivy.resolve(mrid, options, false)

  val getArtifact = {aType:String => (report.getArtifactsReports(mrid)).filter( r => r.getType() == aType).map (r => r.getLocalFile() ).headOption }
  
  val srgFile = getArtifact("srg")
  val jarFile = getArtifact("jar")
  
  (srgFile.get,jarFile.get)
  
//XXX add Error checking
  
//  srgFile match {
//  	case Some(f) => f 
//  	case None    => new File("") // Need to throw an error and expain that the SRG file wasn't found in the Ivy Repo
//  }
  
}

val recalcInheritanceTask = recalcInheritance <<= (packageBin in Compile,  deobfMinecraftJar , target) map { (bin:File, mcjar,t) =>

  val inheritanceFile:File = t / "my.inh"
  
  val infiles = new ArrayList[File]()
  infiles.add(bin)
  infiles.add(mcjar)
  
  val re = CalcInheritance(inheritanceFile,new ArrayList[File](),infiles,infiles )
  inheritanceFile
}

def CalcInheritance(inhFile:File,
					stored_inheritance:ArrayList[File],
				    infiles:ArrayList[File],
				    outfiles:ArrayList[File]):Option[MCPDInheritanceGraph] = {
  println("Calculating inheritance...")
  try {
    
	  val inheritance = new MCPDInheritanceVisitor(inhFile,stored_inheritance)
      val inheritanceProcessor = new MCPDFileHandler(inheritance)
      val failures = inheritanceProcessor.processFiles(infiles, outfiles)
	  inheritance.done()
	  
	  return Some(inheritance.graph)
  }
  catch {
    case e:IOException => //Log failure
      return None
  }
  
}




val reobfuscateTask = reobfuscate <<= (packageBin in Compile, recalcInheritance, retroGuardConfig, target) map { (bin:File, inh, config, t) =>
	val obfzip:File = t / (bin.base + "_ob.jar")
	
  val infiles = new ArrayList[File]()
  infiles.add(bin)
  
  val outfiles = new ArrayList[File]()
  outfiles.add(obfzip)
  
  val storedInh = new ArrayList[File]()
  storedInh.add(inh)
    
  val graph = CalcInheritance(null,storedInh,infiles,outfiles)
  for( gr <- graph ) Translating(config,gr,true,infiles,outfiles)
    
obfzip
}

def Translating(srgFile:File, graph:MCPDInheritanceGraph, invert:Boolean, infiles:ArrayList[File],
				    outfiles:ArrayList[File]) {
  
  try {
	val excluded = new java.util.ArrayList[String]()
    val remapper = new MCPDRemapper(srgFile,excluded,graph,invert)
	
	val remapProcessor = new MCPDFileHandler(remapper);
    val failures = remapProcessor.processFiles(infiles, outfiles);
  } catch {
    case e:IOException => //Log failure
      
  }
  
}

val copyObfuscatedJarTask = copyObfuscated <<= (modDirectoryPath, reobfuscate) map { (modDir, modjar) =>
	
	val destFile:File = new File(modDir, modjar.getName()) 
modjar #> destFile !
}



lazy val obfuscatorTasks = Seq(
recalcInheritanceTask,
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
