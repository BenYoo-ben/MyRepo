
public class GlobalVar {
	
	
	
	static int server_port = 54326;
	
	static String OracleDriverURL = "oracle.jdbc.driver.OracleDriver";
	static String OracleURL = "jdbc:mysql://localhost";
	static String OracleID = "root";
	static String OraclePW = "xhdtls";
	
	static void perror(String err_msg)
	{
		System.out.println("========ERR=======\n"+err_msg+"\n========ERR=======\n");
		System.exit(1);
		
	}
}
