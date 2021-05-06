package nl.andrewlalis.grammar_tool.machine;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class State implements Comparable<State> {
	private final String name;

	public State(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		State state = (State) o;
		return name.equals(state.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(State o) {
		return this.name.compareTo(o.name);
	}

	public static State of(String name) {
		return new State(name);
	}

	public static State of(Set<State> states) {
		if (states.isEmpty()) throw new IllegalArgumentException("Cannot construct state of empty states.");
		if (states.size() == 1) return states.stream().findAny().get();
		String name = states.stream().sorted().map(State::toString).collect(Collectors.joining(", "));
		return new State("{" + name + "}");
	}
}
