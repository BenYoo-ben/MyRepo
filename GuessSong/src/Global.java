
public class Global {

	static char REQUEST_FILE = 0x08;
	//client -> server
	static char INPUT_ANSWER = 0x32;
	//client -> server
	
	static char READY_SIGN = 0x02;
	//client <-> server
	
	static char SENDING_FILE = 0x07;
	//server -> client
	
	static int PORT;
	
	static char RUN_MODE = 0;
	//0 for unknown, 1 for server, 2 for client
}
