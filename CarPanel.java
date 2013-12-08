import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;


public class CarPanel extends JPanel {
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.GREEN);
		g.fillRect(0, 0, 500, 500);
		
		g.setColor(Color.BLACK);
		g.fillOval(250-10, 250-10, 20, 20);
	}

}
