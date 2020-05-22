package de.albraeTonomis;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;

import de.albraeTonomis.Utils.ChatUtils;
import de.albraeTonomis.Utils.DataHandler;

public class WhitelistCommand extends Command implements Listener {

	private static java.util.Map<String, Boolean> enabled;
	private static java.util.Map<String, java.util.List<String>> whitelisted;
	private static java.util.Map<String, java.util.List<String>> removed;
	private static String prefix;
	private static String valuecolor;
	private static String messagecolor;
	private static String kickmessage;

	public WhitelistCommand() {
		super("whitelist", "whitelist.use");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Whitelist Commands"));
			sender.sendMessage(ChatUtils.format(prefix + messagecolor
					+ "/whitelist <on | off> [server]: Enables/disables the whitelist. [optional server argument]"));
			sender.sendMessage(ChatUtils.format(prefix + messagecolor
					+ "/whitelist status [server]: Views the status of the whitelist. [optional server argument]"));
			sender.sendMessage(ChatUtils.format(prefix + messagecolor
					+ "/whitelist add <player> [server]: Adds a player to the whitelist. [optional server argument]"));
			sender.sendMessage(ChatUtils.format(prefix + messagecolor
					+ "/whitelist remove <player> [server]: Removes a player from the whitelist. [optional server argument]"));
		} else {
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
				if (args.length == 1) {
					enabled.put("global", true);
					sender.sendMessage(
							ChatUtils.format(prefix + messagecolor + "The global whitelist has been enabled!"));
				} else {
					String server = BungeeEssentials.getInstance().getProxy().getConfig().getServers().get(args[1])
							.getName();
					if (server == null)
						sender.sendMessage(
								ChatUtils.format(prefix + messagecolor + "That is not a server on the network!"));
					else {
						if (enabled.get(server))
							sender.sendMessage(ChatUtils
									.format(prefix + messagecolor + "That server's whitelist is already enabled!"));
						else {
							enabled.put(server, true);
							sender.sendMessage(ChatUtils.format(prefix + messagecolor + "The whitelist for "
									+ valuecolor + server + messagecolor + " has been enabled!"));
						}
					}
				}
			} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
				if (args.length == 1) {
					enabled.put("global", false);
					sender.sendMessage(
							ChatUtils.format(prefix + messagecolor + "The global whitelist has been disabled!"));
				} else {
					String server = BungeeEssentials.getInstance().getProxy().getConfig().getServers().get(args[1])
							.getName();
					if (server == null)
						sender.sendMessage(
								ChatUtils.format(prefix + messagecolor + "That is not a server on the network!"));
					else {
						if (!enabled.get(server))
							sender.sendMessage(ChatUtils
									.format(prefix + messagecolor + "That server's whitelist is already disabled!"));
						else {
							enabled.put(server, false);
							sender.sendMessage(ChatUtils.format(prefix + messagecolor + "The whitelist for "
									+ valuecolor + server + messagecolor + " has been disabled!"));
						}
					}
				}
			} else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("list")) {
				if (args.length == 1) {
					sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Whitelist Status:"));
					sender.sendMessage(
							ChatUtils.format(prefix + messagecolor + "Toggled: " + valuecolor + enabled.get("global")));
					sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Players whitelisted:"));
					for (String player : whitelisted.get("global"))
						sender.sendMessage(ChatUtils.format(prefix + valuecolor + "- " + player));
					sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Players removing:"));
					for (String player : removed.get("global"))
						sender.sendMessage(ChatUtils.format(prefix + valuecolor + "- " + player));
				} else {
					String server = BungeeEssentials.getInstance().getProxy().getConfig().getServers().get(args[1])
							.getName();
					if (server == null)
						sender.sendMessage(
								ChatUtils.format(prefix + messagecolor + "That is not a server on the network!"));
					else {
						sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Whitelist Status for " + valuecolor
								+ server + messagecolor + ":"));
						sender.sendMessage(ChatUtils
								.format(prefix + messagecolor + "Toggled: " + valuecolor + enabled.get(server)));
						sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Players whitelisted:"));
						for (String player : whitelisted.get(server))
							sender.sendMessage(ChatUtils.format(prefix + valuecolor + "- " + player));
						sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Players removing:"));
						for (String player : removed.get(server))
							sender.sendMessage(ChatUtils.format(prefix + valuecolor + "- " + player));
					}
				}
			} else if (args[0].equalsIgnoreCase("add")) {
				if (args.length == 1)
					sender.sendMessage(ChatUtils.format(prefix + "Usage: /whitelist add <player>"));
				else if (args.length == 2) {
					if(removed.get("global").contains(args[1].toLowerCase()))removed.get("global").remove(args[1].toLowerCase());
					whitelisted.get("global").add(args[1].toLowerCase());
					sender.sendMessage(ChatUtils.format(
							prefix + "Added " + valuecolor + args[1] + messagecolor + " to the global whitelist!"));
				} else {
					String server = BungeeEssentials.getInstance().getProxy().getServerInfo(args[2]).getName();
					whitelisted.get(server).add(args[1].toLowerCase());
					if(removed.get(server).contains(args[1].toLowerCase()))removed.get(server).remove(args[1].toLowerCase());
					sender.sendMessage(ChatUtils.format(prefix + messagecolor + "Added " + valuecolor + args[1]
							+ messagecolor + " to the " + valuecolor + server + messagecolor + " whitelist!"));
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length == 1)
					sender.sendMessage(ChatUtils.format(prefix + "Usage: /whitelist remove <player>"));
				else if (args.length == 2) {
					if (!whitelisted.get("global").contains(args[1].toLowerCase())) {
						removed.get("global").add(args[1].toLowerCase());
						sender.sendMessage(ChatUtils.format(
								prefix + "Removed " + valuecolor + args[1] + messagecolor + " from the global whitelist!"));
					} else {
						whitelisted.get("global").remove(args[1].toLowerCase());
						sender.sendMessage(ChatUtils.format(
								prefix + "Removed " + valuecolor + args[1] + messagecolor + " from the global whitelist!"));
					}
				} else {
					String server = BungeeEssentials.getInstance().getProxy().getServerInfo(args[2]).getName();
					if (server == null)
						sender.sendMessage(
								ChatUtils.format(prefix + messagecolor + "That isn't a server on the network!"));
					else {
						/*if (!whitelisted.get("global").contains(args[1].toLowerCase())|!whitelisted.get("global").contains(args[1].toLowerCase())) {
							sender.sendMessage(ChatUtils
									.format(prefix + messagecolor + "That player is not on the global whitelist!"));
							removed.get("global").add(args[1].toLowerCase());
						} else {*/
							if (whitelisted.get(server).contains(args[1].toLowerCase())) {
							whitelisted.get(server).remove(args[1].toLowerCase());
							sender.sendMessage(ChatUtils
									.format(prefix + messagecolor + "Removed " + valuecolor + args[1] + messagecolor
											+ " from the " + valuecolor + server + messagecolor + " whitelist!"));}
							else{
								removed.get(server).add(args[1].toLowerCase());
								sender.sendMessage(ChatUtils
										.format(prefix + messagecolor + "Removed " + valuecolor + args[1] + messagecolor
												+ " from the " + valuecolor + server + messagecolor + " whitelist!"));}
							//}
						}
					}
				}
			 else {
				sender.sendMessage(ChatUtils.format(
						prefix + messagecolor + "Invalid usage! Type \"/whitelist\" for a list of commands"));
				}
		}
	}

	public static void pluginEnable() {
		enabled = new java.util.HashMap<>();
		whitelisted = new java.util.HashMap<>();
		removed = new java.util.HashMap<>();
		try {
			Configuration whitelistconfig = YamlConfiguration.getProvider(YamlConfiguration.class)
					.load(new File(BungeeEssentials.getInstance().getDataFolder() + "/config.yml"));
			for (String servername : BungeeEssentials.getInstance().getProxy().getConfig().getServers().keySet()) {
				// BungeeEssentials.getInstance().getLogger().info(servername);
				enabled.put(servername, whitelistconfig.getBoolean("whitelist." + servername + ".enabled"));
				whitelisted.put(servername, whitelistconfig.getStringList("whitelist." + servername + ".whitelisted"));
				removed.put(servername, whitelistconfig.getStringList("whitelist." + servername + ".removed"));
			}
			enabled.put("global", whitelistconfig.getBoolean("whitelist.global.enabled"));
			whitelisted.put("global", whitelistconfig.getStringList("whitelist.global.whitelisted"));
			removed.put("global", whitelistconfig.getStringList("whitelist.global.removed"));
			prefix = whitelistconfig.getString("config.prefix");
			valuecolor = whitelistconfig.getString("config.value-color");
			messagecolor = whitelistconfig.getString("config.message-color");
			kickmessage = whitelistconfig.getString("config.kick-message");
		} catch (Exception e) {
			BungeeEssentials.getInstance().getLogger().severe("Failed to get config! Whitelist won't work without it!");
			e.printStackTrace();
		}
	}

	public static void pluginDisable() {
		DataHandler.saveEssentials(enabled, whitelisted, removed);
		enabled = null;
		whitelisted = null;
		removed = null;
		prefix = null;
		valuecolor = null;
		messagecolor = null;
		kickmessage = null;
	}

	@EventHandler
    public void onServerJoin(ServerConnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        String server = e.getTarget().getName();
        if (enabled.get("global") && whitelisted.get("global").contains(p.getUniqueId().toString())) {
        	if ((enabled.get(server) && removed.get(server).contains(p.getName().toLowerCase()))) {
            	removed.get(server).remove(p.getName().toLowerCase());
                whitelisted.get(server).remove(p.getUniqueId().toString());
                e.setCancelled(true);
                p.sendMessage(ChatUtils.format("&cKicked whilst connecting to " + server + ": " + kickmessage));
            }
        	else if (enabled.get(server)) {
            	
            	if ((enabled.get(server) && !whitelisted.get(server).contains(p.getUniqueId().toString()))) {
                	if ((enabled.get(server) && !whitelisted.get(server).contains(p.getName().toLowerCase()))) {
                    e.setCancelled(true);
                    p.sendMessage(ChatUtils.format("&cKicked whilst connecting to " + server + ": " + kickmessage));
                	}
                	else {
                		if(!whitelisted.get("global").contains(p.getUniqueId().toString()))
                		whitelisted.get(server).add(p.getUniqueId().toString());
                		whitelisted.get(server).remove(p.getName().toLowerCase());
                	}
            	}
            }
        }
    }

	@EventHandler
	public void onNetworkJoin(LoginEvent e) {
		PendingConnection p = e.getConnection();
		if ((enabled.get("global") && removed.get("global").contains(p.getName().toLowerCase()))) {
			removed.get("global").remove(p.getName().toLowerCase());
			whitelisted.get("global").remove(p.getUniqueId().toString());
			p.disconnect(ChatUtils.format(
					"&cKicked whilst connecting to " + p.getListener().getDefaultServer() + ": " + kickmessage));
		}
		if ((enabled.get("global") && !whitelisted.get("global").contains(p.getUniqueId().toString()))) {
			if ((enabled.get("global") && !whitelisted.get("global").contains(p.getName().toLowerCase())) || (enabled
					.get(p.getListener().getDefaultServer())
					&& !whitelisted.get(p.getListener().getDefaultServer()).contains(p.getName().toLowerCase()))) {
				p.disconnect(ChatUtils.format(
						"&cKicked whilst connecting to " + p.getListener().getDefaultServer() + ": " + kickmessage));
			} else {
				if(!whitelisted.get("global").contains(p.getUniqueId().toString()))
				whitelisted.get("global").add(p.getUniqueId().toString());
				
				whitelisted.get("global").remove(p.getName().toLowerCase());
			}

		}

	}
}
