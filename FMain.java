import javax.swing.JFrame;


public class FMain {
	
	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setBounds(100,100,500,500);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		CarPanel cp = new CarPanel();
		jf.add(cp);
		cp.repaint();
	}

}
