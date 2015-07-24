package io.github.praeluceantboreus.catra.main;

import io.github.praeluceantboreus.catra.multicore.LocationChecker;
import io.github.praeluceantboreus.catra.serialize.Coords;
import io.github.praeluceantboreus.catra.serialize.Coords.Position;
import io.github.praeluceantboreus.catra.serialize.ListMode;
import io.github.praeluceantboreus.catra.trading.Offer;
import io.github.praeluceantboreus.catra.trading.TradeItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CatraPlugin extends JavaPlugin
{

	private Coords coordinateManager;
	private HashMap<Entity, Offer> offers;
	private HashSet<String> atmospherelist;
	private HashSet<String> groundlist;
	private ListMode atmosphereMode;
	private ListMode groundMode;
	private LocationChecker lifeSafer;
	private int clusterSize;

	@Override
	public void onEnable()
	{
		super.onEnable();
		genConfig();
		coordinateManager = Coords.deserialize(getConfig().getConfigurationSection("worlds"), getServer());
		offers = new HashMap<>();
		atmospherelist = new HashSet<String>(getConfig().getStringList("trader.atmosphere.list"));
		groundlist = new HashSet<String>(getConfig().getStringList("trader.ground.list"));
		atmosphereMode = ListMode.valueOf(getConfig().getString("trader.atmosphere.listmode"));
		groundMode = ListMode.valueOf(getConfig().getString("trader.ground.listmode"));
		lifeSafer = new LocationChecker(atmospherelist, groundlist, atmosphereMode.equals(ListMode.WHITELIST), groundMode.equals(ListMode.WHITELIST));
		clusterSize = getConfig().getInt("advanced.randomclustersize", 100);
		// generateNewTraders();
		// System.out.println(offers);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, final String[] args)
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
		case "newloc":
		{
			if (args.length < 1)
				return false;
			BukkitRunnable br = new BukkitRunnable()
			{

				@Override
				public void run()
				{
					newOffers(args);

				}
			};
			br.runTaskAsynchronously(this);
			return true;
		}
		case "async":
		{
			print();
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
			ArrayList<Material> list = new ArrayList<>();
			list.add(Material.GRASS);
			list.add(Material.STONE);
			list.add(Material.COBBLESTONE);
			list.add(Material.WOOD);
			list.add(Material.SAND);
			list.add(Material.NETHERRACK);
			ArrayList<String> listString = new ArrayList<>();
			for (Material m : list)
				listString.add(m.toString());
			getConfig().addDefault("trader.ground.list", listString);
			getConfig().addDefault("trader.ground.listmode", ListMode.WHITELIST.toString());
		}
		{
			ArrayList<Material> list = new ArrayList<>();
			list.add(Material.AIR);
			list.add(Material.TORCH);
			list.add(Material.SIGN);
			list.add(Material.LADDER);
			ArrayList<String> listString = new ArrayList<>();
			for (Material m : list)
				listString.add(m.toString());
			getConfig().addDefault("trader.atmosphere.list", listString);
			getConfig().addDefault("trader.atmosphere.listmode", ListMode.WHITELIST.toString());
		}
		{
			ArrayList<Map<String, Object>> buys = new ArrayList<>();
			buys.add(new TradeItemStack(new ItemStack(Material.STONE, 32), 64).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.WOOD, 16), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.COAL, 16), 32).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.CARROT_ITEM, 48), 64).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.POTATO_ITEM, 48), 64).serialize());
			getConfig().addDefault("trader.buys", buys);
		}
		{
			ArrayList<Map<String, Object>> sells = new ArrayList<>();
			sells.add(new TradeItemStack(new ItemStack(Material.SAND, 32), 64).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GRAVEL, 32), 64).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.IRON_INGOT, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.NETHER_WARTS, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.CARROT_ITEM, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.POTATO_ITEM, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SUGAR_CANE, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.BLAZE_ROD, 2), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.LAVA_BUCKET, 1), 3).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.WATER_BUCKET, 1), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 0), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 1), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 2), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 3), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 4), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SAPLING, 8, (short) 5), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SLIME_BALL, 3), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GLOWSTONE_DUST, 16), 32).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.REDSTONE, 16), 32).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.MELON_BLOCK, 2), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.PUMPKIN, 2), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GRASS, 48), 64).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GRASS, 48), 64).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.WATER_LILY, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.RED_MUSHROOM, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.BROWN_MUSHROOM, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.CACTUS, 8), 12).serialize());
			getConfig().addDefault("trader.sells", sells);
		}
		{
			ArrayList<String> names = new ArrayList<>();
			names.add(ChatColor.DARK_BLUE + "Gernot");
			names.add(ChatColor.DARK_RED + "Heinrich");
			names.add(ChatColor.GOLD + "Thomas");
			names.add(ChatColor.DARK_GREEN + "Joseph");
			getConfig().addDefault("trader.names", names);
		}
		getConfig().addDefault("advanced.inithashsize", "");
		getConfig().addDefault("advanced.maxloadfactor", 0.75);
		getConfig().addDefault("advanced.randomclustersize", 100);
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
		return getSaveLocatationBetween(min, max);
	}

	public boolean isSafe(final Location loc) throws InterruptedException, ExecutionException
	{
		lifeSafer.setLoc(loc);
		Future<Boolean> retFut = getServer().getScheduler().callSyncMethod(this, lifeSafer);
		return retFut.get();
	}

	public Location getSaveLocatationBetween(Location loc1, Location loc2)
	{
		for (ArrayList<Location> locArr : getLocationsBetween(loc1, loc2))
		{
			for (Location loc : locArr)
			{
				try
				{
					if (isSafe(loc))
						return loc;
				} catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Location>> getLocationsBetween(Location loc1, Location loc2)
	{
		ArrayList<ArrayList<Location>> ret = new ArrayList<>();
		int x1 = loc1.getBlockX();
		int x2 = loc2.getBlockX();
		int y1 = loc1.getBlockY();
		int y2 = loc2.getBlockY();
		int z1 = loc1.getBlockZ();
		int z2 = loc2.getBlockZ();
		ArrayList<Location> cluster = new ArrayList<>();
		int clusterIterator = 0;
		for (int x = x1; (x1 < x2 && x <= x2) || (x1 > x2 && x >= x2) || x == x2; x += (x1 < x2) ? 1 : -1)
		{
			for (int z = z1; (z1 < z2 && z <= z2) || (z1 > z2 && z >= z2) || z == z2; z += (z1 < z2) ? 1 : -1)
			{
				for (int y = y1; (y1 < y2 && y <= y2) || (y1 > y2 && y >= y2) || y == y2; y += (y1 < y2) ? 1 : -1)
				{
					Location loc = new Location(loc1.getWorld(), x, y, z);
					clusterIterator++;
					if (clusterIterator % clusterSize == 0)
					{
						Collections.shuffle(cluster);
						ret.add((ArrayList<Location>) cluster.clone());
						cluster = new ArrayList<>();
					}
					cluster.add(loc);
				}
			}
		}
		if (cluster != null)
		{
			Collections.shuffle(cluster);
			ret.add(cluster);
		}
		System.out.println("all locs");
		Collections.shuffle(ret);
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
		offers.clear();
		for (World world : getTraderWorlds())
		{
			for (int i = 0; i < amount; i++)
			{
				Entity entitiy = world.spawnEntity(getNextLocation(world), EntityType.VILLAGER);
				offers.put(entitiy, generateOffer());
			}
		}
	}

	public Offer generateOffer()
	{
		return new Offer(getTraderItem("trader.buys").toItemStack(), getTraderItem("trader.sells").toItemStack());
	}

	public TradeItemStack getTraderItem(String path)
	{
		List<Map<?, ?>> list = getConfig().getMapList(path);
		ArrayList<Map<?, ?>> arrlist = new ArrayList<>(list);
		Collections.shuffle(arrlist);
		if (arrlist.size() >= 1)
			return TradeItemStack.deserialize(arrlist.get(0));
		return null;
	}

	public ArrayList<World> getTraderWorlds()
	{
		ArrayList<World> worlds = new ArrayList<>();
		for (String worldName : getConfig().getConfigurationSection("worlds").getValues(false).keySet())
			worlds.add(getServer().getWorld(worldName));
		return worlds;
	}

	public ArrayList<String> getNames()
	{
		return new ArrayList<>(getConfig().getStringList("trader.names"));
	}

	public void newOffers(final String[] args)
	{
		for (World world : getTraderWorlds())
		{
			for (int i = 0; i < Integer.parseInt(args[0]); i++)
				System.out.println(world.getName() + ": " + getNextLocation(world));
		}
	}

	public void print()
	{
		// final Location loc = new Location(getTraderWorlds().get(0), 5, 1,
		// 15);
		BukkitRunnable br = new BukkitRunnable()
		{

			@Override
			public void run()
			{
				BukkitRunnable sync = new BukkitRunnable()
				{

					@Override
					public void run()
					{
						// generateNewTraders();
						System.out.println("beg");
					}
				};
				sync.runTask(CatraPlugin.this);
				while (true)
				{
					System.out.println("baum");
					try
					{
						Thread.sleep(50000);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		br.runTaskLaterAsynchronously(this, 0);
	}

	public void spawnTrader(Location loc)
	{
		loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
	}

	public String nextName()
	{
		List<String> list = getConfig().getStringList("trader.names");

		if (list.size() > 0)
			Collections.shuffle(list);
		else
			return ChatColor.DARK_RED + "Richard Stöckl";
		return list.get(0);
	}
}
