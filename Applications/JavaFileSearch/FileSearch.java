import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FileSearch{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	public static void main(String[] args)
	{
		EventHandler EventsHandler = new EventHandler();
		new MainFrame(0.5,EventsHandler);
		
		}

	
}



class MainFrame extends JFrame
{
	
	
	private static final long serialVersionUID = 2;
	private EventHandler eh;

	
	MainFrame(double resol,EventHandler eh)
	{
		this.eh=eh;
		startScreen(resol);
		
		MainSearchPanel msp = new MainSearchPanel(this.eh);
		
		
		
		this.add(msp);
		
		this.setVisible(true);
		
	}
	
	private void startScreen(double size)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		
		this.setSize((int)(width*size), (int)(height*size)/2);
		this.setLocation((int)(width/2-(width*size)/2),(int)(height/2-(height*size)/4));
		
		
		eh.getMainFrame(this);
		this.addWindowListener(eh);
		this.setLayout(new GridLayout(1,1));
		
		this.setVisible(true);
		this.setTitle("Java GUI based FileSearch");
	
	}


}



class EventHandler implements ActionListener,WindowListener,ListSelectionListener
{

	private JButton SearchButton;
	private JButton ExitButton;
	private JFrame MainFrame;
	private JTextField LocationTF;
	private JTextField SearchTF;
	
	private Vector<SearchFrame> SearchFrameVector = new Vector<SearchFrame>();
	private Vector<Search> SearchVector = new Vector<Search>();


	
	
	public void getMainFrame(JFrame jf)
	{
		this.MainFrame = jf;
	}
	
	public void getSearchButton(JButton b)
	{
		this.SearchButton = b;
	}
	
	public void getExitButton(JButton b)
	{
		this.ExitButton = b;
	}
	public void getTextFields(JTextField LocationTF, JTextField SearchTF)
	{
		this.LocationTF=LocationTF;
		this.SearchTF = SearchTF;
	}
	
	public void addSearchFrame(SearchFrame sf)
	{
		sf.addWindowListener(this);
		SearchFrameVector.add(sf);
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		
		
		if(e.getSource().equals(SearchButton))
		{
			try
			{
			
				
				String s = SearchTF.getText().toString();
			

				
				if(s.contains("\"") || s.contains("/") || s.contains(":")|| 
						s.contains(">")|| s.contains("<")|| s.contains("|")|| s.contains("\\") || s.equals(""))
				{
					throw(new Exception("InvalidKEYWORD"));
				}
				
				

				s= LocationTF.getText().toString();
				
				
				
				if(s.contains(">")|| s.contains("<")|| s.contains("|")||  s.equals("") || s.contains("?"))
				{
					throw(new Exception("InvalidDIR"));
				}
				
				if(!Paths.get(s).isAbsolute())
				{
					throw(new Exception("NOTAbsolute"));
				}
				
				Search search = new Search(LocationTF.getText(),SearchTF.getText());
				
				if(search.getValid()==0)
					throw(new Exception("InvalidDIR"));
				SearchVector.add(search);
				SearchFrame sf = new SearchFrame(search,this);
				sf.setCurrent(s);
				search.getSF(sf);
				
				search.start();
			}
			catch(Exception ex)
			{
				if(ex.getMessage().contentEquals("InvalidDIR"))
					JOptionPane.showMessageDialog(null, "Invalid Path", "PathERR", JOptionPane.ERROR_MESSAGE);
						
				if(ex.getMessage().contentEquals("InvalidKEYWORD"))
					JOptionPane.showMessageDialog(null, "Invalid search word.", "SearchERR", JOptionPane.ERROR_MESSAGE);
				
				if(ex.getMessage().contentEquals("NOTAbsolute"))
					JOptionPane.showMessageDialog(null, "Please type directory in absolute path", "PathERR", JOptionPane.ERROR_MESSAGE);
				
			}
		
		}
		
		if(e.getSource().equals(ExitButton))
		{

			System.exit(0);
		}
		
		int i=0;
		SearchFrame SFrame;
		Search search;
		for(i=0;i<SearchVector.size();i++)
		{
			 SFrame = SearchFrameVector.elementAt(i);
			 search = SearchVector.elementAt(i);
			
			if(SFrame!=null)
			{
			if(e.getSource().equals(SFrame.getDelete()))
			{
				if(SFrame.getDefaultTable().getRowCount()>0)
				{
					int deletingIndex = SFrame.getJTable().getSelectedRow();
					
					if(search.deleteFile(SFrame.getJTable().getSelectedRow())==0)
					{
						
						SFrame.getJTable().setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
						{
							
						    @Override
						    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
						    {
						    	final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						        Map attributes = c.getFont().getAttributes();
						        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
						        c.setFont(row==deletingIndex ? new Font(attributes) : c.getFont() );
						        return c;
						    }
						});
						
						JOptionPane.showMessageDialog(SFrame, "The selected File is already deleted.", "FileERR", JOptionPane.ERROR_MESSAGE);
						SFrame.repaint();
					}
					
					
				}
					
				
			}	
		
			if(e.getSource().equals(SFrame.getStop()))
			{
					
				//stop thread
			
				search.interrupt();
			}
		
			if(e.getSource().equals(SFrame.getFinish()))
			{
				//check if thread is running
				//stop thread and exit
				if(!search.isInterrupted())
					search.interrupt();
				
				SFrame.dispose();
				SearchFrameVector.remove(i);
				SearchVector.remove(i);
				
			
			}
			}
		}
	
		
		
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if(e.getSource().equals(MainFrame))
		{
			
			System.exit(0);
		}
		int i=0;
		for(i=0;i<SearchFrameVector.size();i++)
		{
		if(SearchFrameVector.elementAt(i)!=null)
		if(e.getSource().equals(SearchFrameVector.elementAt(i)))
		{
			//stop Thread;
			if(!SearchVector.elementAt(i).isInterrupted())
				SearchVector.elementAt(i).interrupt();
			
			SearchFrameVector.elementAt(i).dispose();
			SearchFrameVector.remove(i);
			SearchVector.remove(i);
			
		
		}
		}
		

		
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
	
		
	}
}

