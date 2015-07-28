package io.github.praeluceantboreus.catra.main;

import io.github.praeluceantboreus.catra.multicore.ClickListener;
import io.github.praeluceantboreus.catra.multicore.LocationChecker;
import io.github.praeluceantboreus.catra.serialize.Coords;
import io.github.praeluceantboreus.catra.serialize.Coords.Position;
import io.github.praeluceantboreus.catra.serialize.ListMode;
import io.github.praeluceantboreus.catra.trading.Offer;
import io.github.praeluceantboreus.catra.trading.TradeItemStack;
import io.github.praeluceantboreus.catra.utils.GaussUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CatraPlugin extends JavaPlugin
{

	private Coords coordinateManager;
	private HashMap<String, Offer> offers;
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
		reloadConfig();
		getServer().getPluginManager().registerEvents(new ClickListener(this), this);
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
		offerLoop();
	}

	@Override
	public void onDisable()
	{
		removeTraders();
		getServer().getScheduler().cancelTasks(this);
		super.onDisable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, final String[] args)
	{
		switch (label)
		{
		case "regen":
		{
			BukkitRunnable br = new BukkitRunnable()
			{

				@Override
				public void run()
				{
					generateNewTraders();

				}
			};
			br.runTaskAsynchronously(this);
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
		getConfig().addDefault("trader.sound.type", Sound.CLICK.toString());
		getConfig().addDefault("trader.sound.volume", 1.0f);
		getConfig().addDefault("trader.sound.pitch", 1.5f);
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
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32, (short) 1), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32, (short) 2), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32, (short) 3), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32, (short) 4), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG, 32, (short) 5), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.COAL, 16, (short) 1), 32).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.CARROT_ITEM, 48), 64).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.POTATO_ITEM, 48), 64).serialize());
			getConfig().addDefault("trader.buys", buys);
		}
		{
			ArrayList<Map<String, Object>> sells = new ArrayList<>();
			sells.add(new TradeItemStack(new ItemStack(Material.SAND, 16), 32).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GRAVEL, 16), 32).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.IRON_INGOT, 1), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.NETHER_WARTS, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.CARROT_ITEM, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.POTATO_ITEM, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.SUGAR_CANE, 6), 9).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.BLAZE_ROD, 2), 3).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.CLAY, 2), 5).serialize());
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
			sells.add(new TradeItemStack(new ItemStack(Material.REDSTONE, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.MELON_BLOCK, 2), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.PUMPKIN, 2), 5).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.GRASS, 16), 48).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.WATER_LILY, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.RED_MUSHROOM, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.BROWN_MUSHROOM, 8), 16).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.CACTUS, 8), 12).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.ENCHANTMENT_TABLE, 1), 1).serialize());
			sells.add(new TradeItemStack(new ItemStack(Material.DIAMOND, 1), 1).serialize());
			getConfig().addDefault("trader.sells", sells);
		}
		{
			ArrayList<String> names = new ArrayList<>();
			ChatColor[] colorArr = { ChatColor.AQUA, ChatColor.BLACK, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_BLUE, ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.GRAY, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW };
			String[] namesArr = { "Heinrich", "Gernot", "Gerhard", "Wolfgang", "Dieter", "Johann", "Wilfried", "Siegfried", "Rudolph", "Joseph" };
			ArrayList<ChatColor> colors = new ArrayList<>(Arrays.asList(colorArr));
			for (String n : namesArr)
			{
				Collections.shuffle(colors);
				names.add(colors.get(0) + n);
			}
			getConfig().addDefault("trader.names", names);
		}
		getConfig().addDefault("advanced.randomclustersize", 100);
		getConfig().addDefault("advanced.locations.use", false);
		getConfig().addDefault("advanced.locations.radius", 12);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public boolean isTrader(Entity entity)
	{
		if (entity == null)
			return false;
		return offers.containsKey(entity.getUniqueId().toString());
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
		Collections.shuffle(ret);
		return ret;
	}

	public ArrayList<Entity> getActiveTraders()
	{
		Set<String> uuids = offers.keySet();
		ArrayList<Entity> entities = new ArrayList<>();
		for (World world : getServer().getWorlds())
			for (Entity ent : world.getEntities())
			{
				if (uuids.contains(ent.getUniqueId().toString()))
					entities.add(ent);
			}
		return entities;
	}

	/*
	 * public void setActiveTraders(ArrayList<Entity> traders) {
	 * ArrayList<String> uuids = new ArrayList<>(); for (Entity entity :
	 * traders) uuids.add(entity.getUniqueId().toString());
	 * getConfig().set("trader.registered", uuids); saveConfig(); }
	 */

	public void removeTraders()
	{
		for (Entity entity : getActiveTraders())
		{
			entity.remove();
		}
		offers = new HashMap<String, Offer>();
	}

	public void generateNewTraders()
	{
		final ArrayList<Entity> trader = new ArrayList<>();
		int amount = getConfig().getInt("trader.amount");
		removeTraders();
		offers.clear();
		for (final World world : getTraderWorlds())
		{
			for (int i = 0; i < amount; i++)
			{
				final Location loc = GaussUtils.makeLocationSpawnReady(getNextLocation(world));
				final Offer offer = generateOffer();
				BukkitRunnable br = new BukkitRunnable()
				{

					@Override
					public void run()
					{
						Entity entitiy = world.spawnEntity(loc, getEntityType());
						entitiy.setCustomName(nextName());
						if (entitiy instanceof LivingEntity)
						{
							LivingEntity lent = (LivingEntity) entitiy;
							PotionEffect dontMove = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10, false, false);
							lent.addPotionEffect(dontMove);
						}
						if (entitiy instanceof Ageable)
						{
							Ageable ageable = (Ageable) entitiy;
							ageable.setBreed(false);
							ageable.setAgeLock(true);
						}
						getLogger().info("Spawned new trader in: " + loc.getWorld().getName() + " on: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
						offers.put(entitiy.getUniqueId().toString(), offer);
						trader.add(entitiy);
					}
				};
				br.runTask(this);
			}
		}
		// setActiveTraders(trader);
	}

	public EntityType getEntityType()
	{
		return EntityType.valueOf(getConfig().getString("trader.entity"));
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

	public String nextName()
	{
		List<String> list = getConfig().getStringList("trader.names");

		if (list.size() > 0)
			Collections.shuffle(list);
		else
			return ChatColor.DARK_RED + "Tschagagwag";
		return list.get(0);
	}

	public Offer getOffer(Entity ent)
	{
		return offers.get(ent.getUniqueId().toString());
	}

	public void offerLoop()
	{
		BukkitRunnable br = new BukkitRunnable()
		{

			@Override
			public void run()
			{
				generateNewTraders();

			}
		};
		br.runTaskTimerAsynchronously(this, 0, getConfig().getLong("trader.interval") / 1000 * 20);
	}
}
