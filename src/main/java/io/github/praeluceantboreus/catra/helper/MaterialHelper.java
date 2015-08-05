package io.github.praeluceantboreus.catra.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public class MaterialHelper
{
	public static HashSet<Material> getBlocks(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isBlock())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getBurnable(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isBurnable())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getEdible(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isEdible())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getFlammable(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isFlammable())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getOccluding(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isOccluding())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getRecords(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isRecord())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getSolid(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isSolid())
				mats.add(material);
		}
		return mats;
	}

	public static HashSet<Material> getTransparent(Plugin plugin)
	{
		HashSet<Material> mats = new HashSet<>();
		for (Material material : Material.values())
		{
			if (material.isTransparent())
				mats.add(material);
		}
		return mats;
	}

	public static void writeAll(Plugin plugin)
	{
		write(getBlocks(plugin), "blocks", plugin);
		write(getBurnable(plugin), "burnable", plugin);
		write(getEdible(plugin), "edible", plugin);
		write(getFlammable(plugin), "flammable", plugin);
		write(getOccluding(plugin), "occlusing", plugin);
		write(getRecords(plugin), "records", plugin);
		write(getSolid(plugin), "solid", plugin);
		write(getTransparent(plugin), "transparent", plugin);
		write(new HashSet<>(Arrays.asList(Material.values())), "all", plugin);
	}

	@SuppressWarnings("deprecation")
	private static void write(HashSet<Material> materials, String filename, Plugin plugin)
	{
		File out = new File(plugin.getDataFolder() + File.separator + "materials" + File.separator + filename);
		if (!out.getParentFile().exists())
			out.getParentFile().mkdirs();
		int greatestlength = 0;
		for (Material mat : materials)
			if (mat.toString().length() > greatestlength)
				greatestlength = mat.toString().length();
		try (FileWriter fw = new FileWriter(out))
		{
			for (Material material : materials)
				fw.write(material + getSpaces(greatestlength - material.toString().length() + 3) + material.getId() + "\n");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String getSpaces(int amount)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < amount; i++)
			sb.append(" ");
		return sb.toString();
	}
}
