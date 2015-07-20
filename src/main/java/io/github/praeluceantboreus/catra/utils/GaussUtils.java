package io.github.praeluceantboreus.catra.utils;

import java.util.Random;

public class GaussUtils
{
	public static int betterRand(int from, int to)
	{
		Random rand = new Random();
		int diff = to - from;
		if (diff == 0)
			return 0;
		boolean negative = diff < 0;
		diff *= (negative) ? -1 : 1;
		int randNumber = rand.nextInt(diff + 1);
		randNumber *= (negative) ? -1 : 1;
		return randNumber + from;
	}
}
