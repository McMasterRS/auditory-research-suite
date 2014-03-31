package edu.mcmaster.maplelab.common.datamodel;

/**
 * This class encapsulates binary answers to the question of interest
 * to a given experiment.  It is loaded dynamically from the session.
 * 
 * @author <a href="mailto:ben.guseman@mseedsoft.com">Ben Guseman</a>
 */
public class BinaryAnswer {
	public static final BinaryAnswer NEUTRAL = new BinaryAnswer(-1, "neutral", "");
	
	/**
	 * Property keys for Answer properties
	 * @author zachbrown
	 */
	private enum Keys {
		answerPositive,
		answerNegative
	}
	
	/**
	 * Get both valid Answer values from a Session.
	 */
	public static <S extends Session<?, ?, ?>> BinaryAnswer[] values(S session) {
		if (session == null) return null;
		
		String answerPositive = session.getString(Keys.answerPositive + ".label", "Yes");
		String answerNegative = session.getString(Keys.answerNegative + ".label", "No");
		String posHotkey = session.getString(Keys.answerPositive + ".hotkey", "y")
				.toLowerCase().substring(0, 1);
		String negHotkey = session.getString(Keys.answerNegative + ".hotkey", "n")
				.toLowerCase().substring(0, 1);
		
		BinaryAnswer[] retval = new BinaryAnswer[2];
		retval[0] = new BinaryAnswer(0, answerPositive, posHotkey);
		retval[1] = new BinaryAnswer(1, answerNegative, negHotkey);
		
		return retval;
	}
	
	/**
     * Build valid Binary Answer from two strings.
     */
    public static BinaryAnswer[] values(String answerPos, String answerNeg) {
            if (answerPos == null || answerNeg == null) return null;
            
            BinaryAnswer[] retval = new BinaryAnswer[2];
            retval[0] = new BinaryAnswer(0, answerPos, "");
            retval[1] = new BinaryAnswer(1, answerNeg, "");
            return retval;
    }
	
	private final int _ordinal;
	private final String _description;
	private final String _hotkey;
	
	private BinaryAnswer(int ordinal, String description, String hotkey) {
		_ordinal = ordinal;
		_description = description;
		_hotkey = hotkey;
	}
	
	public int ordinal() {
		return _ordinal;
	}
	
	public String getHotkey() {
		return _hotkey;
	}
	
	@Override
	public String toString() {
		return _description;
	}
}
