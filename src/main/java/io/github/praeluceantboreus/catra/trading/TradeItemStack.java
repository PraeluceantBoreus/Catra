package io.github.praeluceantboreus.catra.trading;

import io.github.praeluceantboreus.catra.utils.GaussUtils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class TradeItemStack implements ConfigurationSerializable
{
	private ItemStack itemstack;
	private int max;

	public TradeItemStack(ItemStack itemstack, int max)
	{
		this.itemstack = itemstack;
		this.max = max;
	}

	public ItemStack getItemStack()
	{
		return itemstack;
	}

	public int getMin()
	{
		return itemstack.getAmount();
	}

	public int getMax()
	{
		return max;
	}

	@Override
	public Map<String, Object> serialize()
	{
		HashMap<String, Object> ret = new HashMap<>();
		ret.put(TradeItemStack.Value.MAX.serialize(), getMax());
		ret.put(TradeItemStack.Value.ITEMSTACK.serialize(), getItemStack().serialize());
		return ret;
	}

	public static TradeItemStack deserialize(Map<? extends Object, ? extends Object> cs)
	{
		int max = Integer.parseInt(cs.get(TradeItemStack.Value.MAX.serialize()).toString());
		@SuppressWarnings("unchecked")
		ItemStack itemstack = ItemStack.deserialize((Map<String, Object>) cs.get(TradeItemStack.Value.ITEMSTACK.serialize()));
		return new TradeItemStack(itemstack, max);
	}

	public static enum Value
	{
		ITEMSTACK, MAX;

		public String serialize()
		{
			return toString().toLowerCase();
		}

		public static TradeItemStack.Value deserialize(String lowerString)
		{
			return TradeItemStack.Value.valueOf(lowerString.toUpperCase());
		}
	}

	public ItemStack toItemStack()
	{
		ItemStack ret = getItemStack().clone();
		ret.setAmount(GaussUtils.betterRand(getMin(), getMax()));
		return ret;
	}
}
