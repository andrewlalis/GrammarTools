package nl.andrewlalis.grammar_tool;

import nl.andrewlalis.grammar_tool.grammar.ContextFreeGrammar;
import nl.andrewlalis.grammar_tool.machine.FiniteStateMachine;

public class GrammarTool {
	public static void main(String[] args) {
		ContextFreeGrammar g2 = ContextFreeGrammar.fromProductionRules(
				"S",
				"S, A, B, C",
				"a, b, c",
				"S -> A,B",
				"A -> a,S | ε",
				"B -> b,B | ε",
				"C -> c,C | ε"
		);
		System.out.println(g2);

		FiniteStateMachine f1 = FiniteStateMachine.fromString("""
				-> q0 : "" -> q1, "b" -> q2
				   q1 : "c" -> q1, "a" -> q2,
				   q2 : "d" -> q2, "d" -> q3,
				 * q3 : "c" -> q1
				""");
		System.out.println(f1);
		System.out.println("Deterministic? " + f1.isDeterministic());
	}
}