package nl.andrewlalis.grammar_tool.machine;

import nl.andrewlalis.grammar_tool.grammar.Symbol;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FiniteStateMachineTest {
	private static final String fsm1 = """
			-> q0 : "a" -> q1
			 * q1
			""";
	private static final String fsm2 = """
			-> q0 : "a" -> q1, "b" -> q2
			   q1 : "a" -> q0, "" -> q2
			 * q2 : "c" -> q2
			""";
	private static final String fsm3 = """
			-> q0 : "a" -> q1, "b" -> q2
			   q1 : "a" -> q0, "" -> q2, "c" -> q3
			 * q2 : "c" -> q2
			   q3 : "b" -> q2, "" -> q0
			""";

	@Test
	public void testFromString() {
		FiniteStateMachine f1 = FiniteStateMachine.fromString(fsm1);
		FiniteStateMachine f1Eq = FiniteStateMachine.fromTransitions(
				State.of("q0"),
				Set.of(new Transition(State.of("q0"), Symbol.of("a"), State.of("q1"))),
				Set.of(State.of("q1"))
		);
		assertEquals(f1, f1Eq);
		FiniteStateMachine f2 = FiniteStateMachine.fromString(fsm2);
		FiniteStateMachine f2Eq = FiniteStateMachine.fromTransitions(
				State.of("q0"),
				Set.of(
						new Transition(State.of("q0"), Symbol.of("a"), State.of("q1")),
						new Transition(State.of("q0"), Symbol.of("b"), State.of("q2")),
						new Transition(State.of("q1"), Symbol.of("a"), State.of("q0")),
						new Transition(State.of("q1"), Symbol.EMPTY, State.of("q2")),
						new Transition(State.of("q2"), Symbol.of("c"), State.of("q2"))
				),
				Set.of(State.of("q2"))
		);
		assertEquals(f2, f2Eq);
	}

	@Test
	public void testGetTransitionsStartingAt() {
		FiniteStateMachine f1 = FiniteStateMachine.fromString(fsm1);
		Set<Transition> t0 = f1.getTransitionsStartingAt(State.of("q0"));
		assertEquals(t0, Set.of(new Transition(State.of("q0"), Symbol.of("a"), State.of("q1"))));
		Set<Transition> t1 = f1.getTransitionsStartingAt(State.of("q1"));
		assertEquals(t1, Set.of());
		FiniteStateMachine f2 = FiniteStateMachine.fromString(fsm2);
		Set<Transition> t2 = f2.getTransitionsStartingAt(State.of("q0"));
		assertEquals(t2, Set.of(
				new Transition(State.of("q0"), Symbol.of("a"), State.of("q1")),
				new Transition(State.of("q0"), Symbol.of("b"), State.of("q2"))
		));
		assertEquals(2, f2.getTransitionsStartingAt(State.of("q1")).size());
	}

	@Test
	public void testIsDeterministic() {
		assertTrue(FiniteStateMachine.fromString(fsm1).isDeterministic());
		assertFalse(FiniteStateMachine.fromString(fsm2).isDeterministic());
		assertFalse(FiniteStateMachine.fromString(fsm3).isDeterministic());
		assertTrue(FiniteStateMachine.fromString(fsm2).toDeterministic().isDeterministic());
		assertTrue(FiniteStateMachine.fromString(fsm3).toDeterministic().isDeterministic());
	}

	@Test
	public void testToDeterministic() {
		String nfsm1 = """
				-> q0 : "" -> q1, "b" -> q2
				   q1 : "a" -> q2, "c" -> q1
				   q2 : "d" -> q2, "d" -> q3
				 * q3 : "c" -> q0
				""";
		String dfsm1 = """
				-> q0 : "a" -> q2, "b" -> q2, "c" -> q1
				q1 : "a" -> q2, "c" -> q1
				q2 : "d" -> q3
				* q3 : "c" -> q0, "d" -> q3
				""";
		FiniteStateMachine f1 = FiniteStateMachine.fromString(nfsm1);
		assertFalse(f1.isDeterministic());
		FiniteStateMachine d1 = FiniteStateMachine.fromString(dfsm1);
		assertTrue(d1.isDeterministic());
		FiniteStateMachine f1ToD1 = f1.toDeterministic();
		assertEquals(d1.getStateCount(), f1ToD1.getStateCount());
		assertTrue(f1ToD1.isDeterministic());
	}
}
