package io.github.praeluceantboreus.catra.main;

import io.github.praeluceantboreus.catra.serialize.Coords;
import io.github.praeluceantboreus.catra.serialize.Coords.Position;
import io.github.praeluceantboreus.catra.serialize.ListMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CatraPlugin extends JavaPlugin
{

	private Coords coordinateManager;

	@Override
	public void onEnable()
	{
		genConfig();
		coordinateManager = Coords.deserialize(getConfig().getConfigurationSection("worlds"), getServer());
		super.onEnable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		switch (label)
		{
		case "spawnv":
		{
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			System.out.println(p);
			return true;
		}
		default:
			return false;
		}
	}

	public void genConfig()
	{
		Coords coords = new Coords();
		for (World w : getServer().getWorlds())
		{
			coords.addWorld(new Location(w, 0, 0, 0), new Location(w, 100, 255, 300));
		}
		getConfig().addDefault("worlds", coords.serialize());
		getConfig().addDefault("trader.amount", 2);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(0));
		gc.set(GregorianCalendar.HOUR_OF_DAY, 2);
		getConfig().addDefault("trader.interval", gc.getTimeInMillis());
		getConfig().addDefault("trader.entity", EntityType.VILLAGER.toString());
		getConfig().addDefault("trader.registered", new String[0]);
		{
			ArrayList<Material> whitelist = new ArrayList<>();
			ArrayList<Material> blacklist = new ArrayList<>();
			whitelist.add(Material.GRASS);
			whitelist.add(Material.WOOD);
			whitelist.add(Material.SAND);
			whitelist.add(Material.NETHERRACK);
			ArrayList<String> whitelistString = new ArrayList<>();
			for (Material m : whitelist)
				whitelistString.add(m.toString());
			ArrayList<String> blacklistString = new ArrayList<>();
			for (Material m : blacklist)
				blacklistString.add(m.toString());
			getConfig().addDefault("trader.ground.whitelist", whitelistString);
			getConfig().addDefault("trader.ground.blacklist", blacklistString);
			getConfig().addDefault("trader.ground.listmode", ListMode.WHITELIST.toString());
		}
		{
			ArrayList<Material> whitelist = new ArrayList<>();
			ArrayList<Material> blacklist = new ArrayList<>();
			whitelist.add(Material.AIR);
			whitelist.add(Material.TORCH);
			whitelist.add(Material.SIGN);
			whitelist.add(Material.LADDER);
			ArrayList<String> whitelistString = new ArrayList<>();
			for (Material m : whitelist)
				whitelistString.add(m.toString());
			ArrayList<String> blacklistString = new ArrayList<>();
			for (Material m : blacklist)
				blacklistString.add(m.toString());
			getConfig().addDefault("trader.atmosphere.whitelist", whitelistString);
			getConfig().addDefault("trader.atmosphere.blacklist", blacklistString);
			getConfig().addDefault("trader.atmosphere.listmode", ListMode.WHITELIST.toString());
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public boolean isTrader(Entity entity)
	{
		if (entity == null)
			return false;
		List<String> ents = getConfig().getStringList("trader.registered");
		for (String uuid : ents)
			if (entity.getUniqueId().toString().equalsIgnoreCase(uuid))
				return true;
		return false;
	}

	public Location getNextLocation(World world)
	{
		HashMap<Position, Location> locs = coordinateManager.getWorldBounds(world);
		Location min = locs.get(Position.MIN);
		Location max = locs.get(Position.MAX);
		return randomLocationBetween(min, max);
	}

	public Location randomLocationBetween(Location loc1, Location loc2)
	{
		ArrayList<Location> locs = getSaveLocatationsBetween(loc1, loc2);
		if (locs.size() < 1)
			return null;
		Collections.shuffle(locs);
		return locs.get(0);
	}

	public boolean isSafe(Location loc)
	{
		ListMode atmosphereMode = ListMode.valueOf(getConfig().getString("trader.atmosphere.listmode"));
		ArrayList<String> atmosphereWhitelist = new ArrayList<>(getConfig().getStringList("trader.atmosphere.whitelist"));
		ArrayList<String> atmosphereBlacklist = new ArrayList<>(getConfig().getStringList("trader.atmosphere.blacklist"));
		if ((atmosphereMode.equals(ListMode.WHITELIST) && atmosphereWhitelist.contains(loc.getBlock().getType().toString())) || (atmosphereMode.equals(ListMode.BLACKLIST) && !atmosphereBlacklist.contains(loc.getBlock().getType().toString())))
		{
			Material above = loc.clone().add(0, 1, 0).getBlock().getType();
			if ((atmosphereMode.equals(ListMode.WHITELIST) && atmosphereWhitelist.contains(above.toString())) || (atmosphereMode.equals(ListMode.BLACKLIST) && !atmosphereBlacklist.contains(above.toString())))
			{
				ListMode groundMode = ListMode.valueOf(getConfig().getString("trader.ground.listmode"));
				ArrayList<String> groundWhitelist = new ArrayList<>(getConfig().getStringList("trader.ground.whitelist"));
				ArrayList<String> groundBlacklist = new ArrayList<>(getConfig().getStringList("trader.ground.blacklist"));
				String ground = loc.clone().add(0, -1, 0).getBlock().getType().toString();
				if ((groundMode.equals(ListMode.WHITELIST) && groundWhitelist.contains(ground)) || (groundMode.equals(ListMode.BLACKLIST) && !groundBlacklist.contains(ground)))
				{
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Location> getSaveLocatationsBetween(Location loc1, Location loc2)
	{
		ArrayList<Location> ret = new ArrayList<>();
		for (Location loc : getLocationsBetween(loc1, loc2))
			if (isSafe(loc))
				ret.add(loc);
		return ret;
	}

	public ArrayList<Location> getLocationsBetween(Location loc1, Location loc2)
	{
		ArrayList<Location> ret = new ArrayList<>();
		int x1 = loc1.getBlockX();
		int x2 = loc2.getBlockX();
		int y1 = loc1.getBlockY();
		int y2 = loc2.getBlockY();
		int z1 = loc1.getBlockZ();
		int z2 = loc2.getBlockZ();
		for (int x = x1; (x1 < x2 && x <= x2) || (x1 > x2 && x >= x2); x += (x1 < x2) ? 1 : -1)
		{
			for (int y = y1; (y1 < y2 && y <= y2) || (y1 > y2 && y >= y2); y += (y1 < y2) ? 1 : -1)
			{
				for (int z = z1; (z1 < z2 && z <= z2) || (z1 > z2 && z >= z2); z += (z1 < z2) ? 1 : -1)
				{
					Location loc = new Location(loc1.getWorld(), x, y, z);
					ret.add(loc);
				}
			}
		}
		return ret;
	}

	public ArrayList<Entity> getActiveTraders()
	{
		ArrayList<String> uuids = new ArrayList<>(getConfig().getStringList("target.registered"));
		ArrayList<Entity> entities = new ArrayList<>();
		for (World world : getServer().getWorlds())
			for (Entity ent : world.getEntities())
				if (uuids.contains(ent.getUniqueId().toString()))
					entities.add(ent);
		return entities;
	}

	public void setActiveTraders(ArrayList<Entity> traders)
	{
		ArrayList<String> uuids = new ArrayList<>();
		for (Entity entity : traders)
			uuids.add(entity.getUniqueId().toString());
		getConfig().set("trader.registered", uuids.toArray(new String[uuids.size()]));
	}

	public void removeTraders()
	{
		for (Entity entity : getActiveTraders())
			entity.remove();
		setActiveTraders(new ArrayList<Entity>());
	}

	public void generateNewTraders()
	{
		int amount = getConfig().getInt("trader.amount");
		for (String worldName : getConfig().getConfigurationSection("worlds").getValues(false).keySet())
		{
			for (int i = 0; i < amount; i++)
			{
				World world = getServer().getWorld(worldName);
				world.spawnEntity(getNextLocation(world), EntityType.VILLAGER);
			}
		}
	}
}
