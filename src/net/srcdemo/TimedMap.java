package net.srcdemo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TimedMap<K, V> extends HashMap<K, V> {
	private static final long checkingInterval = 30000;
	private static final long serialVersionUID = 2443021456659379262L;
	private final Map<K, Long> insertTime = new HashMap<K, Long>();
	private long lastDeadCheck = 0L;
	private final long timeout;

	public TimedMap(final long timeout) {
		this.timeout = timeout;
	}

	@Override
	public V put(final K key, final V value) {
		removeDead();
		insertTime.put(key, System.currentTimeMillis());
		return super.put(key, value);
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		removeDead();
		final Long now = System.currentTimeMillis();
		for (final K k : m.keySet()) {
			insertTime.put(k, now);
		}
		super.putAll(m);
	}

	@Override
	public V remove(final Object key) {
		final V returnValue = super.remove(key);
		insertTime.remove(key);
		removeDead();
		return returnValue;
	}

	private void removeDead() {
		final long now = System.currentTimeMillis();
		if (now - lastDeadCheck < checkingInterval) {
			return;
		}
		lastDeadCheck = now;
		final Set<K> toRemove = new HashSet<K>();
		for (final K k : keySet()) {
			if (now - insertTime.get(k) > timeout) {
				toRemove.add(k);
			}
		}
		for (final K k : toRemove) {
			super.remove(k);
			insertTime.remove(k);
		}
	}
}
