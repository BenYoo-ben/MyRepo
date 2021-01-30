import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBMS {
	
	Connection conn;
	ResultSet rs = null;
	PreparedStatement psmt = null;
	
	public JDBMS() 
	{
		
	}
	
	int PrepareDB()
	{
		try {
			Class.forName(GlobalVar.OracleDriverURL);
		
			conn = DriverManager.getConnection(GlobalVar.OracleDriverURL,GlobalVar.OracleID,GlobalVar.OraclePW);
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -2;
		}

		return 0;
	}
	
	
}

/*
		****** INSERT STATEMENT EXAMPLE *****
		String sql = "insert into department(deptno, deptname,floor) values (?,?,?)";
			
			int deptno = 1;
			String deptname = "deptname";
			int floor = 1;
			
			psmt = con.prepareStatement(sql);
			psmt.setInt(1, deptno);
			psmt.setString(2, deptname);
			psmt.setInt(3, floor);
			psmt.executeUpdate();
		
		
		******* SELECT/UPDATE STATEMENT EXAMPLE *************
		int floor =1;
			String sql = "select * from department where floor=?";
			
			psmt = con.prepareStatement(sql); 
			psmt.setInt(1, floor);
			rs = psmt.executeQuery(); //use pstm.executeUpdate(); for modifications.
			
			while(rs.next()) {
				int a = rs.getInt("deptno");
				String b = rs.getString("deptname");
				int c = rs.getInt("floor");
				System.out.println("deptno¿∫ "+a+"deptname¿∫ "+b+"floor¿∫ "+c);
			}
			
		
*/