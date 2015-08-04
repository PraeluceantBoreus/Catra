package io.github.praeluceantboreus.catra.multicore;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.bukkit.Location;
import org.bukkit.Material;

public class LocationChecker implements Callable<Boolean>
{
	private HashSet<String> atmospherelist;
	private HashSet<String> groundlist;
	private Location loc;
	boolean atmoW;
	boolean groundW;

	public LocationChecker(HashSet<String> atmospherelist, HashSet<String> groundlist, boolean isAtmoWhite, boolean isGroundWhite, Location loc)
	{
		this.atmospherelist = atmospherelist;
		this.groundlist = groundlist;
		this.loc = loc;
		atmoW = isAtmoWhite;
		groundW = isGroundWhite;
	}

	@Override
	public Boolean call() throws Exception
	{
		if ((atmoW && atmospherelist.contains(loc.getBlock().getType().toString())) || !atmoW && !atmospherelist.contains(loc.getBlock().getType().toString()))
		{
			Material above = loc.clone().add(0, 1, 0).getBlock().getType();
			if ((atmoW && atmospherelist.contains(above.toString())) || !atmoW && !atmospherelist.contains(above.toString()))
			{
				String ground = loc.clone().add(0, -1, 0).getBlock().getType().toString();
				if ((groundW && groundlist.contains(ground)) || !groundW && !groundlist.contains(ground))
				{
					System.out.println(loc + " is safe");
					return true;
				}
			}
		}

		return false;
	}

}
