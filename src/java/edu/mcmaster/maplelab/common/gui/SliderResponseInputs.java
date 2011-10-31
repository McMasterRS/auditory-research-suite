package edu.mcmaster.maplelab.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import edu.mcmaster.maplelab.common.datamodel.Answer;
import edu.mcmaster.maplelab.common.datamodel.ConfidenceLevel;
import edu.mcmaster.maplelab.common.datamodel.ContinuousResponseParameters;
import edu.mcmaster.maplelab.common.datamodel.Response;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class SliderResponseInputs extends ResponseInputs<Response[]> {
	/** Sliders. */
	private List<JSlider> _sliders;
	/** Action listeners. */
	private final List<ActionListener> _listeners = new ArrayList<ActionListener>();
	/** Action forwarder. */
	private final ActionForwarder _forwarder = new ActionForwarder();
	
	/**
     * Default constructor sets the question asked and labels for the two
     * possible choices as indicated.
     */
    public SliderResponseInputs(ContinuousResponseParameters<?>... interpreters) {
        this(true, interpreters);
    }
    
    /**
     * Default constructor sets the question asked and labels for the
     * possible choices as indicated.
     */
	public SliderResponseInputs(boolean enableKeyEvents, 
											ContinuousResponseParameters<?>... interpreters) {
        super(true, enableKeyEvents, interpreters);
        
        if (interpreters.length > 2 || interpreters[0].isDiscrete()) {
        	throw new IllegalArgumentException("Can only handle up to 2 continuous response parameters.");
        }
    }
	
	private void createSliders() {
		if (_sliders == null) {
			ContinuousResponseParameters<?>[] params = 
				(ContinuousResponseParameters<?>[]) getResponseParams();
			_sliders = new ArrayList<JSlider>(getResponseParams().length);
			
			for (int i = 0; i < params.length; i++) {
				ContinuousResponseParameters<?> crp = params[i];
				ConfidenceLevel[] levels = crp.getConfidenceLevels();
				JSlider s = new JSlider(levels[0].ordinal(), 
						levels[levels.length-1].ordinal(), 
						crp.getMiddleValue());
				int divisor = Math.max(levels.length / 10, 1); // we want ~10 divisions
				s.setMajorTickSpacing(levels.length / divisor);
				s.setPaintTicks(true);
				s.setPaintLabels(false);
				s.addChangeListener(_forwarder);
				_sliders.add(s);
			}
		}
	}

	@Override
	protected void setEnabledState(boolean enabled) {
		for (JSlider s : _sliders) {
			s.setEnabled(enabled);
		}
	}

	@Override
	protected JPanel createTop() {
		createSliders();
		
		JPanel outer = new JPanel(new MigLayout("insets 0, fill"));
		JPanel p = new JPanel(new MigLayout("fill", "[right][center][left]", "[][]"));
		outer.add(p);
		outer.setBorder(BorderFactory.createTitledBorder("Response"));
		
		// first slider
		ResponseParameters<?>[] paramArray = getResponseParams();
		ResponseParameters<?> params = paramArray[0];
		Answer[] answers = params.getAnswers();
		// expect 2 answers, but just use the first 2
		p.add(new JLabel(answers[0].toString()));
		p.add(_sliders.get(0), "grow");
		p.add(new JLabel(answers[1].toString()), "wrap");
		
		// second slider, if valid
		if (paramArray.length == 2) {
			params = paramArray[1];
			answers = params.getAnswers();
			// expect 2 answers, but just use the first 2
			p.add(new JLabel(answers[0].toString()));
			p.add(_sliders.get(1), "grow");
			p.add(new JLabel(answers[1].toString()));
		}
		
		return outer;
	}

	@Override
	protected JPanel createBottom() {
		// don't need bottom panel
		return new JPanel(new MigLayout("insets 0"));
	}

	@Override
	public void setSelection(KeyEvent e) {
		JSlider top = _sliders.get(0);
		JSlider bottom = _sliders.size() > 1 ? _sliders.get(1) : null;
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT: {
				top.setValue(top.getValue() - 1);
				break;
			}
			case KeyEvent.VK_RIGHT: {
				top.setValue(top.getValue() + 1);
				break;
			}
			case KeyEvent.VK_UP: {
				if (bottom != null) {
					bottom.setValue(bottom.getValue() + 1);
				}
				break;
			}
			case KeyEvent.VK_DOWN: {
				if (bottom != null) {
					bottom.setValue(bottom.getValue() - 1);
				}
				break;
			}
		}
	}

	@Override
	public void reset() {
		ContinuousResponseParameters<?>[] params = 
				(ContinuousResponseParameters<?>[]) getResponseParams();
		for (int i = 0; i < params.length; i++) {
			_sliders.get(i).setValue(params[i].getMiddleValue());
		}
	}

	@Override
	public boolean isResponseComplete() {
		return true;
	}

	@Override
	protected Response[] getCurrentResponse() {
		ResponseParameters<?>[] params = getResponseParams();
		Response[] retval = new Response[params.length];
		
		for (int i = 0; i < params.length; i++) {
			ContinuousResponseParameters<?> crp = (ContinuousResponseParameters<?>) params[i];
			int val = _sliders.get(i).getValue();
			ConfidenceLevel level = crp.getLevelForValue(val);
			Answer a = crp.getAnswerForValue(val);
			retval[i] = new Response(a, level);
		}
		
		return retval;
	}

	@Override
	public void addActionListener(ActionListener l) {
		synchronized (_listeners) {
			_listeners.add(l);
		}
	}

	@Override
	public void removeActionListener(ActionListener l) {
		synchronized (_listeners) {
			_listeners.remove(l);
		}
	}
	
	/**
	 * Class for converting between change and action events.
	 */
	private class ActionForwarder implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent ce) {
			synchronized (_listeners) {
				ActionEvent ae = new ActionEvent(ce.getSource(), ActionEvent.ACTION_PERFORMED, null);
				for (ActionListener al : _listeners) {
					al.actionPerformed(ae);
				}
			}
		}
	}

}
