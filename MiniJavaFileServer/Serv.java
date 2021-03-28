import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;;

public class Serv
{
	
	private ServerSocket ssocket;
	private int sport = 55551;
	private Vector<String> data;
	private Vector<String> code;
	private int buffersize=0;
	
	public static void main(String[] args)
	{
		Serv server = new Serv();
		server.openData();
		server.getConfig();
	}
	
	void openSocket()
	{
		try {
		ssocket = new ServerSocket(sport);
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	

	void openData()
	{
		try {
		FileReader rw = new FileReader("./data");
		BufferedReader br = new BufferedReader(rw);
		String readLine = null;
		
		data = new Vector<String>();
		code = new Vector<String>();
		String[] parsed;
		while( (readLine = br.readLine())!= null)
		{
			parsed =readLine.split("[?]");
			
			code.add(parsed[0]);
			data.add(parsed[1]);
		}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	void getConfig()
	{
		try {
			FileReader rw = new FileReader("./config");
			BufferedReader br = new BufferedReader(rw);
			String readLine = null;
			
			String[] parsed;
			while( (readLine = br.readLine())!= null)
			{
				parsed =readLine.split("[=]");
				if(parsed[0].equals("buffersize"))
					buffersize=Integer.parseInt(parsed[1]);
				System.out.println(buffersize);
			}
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	
	void addData(String codename, String dataname)
	{
		data.add(dataname);
		code.add(codename);
	}
	
}


class SocketThread extends Thread
{
	private ServerSocket ssocket;
	private Socket sock;
	private Serv server;
	
	SocketThread(ServerSocket s, Serv serv)
	{
		this.ssocket = s;
		this.server = serv;
	}
	
	public void run()
	{
		try {
		sock = ssocket.accept();
		byte[] recv = Receive();
		
		String[] recvd = parseRecv(recv);
		
		int mode = Integer.parseInt(recvd[0]);
		
		switch(mode)
		{
		case 0: 
			String s = "1";
			Send(s.getBytes());
			Download(recvd[2]);
			addData(recvd[1],recvd[2]);
			break;
		case 1:
			break;
		case 2:
			break;
		}
		
		
		}
		catch(Exception e) {
			e.printStackTrace();
			}
	
		
	}
	protected int Send(byte[] buffer) {
		try {
		DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());
		dOut.writeInt(buffer.length);
		dOut.write(buffer);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	protected byte[] Receive()
	{
		byte[] msg = null;
		try {
			DataInputStream dIn = new DataInputStream(sock.getInputStream());
			int length = dIn.readInt();
			if(length > 0)
			{
				msg = new byte[length];
				dIn.readFully(msg,0,msg.length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return msg;
		
	}
	
	protected void addData(String code, String dataname)
	{
		synchronized(this)
		{
			server.addData(code, dataname);
		}
	}
	
	protected String[] parseRecv(byte[] input)
	{
		if(input.length<=0) 
			return null;

		String[] parsed = new String[3];
		
		String whole = new String(input);
		
		parsed = whole.split("[?]");
		
		return parsed;
		
	}
	
	protected void Download(String Filename)
	{
		try {
			byte[] Data = Receive();
			
			File f = new File("./storage"+Filename);
			FileOutputStream fos = new FileOutputStream(f);
			
			fos.write(Data);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	
	
	protected void upload()
	{
		
	}
}