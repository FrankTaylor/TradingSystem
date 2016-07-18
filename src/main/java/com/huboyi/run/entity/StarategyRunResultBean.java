package com.huboyi.run.entity;

import java.util.Arrays;
import java.util.stream.Stream;

public class StarategyRunResultBean {
	
	public static void main(String[] args) {
		
//		Trader raoul = new Trader("Raoul", "Cambridge");
//		Trader mario = new Trader("Mario", "Milan");
//		Trader alan = new Trader("Alan", "Cambridge");
//		Trader brian = new Trader("Brian", "Cambridge");
//		
//		List<Transaction> t = Arrays.asList(
//			new Transaction(brian, 2011, 300),
//			new Transaction(raoul, 2012, 1000),
//			new Transaction(raoul, 2011, 400),
//			new Transaction(mario, 2012, 710),
//			new Transaction(mario, 2012, 700),
//			new Transaction(alan, 2012, 950)
//		);
//		
//		t.stream()
//		.filter(a -> a.getYear() == 2011)
//		.sorted(Comparator.comparing(Transaction::getValue))
//		.forEach(System.out::println);
//		
//		t.stream().map(a -> a.getTrader().getCity()).distinct().forEach(System.out::println);
//		
//		t.stream()
//		.filter(a -> a.getTrader().getCity().equals("Cambridge"))
//		.sorted(Comparator.comparing(a -> a.getTrader().getName()))
//		.forEach(System.out::println);
		
	}
	
	private static class Trader {
		private final String name;
		private final String city;
		
		public Trader(String n, String c) {
			this.name = n;
			this.city = c;
		}

		public String getName() {
			return name;
		}

		public String getCity() {
			return city;
		}
	}
	
	private static class Transaction {
		private final Trader trader;
		private final int year;
		private final int value;
		
		public Transaction(Trader trader, int year, int value) {
			this.trader = trader;
			this.year = year;
			this.value = value;
		}

		public Trader getTrader() {
			return trader;
		}

		public int getYear() {
			return year;
		}

		public int getValue() {
			return value;
		}
		
		public String toString() {
			return "{" + this.trader + ", " + "year: " + this.year + ", " + "value: " + this.value + "}";
		}
	}
	
}
