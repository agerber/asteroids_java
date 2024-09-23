package edu.uchicago.gerber.mvc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {

	private final BorderLayout borderLayout = new BorderLayout();

	public GameFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Component initialization
	private void initialize() throws Exception {
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout);
	}

	@Override
	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}
}
