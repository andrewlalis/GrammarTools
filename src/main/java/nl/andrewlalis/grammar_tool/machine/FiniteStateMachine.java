package nl.andrewlalis.grammar_tool.machine;

import nl.andrewlalis.grammar_tool.grammar.Symbol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FiniteStateMachine {
	private final Set<Symbol> alphabet;
	private final Set<State> states;
	private final Set<State> finalStates;
	private final State startState;
	private final Set<Transition> transitions;

	public FiniteStateMachine(Set<Symbol> alphabet, Set<State> states, Set<State> finalStates, State startState, Set<Transition> transitions) {
		this.alphabet = Objects.requireNonNull(alphabet);
		this.states = Objects.requireNonNull(states);
		this.finalStates = Objects.requireNonNull(finalStates);
		this.startState = Objects.requireNonNull(startState);
		this.transitions = Objects.requireNonNull(transitions);
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

	public int getStateCount() {
		return this.states.size();
	}

	public Set<Transition> getTransitionsStartingAt(State state) {
		Set<Transition> stateTransitions = new HashSet<>();
		for (Transition t : this.transitions) {
			if (t.getStartState().equals(state)) {
				stateTransitions.add(t);
			}
		}
		return stateTransitions;
	}

	public Set<State> getNextStates(State currentState, Symbol acceptingSymbol) {
		Set<State> nextStates = new HashSet<>();
		for (Transition t : this.transitions) {
			if (t.getStartState().equals(currentState) && t.getAcceptingSymbol().equals(acceptingSymbol)) {
				nextStates.add(t.getEndState());
			}
		}
		return nextStates;
	}

	public Set<State> getNextStates(Set<State> states, Symbol acceptingSymbol) {
		Set<State> nextStates = new HashSet<>();
		for (State state : states) {
			nextStates.addAll(this.getNextStates(state, acceptingSymbol));
		}
		return nextStates;
	}

	public Set<State> getEpsilonClosure(State currentState) {
		Set<State> nextStates = this.getNextStates(currentState, Symbol.EMPTY);
		nextStates.add(currentState);
		return nextStates;
	}

	public Set<State> getEpsilonClosure(Set<State> states) {
		Set<State> nextStates = this.getNextStates(states, Symbol.EMPTY);
		nextStates.addAll(states);
		return nextStates;
	}

	public boolean isDeterministic() {
		for (State state : this.states) {
			for (Symbol symbol : this.alphabet) {
				if (this.getEpsilonClosure(this.getNextStates(state, symbol)).size() > 1) {
					return false;
				}
			}
		}
		return true;
	}

	public FiniteStateMachine toDeterministic() {
		Set<State> finalStates = new HashSet<>();
		Set<Transition> transitions = new HashSet<>();
		Set<State> startClosure = this.getEpsilonClosure(this.startState);
		State startState = State.of(startClosure);
		Deque<Set<State>> stateQueue = new LinkedList<>();
		Set<Set<State>> visitedClosures = new HashSet<>();
		stateQueue.add(startClosure);
		while (!stateQueue.isEmpty()) {
			Set<State> currentClosure = stateQueue.pop();
			visitedClosures.add(currentClosure);
			State combinedState = State.of(currentClosure);
			for (State state : currentClosure) {
				if (this.finalStates.contains(state)) {
					finalStates.add(combinedState);
				}
			}
			for (Symbol symbol : this.alphabet) {
				if (symbol.isEmpty()) continue;
				Set<State> nextStates = this.getNextStates(currentClosure, symbol);
				Set<State> nextStateClosure = this.getEpsilonClosure(nextStates);
				if (nextStateClosure.isEmpty()) continue;
				State combinedNextState = State.of(nextStateClosure);
//				System.out.println(combinedState + " : " + "\"" + symbol + "\" -> " + combinedNextState);
				transitions.add(new Transition(combinedState, symbol, combinedNextState));
				if (!visitedClosures.contains(nextStateClosure)) {
					stateQueue.add(nextStateClosure);
				}
			}
		}
		return FiniteStateMachine.fromTransitions(startState, transitions, finalStates);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<State> sortedStates = this.states.stream().sorted().collect(Collectors.toList());
		for (State state : sortedStates) {
			if (this.startState.equals(state)) {
				sb.append("-> ");
			} else if (this.finalStates.contains(state)) {
				sb.append(" * ");
			} else {
				sb.append("   ");
			}
			sb.append(state);
			List<Transition> sortedTransitions = this.getTransitionsStartingAt(state).stream().sorted().collect(Collectors.toList());
			if (!sortedTransitions.isEmpty()) {
				sb.append(" : ");
				for (int i = 0; i < sortedTransitions.size(); i++) {
					Transition t = sortedTransitions.get(i);
					sb.append('"').append(t.getAcceptingSymbol()).append('"').append(" -> ").append(t.getEndState());
					if (i < sortedTransitions.size() - 1) sb.append(", ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static FiniteStateMachine fromTransitions(State startState, Set<Transition> transitions, Set<State> finalStates) {
		Set<Symbol> alphabet = new HashSet<>();
		Set<State> states = new HashSet<>();
		for (Transition t : transitions) {
			states.add(t.getStartState());
			states.add(t.getEndState());
			alphabet.add(t.getAcceptingSymbol());
		}
		return new FiniteStateMachine(alphabet, states,finalStates, startState, transitions);
	}

	/**
	 * Constructs an FSM from a series of strings depicting states and their
	 * possible transitions. The following format is used:
	 * <pre><code>
	 *     -> q0 : "a" -> q1, "" -> q2
	 *        q1 : "b" -> q2, "c" -> q3
	 *      * q2 : "c" -> q3
	 *      * q3
	 * </code></pre>
	 * @param fsmString The string containing the FSM specification.
	 * @return The finite state machine that was created.
	 */
	public static FiniteStateMachine fromString(String fsmString) {
		String[] stateStrings = fsmString.split("\\n+");
		State startState = null;
		Set<State> finalStates = new HashSet<>();
		Set<Transition> transitions = new HashSet<>();
		for (String stateString : stateStrings) {
			if (stateString.isBlank()) continue;
			String[] parts = stateString.split("\\s*:\\s*");
			String stateDefinition = parts[0].trim();
			String[] definitionParts = stateDefinition.split("\\s+");
			String modifier = null;
			String stateName;
			if (definitionParts.length > 1) {
				modifier = definitionParts[0].trim();
				stateName = definitionParts[1].trim();
			} else {
				stateName = definitionParts[0].trim();
			}
			State state = new State(stateName);
			if (modifier != null) {
				if (modifier.trim().equals("->")) {
					startState = state;
				} else if (modifier.trim().equals("*")) {
					finalStates.add(state);
				} else {
					throw new IllegalArgumentException("Invalid state modifier: " + modifier);
				}
			}
			if (parts.length == 1) continue;
			String[] transitionDefinitions = parts[1].split("\\s*,\\s*");
			for (String transitionDefinition : transitionDefinitions) {
				String[] transitionParts = transitionDefinition.split("\\s*->\\s*");
				if (transitionParts.length != 2) throw new IllegalArgumentException("Invalid transition format: " + transitionDefinition);
				Pattern p = Pattern.compile("\"([^\"]*)\"");
				Matcher m = p.matcher(transitionParts[0]);
				if (!m.find()) throw new IllegalArgumentException("Invalid transition format: " + transitionDefinition);
				Symbol acceptingSymbol = new Symbol(m.group(1));
				State endState = new State(transitionParts[1].trim());
				transitions.add(new Transition(state, acceptingSymbol, endState));
			}
		}
		return fromTransitions(startState, transitions, finalStates);
	}
}