class MainSearchPanel extends JPanel
{
	private static final long serialVersionUID = 3;
	private JButton SearchButton = new JButton("Search");
	private JButton ExitButton = new JButton("Exit");
	private JTextField LocationTF = new JTextField();
	private JTextField SearchTF = new JTextField();
	private Font f;
	private EventHandler eh;
	
	MainSearchPanel(EventHandler eh)
	{
		this.eh =eh;
		setScreen();
	}
		
	private void setScreen()
	{
		
		f = new Font("D2 Coding",Font.PLAIN,20);;
		SearchButton.setFont(f);
		ExitButton.setFont(f);
		LocationTF.setFont(f);;
		SearchTF.setFont(f);
		JLabel j1 = new JLabel("Directory : ",SwingConstants.CENTER);
		JLabel j2 = new JLabel("Search : ",SwingConstants.CENTER);
	
	     SearchButton.setBackground(new Color(59, 89, 182));
	     SearchButton.setForeground(Color.WHITE);
	     SearchButton.setFocusPainted(false);
	     SearchButton.setFont(new Font("D2 Coding",Font.BOLD,17));
	     
	     ExitButton.setBackground(new Color(59, 89, 182));
	     ExitButton.setForeground(Color.WHITE);
	     ExitButton.setFocusPainted(false);
	     ExitButton.setFont(new Font("D2 Coding",Font.BOLD,17));
		
		j1.setFont(f); 
		j2.setFont(f);	
		
		this.setLayout(new GridBagLayout());
		
		 GridBagConstraints GBC = new GridBagConstraints();
		 
		
		GBC.fill = GridBagConstraints.BOTH;
		GBC.weightx=1;
		GBC.weighty=1;
		
		GBC.gridx=0;
		GBC.gridy=0;
		
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		GBC.weighty=0.6;
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=1;
		GBC.gridy=0;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=2;
		GBC.gridy=0;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=3;
		GBC.gridy=0;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=4;
		GBC.gridy=0;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=5;
		GBC.gridy=0;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.weighty=0.5;
		GBC.gridx=0;
		GBC.gridy=1;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		this.add(j1,GBC);
		
		
		GBC.gridx=1;
		GBC.gridy=1;
		GBC.gridwidth=4;
		GBC.gridheight=1;
		this.add(LocationTF,GBC);
		
		GBC.gridx=0;
		GBC.gridy=2;
		
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		GBC.weighty=0.6;
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=1;
		GBC.gridy=2;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=2;
		GBC.gridy=2;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=3;
		GBC.gridy=2;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=4;
		GBC.gridy=2;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=5;
		GBC.gridy=2;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.weighty=0.5;
		
		GBC.gridx=0;
		GBC.gridy=3;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		this.add(j2,GBC);
		
		
		GBC.gridx=1;
		GBC.gridy=3;
		GBC.gridwidth=4;
		GBC.gridheight=1;
		this.add(SearchTF,GBC);
		
		GBC.gridx=0;
		GBC.gridy=4;
		
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=1;
		GBC.gridy=4;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=2;
		GBC.gridy=4;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=3;
		GBC.gridy=4;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=4;
		GBC.gridy=4;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.gridx=5;
		GBC.gridy=4;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		
		this.add(new JLabel(""),GBC);
		
		GBC.weighty=0.7;
		
		GBC.gridx=1;
		GBC.gridy=5;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		this.add(SearchButton,GBC);
		
		
		GBC.gridx=3;
		GBC.gridy=5;
		GBC.gridwidth=1;
		GBC.gridheight=1;
		this.add(ExitButton,GBC);
		
		
		eh.getSearchButton(this.SearchButton);
		eh.getExitButton(this.ExitButton);
		eh.getTextFields(LocationTF, SearchTF);
		this.SearchButton.addActionListener(eh);
		this.ExitButton.addActionListener(eh);
		
	
		
		this.setVisible(true);
		

		
		
		
		
		
	}
}

