package io.github.praeluceantboreus.catra.multicore;

import io.github.praeluceantboreus.catra.main.CatraPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;

public class ClickListener implements Listener
{
	private CatraPlugin plugin;

	public ClickListener(CatraPlugin plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent pie)
	{
		Entity ent = pie.getRightClicked();
		if (plugin.isTrader(ent))
		{
			Inventory inv = plugin.getServer().createInventory(pie.getPlayer(), 9);
			inv.addItem(plugin.getOffer(ent).getBuys());
			inv.addItem(plugin.getOffer(ent).getSells());
			pie.getPlayer().openInventory(inv);
			pie.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTraderDamage(EntityDamageEvent ede)
	{
		if(plugin.isTrader(ede.getEntity()))
			ede.setCancelled(true);
	}
}
