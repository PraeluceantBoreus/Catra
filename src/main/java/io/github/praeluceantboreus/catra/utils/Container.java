package io.github.praeluceantboreus.catra.utils;

import java.util.Collection;
import java.util.HashMap;

public class Container<T>
{
	private HashMap<T, Integer> map;

	public Container()
	{
		map = new HashMap<>();
	}

	public Container(Collection<T> coll)
	{
		map = new HashMap<>();
		addAll(coll);
	}

	public void add(T element)
	{
		if (element == null)
			return;
		map.put(element, 1);
	}

	public void addAll(Collection<T> coll)
	{
		if (coll == null)
			return;
		for (T item : coll)
			add(item);
	}

	public Integer remove(T element)
	{
		return map.remove(element);
	}

	public Integer get(T element)
	{
		return map.get(element);
	}

	public boolean contains(T element)
	{
		return map.get(element) != null;
	}
}