class SearchFrame extends JFrame
{
	private static final long serialVersionUID = 4;
	private JLabel currentSearching = new JLabel("Now Searching : ");
	private JLabel currentDirectory = new JLabel("");
	private JButton delete = new JButton("Remove");
	private JButton stop = new JButton("Stop");
	private JButton finish = new JButton("Done");
	private JScrollPane scrollpane = new JScrollPane();
	private DefaultTableModel DefaultTable = new DefaultTableModel();
	private JTable table = new JTable();
	private Search search;
	private Vector<File> FileVector = new Vector<File>();
	private EventHandler eh;
	
	SearchFrame(Search search,EventHandler eh)
	{
		this.eh=eh;
		this.search=search;
		setScreen(0.5);
	}
	
	private void setScreen(double size)
	{
		this.setTitle("Searching...");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		
		this.setSize((int)(width*size), (int)(height*size));
		this.setLocation((int)(width/2-(width*size)/2) ,(int)(height/2-(height*size)/2) );
	
		
		this.setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		top.add("West",currentSearching);
		top.add("Center",currentDirectory);
		this.add("North",top);

		 delete.setBackground(new Color(59, 89, 182));
		 delete.setForeground(Color.WHITE);
		 delete.setFocusPainted(false);
		 delete.setFont(new Font("D2 Coding",Font.BOLD,13));
	     
		 stop.setBackground(new Color(59, 89, 182));
		 stop.setForeground(Color.WHITE);
		 stop.setFocusPainted(false);
		 stop.setFont(new Font("D2 Coding",Font.BOLD,13));
	     
		 finish.setBackground(new Color(59, 89, 182));
		 finish.setForeground(Color.WHITE);
		 finish.setFocusPainted(false);
		 finish.setFont(new Font("D2 Coding",Font.BOLD,13));

	      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	       
	      DefaultTable.addColumn("FileName");
	      DefaultTable.addColumn("FileSize");
	      DefaultTable.addColumn("Modified Date");
	      DefaultTable.addColumn("Location");
	     
	    
	       table.setModel(DefaultTable);
	      
	       
	        JScrollPane scrollPane = new JScrollPane(table);
	        scrollPane.setBackground(Color.white);
	        table.setFont(new Font("D2 Coding",Font.PLAIN,15));
	        this.add("Center",scrollPane);

	        
	     JPanel bot = new JPanel();
	     bot.setLayout(new GridLayout(1,3));
	     bot.add(delete);
	     bot.add(stop);
	     bot.add(finish);
		
	     this.add("South",bot);
	     
	  
	    delete.addActionListener(eh);
		stop.addActionListener(eh);
		finish.addActionListener(eh);
		table.getSelectionModel().addListSelectionListener(eh);
	    eh.addSearchFrame(this);
		setVisible(true);
		 
       
	}
	

	
	public JButton getDelete()
	{
		return this.delete;
	}
	
