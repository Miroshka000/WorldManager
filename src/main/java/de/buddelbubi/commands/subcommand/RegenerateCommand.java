package de.buddelbubi.commands.subcommand;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Location;
import org.iq80.leveldb.util.FileUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import de.buddelbubi.WorldManager;

public class RegenerateCommand extends SubCommand {

    public RegenerateCommand() {
        super("regenerate");
        this.setAliases(new String[]{
                "regenerate",
                "reg",
                "reset",
                "regen"
        });
    }

    @Override
    public CommandParameter[] getParameters() {

        LinkedList<CommandParameter> parameters = new LinkedList<>();
        parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newType("world", true, CommandParamType.STRING));
        return parameters.toArray(new CommandParameter[parameters.size()]);
    }

    @Override
    public boolean execute(CommandSender sender, String arg1, String[] args) {

        if (!sender.hasPermission("worldmanager.admin") && !sender.hasPermission("worldmanager.regenerate")) {
            sender.sendMessage(WorldManager.prefix + "§cYou are lacking the permission §e'worldmanager.regenerate'.");
            return false;
        } else {
            if (args.length == 1 || args.length == 2) {
                Level l = null;
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        l = ((Player) sender).getLevel();
                    } else {
                        sender.sendMessage(WorldManager.prefix + "§cDo /worldmanager regenerate [World].");
                        return false;
                    }
                } else if (args.length == 2) {
                    l = Server.getInstance().getLevelByName(args[1]);
                    if (l == null) {
                        sender.sendMessage(WorldManager.prefix + "§cThis world does not exist.");
                        return false;
                    }
                }

                long seed = l.getSeed();
                Generator generator = l.getGenerator();
                String name = l.getFolderName();
                HashMap<String, Location> locations = new HashMap<>();
                for(Player p : l.getPlayers().values()) {
                    locations.put(p.getName(), p.getLocation());
                    p.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
                }

                if (!Server.getInstance().unloadLevel(l, true)) {
                    sender.sendMessage(WorldManager.prefix + "§cFailed to unload the world. Ensure no players are in the world.");
                    return false;
                }

                File regionfolder = new File(Server.getInstance().getDataPath() + "worlds/" + name + "/region");
                if (regionfolder.exists() && regionfolder.isDirectory()) {
                    FileUtils.deleteDirectoryContents(regionfolder);
                }

                File worldfolder = new File(Server.getInstance().getDataPath() + "worlds/" + name);
                if (worldfolder.exists() && worldfolder.isDirectory()) {
                    FileUtils.deleteDirectoryContents(worldfolder);
                    worldfolder.delete();
                }

                Server.getInstance().generateLevel(name, seed, generator.getClass());
                l = Server.getInstance().getLevelByName(name);
                for(String s : locations.keySet()) {
                    Player p = Server.getInstance().getPlayer(s);
                    Location loc = locations.get(s);
                    loc.setLevel(l);
                    p.teleport(loc);
                }
                sender.sendMessage(WorldManager.prefix + "§7World §8" + name + " §7regenerated.");
            } else {
                sender.sendMessage(WorldManager.prefix + "§cDo /worldmanager regenerate [World].");
            }
        }
        return false;
    }

}
