package io.github.praeluceantboreus.catra.utils;

import java.util.Random;

import org.bukkit.Location;

public class GaussUtils
{
	public static int betterRand(int from, int to)
	{
		Random rand = new Random();
		int diff = to - from;
		if (diff == 0)
			return 0;
		boolean negative = diff < 0;
		diff *= (negative) ? -1 : 1;
		int randNumber = rand.nextInt(diff + 1);
		randNumber *= (negative) ? -1 : 1;
		return randNumber + from;
	}

	public static Location makeLocationSpawnReady(Location loc)
	{
		loc.setX(fixCoordinate(loc.getX()));
		loc.setZ(fixCoordinate(loc.getZ()));
		return loc;
	}

	public static double fixCoordinate(double coordinate)
	{
		return coordinate += 0.5 * ((coordinate < 0) ? -1 : 1);
	}
}
