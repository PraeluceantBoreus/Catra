package io.github.praeluceantboreus.catra.main;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class CatraPlugin extends JavaPlugin
{
	public void genConfig()
	{
		getConfig().addDefault("trader.amount", 2);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(0));
		gc.set(GregorianCalendar.HOUR_OF_DAY, 2);
		getConfig().addDefault("trader.interval", gc.getTimeInMillis());
		getConfig().addDefault("trader.entity", EntityType.VILLAGER.toString());
		getConfig().addDefault("trader.registered", new String[0]);
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
}
