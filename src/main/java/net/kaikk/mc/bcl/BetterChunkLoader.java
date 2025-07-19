package net.kaikk.mc.bcl;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kaikk.mc.bcl.datastore.DataStoreManager;
import net.kaikk.mc.bcl.datastore.MySqlDataStore;
import net.kaikk.mc.bcl.datastore.XmlDataStore;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import net.milkbowl.vault.permission.Permission;

public class BetterChunkLoader extends JavaPlugin {
	private static BetterChunkLoader instance;
	private Config config;
	private static Permission permissions;
	public boolean enabled;
	
	public void onLoad() {
		// Register XML DataStore
		DataStoreManager.registerDataStore("XML", XmlDataStore.class);
		
		// Register MySQL DataStore
		DataStoreManager.registerDataStore("MySQL", MySqlDataStore.class);
	}
	
	public void onEnable() {
		// check if forge is running
		try {
			Class.forName("net.minecraftforge.common.ForgeVersion");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("BukkitForge/MCPC+ and BCLForgeLib are needed to run this plugin!");
		}
		
		// check if BCLForgeLib is present
		try {
			Class.forName("net.kaikk.mc.bcl.forgelib.BCLForgeLib");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("BCLForgeLib is needed to run this plugin!");
		}
		
		instance=this;
		
		this.enable();
	}
	
	public void enable() {
		// load vault permissions
		if (!this.enabled) {
			permissions = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
			
			try {
				// load config
				this.getLogger().info("Loading config...");
				this.config = new Config(this);
				
				// load messages localization
				Messages.load(this, "messages.yml");
				
				// instantiate data store, if needed
				if (DataStoreManager.getDataStore()==null || !DataStoreManager.getDataStore().getName().equals(config.dataStore)) {
					DataStoreManager.setDataStoreInstance(config.dataStore);
				}
				
				// load datastore
				this.getLogger().info("Loading "+DataStoreManager.getDataStore().getName()+" Data Store...");
				DataStoreManager.getDataStore().load();
				
				this.getLogger().info("Loaded "+DataStoreManager.getDataStore().getChunkLoaders().size()+" chunk loaders data.");
				this.getLogger().info("Loaded "+DataStoreManager.getDataStore().getPlayersData().size()+" players data.");
				
				// load always on chunk loaders
				int count=0;
				for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
					if (cl.isLoadable()) {
						BCLForgeLib.instance().addChunkLoader(cl);
						count++;
					}
				}
				
				this.getLogger().info("Loaded "+count+" always-on chunk loaders.");
				
				this.getLogger().info("Loading Listeners...");
				this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
				this.getCommand("betterchunkloader").setExecutor(new CommandExec(this));
				
				this.getLogger().info("Load complete.");
			} catch (Exception e) {
				e.printStackTrace();
				this.getLogger().warning("Load failed!");
				Bukkit.getPluginManager().disablePlugin(this);
			}
			this.enabled = true;
		}
	}
	
	public void onDisable() {
		this.disable();
		instance=null;
	}
	
	public void disable() {
		if (this.enabled) {
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				BCLForgeLib.instance().removeChunkLoader(cl);
			}
			this.enabled = false;
		}
	}

	public static BetterChunkLoader instance() {
		return instance;
	}
	
	public static long getPlayerLastPlayed(String player) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

		if (offlinePlayer == null) {
			return 0;
		}

		if (offlinePlayer.getLastPlayed()!=0) {
			return offlinePlayer.getLastPlayed();
		} else if (offlinePlayer.getName()!=null && !offlinePlayer.getName().isEmpty()) {
			return getPlayerDataLastModified(player);
		}
		
		return 0;
	}

	public static long getPlayerDataLastModified(String playerName) {
		File playerData = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players"+File.separator+playerName+".dat");
		if (playerData.exists()) {
			return playerData.lastModified();
		}
		return 0;
	}
	
	public Config config() {
		return this.config;
	}
	
	public static boolean hasPermission(OfflinePlayer player, String permission) {
		try {
			return permissions.playerHas(player.getPlayer(), permission) || player.isOp();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean hasPermission(Player player, String permission) {
		try {
			return permissions.playerHas(player, permission) || player.isOp();
		} catch (Exception e) {
			return false;
		}
	}
}
