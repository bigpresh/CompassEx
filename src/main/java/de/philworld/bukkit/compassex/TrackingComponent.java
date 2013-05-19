package de.philworld.bukkit.compassex;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.WHITE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.philworld.bukkit.compassex.command.Command;
import de.philworld.bukkit.compassex.command.CommandContext;

public class TrackingComponent extends Component implements Listener {

	/**
	 * Hashmap watcher => watched
	 */
	private final HashMap<String, String> watchList = new HashMap<String, String>(2);
	private final long updateRate;
	private final CompassUpdaterTask updater;

	public TrackingComponent(CompassEx plugin) {
		super(plugin);
		updateRate = plugin.getConfig().getInt("live-update-rate", 200);
		updater = new CompassUpdaterTask();
		updater.start();
	}

	@Command(aliases = { "live" }, permission = "compassex.live")
	public void live(CommandContext context, Player p) {
		List<Player> foundPlayers = plugin.getServer().matchPlayer(context.arg1);

		if (foundPlayers.size() != 1) {
			sendMessage(p, "Player cannot be found.");
			return;
		}

		Player target = foundPlayers.get(0);

		if (plugin.hiding.isHidden(p) && !p.hasPermission("compassex.admin")) {
			sendMessage(p, "Player cannot be found.");
			return;
		}

		try {
			setWatcher(p, target);
		} catch (IllegalArgumentException e) {
			sendMessage(p, e.getMessage());
			return;
		}

		sendMessage(p, "Your compass is now pointing live to " + BLUE + target.getDisplayName() + WHITE + ".");
	}

	/**
	 * Sets a watcher and the watched player, starts the task if not running.
	 * 
	 * @throws IllegalArgumentException
	 *             If both players are the same entity.
	 */
	public void setWatcher(Player watcher, Player watched) {
		if (watcher.equals(watched))
			throw new IllegalArgumentException("Watcher and watched player may not be the same!");

		watchList.put(watcher.getName(), watched.getName());
		updater.start();
	}

	/**
	 * Removes a player from the watchList, watchers as well as watched
	 */
	public void removePlayer(Player player) {
		String name = player.getName();

		if (watchList.isEmpty()) {
			disable();
			return;
		}

		Iterator<Entry<String, String>> it = watchList.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> pair = it.next();
			if (pair.getValue().equals(name)) {
				Player watcher = Bukkit.getServer().getPlayer(pair.getKey());
				if (watcher != null)
					sendMessage(watcher, "Your watched player, " + BLUE + pair.getValue() + WHITE
							+ ", has left the server.");
			} else if (pair.getKey().equals(name)) {
			} else {
				continue;
			}

			it.remove();
		}

		if (watchList.isEmpty())
			disable();
	}

	public void removeWatcher(Player player) {
		String name = player.getName();
		watchList.remove(name);
		if (watchList.isEmpty()) {
			disable();
		}
	}

	public void disable() {
		updater.stop();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		removePlayer(event.getPlayer());
	}

	private class CompassUpdaterTask implements Runnable {

		private int taskId = -2;

		public void start() {
			if (isRunning())
				return;
			taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 40L, updateRate);
		}

		public boolean isRunning() {
			return taskId > 0;
		}

		public void stop() {
			if (!isRunning())
				return;
			plugin.getServer().getScheduler().cancelTask(taskId);
			taskId = -2;
		}

		@Override
		public void run() {
			Server server = plugin.getServer();
			for (Entry<String, String> entry : watchList.entrySet()) {
				Player watcher = server.getPlayer(entry.getKey());
				Player watched = server.getPlayer(entry.getValue());
				watcher.setCompassTarget(watched.getLocation());
				watcher.saveData();
			}
		}
	}

}
