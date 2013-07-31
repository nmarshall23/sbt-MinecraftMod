package com.gmail.nmarshall23

import sbt._
import org.ldg.mcpd.MCPDInheritanceVisitor
import org.ldg.mcpd.MCPDFileHandler
import scala.collection.JavaConverters._
import java.io.IOException
import org.ldg.mcpd.MCPDInheritanceGraph
import org.ldg.mcpd.MCPDRemapper

object ObfuscationHandler {
  
  var TempDir:File = new File(".")

 def CreateInheritanceTable(infiles:List[File]):File = {
   val inheritanceFile = TempDir / "inheritanceFile.inh"
   val storedInheritanceFiles = List.empty[File]
   val outfiles = infiles
   DoMCPD_InheritanceProcess(inheritanceFile,storedInheritanceFiles,infiles,outfiles)
   
   return inheritanceFile
 }
  
 def ReobfuscateJar(srgFile:File,inheritanceFile:File,infiles:List[File],outfiles:List[File]){
   
   DoMCPD_InheritanceProcess(null,List[File](inheritanceFile),infiles,outfiles).foreach{ graph  => 
     DoMCPD_Remapping(srgFile,graph,true,infiles,outfiles)
   }
 } 
  
 private def DoMCPD_Remapping(	srgFile:File,
		 				graph:MCPDInheritanceGraph,
		 				invert:Boolean,
		 				infiles:List[File],
		 				outfiles:List[File]) {
  
  try {
	
    val remapper = new MCPDRemapper(srgFile, List.empty[String].asJava, graph, invert)
	
	val remapProcessor = new MCPDFileHandler(remapper);
    val failures = remapProcessor.processFiles(infiles.asJava, outfiles.asJava);
  } catch {
    case e:IOException => //Log failure
      
  }
  
}
 
 private def DoMCPD_InheritanceProcess(	inheritanceFile:File,
		 						storedInheritanceFiles:List[File],
		 						infiles:List[File],
		 						outfiles:List[File]
		 						):Option[MCPDInheritanceGraph] = {
   try {
     val inheritance = new MCPDInheritanceVisitor(inheritanceFile,storedInheritanceFiles.asJava)
     val inheritanceProcessor = new MCPDFileHandler(inheritance)
     val failures = inheritanceProcessor.processFiles(infiles.asJava, outfiles.asJava)
     //Should write numbers of failures to log..
	 inheritance.done()
	 return Some(inheritance.graph)
   }
  catch {
    case e:IOException => //Log Exception
      return None
  }
 }
 
  
}