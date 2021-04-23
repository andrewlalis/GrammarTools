package nl.andrewlalis.grammar_tool.grammar;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ProductionRule implements Comparable<ProductionRule> {
	private final Symbol beginSymbol;
	private final List<Symbol> producedSymbols;

	public ProductionRule(Symbol beginSymbol, List<Symbol> producedSymbols) {
		this.beginSymbol = Objects.requireNonNull(beginSymbol);
		this.producedSymbols = Objects.requireNonNull(producedSymbols);
	}

	public boolean isEmpty() {
		return this.producedSymbols.isEmpty();
	}

	public String getProducedSymbolsString() {
		if (this.producedSymbols.isEmpty()) return "ε";
		return this.producedSymbols.stream().map(Symbol::getIdentifier).collect(Collectors.joining(","));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProductionRule that = (ProductionRule) o;
		return getBeginSymbol().equals(that.getBeginSymbol()) && getProducedSymbols().equals(that.getProducedSymbols());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBeginSymbol(), getProducedSymbols());
	}

	@Override
	public String toString() {
		return this.beginSymbol + " -> " + this.getProducedSymbolsString();
	}

	@Override
	public int compareTo(ProductionRule o) {
		int beginComparison = this.beginSymbol.compareTo(o.beginSymbol);
		if (beginComparison != 0) return beginComparison;
		return -1 * Integer.compare(this.producedSymbols.size(), o.producedSymbols.size());
	}

	public static ProductionRule of(Symbol beginSymbol, Symbol... producedSymbols) {
		return new ProductionRule(beginSymbol, Arrays.asList(producedSymbols));
	}

	@SafeVarargs
	public static Set<ProductionRule> setOf(Symbol beginSymbol, List<Symbol>... producedSymbols) {
		Set<ProductionRule> rules = new HashSet<>();
		for (var symbolsList : producedSymbols) {
			rules.add(new ProductionRule(beginSymbol, symbolsList));
		}
		return rules;
	}

	public static Set<ProductionRule> of(String expression) {
		Scanner scanner = new Scanner(expression);
		Symbol beginSymbol = Symbol.of(scanner.next("\\w+"));
		scanner.next("->");
		String[] productions = scanner.nextLine().split("\\|");
		Set<ProductionRule> rules = new HashSet<>();
		for (String productionExpr : productions) {
			String[] symbolNames = productionExpr.split(",");
			if (symbolNames.length == 1 && symbolNames[0].trim().equalsIgnoreCase("ε")) {
				rules.add(ProductionRule.of(beginSymbol));
				continue;
			}
			rules.add(ProductionRule.of(beginSymbol, Symbol.arrayOf(symbolNames)));
		}
		return rules;
	}
}
