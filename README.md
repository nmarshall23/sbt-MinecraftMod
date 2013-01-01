sbt-minecraft-plugin
A plugin for SBT, to assist with writing Minecraft mods, with Scala.

I'm not going to teach you how to write Minecraft mods with Java, and MCP.

I am assuming that you are using linux. I've not tried this on any other platform.

First, setup MCP, and then check out mcp_debfuscate https://github.com/FunnyMan3595/mcp_deobfuscate
You will be using mcp_deobfuscate to reobfuscate your mod.

You will need to use mcp_deobfuscate to build a retroGuard config file, I have no idea why MCP doesn't ship with the full config file.
Follow the directions for mcp_deobfuscate, then copy the temp/client_deobf.srg that's your retroGuard config file.

Now that you have a deobfuscated version of minecraft.jar and a RG config file, your ready to use this plugin.

Clone the plugin and publish-local, 
Copy this next part into your build.sbt

At the top of your build.sbt add: 

  import ObfuscaterKeys._

Latter in the file to add: 

AddMinecraftPluginDefaults

This adds the default setting to your sbt.

You have to define settings for:

  mcpDeobfuscatePath
  deobfMinecraftJar 
  retroGuardConfig 


I'd like your thoughts on what good defaults would be. 

Here is a snip from one of my projects:
---------------------------- 

MinecraftPluginSettings

mcpDeobfuscatePath := Path.userHome + "/git/mcp_deobfuscate/mcpd.sh"

minecraftVersion := "1.4.5"

baseConfigPath <<= (minecraftVersion) { mcVersion =>
 Path.userHome + "/Projects/mcMods/" + mcVersion + "BTWmix/"
}

deobfMinecraftJar <<= (baseConfigPath, minecraftVersion) { (path,version) => 
	path + "minecraft_" + version + "-deobf.jar"
}

retroGuardConfig <<= (baseConfigPath) { path => path + "decobj_config/full.srg" }



------------------------------

The task Minecraft-InstallMod will copy your mod to your minecraft mod dir.
If your using an OS other then linux you will need to refine
minecraftHome := Path.userHome ...
It's defined as a File setting.


Known BUGs:

When you subclass an obfuscated class, the obfuscator doesn't always pickup all of the inheritated fields and methods.
A work around is to look up in your rg config, and add the instruction to obfuscate your class. Just find the inheritated class and look for what not being obfuscated. Yes, I am very aware how much of a pain these bugs are. I would suggest using a java decomiler like: http://java.decompiler.free.fr to check if the obfuscator is working on your classes.
