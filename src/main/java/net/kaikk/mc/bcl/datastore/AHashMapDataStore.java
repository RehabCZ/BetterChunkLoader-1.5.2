package net.kaikk.mc.bcl.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kaikk.mc.bcl.BlockLocation;
import net.kaikk.mc.bcl.CChunkLoader;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;

import org.bukkit.Effect;

/** An implementation of IDataStore that stores data into HashMaps
 * It's abstract because it doesn't write any data on disk: all data will be lost at server shutdown
 * Classes that extend this class should store the data somewhere. */
public abstract class AHashMapDataStore implements IDataStore {
	protected Map<String, List<CChunkLoader>> chunkLoaders;
	protected Map<String, PlayerData> playersData;
	
	@Override
	public List<CChunkLoader> getChunkLoaders() {
		List<CChunkLoader> chunkLoaders = new ArrayList<CChunkLoader>();
		for (List<CChunkLoader> clList : this.chunkLoaders.values()) {
			chunkLoaders.addAll(clList);
		}
		return chunkLoaders;
	}

	@Override
	public List<CChunkLoader> getChunkLoadersByWorld(String worldName) {
		List<CChunkLoader> list = this.chunkLoaders.get(worldName);
		if (list==null) {
			return Collections.emptyList();
		}
		return list;
	}

	@Override
	public List<CChunkLoader> getChunkLoadersAt(String worldName, int chunkX, int chunkZ) {
		List<CChunkLoader> chunkLoaders = new ArrayList<CChunkLoader>();
		for (CChunkLoader cl : this.getChunkLoadersByWorld(worldName)) {
			if (cl.getChunkX()==chunkX && cl.getChunkZ()==chunkZ) {
				chunkLoaders.add(cl);
			}
		}
		return chunkLoaders;
	}

	@Override
	public List<CChunkLoader> getChunkLoaders(String ownerId) {
		List<CChunkLoader> chunkLoaders = new ArrayList<CChunkLoader>();
		for (CChunkLoader cl : this.getChunkLoaders()) {
			if (cl.getOwner().equals(ownerId)) {
				chunkLoaders.add(cl);
			}
		}
		return chunkLoaders;
	}

	@Override
	public CChunkLoader getChunkLoaderAt(BlockLocation blockLocation) {
		for (CChunkLoader cl : this.getChunkLoadersByWorld(blockLocation.getWorldName())) {
			if (cl.getLoc().getX()==blockLocation.getX() && cl.getLoc().getZ()==blockLocation.getZ() && cl.getLoc().getY()==blockLocation.getY()) {
				return cl;
			}
		}
		return null;
	}
	
	@Override
	public void addChunkLoader(CChunkLoader chunkLoader) {
		List<CChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorldName());
		if (clList==null) {
			clList = new ArrayList<CChunkLoader>();
			this.chunkLoaders.put(chunkLoader.getWorldName(), clList);
		}
		
		clList.add(chunkLoader);
		chunkLoader.getLoc().getLocation().getWorld().playEffect(chunkLoader.getLoc().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
		if (chunkLoader.isLoadable()) {
			BCLForgeLib.instance().addChunkLoader(chunkLoader);
		}
	}

	@Override
	public void removeChunkLoader(CChunkLoader chunkLoader) {
		List<CChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorldName());
		if (clList!=null) {
			if (chunkLoader.blockCheck()) {
				chunkLoader.getLoc().getLocation().getWorld().playEffect(chunkLoader.getLoc().getLocation(), Effect.POTION_BREAK, 0);
			}
			clList.remove(chunkLoader);
			BCLForgeLib.instance().removeChunkLoader(chunkLoader);
		}
	}
	
	@Override
	public void removeChunkLoaders(String ownerId) {
		List<CChunkLoader> clList = this.getChunkLoaders(ownerId);
		for (CChunkLoader cl : clList) {
			this.getChunkLoadersByWorld(cl.getWorldName()).remove(cl);
		}
	}
	
	@Override
	public void changeChunkLoaderRange(CChunkLoader chunkLoader, byte range) {
		if (chunkLoader.isLoadable()) {
			BCLForgeLib.instance().removeChunkLoader(chunkLoader);
		}
		
		chunkLoader.setRange(range);
		
		if (chunkLoader.isLoadable()) {
			BCLForgeLib.instance().addChunkLoader(chunkLoader);
		}
	}

	@Override
	public int getAlwaysOnFreeChunksAmount(String playerId) {
		int clAmount=this.getPlayerData(playerId).getAlwaysOnChunksAmount();
		for (CChunkLoader cl : this.getChunkLoaders(playerId)) {
			if (cl.isAlwaysOn()) {
				clAmount-=cl.size();
			}
		}
		
		return clAmount;
	}

	@Override
	public int getOnlineOnlyFreeChunksAmount(String playerId) {
		int clAmount=this.getPlayerData(playerId).getOnlineOnlyChunksAmount();
		for (CChunkLoader cl : this.getChunkLoaders(playerId)) {
			if (!cl.isAlwaysOn()) {
				clAmount-=cl.size();
			}
		}
		
		return clAmount;
	}

	@Override
	public void setAlwaysOnChunksLimit(String playerId, int amount) {
		PlayerData playerData = this.getPlayerData(playerId);
		playerData.setAlwaysOnChunksAmount(amount);
	}

	@Override
	public void setOnlineOnlyChunksLimit(String playerId, int amount) {
		PlayerData playerData = this.getPlayerData(playerId);
		playerData.setOnlineOnlyChunksAmount(amount);
	}

	@Override
	public void addAlwaysOnChunksLimit(String playerId, int amount) {
		PlayerData playerData = this.getPlayerData(playerId);
		playerData.setAlwaysOnChunksAmount(playerData.getAlwaysOnChunksAmount()+amount);
	}

	@Override
	public void addOnlineOnlyChunksLimit(String playerId, int amount) {
		PlayerData playerData = this.getPlayerData(playerId);
		playerData.setOnlineOnlyChunksAmount(playerData.getOnlineOnlyChunksAmount()+amount);
	}
	
	@Override
	public PlayerData getPlayerData(String playerId) {
		PlayerData playerData = this.playersData.get(playerId);
		if (playerData==null) {
			playerData = new PlayerData(playerId);
			this.playersData.put(playerId, playerData);
		}
		return playerData;
	}
	
	@Override
	public List<PlayerData> getPlayersData() {
		return new ArrayList<>(this.playersData.values());
	}
}