	public JButton getStop()
	{
		return this.stop;
	}
	
	
	public JButton getFinish()
	{
		return this.finish;
	}
	
	
	public DefaultTableModel getDefaultTable()
	{
		return this.DefaultTable;
	}
	
	
	public JTable getJTable()
	{
		return this.table;
	}
	
	public Search getSearch()
	{
		return this.search;
	}
	public void setCurrent(String s)
	{
			currentDirectory.setText(s);
	}
	
	public void addF(File f)
	{
		
		
		 try {
			 
		FileVector.add(f);
	FileTime filetime;
	
	
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
		
		//	Path path = f.getAbsolutePath();
			FileTime fileTime = Files.getLastModifiedTime(Paths.get(f.getAbsolutePath()));
		
		 
		DefaultTable.addRow(new Object[]{f.getName(), f.length()+"byte", new SimpleDateFormat("MM/dd/yyyy hh:mm").format(fileTime.toMillis()),f.getAbsolutePath()});
		 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public Vector<File> getFileVector()
	{
		return this.FileVector;
	}

}



class Search extends Thread
{
	
	
	private String SearchingText;
	private SearchFrame sf;
	private Stack<File> stack = new Stack<File>();
	private int valid=0;
	private Matcher matcher;
	private Pattern pattern;
	
	Search(String startDir,String searchingText)
	{
			
		File f = new File(startDir);
		
		if(f.isDirectory())
		{
			
			File[] tmp = f.listFiles(new FileFilter() {
			    @Override
			    public boolean accept(File file) {
			        return !file.isHidden();
			    }
			});
			int i=0;
			for(i=0;i<tmp.length;i++)
			{
				stack.push(tmp[i]);
			}
			valid=1;
		}
		else
		{
			
			valid=0;
		}
		this.SearchingText=formString(searchingText);
		
		pattern = Pattern.compile(SearchingText);
		
		
	}
	void getSF(SearchFrame sf)
	{
		this.sf=sf;
	}
	
	
	@Override
	public void run()
	{
		try {

			
		while(!stack.isEmpty())
		{
			
			if(this.isInterrupted())
				throw new InterruptedException();
			File f = stack.pop();
		
			
			if(f.isDirectory())
			{
				
				
				File[] tmp = f.listFiles(new FileFilter() {
				    @Override
				    public boolean accept(File file) {
				    	try {
				    	Path path = Paths.get(file.getAbsolutePath());
				    	
							DosFileAttributes dfa = Files.readAttributes(path, DosFileAttributes.class);
							
							return (!dfa.isSystem() || !dfa.isOther());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						return true;
				    	
				        
				    }
				});
				int i=0;
				for(i=0;i<tmp.length;i++)
				{
					stack.push(tmp[i]);
				}
			}
			else
			{
				sf.setCurrent(f.getParent());
				if((pattern.matcher(f.getName())).find())
				{
					
					Runnable addtoJList = new Runnable()
					{
						public void run()
						{
							sf.addF(f);
						}
					};
					SwingUtilities.invokeLater(addtoJList);
					
				}
			}
			
		}
		
		}
		catch(InterruptedException e)
		{
			this.interrupt();
			e.printStackTrace();
		}
	}
	
	String formString(String input)
	{
		
		input = input.replace(".","[.]{1}");
		input = input.replace("?", ".{1}");
		input = input.replace("*", ".*");
		
		int i=0;
		char c;
		
		String s="";
		if(input.charAt(0)!='*')
		{
			s+="^";
		}

		s+=input;
		
		if(input.charAt(input.length()-1)!='*')
		s+="$";
		
	
		return s;
	}
	
	public synchronized int deleteFile(int index)
	{
	
			File F = sf.getFileVector().get(index);
		if(F.exists())
		{
			sf.getDefaultTable().removeRow(index);
			F.delete();
			sf.getFileVector().remove(index);
			return 1;
		}
		else
		{
			return 0;
		}
		
	}
	
	int getValid()
	{
		return this.valid;
	}
}
