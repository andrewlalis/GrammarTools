package nl.andrewlalis.grammar_tool.machine;

import lombok.Getter;
import nl.andrewlalis.grammar_tool.grammar.Symbol;

import java.util.Objects;

@Getter
public class Transition implements Comparable<Transition> {
	private final State startState;
	private final Symbol acceptingSymbol;
	private final State endState;

	public Transition(State startState, Symbol acceptingSymbol, State endState) {
		this.startState = startState;
		this.acceptingSymbol = acceptingSymbol;
		this.endState = endState;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Transition that = (Transition) o;
		return startState.equals(that.startState) &&
				acceptingSymbol.equals(that.acceptingSymbol) &&
				endState.equals(that.endState);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startState, acceptingSymbol, endState);
	}

	@Override
	public String toString() {
		return String.format("%s (%s) -> %s", this.startState, this.acceptingSymbol, this.endState);
	}

	public boolean isEpsilon() {
		return this.acceptingSymbol.isEmpty();
	}

	@Override
	public int compareTo(Transition o) {
		int result = this.startState.compareTo(o.getStartState());
		if (result != 0) return result;
		int symbolResult = this.acceptingSymbol.compareTo(o.getAcceptingSymbol());
		if (symbolResult != 0) return symbolResult;
		return this.endState.compareTo(o.getEndState());
	}
}
