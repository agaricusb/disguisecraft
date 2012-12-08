package pgDev.bukkit.DisguiseCraft.listeners.attack;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DynamicClassFunctions;

public class PlayerAttack {
	public Object attacker;
	public Object victim;
	
	public PlayerAttack(Player attacker, Player victim) {
		this.attacker = DynamicClassFunctions.convertPlayerEntity(attacker);
		this.victim = DynamicClassFunctions.convertPlayerEntity(victim);;
	}
}
