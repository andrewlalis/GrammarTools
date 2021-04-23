package nl.andrewlalis.grammar_tool.grammar;

import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Symbol implements Comparable<Symbol> {
	@Getter
	private final String identifier;

	public Symbol(String identifier) {
		this.identifier = Objects.requireNonNull(identifier).trim();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Symbol symbol = (Symbol) o;
		return getIdentifier().equals(symbol.getIdentifier());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIdentifier());
	}

	@Override
	public String toString() {
		return this.identifier;
	}

	@Override
	public int compareTo(Symbol o) {
		return this.identifier.compareTo(o.identifier);
	}

	public static Symbol of(String identifier) {
		return new Symbol(identifier);
	}

	public static Set<Symbol> setOf(String... identifiers) {
		Set<Symbol> symbols = new HashSet<>();
		for (String i : identifiers) {
			symbols.add(new Symbol(i));
		}
		return symbols;
	}

	public static Symbol[] arrayOf(String... identifiers) {
		Symbol[] symbols = new Symbol[identifiers.length];
		int index = 0;
		for (String i : identifiers) {
			symbols[index++] = new Symbol(i);
		}
		return symbols;
	}
}
