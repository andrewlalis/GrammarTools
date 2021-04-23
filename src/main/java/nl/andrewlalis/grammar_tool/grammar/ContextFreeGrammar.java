package nl.andrewlalis.grammar_tool.grammar;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ContextFreeGrammar {
	private final Set<Symbol> nonTerminalSymbols;
	private final Set<Symbol> terminalSymbols;
	private final Set<ProductionRule> productionRules;
	private final Symbol startSymbol;

	public ContextFreeGrammar(Set<Symbol> nonTerminalSymbols, Set<Symbol> terminalSymbols, Set<ProductionRule> productionRules, Symbol startSymbol) {
		this.nonTerminalSymbols = Objects.requireNonNull(nonTerminalSymbols);
		this.terminalSymbols = Objects.requireNonNull(terminalSymbols);
		this.productionRules = Objects.requireNonNull(productionRules);
		this.startSymbol = Objects.requireNonNull(startSymbol);
		this.ensureValidElements();
	}

	private void ensureValidElements() {
		if (!nonTerminalSymbols.contains(startSymbol)) {
			throw new IllegalArgumentException("Start symbol must be an element of the set of non-terminal symbols.");
		}
		Set<Symbol> overlaps = new HashSet<>(this.terminalSymbols);
		overlaps.retainAll(this.nonTerminalSymbols);
		if (!overlaps.isEmpty()) {
			throw new IllegalArgumentException("Terminal and non-terminal symbols are overlapping: " + overlaps);
		}
		for (ProductionRule rule : productionRules) {
			if (!nonTerminalSymbols.contains(rule.getBeginSymbol())) {
				throw new IllegalArgumentException("Production rule " + rule.toString() + " must begin with a symbol from the set of non-terminals.");
			}
			for (Symbol s : rule.getProducedSymbols()) {
				if (!nonTerminalSymbols.contains(s) && !terminalSymbols.contains(s)) {
					throw new IllegalArgumentException("Production rule " + rule.toString() + " must produce a string containing symbols that are elements of either terminals or non-terminals.");
				}
			}
		}
	}

	public boolean isSymbolTerminal(Symbol s) {
		return this.terminalSymbols.contains(s);
	}

	public boolean isSymbolNonTerminal(Symbol s) {
		return this.nonTerminalSymbols.contains(s);
	}

	public Set<ProductionRule> findRulesByStartingSymbol(Symbol s) {
		Set<ProductionRule> rules = new HashSet<>();
		for (var rule : this.productionRules) {
			if (rule.getBeginSymbol().equals(s)) rules.add(rule);
		}
		return rules;
	}

	/**
	 * Determines if a symbol is recursive in the grammar. A symbol is defined
	 * as recursive if it is a non-terminal that begins at least one production
	 * rule, and by following the non-terminals that rule produces, the symbol
	 * is again encountered as the result of another production rule.
	 * @param s The symbol to check.
	 * @return True if the symbol is recursive, or false otherwise.
	 */
	public boolean isSymbolRecursive(Symbol s) {
		Set<Symbol> symbolsToCheck = new HashSet<>();
		symbolsToCheck.add(s);
		Set<ProductionRule> rulesToCheck = new HashSet<>(this.productionRules);
		while (!rulesToCheck.isEmpty()) {
			Set<ProductionRule> rulesToAdd = new HashSet<>();
			Set<ProductionRule> rulesToRemove = new HashSet<>();
			for (var rule : rulesToCheck) {
				if (rule.getProducedSymbols().contains(s))  return true;
				if (symbolsToCheck.contains(rule.getBeginSymbol())) {
					for (var symbol : rule.getProducedSymbols()) {
						if (this.isSymbolNonTerminal(symbol)) {
							rulesToAdd.addAll(this.productionRules);
						}
					}
				}
				rulesToRemove.add(rule);
			}
			rulesToCheck.removeAll(rulesToRemove);
			rulesToCheck.addAll(rulesToAdd);
		}
		return false;
	}

	public ContextFreeGrammar toProductiveForm() {
		Symbol newStart = this.startSymbol;
		Set<Symbol> nonTerminals = new HashSet<>(this.nonTerminalSymbols);
		Set<Symbol> terminals = new HashSet<>(this.terminalSymbols);
		Set<ProductionRule> rules = new HashSet<>(this.productionRules);
		if (this.isSymbolRecursive(this.startSymbol)) {
			newStart = Symbol.of("_T");
			nonTerminals.add(newStart);
			rules.add(ProductionRule.of(newStart, this.startSymbol));
		}

		// Find all nullables.


		return new ContextFreeGrammar(nonTerminals, terminals, rules, newStart);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ContextFreeGrammar that = (ContextFreeGrammar) o;
		return getNonTerminalSymbols().equals(that.getNonTerminalSymbols())
				&& getTerminalSymbols().equals(that.getTerminalSymbols())
				&& getProductionRules().equals(that.getProductionRules())
				&& getStartSymbol().equals(that.getStartSymbol());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getNonTerminalSymbols(), getTerminalSymbols(), getProductionRules(), getStartSymbol());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Map<Symbol, SortedSet<ProductionRule>> rulesMap = new HashMap<>();
		SortedSet<Symbol> beginSymbols = new TreeSet<>();
		for (ProductionRule rule : this.productionRules) {
			beginSymbols.add(rule.getBeginSymbol());
			if (!rulesMap.containsKey(rule.getBeginSymbol())) {
				rulesMap.put(rule.getBeginSymbol(), new TreeSet<>());
			}
			rulesMap.get(rule.getBeginSymbol()).add(rule);
		}
		// Do start symbol explicitly at the beginning.
		beginSymbols.remove(this.startSymbol);
		sb.append(this.startSymbol.getIdentifier()).append(" -> ");
		sb.append(rulesMap.get(this.getStartSymbol()).stream().map(ProductionRule::getProducedSymbolsString).collect(Collectors.joining(" | ")));
		sb.append("\n");

		for (Symbol s : beginSymbols) {
			sb.append(s.getIdentifier()).append(" -> ");
			sb.append(rulesMap.get(s).stream().map(ProductionRule::getProducedSymbolsString).collect(Collectors.joining(" | ")));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static ContextFreeGrammar fromProductionRules(String start, String nonTerminals, String terminals, String... ruleExpressions) {
		Set<ProductionRule> rules = new HashSet<>();
		for (String ruleExpr : ruleExpressions) {
			rules.addAll(ProductionRule.of(ruleExpr));
		}
		return new ContextFreeGrammar(
				Symbol.setOf(nonTerminals.split(",")),
				Symbol.setOf(terminals.split(",")),
				rules,
				Symbol.of(start)
		);
	}
}
