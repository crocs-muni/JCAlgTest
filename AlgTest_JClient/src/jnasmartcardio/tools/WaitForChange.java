package jnasmartcardio.tools;

import jnasmartcardio.Smartcardio;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminals.State;
import javax.smartcardio.TerminalFactory;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple utility to demonstrate {@link CardTerminals#waitForChange()} and
 * {@link CardTerminals#list(State)}.
 */
public class WaitForChange {
	private static List<String> terminalNames(List<CardTerminal> terminals) {
		ArrayList<String> r = new ArrayList<String>(terminals.size());
		for (int i = 0; i < terminals.size(); i++)
			r.add(terminals.get(i).getName());
		return r;
	}
	public static void main(String[] args) throws Exception {
		TerminalFactory context;
		CardTerminals terminals;
		if (true) {
			Security.addProvider(new Smartcardio());
			context = TerminalFactory.getInstance("PC/SC", null, Smartcardio.PROVIDER_NAME);
			terminals = context.terminals();
		} else {
			TerminalFactory terminalFactory = TerminalFactory.getDefault();
			terminals = terminalFactory.terminals();
		}
		List<CardTerminal> present = terminals.list(State.CARD_PRESENT);
		List<CardTerminal> absent = terminals.list(State.CARD_ABSENT);
		System.out.format("Initial: cards are present in %s; cards are absent from %s%n", terminalNames(present), terminalNames(absent));
		while (true) {
			terminals.waitForChange();
			List<CardTerminal> inserted = terminals.list(State.CARD_INSERTION);
			List<CardTerminal> removed = terminals.list(State.CARD_REMOVAL);
			System.out.format("Card inserted in %s; cards removed from %s%n", terminalNames(inserted), terminalNames(removed));
		}
	}
}

