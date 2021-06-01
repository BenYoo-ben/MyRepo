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
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.util.Iterator;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Serv {

	private ServerSocket ssocket;

	// sport in config
	private int sport = 0;
	private Vector<String> data;
	private Vector<String> code;

	// buffersize in config
	private int buffersize = 0;
	private FileWriter fw;

	public static void main(String[] args) {

		// make server class
		Serv server = new Serv();
		// get configurations (sport, buffersize etc.)
		server.getConfig();

		// read data
		server.openData();

		//open ServerSocket
		server.openSocket();
		long conn_count = 0;
		while (true) {

			//start threads with tcp-handshake finished sockets
			Socket s = server.socketAccept();

			SocketThread st = new SocketThread(server, s);
			st.start();
			System.out.println("Conn:" + conn_count++ + " UP!");
		}
	}

	void openSocket() {
		try {
			ssocket = new ServerSocket(sport);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void openData() {
		try {
			//open data, read file data
			FileReader rw = new FileReader("./data");
			fw = new FileWriter("./data", true);
			BufferedReader br = new BufferedReader(rw);
			String readLine = null;

			data = new Vector<String>();
			code = new Vector<String>();
			String[] parsed;
			while ((readLine = br.readLine()) != null) {
				//parse meta-data
				parsed = readLine.split("[?]");

				code.add(parsed[0]);
				data.add(parsed[1]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void getConfig() {
		try {
			//read config files
			FileReader rw = new FileReader("./config");
			BufferedReader br = new BufferedReader(rw);
			String readLine = null;

			String[] parsed;
			while ((readLine = br.readLine()) != null) {
				parsed = readLine.split("[=]");
				if (parsed[0].equals("buffersize"))
					buffersize = Integer.parseInt(parsed[1]);

				if (parsed[0].equals("sport"))
					sport = Integer.parseInt(parsed[1]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//synchronized no multiple access
	synchronized void addData(String codename, String dataname) {

		data.add(dataname);
		code.add(codename);
		try {
			fw.write(codename + "?" + dataname + "\n");
			fw.flush();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
//synchronized no multiple access
	synchronized int removeData(String codename, String dataname) {
		System.out.println("Remove Start...");
		int loc;
		if ((loc = findFile(codename, dataname)) != 0)
			return loc;
		else
			loc = findFileLocation(dataname);

		data.remove(loc);
		code.remove(loc);

		//if file not exists or can't delete return 3
		try {
			File f = new File("./storage/"+dataname);
			if(f.exists()) {
				if(f.delete())
					System.out.println("Removing ... ");
				else
					return 3;
			}
			else
				return 3;
			
			//reset filewriter and etc.
			FileReader rw = new FileReader("./data");
			BufferedReader br = new BufferedReader(rw);
			String readLine = null;
			String fullData = "";

			String[] parsed;
			while ((readLine = br.readLine()) != null) {
				parsed = readLine.split("[?]");
				if (!(parsed[1].equals(dataname))) {
					fullData += readLine;
					fullData += "\n";
				}
			}
			fw.close();
			fw = new FileWriter("./data");
			fw.write(fullData);
			fw.flush();
			fw.close();
			fw = new FileWriter("./data",true);

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Remove Complete!");
		return 0;
	}

	ServerSocket getServerSocket() {
		return this.ssocket;
	}

	//return tcp-handshake finished sockets;
	Socket socketAccept() {
		Socket s = null;
		try {

			s = ssocket.accept();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	int getBuffersize() {
		return this.buffersize;
	}
//returns datalist
	String getDataList() {
		String s = "";

		System.out.println();
		Iterator<String> i = data.iterator();

		boolean able = false;
		if (i.hasNext())
			able = true;
		while (able) {
			s += i.next();

			if (i.hasNext())
				s += "?";
			else
				able = false;
		}
		return s;
	}

/*fild file if exists
 * 0 = Success
 * 1 = no file found
 * 2 = code wrong
 */
	int findFile(String filecode, String filename) {
		Iterator<String> i = data.iterator();
		Iterator<String> ic = code.iterator();

		while (i.hasNext()) {
			String s = i.next();
			String c = ic.next();
			if (filename.equals(s)) {
				if (filecode.equals(c)) // success return;
					return 0;
				else// code wrong return;
					return 2;

			}
		}

		// no file found return
		return 1;
	}
//returns file location, used to delete
	int findFileLocation(String filename) {
		Iterator<String> i = data.iterator();

		int loc = 0;
		while (i.hasNext()) {
			String s = i.next();

			if (filename.equals(s)) {
				return loc;
			}
			loc++;
		}
		return -1;

	}

}

class SocketThread extends Thread {

	//global
	private Socket sock;
	private Serv server;

	private FileOutputStream fos = null;
	private ByteArrayInputStream bin = null;
	private BufferedOutputStream bos = null;

	SocketThread(Serv serv, Socket s) {

		this.server = serv;
		this.sock = s;
	}

	//when class dies close all stream
	public void finalize() {
		try {
			fos.close();
			bin.close();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//main for thread(single socket operation)
	public void run() {
		try {

			//send 0 when conn established
			Send(new String("0").getBytes());

			//recv wanted operation from client
			byte[] recv = Receive();

			/*parse client operation
			 * FORMAT > [Operation-TYPE 0~3]?[PWCODE]?[FILENAME]
			 * example > 0?pswd123?benyoo.png
			 * 
			 * MODE
			 * 0 - Data Upload(Client->Server)
			 * 1 - Data Download(Server->Client)
			 * 2 - View Datalist(Server->Client)
			 * 3 - Remove Data(Client->Server)
			 */
			String[] recvd = parseRecv(recv);

			//get mode
			int mode = Integer.parseInt(recvd[0]);
			int r;
			
			//mode -> switch(mode)
			switch (mode) {
			case 0:
				/*	1. C -> S : client sends request data in first req.
				 * 	2. S -> C : Dummy data, ready for 
				 * 	3. C -> S : Client sends actual data in byte array format.
				 * 	4. Socket close ...
				 */
				Send(new String("0").getBytes());
				Download(recvd[2]);
				addData(recvd[1], recvd[2]);
				sock.close();
				break;
			case 1:
				/*	1. C -> S : client sends request data in first req(data info that client wants)
				 * 	2. S -> C : Server searches for data in storage then send status( look at findFile for desc)
				 *	3. C -> S : Client sends dummy data when ready to download
				 * 	4. S -> C : Server sends actual data
				 *  5. Socket close ...
				 */
				r = server.findFile(recvd[1], recvd[2]);
				Send(String.valueOf(r).getBytes());
				if (r == 0) {
					Receive();
					UploadFile(recvd[2]);
					sock.close();
				}
				break;
			case 2:
				/* 1. C -> S : Client sends request data
				 * 2. S -> C : Server sends data list immediately.
				 * 3. Socket close ...
				 */
				UploadDataList();
				sock.close();
				break;
			case 3:
				/* 1. C -> S : Client sends request data in first req(code and filename)
				 * 2. S -> C : Server searches for data in stroage then send status
				 * (look at findFile & Remove File for more info)
				 * 3. S -> S : Server deletes File if file exists then send 0
				 * 4. Socket close ...
				 */
				r = server.findFile(recvd[1], recvd[2]);
				if(r!=0)
					Send(String.valueOf(r).getBytes());
				else
				{
					r = server.removeData(recvd[1],recvd[2]);
					Send(String.valueOf(r).getBytes());
				}
				sock.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected int Send(byte[] buffer) {
		try {
			
			DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());
		
				//data in format [INT:Datasize][BYTE:Actual Data]
			dOut.writeInt(buffer.length);
			dOut.write(buffer);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	protected byte[] Receive() {
		byte[] msg = null;
		try {
			DataInputStream dIn = new DataInputStream(sock.getInputStream());
			//data in format [INT:Datasize][BYTE:Actual Data]
			int length = dIn.readInt();
			if (length > 0) {
				msg = new byte[length];
				dIn.readFully(msg, 0, msg.length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		// System.out.println("RECVD : \n" + new String(msg));
		return msg;

	}

	protected void addData(String code, String dataname) {
		server.addData(code, dataname);

	}

	protected String[] parseRecv(byte[] input) {
		if (input.length <= 0)
			return null;

		//parse request data(in [OP_TYPE]?[CODE]?[NAME])
		String[] parsed = new String[3];

		String whole = new String(input);

		parsed = whole.split("[?]");

		return parsed;

	}

	protected void Download(String Filename) {
		System.out.println("Download Start");

		try {
			byte[] Data = Receive();

			int readCount = 0;
			//Download actual file...
			File f = new File("./storage/" + Filename);
			fos = new FileOutputStream(f);
			bin = new ByteArrayInputStream(Data);
			bos = new BufferedOutputStream(fos);
			byte[] outBuffer = new byte[server.getBuffersize()];

			System.out.println("Data Write...");
			while ((readCount = bin.read(outBuffer)) > 0) {
				bos.write(outBuffer, 0, readCount);
			}

			System.out.println("Data Write Complete!");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void UploadFile(String Filename) {
		try {
			System.out.println("Data Read Start...");
			byte[] byteArray = Files.readAllBytes(Paths.get("./storage/" + Filename));
			System.out.println("Data Read Complete!");
			System.out.println("Data Send Start...");
			Send(byteArray);
			System.out.println("Data Send Complete!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void UploadDataList() {
		String DataList;
		DataList = server.getDataList();
		System.out.println("Sending : " + DataList);
		Send(DataList.getBytes());

	}
}