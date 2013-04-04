package de.philworld.bukkit.compassex;

import java.io.IOException;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import de.philworld.bukkit.compassex.command.HelpManager;

public class CompassEx extends JavaPlugin {

	static Economy economy = null;

	HelpManager helpManager = new HelpManager();

	FileConfiguration config;
	DynmapHelper dynmapHelper;
	String markerIcon;

	TrackingComponent tracking;
	SavingComponent saving;
	HidingComponent hiding;
	DeathpointComponent death;
	GeneralComponent general;

	@Override
	public void onEnable() {
		loadConfiguration();

		general = new GeneralComponent(this);
		death = new DeathpointComponent(this);
		saving = new SavingComponent(this);
		tracking = new TrackingComponent(this);
		hiding = new HidingComponent(this);

		boolean enableDynmap = getConfig().getBoolean("enable-dynmap", true);
		markerIcon = getConfig().getString("dynmap-icon", "compass");

		if (setupEconomy()) {
			getLogger().log(Level.INFO, "Using Vault for payment.");
		} else {
			getLogger().log(Level.INFO,
					"Vault was not found, all actions will be free!");
		}

		if (enableDynmap) {
			dynmapHelper = DynmapHelper.init(this);
			if (dynmapHelper != null) {
				getLogger().log(Level.INFO, "Dynmap Support is enabled!");
			} else {
				getLogger()
						.log(Level.WARNING,
								"Dynmap Support could not be enabled: Dynmap not found!");
			}
		}

		try {
			new Metrics(this).start();
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "Error enabling Metrics:", e);
		}
	}

	@Override
	public void onDisable() {
		economy = null;
		tracking.disable();
		try {
			saving.save();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not save locations!", e);
		}
	}

	/**
	 * Loads the configuration and inserts the defaults.
	 */
	private void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	private boolean setupEconomy() {
		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.economy.Economy.class);

			if (economyProvider == null)
				return false;

			economy = economyProvider.getProvider();

			return true;
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

}
