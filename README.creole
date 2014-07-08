= NametagEdit

== Description
NametagEdit is an easy to configure plugin that allows users to change their nametags and tab names. With NametagEdit, users can add an additional 16 characters before and after their name. Additionally, the reflection in use will keep the plugin compatible with several minecraft versions (currently compatible with 1.5.2 - 1.7.9).

*BukkitDev: [Click Here](http://dev.bukkit.org/bukkit-plugins/nametagedit/)
*Dev Builds: [Click Here](http://ci.playmc.cc/job/NametagEdit)

== Configuration
When the plugin  starts for the first time, it will generate 3 configurable files. They are: "config.yml", "groups.yml" and "players.yml".

=== config.yml

| Option  | Value  | Description |
| TabListDisabled | true/false | If enabled, NametagEdit will not alter a user's name in the tab. This can also be overriden with the function setPlayerListName(); |
| MetricsEnabled  | true/false | If 'true', small amounts of data will be sent to MCstats.org so we can keep track of the plugin's popularity. |

=== groups.yml

Groups:

```  
  Moderator: // This is the 'key' or the thing to distinguish this group //  
	Permission: nte.moderator // This is the permission required to have this nametag - it can be anything //  
	Prefix: '&2' // Both Prefix/Suffix are REQUIRED, even if they are blank. String length will be automatically appended //  
	Suffix: '&f'
```

=== players.yml

```
Players:
  b5ccebaa-0623-4370-af73-0ec985dfa3b0: // This is the 'key' to identify the players //   
	Name: sgtcazeyt // This is the friendly name of the player //  
	Prefix: '&b' // Both Prefix/Suffix are REQUIRED, even if they are blank. String length will be automatically appended //
    Suffix: '&c'
```
== FAQ

**My client crashes with the reason "Cannot remove from NTE #". Why is this?**

Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two scoreboard plugins, whether through packets
or the bukkit scoreboard api - which are basically the same thing, that alter team prefixes/suffixes, will have conflicts. There is currently no way around this.

**My prefixes/suffixes are incorrect. What's wrong?**

This is likely to due to the structure of your permissions. Please ensure the correct groups inherit the correct permissions.