import java.util.ArrayList;
import java.util.HashMap;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
 * 
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다. section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	HashMap<String, Integer> extDef;
	ArrayList<String> extRef;

	ArrayList<MRecord> modRecord = new ArrayList<>();

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab    : 해당 section과 연결되어있는 symbol table
	 * @param literaTab : 해당 section과 연결되어있는 literal table
	 * @param instTab   : instruction 명세가 정의된 instTable
	 */

	public TokenTable(LabelTable symTab, LabelTable literalTab, InstTable instTab) {
		// ...
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		// ...

		Token t = tokenList.get(index);
		
		Instruction inst;
		int obj_code = 0;

		if ((inst = instTab.SearchInstTable(t)) != null) {

			if (inst.opcode == 0x4C) {

				
				int opcode = 0x4F;
				obj_code += (opcode << 16);
				t.objectCode = String.format("%02X", obj_code);
				t.byteSize = 3;
				tokenList.set(index, t);
				return;
			}

			if (inst.format == 34) {
				if (t.getFlag(TokenTable.eFlag) > 0) {
					t.byteSize = 4;
				} else
					t.byteSize = 3;
			} else
				t.byteSize = inst.format;

			obj_code += (inst.opcode) << (8 * (t.byteSize - 1));

			if (t.getFlag(TokenTable.iFlag) > 0 || t.getFlag(TokenTable.nFlag) > 0) {

			} else if (t.byteSize >= 3) {
				t.setFlag(TokenTable.iFlag, 1);
				t.setFlag(TokenTable.nFlag, 1);
			}

			if (t.byteSize >= 3) {
				if (t.operand.length > 1 && t.operand[1].equals("X")) {
					t.setFlag(TokenTable.xFlag, 1);
				}
				int sym_addr = -1;
				if (t.operand[0].contains("=")) {
					t.operand[0] = t.operand[0].substring(3, t.operand[0].length() - 1);
				}
				if ((sym_addr = this.symTab.search(t.operand[0])) != -1
						|| (sym_addr = this.literalTab.search(t.operand[0])) != -1) {
					if (t.byteSize == 4) {
						obj_code += t.location;
					} else // relative mode
					{
						int differ = sym_addr - t.location - t.byteSize;
						if (differ >= -2048 && differ < 2048) {
							// pc relative

							t.setFlag(TokenTable.pFlag, 1);
							if (differ < 0) {
								//12bit 음수처리 
								differ ^= 0xFFFFF000;
							}

							obj_code += differ;
						} else // base relative
						{
							differ = sym_addr - Assembler.base_register;
							if (t.getFlag(TokenTable.xFlag) > 0 && Assembler.base_register != -1 && differ < 4096) {
								obj_code += differ;

							} else {
								System.out.println("Can't use relative addressing");
								System.exit(1);
							}

						}
					}
				} else // can not find symbol in sym_table
				{
					if (t.getFlag(TokenTable.iFlag) > 0 && t.getFlag(TokenTable.nFlag) <= 0) {
						obj_code += Integer.parseInt(t.operand[0], 10);
					} else if (extRef.contains(t.operand[0]) ||
							t.operand[0].contains("-") ||t.operand[0].contains("+") ) {
						
						if(t.operand[0].contains("-"))
						{
							if(t.byteSize==3)
							{
								ConfigureModifyRecord(t,"\\-",3);
							}
							else if(t.byteSize==4)
							{
								ConfigureModifyRecord(t,"\\-",5);
							}
						}else if(t.operand[0].contains("\\+"))
						{
							if(t.byteSize==3)
							{
								ConfigureModifyRecord(t,"\\+",3);
							}
							else if(t.byteSize==4)
							{
								ConfigureModifyRecord(t,"\\+",5);
							}
						}
						else
						{
							if(t.byteSize==3)
							{
								ConfigureModifyRecord(t,"\\+",3);
							}
							else if(t.byteSize==4)
							{
								ConfigureModifyRecord(t,"\\+",5);
							}
						}
						
						
					} else {
						System.out.println("ERROR.");
						System.exit(3);
					}
				}
			} else if (t.byteSize == 2) {
				int counter = 0;
				int addr = 0;
				int caught = 0;
				while (counter < t.operand.length) {
					String s = t.operand[counter];
					caught = 0;
					switch (s) {
					case "A":
						addr += 0;
						caught = 1;
						break;
					case "X":
						addr += 1;
						caught = 1;
						break;
					case "L":
						addr += 2;
						caught = 1;
						break;
					case "B":
						addr += 3;
						caught = 1;
						break;
					case "S":
						addr += 4;
						caught = 1;
						break;
					case "T":
						addr += 5;
						caught = 1;
						break;
					case "F":
						addr += 6;
						caught = 1;
						break;
					case "PC":
						addr += 8;
						caught = 1;
						break;
					case "SW":
						addr += 9;
						caught = 1;
						break;
					case ",":
						addr <<= 4;
						caught = 1;
						break;
					default:
						break;
					}
					if (caught == 0) {
						addr += Integer.parseInt(s, 10);
					}
					counter++;
				}

				if (counter <= 1) {
					addr <<= 4;
				}

				obj_code += addr;
			}

			if (t.byteSize == 4) {
				obj_code += (t.nixbpe << 20);
				t.objectCode = String.format("%08X", obj_code);
			} else if (t.byteSize == 3) {
				obj_code += (t.nixbpe << 12);
				t.objectCode = String.format("%06X", obj_code);
			}
			else
			{
				t.objectCode = String.format("%04X", obj_code);
			}

			
			
			tokenList.set(index, t);
			return;
		} else if (t.operator.equals("WORD")) {
			t.byteSize=3;
			obj_code=0;
		
			if (t.operand[0].contains("-")) {
				
				ConfigureModifyRecord(t,"\\-",6);
			} else if (t.operand[0].contains("+")) {
				ConfigureModifyRecord(t,"\\+",6);
			} else if(extRef.contains(t.operand[0])) {
				ConfigureModifyRecord(t,"\\+",6);
			} else
			{
				obj_code = Integer.parseInt(t.operand[0],10);
			}
			
			t.objectCode = t.objectCode = String.format("%06X", obj_code);
			tokenList.set(index, t);
			return;
		}else if(t.operator.equals("BYTE"))
		{
			t.objectCode="";
			if(t.operand[0].charAt(0)=='X')
			{
				t.objectCode = t.operand[0].substring(2,4);
				t.byteSize=1;
			}
			else if(t.operand[0].charAt(0)=='C')
			{
				int j=2;
				int size=0;
				char c;
				while( (c = t.operand[0].charAt(j))!='\'')
				{
					t.objectCode += String.format("%6X",(int)c);
					size++;
				}
				t.byteSize=size;
			}
			tokenList.set(index, t);
			return;
		}
	
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

	void connect_exts(HashMap<String, Integer> extd, ArrayList<String> extr) {
		this.extDef = extd;
		this.extRef = extr;
	}

	void ConfigureModifyRecord(Token t, String delimiter, int writable)
	{	
		if(writable==6)
			t.location--;
		
		
		String input = t.operand[0];
		if(input.charAt(0)==delimiter.charAt(0))
		{
			input = input.substring(1);
			String[] a = input.split(delimiter);
			int i=1;
			while(i<a.length)
			{	
				modRecord.add(new MRecord(t.location, writable, delimiter.charAt(1),a[i]));
				i++;
			}
		}
		else
		{
			String[] a = input.split(delimiter);
			int i=0;
			while(i<a.length)
			{	
				modRecord.add(new MRecord(t.location, writable, delimiter.charAt(1),a[i]));
				i++;
			}
		}
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		// initialize ???
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		nixbpe = 0;
		String[] tokens = line.split("\t");

		int i = 0;
		if (i < tokens.length)
			label = tokens[i];
		else
			return;
		i++;

		if (i < tokens.length)
			operator = tokens[i];
		else
			return;

		if (operator.contains("+")) {
			byteSize = 4;
			setFlag(TokenTable.eFlag, 1);
			operator = operator.substring(1);
		}

		i++;

		if (i < tokens.length)
			operand = tokens[i].split(",");
		else
			return;
		if (operand[0].contains("#")) {
			setFlag(TokenTable.iFlag, 1);
			operand[0] = operand[0].substring(1);
		} else if (operand[0].contains("@")) {
			setFlag(TokenTable.nFlag, 1);
			operand[0] = operand[0].substring(1);
		}

		i++;

		if (i < tokens.length)
			comment = tokens[i];
		else
			return;

	}

	/**
	 * n,i,x,b,p,e flag를 설정한다.
	 * 
	 * inst_fmt_address = Con 사용 예 : setFlag(nFlag, 1) 또는 setFlag(TokenTable.nFlag,
	 * 1)
	 * 
	 * @param flag  : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {

		if (value == 1)
			nixbpe = (char) (nixbpe | flag);
		else
			nixbpe = (char) (nixbpe - flag);

	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다.
	 * 
	 * 사용 예 : getFlag(nFlag) 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}

}

class MRecord {
	int addr;
	int length;
	char modification_flag;
	String symbol;

	public MRecord(int addr, int length, char modification_flag,String symbol)
	{
		this.addr = addr+1;
		this.length =length;
		this.modification_flag = modification_flag;
		this.symbol=  symbol;
	}
	/*
	 * check if operand is in format EXTREF1 -+ EXTREF2 ...
	 * 
	 * @return : result value;
	 */

}
