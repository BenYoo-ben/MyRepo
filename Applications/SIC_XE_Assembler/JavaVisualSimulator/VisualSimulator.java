package SP20_simulator;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다. 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트
 * 하는 역할을 수행한다.
 * 
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	EventHandler eventHandler = new EventHandler(this);

	JFrame MainFrame = new JFrame("SIC/XE Simulator");

	int JPanelNum = 50;
	int LabelNum = 50;
	int TextFieldNum = 50;
	int ButtonNum = 50;

	JPanel[] JPanels = new JPanel[JPanelNum];
	Label[] Labels = new Label[LabelNum];
	Button[] Buttons = new Button[ButtonNum];
	TextField[] TextFields = new TextField[TextFieldNum];

	JPanelsIndex JPIndex;
	ButtonsIndex BIndex;
	TextFieldsIndex TFIndex;
	
	interface JPanelsIndex {
		final int TOP = 0;
		final int MID = 1;
		final int BOTTOM = 2;
		final int TOP_TOP = 3;
		final int MID_LEFT = 4;
		final int MID_RIGHT = 5;
		final int MID_RIGHT_LEFT = 6;
		final int MID_RIGHT_RIGHT = 7;

	}


	interface ButtonsIndex {
		final int FILEOPEN = 0;
		
		final int RUN_ONE_STEP = 1;
		final int RUN_ALL_STEP = 2;
		final int EXIT = 3;

	}

	interface TextFieldsIndex {
		final int FILENAME = 0;
		final int PROGRAM_NAME = 1;
		final int START_ADDRESS_OF_OBJECT_PROGRAM = 2;
		final int LENGTH_Of_PROGRAM = 3;

		final int ADDRESS_OF_FIRST_INSTRUCTION_IN_OBJECT_PROGRAM = 4;
		final int START_ADDRESS_IN_MEMORY = 5;
		final int TARGET_ADDRESS = 6;
	
		final int REGISTER_A_DEC = 7;
		final int REGISTER_A_HEX = 8;
		final int REGISTER_X_DEC = 9;
		final int REGISTER_X_HEX = 10;
		final int REGISTER_L_DEC = 11;
		final int REGISTER_L_HEX = 12;
		final int REGISTER_PC_DEC = 13;
		final int REGISTER_PC_HEX = 14;
		final int REGISTER_SW_DEC = 15;
		final int REGISTER_SW_HEX = 16;
		
		final int REGISTER_B_DEC = 17;
		final int REGISTER_B_HEX = 18;
		final int REGISTER_S_DEC = 19;
		final int REGISTER_S_HEX = 20;
		final int REGISTER_T_DEC = 21;
		final int REGISTER_T_HEX = 22;
		final int REGISTER_F_DEC = 23;
		final int REGISTER_F_HEX = 24;
		
		final int INSTRUCTIONS = 25;
		final int DEVICE_IN_USE = 26;
		final int LOG = 27;
		
	}

	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program) {
		// ...
		
		
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep() {

	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep() {

	};

	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update() {

	};

	/*
	 * Prepare Screen
	 */

	public void setGUI(int x, int y) {
		MainFrame.setVisible(true);
		MainFrame.setLocation(1400, 100);
		MainFrame.setLayout(new GridLayout(3, 1));
		MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MainFrame.setSize(x, y);

		for (int i = 0; i < 4; i++) {
			JPanels[i] = new JPanel();
			MainFrame.add(JPanels[i]);
		}

		JPanels[JPanelsIndex.TOP].setLayout(new GridLayout(8, 2));

		JPanels[JPanelsIndex.TOP_TOP].setLayout(new GridLayout(1, 2));

		Buttons[ButtonsIndex.FILEOPEN] = new Button("open");
		Buttons[ButtonsIndex.FILEOPEN].addActionListener(this.eventHandler);
		TextFields[TextFieldsIndex.FILENAME] = new TextField();
		TextFields[TextFieldsIndex.FILENAME].setEditable(false);
		TextFields[TextFieldsIndex.FILENAME].setBackground(Color.WHITE);

		JPanels[JPanelsIndex.TOP_TOP].add(new Label("FileName :"));
		JPanels[JPanelsIndex.TOP_TOP].add(TextFields[TextFieldsIndex.FILENAME]);

		JPanels[JPanelsIndex.TOP].add(JPanels[JPanelsIndex.TOP_TOP]);
		JPanels[JPanelsIndex.TOP].add(Buttons[ButtonsIndex.FILEOPEN]);

		JPanels[JPanelsIndex.TOP].add(new Label("H (Header Record)"));
		JPanels[JPanelsIndex.TOP].add(new Label("E (End Record)"));


		JPanels[JPanelsIndex.TOP].add(new Label("Program Name"));
		JPanels[JPanelsIndex.TOP].add(new Label("Address of First Instruction"));

		TextFields[TextFieldsIndex.PROGRAM_NAME] = new TextField();
		TextFields[TextFieldsIndex.PROGRAM_NAME].setEditable(false);
		TextFields[TextFieldsIndex.PROGRAM_NAME].setBackground(Color.WHITE);

		TextFields[TextFieldsIndex.ADDRESS_OF_FIRST_INSTRUCTION_IN_OBJECT_PROGRAM] = new TextField();
		TextFields[TextFieldsIndex.ADDRESS_OF_FIRST_INSTRUCTION_IN_OBJECT_PROGRAM].setEditable(false);
		TextFields[TextFieldsIndex.ADDRESS_OF_FIRST_INSTRUCTION_IN_OBJECT_PROGRAM].setBackground(Color.white);

		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.PROGRAM_NAME]);
		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.ADDRESS_OF_FIRST_INSTRUCTION_IN_OBJECT_PROGRAM]);
	
		JPanels[JPanelsIndex.TOP].add(new Label("Start Address of Object Program"));
		JPanels[JPanelsIndex.TOP].add(new Label("Start Address in Memory"));
		
		TextFields[TextFieldsIndex.START_ADDRESS_OF_OBJECT_PROGRAM] = new TextField();
		TextFields[TextFieldsIndex.START_ADDRESS_OF_OBJECT_PROGRAM].setEditable(false);
		TextFields[TextFieldsIndex.START_ADDRESS_OF_OBJECT_PROGRAM].setBackground(Color.WHITE);

		TextFields[TextFieldsIndex.START_ADDRESS_IN_MEMORY] = new TextField();
		TextFields[TextFieldsIndex.START_ADDRESS_IN_MEMORY].setEditable(false);
		TextFields[TextFieldsIndex.START_ADDRESS_IN_MEMORY].setBackground(Color.white);
		
		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.START_ADDRESS_OF_OBJECT_PROGRAM]);
		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.START_ADDRESS_IN_MEMORY]);
		
		JPanels[JPanelsIndex.TOP].add(new Label("Length of Program"));
		JPanels[JPanelsIndex.TOP].add(new Label("Target Address"));
		
		TextFields[TextFieldsIndex.LENGTH_Of_PROGRAM] = new TextField();
		TextFields[TextFieldsIndex.LENGTH_Of_PROGRAM].setEditable(false);
		TextFields[TextFieldsIndex.LENGTH_Of_PROGRAM].setBackground(Color.WHITE);

		TextFields[TextFieldsIndex.TARGET_ADDRESS] = new TextField();
		TextFields[TextFieldsIndex.TARGET_ADDRESS].setEditable(false);
		TextFields[TextFieldsIndex.TARGET_ADDRESS].setBackground(Color.white);
		
		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.LENGTH_Of_PROGRAM]);
		JPanels[JPanelsIndex.TOP].add(TextFields[TextFieldsIndex.TARGET_ADDRESS]);
		
		JPanels[JPanelsIndex.MID].setLayout(new GridLayout(1,2));
		
		JPanels[JPanelsIndex.MID_LEFT] = new JPanel();
		JPanels[JPanelsIndex.MID_LEFT].setLayout(new GridLayout(14,3));
		
		JPanels[JPanelsIndex.MID_RIGHT] = new JPanel();
		JPanels[JPanelsIndex.MID_RIGHT].setLayout(new GridLayout(1,2));
		
		JPanels[JPanelsIndex.MID_RIGHT_LEFT] = new JPanel();
		JPanels[JPanelsIndex.MID_RIGHT_LEFT].setLayout(new BorderLayout());
		
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT] = new JPanel();
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].setLayout(new GridLayout(6,1));
		
		JPanels[JPanelsIndex.MID].add(JPanels[JPanelsIndex.MID_LEFT]);
		JPanels[JPanelsIndex.MID].add(JPanels[JPanelsIndex.MID_RIGHT]);
		
		JPanels[JPanelsIndex.MID_RIGHT].add(JPanels[JPanelsIndex.MID_RIGHT_LEFT]);
		JPanels[JPanelsIndex.MID_RIGHT].add(JPanels[JPanelsIndex.MID_RIGHT_RIGHT]);
		
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Register"));
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Dec"));
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Hex"));
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("A (#0)"));
		TextFields[TextFieldsIndex.REGISTER_A_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_A_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_A_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_A_DEC]);
		TextFields[TextFieldsIndex.REGISTER_A_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_A_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_A_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_A_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("X (#1)"));
		TextFields[TextFieldsIndex.REGISTER_X_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_X_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_X_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_X_DEC]);
		TextFields[TextFieldsIndex.REGISTER_X_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_X_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_X_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_X_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("L (#2)"));
		TextFields[TextFieldsIndex.REGISTER_L_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_L_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_L_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_L_DEC]);
		TextFields[TextFieldsIndex.REGISTER_L_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_L_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_L_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_L_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("PC (#8)"));
		TextFields[TextFieldsIndex.REGISTER_PC_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_PC_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_PC_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_PC_DEC]);
		TextFields[TextFieldsIndex.REGISTER_PC_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_PC_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_PC_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_PC_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("SW (#9)"));
		TextFields[TextFieldsIndex.REGISTER_SW_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_SW_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_SW_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_SW_DEC]);
		TextFields[TextFieldsIndex.REGISTER_SW_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_SW_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_SW_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_SW_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Register(for XE)"));
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		
		JPanels[JPanelsIndex.MID_LEFT].add(new JPanel());
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Dec"));
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("Hex"));
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("B (#3)"));
		TextFields[TextFieldsIndex.REGISTER_B_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_B_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_B_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_B_DEC]);
		TextFields[TextFieldsIndex.REGISTER_B_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_B_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_B_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_B_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("S (#4)"));
		TextFields[TextFieldsIndex.REGISTER_S_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_S_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_S_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_S_DEC]);
		TextFields[TextFieldsIndex.REGISTER_S_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_S_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_S_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_S_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("T (#5)"));
		TextFields[TextFieldsIndex.REGISTER_T_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_T_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_T_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_T_DEC]);
		TextFields[TextFieldsIndex.REGISTER_T_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_T_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_T_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_T_HEX]);
		
		JPanels[JPanelsIndex.MID_LEFT].add(new Label("F (#6)"));
		TextFields[TextFieldsIndex.REGISTER_F_DEC] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_F_DEC].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_F_DEC].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_F_DEC]);
		TextFields[TextFieldsIndex.REGISTER_F_HEX] = new TextField();
		TextFields[TextFieldsIndex.REGISTER_F_HEX].setEditable(false);
		TextFields[TextFieldsIndex.REGISTER_F_HEX].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_LEFT].add(TextFields[TextFieldsIndex.REGISTER_F_HEX]);
		
		JPanels[JPanelsIndex.MID_RIGHT_LEFT].add(new Label("Instructions"),"North");
		TextFields[TextFieldsIndex.INSTRUCTIONS] = new TextField();
		TextFields[TextFieldsIndex.INSTRUCTIONS].setEditable(false);
		TextFields[TextFieldsIndex.INSTRUCTIONS].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_RIGHT_LEFT].add(TextFields[TextFieldsIndex.INSTRUCTIONS],"Center");
		
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(new Label("Device in Use"));
		TextFields[TextFieldsIndex.DEVICE_IN_USE] = new TextField();
		TextFields[TextFieldsIndex.DEVICE_IN_USE].setEditable(false);
		TextFields[TextFieldsIndex.DEVICE_IN_USE].setBackground(Color.white);
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(TextFields[TextFieldsIndex.DEVICE_IN_USE]);
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(new JPanel());
		
		Buttons[ButtonsIndex.RUN_ONE_STEP] = new Button("Run (1 step)");
		Buttons[ButtonsIndex.RUN_ONE_STEP].addActionListener(this.eventHandler);
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(Buttons[ButtonsIndex.RUN_ONE_STEP]);
		Buttons[ButtonsIndex.RUN_ALL_STEP] = new Button("Run (ALL step)");
		Buttons[ButtonsIndex.RUN_ALL_STEP].addActionListener(this.eventHandler);
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(Buttons[ButtonsIndex.RUN_ALL_STEP]);
		Buttons[ButtonsIndex.EXIT] = new Button("Exit");
		Buttons[ButtonsIndex.EXIT].addActionListener(this.eventHandler);
		JPanels[JPanelsIndex.MID_RIGHT_RIGHT].add(Buttons[ButtonsIndex.EXIT]);
		
		JPanels[JPanelsIndex.BOTTOM].setLayout(new BorderLayout());
		JPanels[JPanelsIndex.BOTTOM].add(new Label("Log(Instruction)"),"North");
		TextFields[TextFieldsIndex.LOG] = new TextField();
		TextFields[TextFieldsIndex.LOG].setEditable(false);
		TextFields[TextFieldsIndex.LOG].setBackground(Color.white);
		JPanels[JPanelsIndex.BOTTOM].add(TextFields[TextFieldsIndex.LOG],"Center");

	}

	public void ExitEvent()
	{
		this.MainFrame.setVisible(false);
		System.exit(0);
	}
	
	public File FileOpenEvent() {
		JFileChooser fileChooser = new JFileChooser();
		
		int choice = fileChooser.showOpenDialog(MainFrame);
		if(choice == JFileChooser.APPROVE_OPTION)
		{
			this.TextFields[TextFieldsIndex.FILENAME].setText(fileChooser.getSelectedFile().getName());
			return fileChooser.getSelectedFile();
		}
		else
			return null;
		
	}
	public static void main(String[] args) {

		VisualSimulator vs = new VisualSimulator();
		vs.setGUI(800,950);
		vs.MainFrame.revalidate();
		vs.MainFrame.repaint();
	}

}

class EventHandler implements ActionListener {
	VisualSimulator vs;

	EventHandler(VisualSimulator input_vs) {
		this.vs = input_vs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource().equals(vs.Buttons[vs.BIndex.RUN_ONE_STEP])){
			
			System.out.println("One step.");
		}else if(e.getSource().equals(vs.Buttons[vs.BIndex.FILEOPEN])) {
			System.out.println("File Open");
			vs.load(vs.FileOpenEvent());
			
		}else if(e.getSource().equals(vs.Buttons[vs.BIndex.RUN_ALL_STEP])) {
			System.out.println("Run All");
			
		}else if(e.getSource().equals(vs.Buttons[vs.BIndex.EXIT])) {
			System.out.println("Exit");
			vs.ExitEvent();
			
		}
		

	}

}
