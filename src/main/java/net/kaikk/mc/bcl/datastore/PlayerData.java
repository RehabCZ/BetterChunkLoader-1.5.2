package net.kaikk.mc.bcl.datastore;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.kaikk.mc.bcl.BetterChunkLoader;

@XmlRootElement
public class PlayerData {
	private String playerId;
	private int alwaysOnChunksAmount, onlineOnlyChunksAmount;
	
	PlayerData(){}
	
	public PlayerData(String playerId) {
		this.playerId = playerId;
		this.alwaysOnChunksAmount=BetterChunkLoader.instance().config().defaultChunksAmountAlwaysOn;
		this.onlineOnlyChunksAmount=BetterChunkLoader.instance().config().defaultChunksAmountOnlineOnly;
	}

	public PlayerData(String playerId, int alwaysOnChunksAmount, int onlineOnlyChunksAmount) {
		this.playerId = playerId;
		this.alwaysOnChunksAmount = alwaysOnChunksAmount;
		this.onlineOnlyChunksAmount = onlineOnlyChunksAmount;
	}

	public String getPlayerId() {
		return playerId;
	}
	
	@XmlAttribute(name="id")
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	/** Total amount of always on chunks that this player can load */
	public int getAlwaysOnChunksAmount() {
		return alwaysOnChunksAmount;
	}
	
	@XmlAttribute(name="aon")
	public void setAlwaysOnChunksAmount(int alwaysOnChunksAmount) {
		this.alwaysOnChunksAmount = alwaysOnChunksAmount;
	}
	
	/** Total amount of online only chunks that this player can load */
	public int getOnlineOnlyChunksAmount() {
		return onlineOnlyChunksAmount;
	}
	
	@XmlAttribute(name="oon")
	public void setOnlineOnlyChunksAmount(int onlineOnlyChunksAmount) {
		this.onlineOnlyChunksAmount = onlineOnlyChunksAmount;
	}
}
