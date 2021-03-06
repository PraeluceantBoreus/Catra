package io.github.praeluceantboreus.catra.main;

import io.github.praeluceantboreus.catra.helper.MaterialHelper;
import io.github.praeluceantboreus.catra.multicore.ClickListener;
import io.github.praeluceantboreus.catra.multicore.LocationChecker;
import io.github.praeluceantboreus.catra.serialize.ListMode;
import io.github.praeluceantboreus.catra.trading.Offer;
import io.github.praeluceantboreus.catra.trading.TradeItemStack;
import io.github.praeluceantboreus.catra.utils.GaussUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CatraPlugin extends JavaPlugin
{

	private HashMap<String, Offer> offers;
	private HashSet<String> atmospherelist;
	private HashSet<String> groundlist;
	private ListMode atmosphereMode;
	private ListMode groundMode;
	private int clusterSize, radius, traderamount;
	private long lastGen = 0;
	private BukkitRunnable offerThread;
	private boolean offerLock = false;
	private ArrayList<Location> playerLocs;

	@Override
	public void onEnable()
	{
		super.onEnable();
		genConfig();
		reloadConfig();
		getServer().getPluginManager().registerEvents(new ClickListener(this), this);
		MaterialHelper.writeAll(this);
		offers = new HashMap<>();
		atmospherelist = new HashSet<String>(getConfig().getStringList("trader.atmosphere.list"));
		groundlist = new HashSet<String>(getConfig().getStringList("trader.ground.list"));
		atmosphereMode = ListMode.valueOf(getConfig().getString("trader.atmosphere.listmode"));
		groundMode = ListMode.valueOf(getConfig().getString("trader.ground.listmode"));
		clusterSize = getConfig().getInt("advanced.randomclustersize", 100);
		radius = getConfig().getInt("advanced.locations.radius", 12);
		traderamount = getConfig().getInt("trader.amount", 4);
		playerLocs = new ArrayList<>();
		offerThread = new BukkitRunnable()
		{

			@Override
			public void run()
			{
				if (System.currentTimeMillis() - lastGen >= getConfig().getLong("trader.interval") && !offerLock && getServer().getOnlinePlayers().size() > 0)
				{
					offerLock = true;
					playerLocs.clear();
					for (Player player : getServer().getOnlinePlayers())
						playerLocs.add(player.getLocation());
					BukkitRunnable br = new BukkitRunnable()
					{

						@Override
						public void run()
						{
							ArrayList<Location> safeLocs = new ArrayList<>();
							for (Location playerLoc : playerLocs)
							{
								safeLocs.addAll(getSaveLocatationsBetween(playerLoc.clone().add(-radius, -radius, -radius), playerLoc.clone().add(radius, radius, radius)));
								if (safeLocs.size() >= traderamount)
									break;
							}
							for (int i = 0; i < getConfig().getInt("trader.amount"); i++)
								generateNewTraders(safeLocs);
						}
					};
					br.runTaskAsynchronously(CatraPlugin.this);
				}
			}
		};
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
			lastGen = 0;
			return true;
		}
		default:
			return false;
		}
	}

	public void genConfig()
	{
		getConfig().addDefault("trader.amount", 4);
		getConfig().addDefault("trader.interval", 10 * 60 * 1000);
		getConfig().addDefault("trader.entity", EntityType.VILLAGER.toString());
		getConfig().addDefault("trader.sound.type", Sound.CLICK.toString());
		getConfig().addDefault("trader.sound.volume", 1.0f);
		getConfig().addDefault("trader.sound.pitch", 1.5f);
		getConfig().addDefault("trader.registered", new String[0]);
		{
			ArrayList<Material> list = new ArrayList<>();
			list.add(Material.STONE);
			list.add(Material.GRASS);
			list.add(Material.DIRT);
			list.add(Material.COBBLESTONE);
			list.add(Material.WOOD);
			list.add(Material.SAND);
			list.add(Material.GRAVEL);
			list.add(Material.GOLD_ORE);
			list.add(Material.IRON_ORE);
			list.add(Material.COAL_ORE);
			list.add(Material.LOG);
			list.add(Material.LOG_2);
			list.add(Material.LEAVES);
			list.add(Material.LEAVES_2);
			list.add(Material.SPONGE);
			list.add(Material.GLASS);
			list.add(Material.LAPIS_ORE);
			list.add(Material.LAPIS_BLOCK);
			list.add(Material.DISPENSER);
			list.add(Material.SANDSTONE);
			list.add(Material.NOTE_BLOCK);
			list.add(Material.BED_BLOCK);
			list.add(Material.WOOL);
			list.add(Material.GOLD_BLOCK);
			list.add(Material.IRON_BLOCK);
			list.add(Material.DOUBLE_STEP);
			list.add(Material.DOUBLE_STONE_SLAB2);
			list.add(Material.STEP);
			list.add(Material.BRICK);

			list.add(Material.HUGE_MUSHROOM_1);
			list.add(Material.REDSTONE_LAMP_ON);
			list.add(Material.WOOD_STEP);
			list.add(Material.ICE);
			list.add(Material.QUARTZ_BLOCK);
			list.add(Material.MELON_BLOCK);
			list.add(Material.EMERALD_ORE);
			list.add(Material.ENDER_STONE);
			list.add(Material.BOOKSHELF);
			list.add(Material.QUARTZ_STAIRS);
			list.add(Material.QUARTZ_ORE);
			list.add(Material.HUGE_MUSHROOM_2);
			list.add(Material.NETHER_BRICK);
			list.add(Material.DIAMOND_BLOCK);
			list.add(Material.OBSIDIAN);
			list.add(Material.MOSSY_COBBLESTONE);
			list.add(Material.SOIL);
			list.add(Material.BIRCH_WOOD_STAIRS);
			list.add(Material.TNT);
			list.add(Material.SNOW_BLOCK);
			list.add(Material.FURNACE);
			list.add(Material.EMERALD_BLOCK);
			list.add(Material.ENCHANTMENT_TABLE);
			list.add(Material.IRON_TRAPDOOR);
			list.add(Material.STONE_PLATE);
			list.add(Material.BRICK_STAIRS);
			list.add(Material.SANDSTONE_STAIRS);
			list.add(Material.IRON_PLATE);
			list.add(Material.SMOOTH_BRICK);
			list.add(Material.TRAP_DOOR);
			list.add(Material.GOLD_PLATE);
			list.add(Material.WOOD_PLATE);
			list.add(Material.WORKBENCH);
			list.add(Material.GOLD_BLOCK);
			list.add(Material.STAINED_CLAY);
			list.add(Material.JACK_O_LANTERN);
			list.add(Material.GLASS);
			list.add(Material.PUMPKIN);
			list.add(Material.JUKEBOX);
			list.add(Material.DOUBLE_STEP);
			list.add(Material.NETHERRACK);
			list.add(Material.WOOD);
			list.add(Material.SLIME_BLOCK);
			list.add(Material.REDSTONE_BLOCK);
			list.add(Material.DARK_OAK_STAIRS);
			list.add(Material.SMOOTH_STAIRS);
			list.add(Material.GLOWING_REDSTONE_ORE);
			list.add(Material.BEACON);
			list.add(Material.COBBLESTONE_STAIRS);
			list.add(Material.STAINED_GLASS);
			list.add(Material.ANVIL);
			list.add(Material.REDSTONE_LAMP_OFF);
			list.add(Material.HAY_BLOCK);
			list.add(Material.GLOWSTONE);
			list.add(Material.DISPENSER);
			list.add(Material.COAL_BLOCK);
			list.add(Material.JUNGLE_WOOD_STAIRS);
			list.add(Material.SANDSTONE);
			list.add(Material.SPRUCE_WOOD_STAIRS);
			list.add(Material.ACACIA_STAIRS);
			list.add(Material.RED_SANDSTONE);
			list.add(Material.REDSTONE_ORE);
			list.add(Material.HARD_CLAY);
			list.add(Material.IRON_BLOCK);
			list.add(Material.CLAY);
			list.add(Material.MYCEL);
			list.add(Material.SOUL_SAND);
			list.add(Material.PACKED_ICE);
			list.add(Material.NOTE_BLOCK);
			list.add(Material.BURNING_FURNACE);
			list.add(Material.PRISMARINE);
			list.add(Material.RED_SANDSTONE_STAIRS);
			list.add(Material.SEA_LANTERN);
			list.add(Material.DIAMOND_ORE);
			list.add(Material.WOOD_DOUBLE_STEP);
			list.add(Material.CARPET);
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
			list.add(Material.BROWN_MUSHROOM);
			list.add(Material.FLOWER_POT);
			list.add(Material.COCOA);
			list.add(Material.STONE_BUTTON);
			list.add(Material.RED_MUSHROOM);
			list.add(Material.VINE);
			list.add(Material.DEAD_BUSH);
			list.add(Material.REDSTONE_TORCH_ON);
			list.add(Material.LEVER);
			list.add(Material.SUGAR_CANE_BLOCK);
			list.add(Material.SAPLING);
			list.add(Material.YELLOW_FLOWER);
			list.add(Material.WOOD_BUTTON);
			list.add(Material.RED_ROSE);
			list.add(Material.NETHER_WARTS);
			list.add(Material.LONG_GRASS);
			list.add(Material.DOUBLE_PLANT);
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
			buys.add(new TradeItemStack(new ItemStack(Material.LOG_2, 32, (short) 1), 48).serialize());
			buys.add(new TradeItemStack(new ItemStack(Material.LOG_2, 32, (short) 2), 48).serialize());
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
		getConfig().addDefault("advanced.locations.radius", 24);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public boolean isTrader(Entity entity)
	{
		if (entity == null)
			return false;
		return offers.containsKey(entity.getUniqueId().toString());
	}

	public boolean isSafe(final Location loc) throws InterruptedException, ExecutionException
	{
		LocationChecker lifeSafer = new LocationChecker(atmospherelist, groundlist, this.atmosphereMode.equals(ListMode.WHITELIST), this.groundMode.equals(ListMode.WHITELIST), loc);
		Future<Boolean> retFut = getServer().getScheduler().callSyncMethod(this, lifeSafer);
		return retFut.get();
	}

	public ArrayList<Location> getSaveLocatationsBetween(Location loc1, Location loc2)
	{
		ArrayList<ArrayList<Location>> locs = getLocationsArroundLocations(playerLocs, radius);
		ArrayList<Location> ret = new ArrayList<>();
		Collections.shuffle(locs);
		for (ArrayList<Location> locArr : locs)
		{
			Collections.shuffle(locArr);
			for (Location loc : locArr)
			{
				try
				{
					if (isSafe(loc))
					{
						ret.add(loc);
						if (ret.size() >= traderamount)
							return ret;
						break;
					}
				} catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
			}
		}
		return ret;
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
						ret.add((ArrayList<Location>) cluster.clone());
						cluster = new ArrayList<>();
					}
					cluster.add(loc);
				}
			}
		}
		if (cluster != null)
			ret.add(cluster);
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

	public void removeTraders()
	{
		for (Entity entity : getActiveTraders())
		{
			entity.remove();
		}
		offers = new HashMap<String, Offer>();
	}

	public void generateNewTraders(ArrayList<Location> safeLocs)
	{
		final ArrayList<Entity> trader = new ArrayList<>();
		if (offers.keySet().size() >= getConfig().getInt("trader.amount"))
		{
			removeTraders();
			offers.clear();
			for (Player player : getServer().getOnlinePlayers())
				player.closeInventory();
		}
		Collections.shuffle(safeLocs);
		final Location loc;
		if (safeLocs.size() >= 1)
		{
			loc = GaussUtils.makeLocationSpawnReady(safeLocs.get(0));
			safeLocs.remove(0);
		} else
		{
			offerLock = false;
			lastGen = 0;
			return;
		}
		final Offer offer = generateOffer();
		BukkitRunnable br = new BukkitRunnable()
		{

			@Override
			public void run()
			{
				Entity entitiy = loc.getWorld().spawnEntity(loc, getEntityType());
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
				if (offers.keySet().size() >= getConfig().getInt("trader.amount"))
				{
					lastGen = System.currentTimeMillis();
					offerLock = false;
				}
			}
		};
		br.runTask(this);
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
		offerThread.runTaskTimerAsynchronously(this, 0, 60);
	}

	public ArrayList<ArrayList<Location>> getLocationsArroundLocations(ArrayList<Location> locations, int radius)
	{
		ArrayList<ArrayList<Location>> ret = new ArrayList<>();
		for (Location loc : locations)
		{
			ret.addAll(getLocationsBetween(loc.clone().add(radius, radius, radius), loc.clone().add(-radius, -radius, -radius)));
		}
		return ret;
	}
}
