package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.arenas.CTFArena;
import net.slipcor.pvparena.arenas.PumpkinArena;
import net.slipcor.pvparena.arenas.TeamArena;
import net.slipcor.pvparena.arenas.FreeArena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * arena manager class
 * 
 * -
 * 
 * provides access to the arenas, search them, trigger events, communication
 * etc.
 * 
 * @author slipcor
 * 
 * @version v0.5.4
 * 
 */

public class ArenaManager {
	private static Map<String, Arena> arenas = new HashMap<String, Arena>();
	private static DebugManager db = new DebugManager();

	/**
	 * load all configs in the PVP Arena folder
	 */
	public static void load_arenas() {
		db.i("loading arenas...");
		try {
			File path = new File("plugins/pvparena");
			File[] f = path.listFiles();
			int i;
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory() && f[i].getName().contains("config_")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					db.i("standard arena: " + sName);
					loadArena(sName, "");
				}
			}
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory()
						&& f[i].getName().contains("config.free_")) {
					String sName = f[i].getName().replace("config.free_", "");
					sName = sName.replace(".yml", "");
					db.i("free arena: " + sName);
					loadArena(sName, "free");
				}
			}
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory()
						&& f[i].getName().contains("config.ctf_")) {
					String sName = f[i].getName().replace("config.ctf_", "");
					sName = sName.replace(".yml", "");
					db.i("ctf arena: " + sName);
					loadArena(sName, "ctf");
				}
			}
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory()
						&& f[i].getName().contains("config.pumpkin_")) {
					String sName = f[i].getName().replace("config.pumpkin_", "");
					sName = sName.replace(".yml", "");
					db.i("pumpkin arena: " + sName);
					loadArena(sName, "pumpkin");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * load a specific arena
	 * 
	 * @param configFile
	 *            the file to load
	 * @param type
	 *            the arena type
	 */
	public static void loadArena(String configFile, String type) {
		Arena arena;
		db.i("loading arena " + configFile + " (" + type + ")");
		if (type.equals("free")) {
			arena = new FreeArena(configFile);
		} else if (type.equals("ctf")) {
			arena = new CTFArena(configFile);
		} else if (type.equals("pumpkin")) {
			arena = new PumpkinArena(configFile);
		} else {
			arena = new TeamArena(configFile);
		}

		arenas.put(arena.name, arena);
	}

	/**
	 * search the arenas by player
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return the arena name if found, null otherwise
	 */
	public static String getArenaNameByPlayer(Player pPlayer) {
		for (Arena arena : arenas.values()) {
			if (arena.playerManager.existsPlayer(pPlayer))
				return arena.name;
		}
		return null;
	}

	/**
	 * search the arenas by player
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return the arena instance if found, null otherwise
	 */
	public static Arena getArenaByPlayer(Player pPlayer) {
		for (Arena arena : arenas.values()) {
			if (arena.playerManager.existsPlayer(pPlayer))
				return arena;
		}
		return null;
	}

	/**
	 * search the arenas by location
	 * 
	 * @param location
	 *            the location to find
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByRegionLocation(Location location) {
		for (Arena arena : arenas.values()) {
			if (arena.contains(location))
				return arena;
		}
		return null;
	}

	/**
	 * search the arenas by arena name
	 * 
	 * @param sName
	 *            the arena name
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByName(String sName) {
		return arenas.get(sName);
	}

	/**
	 * count the arenas
	 * 
	 * @return the arena count
	 */
	public static int count() {
		return arenas.size();
	}

	/**
	 * return the first arena
	 * 
	 * @return the first arena instance
	 */
	public static Arena getFirst() {
		for (Arena arena : arenas.values()) {
			return arena;
		}
		return null;
	}

	/**
	 * get all arena names
	 * 
	 * @return a string with all arena names joined with comma
	 */
	public static String getNames() {
		String result = "";
		for (String sName : arenas.keySet())
			result += (result.equals("") ? "" : ", ") + sName;
		db.i("arenas: " + result);
		return result;
	}

	/**
	 * powerup tick, tick each arena that uses powerups
	 */
	public static void powerupTick() {
		for (Arena arena : arenas.values()) {
			if (arena.pm == null)
				continue;
			db.i("ticking: arena " + arena.name);
			arena.pm.tick();
		}
	}

	/**
	 * check if an arena has interfering regions with other arenas
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if no running arena interfering, false otherwise
	 */
	public static boolean checkRegions(Arena arena) {
		for (Arena a : arenas.values()) {
			if (a.equals(arena))
				continue;

			if ((a.fightInProgress) && !a.checkRegion(arena)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * reset all arenas
	 */
	public static void reset() {
		for (Arena arena : arenas.values()) {
			db.i("resetting arena " + arena.name);
			arena.reset();
		}
	}

	/**
	 * send a message to everyone on the server
	 * 
	 * @param msg
	 *            the message to send
	 */
	public static void tellPublic(String msg) {
		db.i("broadcast: " + msg);
		Bukkit.getServer().broadcastMessage(
				ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
	}

	/**
	 * send a message to a single player
	 * 
	 * @param player
	 *            the player to send to
	 * @param msg
	 *            the message to send
	 */
	public static void tellPlayer(Player player, String msg) {
		db.i("@" + player.getName() + ": " + msg);
		player.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE
				+ msg);
	}

	/**
	 * take a string array, sort it by first dimension and return
	 * 
	 * @param x
	 *            the input array
	 * @return the sorted output array
	 */
	public static String[][] sort(String[][] x) {
		boolean undone = true;
		String temp;
		String itemp;

		while (undone) {
			undone = false;
			for (int i = 0; i < x.length - 1; i++)
				if (Integer.parseInt(x[i][1]) < Integer.parseInt(x[i + 1][1])) {
					temp = x[i][1];
					x[i][1] = x[i + 1][1];
					x[i + 1][1] = temp;

					itemp = x[i][0];
					x[i][0] = x[i + 1][0];
					x[i + 1][0] = itemp;
					undone = true;
				}
		}
		return x;
	}

	/**
	 * unload and delete an arena
	 * 
	 * @param string
	 *            the arena name to unload
	 */
	public static void unload(String string) {
		Arena a = arenas.get(string);
		db.i("unloading arena " + a.name);
		a.forcestop();
		arenas.remove(string);
		a.cfg.delete();

		File path = new File("plugins/pvparena/stats_" + string + ".yml");
		path.delete();
		a = null;
	}
}
