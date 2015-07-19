package io.github.praeluceantboreus.catra.serialize;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Coords implements ConfigurationSerializable
{
	private HashMap<World, HashMap<Position, Location>> locations;

	public Coords()
	{
		locations = new HashMap<>();
	}

	public void addWorld(Location min, Location max) throws IllegalArgumentException
	{
		if (min == null || max == null)
			throw new IllegalArgumentException("Locatitions are not allowed to be null");
		if (!min.getWorld().equals(max.getWorld()))
			throw new IllegalArgumentException("the world from the Locations are not the same");
		HashMap<Position, Location> locs = new HashMap<>();
		min = min.clone();
		max = max.clone();
		roundLocation(min);
		roundLocation(max);
		locs.put(Position.MIN, min);
		locs.put(Position.MAX, max);
		locations.put(min.getWorld(), locs);
	}

	@Override
	public Map<String, Object> serialize()
	{
		HashMap<String, Object> ret = new HashMap<>();
		for (World world : locations.keySet())
		{
			HashMap<Position, Location> values = locations.get(world);
			HashMap<String, Object> depthSave = new HashMap<>();
			for (Position pos : values.keySet())
			{
				Location loc = values.get(pos);
				HashMap<String, Object> coords = new HashMap<>();
				coords.put(Coordinate.X.toString().toLowerCase(), loc.getBlockX());
				coords.put(Coordinate.Y.toString().toLowerCase(), loc.getBlockY());
				coords.put(Coordinate.Z.toString().toLowerCase(), loc.getBlockZ());
				depthSave.put(pos.toString().toLowerCase(), coords);
			}
			ret.put(world.getName(), depthSave);
		}
		return ret;
	}

	public static Coords deserialize(ConfigurationSection cs, Server server)
	{
		System.out.println(cs);
		Coords ret = new Coords();
		for (String worldName : cs.getValues(false).keySet())
		{
			World world = server.getWorld(worldName);
			ConfigurationSection worldSection = cs.getConfigurationSection(worldName);
			HashMap<Position, Location> locs = new HashMap<>();
			for (String posSection : worldSection.getValues(false).keySet())
			{
				ConfigurationSection coordsSection = worldSection.getConfigurationSection(posSection);
				Location loc = new Location(world, coordsSection.getInt(Coordinate.X.toString().toLowerCase()), coordsSection.getInt(Coordinate.Y.toString().toLowerCase()), coordsSection.getInt(Coordinate.Z.toString().toLowerCase()));
				locs.put(Position.valueOf(posSection.toUpperCase()), loc);
			}
			ret.addWorld(locs.get(Position.MIN), locs.get(Position.MAX));
		}
		return ret;
	}

	public void roundLocation(Location loc)
	{
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY());
		loc.setZ(loc.getBlockZ());
	}

	@Override
	public String toString()
	{
		return locations.toString();
	}
}

enum Position
{
	MIN, MAX
}

enum Coordinate
{
	X, Y, Z
}
