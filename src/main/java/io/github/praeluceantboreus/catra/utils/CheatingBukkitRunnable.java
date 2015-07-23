package io.github.praeluceantboreus.catra.utils;

import java.util.ArrayList;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class CheatingBukkitRunnable<T> extends BukkitRunnable
{
	private ArrayList<T> arr;
	private boolean done;
	
	public CheatingBukkitRunnable(ArrayList<T> arr)
	{
		this.arr = arr;
	}
	
	public ArrayList<T> getList()
	{
		return arr;
	}

	public boolean isDone()
	{
		return done;
	}

	public void setDone(boolean done)
	{
		this.done = done;
	}
	
	
}
