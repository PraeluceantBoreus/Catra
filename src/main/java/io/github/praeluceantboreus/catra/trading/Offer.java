package io.github.praeluceantboreus.catra.trading;

import org.bukkit.inventory.ItemStack;

public class Offer
{
	private ItemStack buys, sells;

	public ItemStack getBuys()
	{
		return buys;
	}

	public ItemStack getSells()
	{
		return sells;
	}

	public Offer(ItemStack buys, ItemStack sells)
	{
		super();
		this.buys = buys;
		this.sells = sells;
	}

}
