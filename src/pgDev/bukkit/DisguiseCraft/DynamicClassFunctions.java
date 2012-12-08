package pgDev.bukkit.DisguiseCraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;

public class DynamicClassFunctions {
	static String nmsPrefix = "net.minecraft.server.";
	static String obcPrefix = "org.bukkit.craftbukkit.";
	
	static String nmsPackage = "";
	static String obcPackage = "";
	
	public static HashMap<Player, Object> netServerHandlers = new HashMap<Player, Object>();

	public static boolean setPackages() {
		final Package[] packages = Package.getPackages();
		for (Package p : packages) {
			String name = p.getName();
			if (name.startsWith(nmsPrefix)) {
				try {
					nmsPackage = Class.forName("NetServerHandler").getPackage().getName();
					if (!nmsPackage.equals("") && !obcPackage.equals("")) {
						return true;
					}
				} catch (ClassNotFoundException e) {
				}
			} else if (name.startsWith(obcPrefix)) {
				if (name.endsWith("libs")) {
					try {
						obcPackage = Class.forName("CraftServer").getPackage().getName();
						if (!nmsPackage.equals("") && !obcPackage.equals("")) {
							return true;
						}
					} catch (ClassNotFoundException e) {
					}
				}
			}
		}
		return false;
	}
	
	public static HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();
	public static boolean setClasses() {
		try {
			classes.put("CraftPlayer", Class.forName(obcPackage + ".entity.CraftPlayer"));
			classes.put("EntityPlayer", Class.forName(nmsPackage + ".EntityPlayer"));
			classes.put("NetServerHandler", Class.forName(nmsPackage + ".NetServerHandler"));
			classes.put("Packet", Class.forName(nmsPackage + ".Packet"));
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static HashMap<String, Method> methods = new HashMap<String, Method>();
	public static boolean setMethods() {
		try {
			methods.put("CraftPlayer.getHandle()", classes.get("CraftPlayer").getDeclaredMethod("getHandle"));
			methods.put("NetServerHandler.sendPacket(Packet)", classes.get("NetServerHandler").getDeclaredMethod("sendPacket", classes.get("Packet")));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static HashMap<String, Field> fields = new HashMap<String, Field>();
	public static boolean setFields() {
		try {
			fields.put("EntityPlayer.netServerHandler", classes.get("EntityPlayer").getDeclaredField("netServerHandler"));
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static Object getCastTo(Object toCast, String newClass) {
		return classes.get(newClass).cast(toCast);
	}
	
	
	public static void addNSH(Player player) {
		try {
			Object entityPlayer = methods.get("CraftPlayer.getHandle()").invoke(player);
			netServerHandlers.put(player, fields.get("EntityPlayer.netServerHandler").get(entityPlayer));
		} catch (Exception e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Could not obtain NSH of player: " + player.getName(), e);
		}
	}
	
	public static void removeNSH(Player player) {
		netServerHandlers.remove(player);
	}
	
	
	public static void sendPacket(Player player, Object packet) {
		if (netServerHandlers.containsKey(player)) {
			try {
				methods.get("NetServerHandler.sendPacket(Packet)").invoke(netServerHandlers.get(player), packet);
			} catch (Exception e) {
				DisguiseCraft.logger.log(Level.SEVERE, "Error sending packet to player: " + player.getName(), e);
			}
		} else {
			DisguiseCraft.logger.log(Level.WARNING, "NSH not found for player: " + player.getName());
		}
	}
	
}
