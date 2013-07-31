sbt-MinecraftMod
================

A SBT plugin to assist with reobfuscating minecraft mods.

You can write mods using this with scala or java. 

I will assume that you have some knowledge of java, scala, sbt, and are using Linux. 
You must have Java, sbt, and Ivy installed.

To use this first you must setup your environment. 

First, setup MCP. 
Run decompile.sh, then recompile.sh.
Next check out my mcmoding-setupscripts. Follow those instructions for publishing the decompiled minecraft.jar to your local Ivy repo.

Next git clone this plugin, then run sbt, and publish-local, 

You are now ready to create a new sbt project and use this plugin. 

In project/plugins.sbt add:

addSbtPlugin("com.gmail.nmarshall23" % "minecraft-obfuscater-plugin" % "0.3.2" classifier "assembly")

Next in build.sbt add: MinecraftReobfuscaterSettings

You may want to change these two settings. 

modDirectoryPath  := Path.userHome / ".minecraft" / "plugins"

minecraftLibrary := "de.ocean-labs" % "mcp" % "1.5.2"

Change the minecraftLibrary version to what ever version you installed to the local Ivy repo.

Lastly after you're mod is compiling. Use the Minecraft-InstallMod task to copy your mod to modDirectoryPath

I hope to soon have a example project that you can clone from github. If you get this working I would love to heard about it.
