package com.gmail.nmarshall23

import sbt._
import org.apache.ivy.Ivy
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.resolve.ResolveOptions

object IvyHandler {

  def RetrieveModule(config:ModuleID) = {
  
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
  
}