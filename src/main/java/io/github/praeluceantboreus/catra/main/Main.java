package io.github.praeluceantboreus.catra.main;

import io.github.praeluceantboreus.catra.utils.Container;

public class Main
{

	public static void main(String[] args)
	{
		Container<String> c = new Container<>();
		c.add("richi");
		System.out.println(c.get("richi"));
		System.out.println(c.get("baum"));
		System.out.println(c.contains("richi"));
		System.out.println(c.contains("baum"));
	}

}
