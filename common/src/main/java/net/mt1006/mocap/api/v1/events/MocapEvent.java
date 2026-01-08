package net.mt1006.mocap.api.v1.events;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class MocapEvent<T>
{
	private final List<T> listeners = new ArrayList<>();

	@ApiStatus.Internal
	public final T invoker;

	MocapEvent(Function<List<T>, T> invokerFactory)
	{
		this.invoker = invokerFactory.apply(listeners);
	}

	public void register(T listener)
	{
		listeners.add(listener);
	}
}
