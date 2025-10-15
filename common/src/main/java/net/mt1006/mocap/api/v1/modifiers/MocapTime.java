package net.mt1006.mocap.api.v1.modifiers;

public class MocapTime
{
	public static final MocapTime ZERO = new MocapTime(0.0, 0);
	public final double seconds;
	public final int ticks;

	private MocapTime(double seconds, int ticks)
	{
		this.seconds = seconds;
		this.ticks = ticks;
	}

	public static MocapTime fromSeconds(double seconds)
	{
		return seconds != 0.0 ? new MocapTime(seconds, (int)Math.round(seconds * 20.0)) : ZERO;
	}

	public static MocapTime fromTicks(int ticks)
	{
		return ticks != 0 ? new MocapTime((double)ticks / 20.0, ticks) : ZERO;
	}

	public MocapTime add(MocapTime delay)
	{
		return fromSeconds(seconds + delay.seconds);
	}

	@Override public boolean equals(Object obj)
	{
		return obj instanceof MocapTime && (ticks == ((MocapTime)obj).ticks);
	}

	@Override public int hashCode()
	{
		return ticks;
	}
}
