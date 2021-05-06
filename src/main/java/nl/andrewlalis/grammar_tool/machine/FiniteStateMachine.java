package nl.andrewlalis.grammar_tool.machine;

import nl.andrewlalis.grammar_tool.grammar.Symbol;

import java.util.Objects;
import java.util.Set;

public class FiniteStateMachine {
	private final Set<Symbol> alphabet;
	private final Set<State> states;
	private final Set<State> finalStates;
	private final State startState;
	private final Set<Transition> transitions;

	public FiniteStateMachine(Set<Symbol> alphabet, Set<State> states, Set<State> finalStates, State startState, Set<Transition> transitions) {
		this.alphabet = alphabet;
		this.states = states;
		this.finalStates = finalStates;
		this.startState = startState;
		this.transitions = transitions;
		this.ensureValidElements();
	}

	private void ensureValidElements() {
		if (!this.states.contains(this.startState)) {
			throw new IllegalArgumentException("Start state is not an element of the whole set of states.");
		}
		if (!this.states.containsAll(this.finalStates)) {
			throw new IllegalArgumentException("Not all final states belong to the whole set of states.");
		}
		if (this.finalStates.isEmpty()) {
			throw new IllegalArgumentException("No final states. There must be at least one final state.");
		}
		for (Transition t : this.transitions) {
			if (!this.states.contains(t.getStartState())) {
				throw new IllegalArgumentException("Start state of transition does not belong to the whole set of states: " + t);
			}
			if (!this.states.contains(t.getEndState())) {
				throw new IllegalArgumentException("End state of transition does not belong to the whole set of states: " + t);
			}
			if (!this.alphabet.contains(t.getAcceptingSymbol())) {
				throw new IllegalArgumentException("Accepting symbol of transition is not in the alphabet: " + t);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FiniteStateMachine that = (FiniteStateMachine) o;
		return alphabet.equals(that.alphabet) &&
				states.equals(that.states) &&
				finalStates.equals(that.finalStates) &&
				startState.equals(that.startState) &&
				transitions.equals(that.transitions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alphabet, states, finalStates, startState, transitions);
	}
}
