import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI extends JFrame{
	
	JPanel MainJPanel;
	
	JButton makeRoomButton;
	JButton joinRoomButton;
	Handle handle = new Handle();
	
	
	GUI()
	{
		handle.gui = this;
		setScreen();
	}
	
	void setScreen()
	{
		this.addWindowListener(handle);
		MainJPanel = new JPanel();
		this.setVisible(true);
		this.setSize(1024,800);
		this.setLocation(300,300);
		MainJPanel.setLayout(new GridLayout(3,4));;
		
		makeRoomButton = new JButton("Make Room");
		joinRoomButton = new JButton("Join Room");
		
		this.setLayout(new BorderLayout());
		this.add(MainJPanel,"Center");
		
		MainJPanel.add(makeRoomButton);
		MainJPanel.add(joinRoomButton);
		
		makeRoomButton.addActionListener(handle);
		joinRoomButton.addActionListener(handle);
		
	}


}
