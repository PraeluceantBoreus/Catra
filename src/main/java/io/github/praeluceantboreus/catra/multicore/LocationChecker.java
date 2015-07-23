package io.github.praeluceantboreus.catra.multicore;

import io.github.praeluceantboreus.catra.serialize.ListMode;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.bukkit.Location;
import org.bukkit.Material;

public class LocationChecker implements Callable<Boolean>
{
	private HashSet<String> atmosphereWhitelist;
	private HashSet<String> atmosphereBlacklist;
	private HashSet<String> groundWhitelist;
	private HashSet<String> groundBlacklist;
	private Location loc;
	boolean atmoW;
	boolean groundW;

	public LocationChecker(HashSet<String> atmosphereWhitelist, HashSet<String> atmosphereBlacklist, HashSet<String> groundWhitelist, HashSet<String> groundBlacklist, ListMode atmosphereMode, ListMode groundMode)
	{
		this.atmosphereWhitelist = atmosphereWhitelist;
		this.atmosphereBlacklist = atmosphereBlacklist;
		this.groundWhitelist = groundWhitelist;
		this.groundBlacklist = groundBlacklist;
		atmoW = atmosphereMode.equals(ListMode.WHITELIST);
		groundW = groundMode.equals(ListMode.WHITELIST);
	}

	public void setLoc(Location loc)
	{
		this.loc = loc;
	}

	@Override
	public Boolean call() throws Exception
	{
		if ((atmoW && atmosphereWhitelist.contains(loc.getBlock().getType().toString())) || !atmoW && !atmosphereBlacklist.contains(loc.getBlock().getType().toString()))
		{
			Material above = loc.clone().add(0, 1, 0).getBlock().getType();
			if ((atmoW && atmosphereWhitelist.contains(above.toString())) || !atmoW && !atmosphereBlacklist.contains(above.toString()))
			{
				String ground = loc.clone().add(0, -1, 0).getBlock().getType().toString();
				if ((groundW && groundWhitelist.contains(ground)) || !groundW && !groundBlacklist.contains(ground))
				{
					return true;
				}
			}
		}

		return false;
	}

}
