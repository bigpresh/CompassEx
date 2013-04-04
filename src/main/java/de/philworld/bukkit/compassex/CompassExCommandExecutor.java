package de.philworld.bukkit.compassex;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.philworld.bukkit.compassex.command.CommandContext;
import de.philworld.bukkit.compassex.command.CommandManager;
import de.philworld.bukkit.compassex.util.PermissionException;

public class CompassExCommandExecutor implements CommandExecutor {

	private final CompassEx plugin;
	private final CommandManager manager;

	public CompassExCommandExecutor(CompassEx plugin) {
		this.plugin = plugin;
		manager = new CommandManager(plugin.getLogger());
		manager.register(plugin.tracking);
		manager.register(plugin.saving);
		manager.register(plugin.hiding);
		manager.register(plugin.death);
		manager.register(plugin.general);

		plugin.getCommand("compass").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (manager.onCommand(sender, command, label, args))
			return true;
		Player p = (Player) sender;
		try {
			if (args.length == 3) {
				plugin.general.position(
						new CommandContext(command, label, args), p);
			} else if (p.hasPermission("compassex.player") && args.length == 1) {
				plugin.general.player(new CommandContext(command, label, args),
						p);
			} else {
				plugin.general
						.help(new CommandContext(command, label, args), p);
			}
		} catch (PermissionException e) {
			e.send(p);
		}
		return true;
	}
}
