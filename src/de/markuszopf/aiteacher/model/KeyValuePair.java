package de.markuszopf.aiteacher.model;
public class KeyValuePair<T> {

	public T key;
	public double value;

	public KeyValuePair(T key, double value) {
		this.key = key;
		this.value = value;
	}
}
