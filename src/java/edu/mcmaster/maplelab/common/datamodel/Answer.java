package edu.mcmaster.maplelab.common.datamodel;

import java.util.ArrayList;

/**
 * This class mimics an enum class for answers to the question of interest
 * to a given experiment.  It is loaded dynamically from the session.
 * The class must be initialized (initialize(Session session)) to populate
 * its values.
 * 
 * @author <a href="mailto:ben.guseman@mseedsoft.com">Ben Guseman</a>
 */
public class Answer {
	public static final Answer NEUTRAL = new Answer(-1, "neutral");
	
	/**
	 * Get all valid Answer values.
	 */
	public static Answer[] values(String... answers) {
		if (answers == null) return null;
		
		ArrayList<Answer> answerList = new ArrayList<Answer>(answers.length);
		for (int i = 0; i < answers.length; i++) {
			answerList.add(new Answer(i, answers[i]));
		}
		
		Answer[] retval = new Answer[answerList.size()];
		return answerList.toArray(retval);
	}
	
	private final int _ordinal;
	private final String _description;
	
	private Answer(int ordinal, String description) {
		_ordinal = ordinal;
		_description = description;
	}
	
	public int ordinal() {
		return _ordinal;
	}
	
	@Override
	public String toString() {
		return _description;
	}
}
