package com.gentics.mesh.musetech;

public class Tuple<T, B> {
	T a;
	B b;

	public Tuple(T a, B b) {
		this.a = a;
		this.b = b;
	}

	public T getA() {
		return a;
	}

	public B getB() {
		return b;
	}

	public static <T, B> Tuple<T, B> tuple(T a, B b) {
		return new Tuple<>(a, b);
	}
}