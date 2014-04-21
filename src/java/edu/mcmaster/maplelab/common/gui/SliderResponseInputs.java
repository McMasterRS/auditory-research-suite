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

import edu.mcmaster.maplelab.common.datamodel.BinaryAnswer;
import edu.mcmaster.maplelab.common.datamodel.ContinuousResponseParameters;
import edu.mcmaster.maplelab.common.datamodel.MultiResponse;
import edu.mcmaster.maplelab.common.datamodel.ResponseParameters;

public class SliderResponseInputs extends ResponseInputs<MultiResponse> {
	/** Sliders. */
	private List<SliderGroup> _sliders;
	/** Action listeners. */
	private final List<ActionListener> _listeners = new ArrayList<ActionListener>();
	/** Action forwarder. */
	private final ActionForwarder _forwarder = new ActionForwarder();
	
	/**
     * Default constructor sets the question asked and labels for the two
     * possible choices as indicated.
     */
    public SliderResponseInputs(ContinuousResponseParameters<?, ?>... interpreters) {
        this(true, interpreters);
    }
    
    /**
     * Default constructor sets the question asked and labels for the
     * possible choices as indicated.
     */
	public SliderResponseInputs(boolean enableKeyEvents, 
			ContinuousResponseParameters<?, ?>... interpreters) {
        super(true, enableKeyEvents, interpreters);
        
        if (interpreters.length > 2 || (interpreters.length > 0 && interpreters[0].isDiscrete())) {
        	throw new IllegalArgumentException("Can only handle up to 2 continuous response parameters.");
        }
    }
	
	private void createSliders() {
		if (_sliders == null) {
			ContinuousResponseParameters<?, ?>[] params = 
				(ContinuousResponseParameters<?, ?>[]) getResponseParams();
			_sliders = new ArrayList<SliderGroup>(params.length);
			
			for (int i = 0; i < params.length; i++) {
				ContinuousResponseParameters<?, ?> crp = params[i];
				int max = crp.getMax();
				int min = crp.getMin();
				int size = max - min;
				JSlider s = new JSlider(min, max, crp.getMiddleValue());
				int divisor = Math.max(size / 10, 1); // we want ~10 divisions
				s.setMajorTickSpacing(size / divisor);
				s.setPaintTicks(false); // Defaulted off... can change via enableSliderTickMarks()
				s.setPaintLabels(false);
				s.addChangeListener(_forwarder);
				s.setFocusable(false); // Make sure we cannot tab cycle to slider controls... 
				// expect 2 answers, but just use the first 2
				BinaryAnswer[] answers = crp.getAnswers();
				_sliders.add(new SliderGroup(s, new JLabel(answers[0].toString()), 
						new JLabel(answers[1].toString())));
			}
		}
	}
	
	/**
	 * Set the sliders to show tick marks or not.
	 * @param enable
	 */
	public void enableSliderTickMarks(boolean enable) {
		if (_sliders != null) {
			for (SliderGroup sg : _sliders) {
				sg.getSlider().setPaintTicks(enable);
			}
		}
	}

	@Override
	protected void setEnabledState(boolean enabled) {
		for (SliderGroup s : _sliders) {
			s.getSlider().setEnabled(enabled);
		}
	}
	
	public void setInputVisibility(int inputIndex, boolean visible) {
		if (inputIndex < 0 || inputIndex >= _sliders.size()) return;
		_sliders.get(inputIndex).setVisible(visible);
	}

	@Override
	protected JPanel createTop() {
		createSliders();
		
		JPanel outer = new JPanel(new MigLayout("insets 0, fill, hidemode 0"));
		
		int sliderLength = getResponseParams()[0].getSession().getSliderLength();
		String colFormat = String.format("[right][center, %d!][left]", sliderLength);
		JPanel p = new JPanel(new MigLayout("insets 0, fill, hidemode 0"
				, colFormat, "[][]"));
		outer.add(p, "growx");
		outer.setBorder(BorderFactory.createTitledBorder("Response"));
		
		// first slider
		if (_sliders.size() == 0) return outer;
		SliderGroup sg = _sliders.get(0);
		// expect 2 answers, but just use the first 2
		p.add(sg.getLeft());
		p.add(sg.getSlider(), "growx");
		p.add(sg.getRight(), "wrap");
		
		// second slider, if valid
		if (_sliders.size() == 2) {
			sg = _sliders.get(1);
			p.add(sg.getLeft());
			p.add(sg.getSlider(), "growx");
			p.add(sg.getRight());
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
		if (_sliders.size() == 0) return;
		JSlider top = _sliders.get(0).getSlider();
		JSlider bottom = _sliders.size() > 1 ? _sliders.get(1).getSlider() : null;
		
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
		ContinuousResponseParameters<?, ?>[] params = 
				(ContinuousResponseParameters<?, ?>[]) getResponseParams();
		for (int i = 0; i < params.length; i++) {
			_sliders.get(i).getSlider().setValue(params[i].getMiddleValue());
		}
	}

	@Override
	public boolean isResponseComplete() {
		return true;
	}

	@Override
	protected MultiResponse getCurrentResponse() {
		ResponseParameters<?, ?>[] params = getResponseParams();
		MultiResponse retval = new MultiResponse(params.length);
		
		for (int i = 0; i < params.length; i++) {
			ContinuousResponseParameters<?, ?> crp = (ContinuousResponseParameters<?, ?>) params[i];
			int val = _sliders.get(i).getSlider().getValue();
			retval.setResponse(i, crp.getResponseForValue(val));
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
	
	private static class SliderGroup {
		private final JSlider _slider;
		private final JLabel[] _labels = new JLabel[2];;
		public SliderGroup(JSlider s, JLabel left, JLabel right) {
			_slider = s;
			_labels[0] = left;
			_labels[1] = right;
		}
		public JSlider getSlider() { return _slider; }
		public JLabel getLeft() { return _labels[0]; }
		public JLabel getRight() { return _labels[1]; }
		public void setVisible(boolean visible) {
			_slider.setVisible(visible);
			for (JLabel l : _labels) {
				l.setVisible(visible);
			}
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
