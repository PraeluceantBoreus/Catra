package io.github.praeluceantboreus.catra.multicore;

import io.github.praeluceantboreus.catra.main.CatraPlugin;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ClickListener implements Listener
{
	private CatraPlugin plugin;
	private HashSet<String> trading;
	private static final int sellsslot = 2, buysslot = 6;

	public ClickListener(CatraPlugin plugin)
	{
		this.plugin = plugin;
		trading = new HashSet<>();
	}

	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent pie)
	{
		Entity ent = pie.getRightClicked();
		if (plugin.isTrader(ent))
		{
			Inventory inv = plugin.getServer().createInventory(pie.getPlayer(), 9, ent.getCustomName());
			inv.setItem(buysslot, plugin.getOffer(ent).getBuys());
			inv.setItem(sellsslot, plugin.getOffer(ent).getSells());
			pie.getPlayer().openInventory(inv);
			trading.add(pie.getPlayer().getUniqueId().toString());
			pie.setCancelled(true);
		}
	}

	@EventHandler
	public void onTraderDamage(EntityDamageEvent ede)
	{
		if (plugin.isTrader(ede.getEntity()))
			ede.setCancelled(true);
	}

	@EventHandler
	public void onInvClose(InventoryCloseEvent ice)
	{
		trading.remove(ice.getPlayer().getUniqueId().toString());
	}

	@EventHandler
	public void onItemsChange(InventoryClickEvent ice)
	{
		if (trading.contains(ice.getWhoClicked().getUniqueId().toString()))
		{
			if (!ice.getClickedInventory().getType().equals(InventoryType.PLAYER))
			{
				if (ice.getSlot() == buysslot || ice.getSlot() == sellsslot)
				{
					HumanEntity player = ice.getWhoClicked();
					Inventory tradingInv = ice.getClickedInventory();
					Inventory playerInv = player.getInventory();
					ItemStack buys = tradingInv.getItem(buysslot);
					ItemStack sells = tradingInv.getItem(sellsslot);
					if (playerInv.containsAtLeast(buys, buys.getAmount()))
					{
						playerInv.removeItem(buys);
						HashMap<Integer, ItemStack> rest = playerInv.addItem(sells);
						for (ItemStack is : rest.values())
							player.getWorld().dropItemNaturally(player.getLocation(), is);
					}
				}
			}
			ice.setCancelled(true);
		}
	}
}
