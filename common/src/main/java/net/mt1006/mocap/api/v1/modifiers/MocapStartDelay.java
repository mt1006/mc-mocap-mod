package net.mt1006.mocap.api.v1.modifiers;

public class MocapStartDelay
{
	public static final MocapStartDelay ZERO = new MocapStartDelay(0.0);
	public final double seconds;
	public final int ticks;

	private MocapStartDelay(double seconds)
	{
		this.seconds = seconds;
		this.ticks = (int)Math.round(seconds * 20.0);
	}

	public static MocapStartDelay fromSeconds(double seconds)
	{
		return seconds != 0.0 ? new MocapStartDelay(seconds) : ZERO;
	}

	public MocapStartDelay add(MocapStartDelay delay)
	{
		return fromSeconds(seconds + delay.seconds);
	}
}
