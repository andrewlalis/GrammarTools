package nl.andrewlalis.grammar_tool;

import nl.andrewlalis.grammar_tool.grammar.ContextFreeGrammar;

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
		ContextFreeGrammar productive = g2.toProductiveForm();
		System.out.println(productive);
	}
}