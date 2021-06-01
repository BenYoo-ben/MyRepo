import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Assembler: 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인루틴이다. 프로그램의 수행 작업은 다음과 같다.
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다.
 * 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다
 * 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1)
 * 
 * 4) 분석된 내용을바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 * 작성중의 유의사항:
 * 
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.
 * 
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨
 * 
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 * 
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<LabelTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LabelTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;

	
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. 필요한 경우 String 대신 별도의 클래스를
	 * 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;


	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<LabelTable>();
		literaltabList = new ArrayList<LabelTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/*
	 * 프로그램의 구현을 위해 사용한 자료구조들
	 */
	//각 control section의 프로그램명을 보관
	ArrayList<String> prog_name = new ArrayList<>();
	//각 control section의 시작 주소를 보관
	ArrayList<Integer> start_addr = new ArrayList<>();
	//base register가 사용되었다면 저장한 symbol 주소를 보관
	static int base_register = -1;
	//control section count
	int pbc = -1;
	
	//END operator's operand
	String end_operand = null;

	//EXTDEFs
	ArrayList<HashMap<String, Integer>> extDef = new ArrayList<>();
	//EXTDEF in current control section
	ArrayList<String> current_extDef = new ArrayList<>();
	
	//EXTREFs
	ArrayList<ArrayList<String>> extRef = new ArrayList<>();
	//각 control section의 프로그램 길이를 저장
	ArrayList<Integer> program_length = new ArrayList<>();
	
	
	
	
	
	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab");
		assembler.printLiteralTable("literaltab");
		assembler.pass2();
		assembler.printObjectCode("output");

	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			File f = new File(inputFile);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String input_line = null;
			;
			while ((input_line = br.readLine()) != null) {
				lineList.add(input_line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * pass1 과정을 수행한다.
	 * 
	 * 1) 프로그램 소스를 스캔하여 토큰 단위로 분리한 뒤 토큰 테이블을 생성.
	 * 
	 * 2) symbol, literal 들을 SymbolTable, LiteralTable에 정리.
	 * 
	 * 주의사항: SymbolTable, LiteralTable, TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void pass1() {

		Iterator<String> i = lineList.iterator();

		int locctr = 0;
		int lit_count = 0;
		String s = null;

		if (pbc == -1) {
			pbc++;

			symtabList.add(new LabelTable());
			literaltabList.add(new LabelTable());
			TokenList.add(new TokenTable(symtabList.get(pbc), literaltabList.get(pbc), instTable));
			extDef.add(new HashMap<String, Integer>());
			extRef.add(new ArrayList<String>());
			TokenList.get(pbc).connect_exts(extDef.get(pbc), extRef.get(pbc));
		}

		if (i.hasNext()) {
			s = i.next();

			Token token = new Token(s);
			if (token.operator.equals("START")) {
				prog_name.add(token.label);
				start_addr.add(Integer.parseInt(token.operand[0], 10));

				locctr = start_addr.get(pbc);
				s = i.next();
			} else
				locctr = 0;

			if (token.label.length() != 0) {
				// search for symtab
				if (symtabList.get(pbc).search(token.label) != -1) {
					System.out.println("Error: Duplicate Symbol");
					System.exit(1);

				} else if (!token.label.equals("CSECT")) {
					symtabList.get(pbc).putName(token.label, locctr);
				}
			}
		}

		while (!s.contains("\tEND\t")) {

			// System.out.println(s);
			boolean is_comment = false;

			if (s.charAt(0) == '.') {
				is_comment = true;
			}

			if (!is_comment) {
				Token token = new Token(s);

				token.location = locctr;
				// when label field != empty
				if (token.label.length() != 0) {
					// search for symtab
					if (symtabList.get(pbc).search(token.label) != -1) {
						System.out.println("Error: Duplicate Symbol");
						System.exit(1);

					} else if (!token.operator.equals("CSECT")) {
						symtabList.get(pbc).putName(token.label, locctr);
					}
				}

				if (token.operand != null && token.operand[0].length() > 0)
					if (token.operand[0].charAt(0) == '='
							&& !literaltabList.get(pbc).label.contains(token.operand[0])) {
						literaltabList.get(pbc).label.add(token.operand[0]);
					}

				if (instTable.SearchInstTable(token) != null) {
					int eval = -1;
					int add_val = 0;
					eval = instTable.SearchInstTable(token).format;
					if (eval == 34) {
						if (token.getFlag(TokenTable.eFlag) > 0)
							add_val = 4;
						else
							add_val = 3;
					} else {
						add_val = eval;
					}

					locctr += add_val;

				} else if (token.operator.equals("WORD")) {
					locctr += 3;
				} else if (token.operator.equals("RESW")) {
					locctr += 3 * Integer.parseInt((token.operand[0]), 10);
					s = i.next();
					continue;
				} else if (token.operator.equals("RESB")) {
					locctr += Integer.parseInt((token.operand[0]), 10);
					s = i.next();
					continue;
				} else if (token.operator.equals("BYTE")) {
					if (token.operand[0].charAt(0) == 'X') {
						locctr += 1;
					} else {
						locctr += token.operand[0].length() - 3;
					}
				} else if (token.operator.equals("BASE")) {
					// do nothing.
				} else if (token.operator.equals("EXTDEF") || token.operator.equals("EXTREF")) {
					int mode = -1;
					if (token.operator.equals("EXTDEF"))
						mode = 0;
					else
						mode = 1;

					for (int iterator_i = 0; iterator_i < token.operand.length; iterator_i++) {
						if (mode == 0) {
							current_extDef.add(token.operand[iterator_i]);
						} else {
							extRef.get(pbc).add(token.operand[iterator_i]);
						}
					}
					s = i.next();
					continue;
				} else if (token.operator.equals("CSECT")) {

					prog_name.add(token.label);
					Iterator ite = current_extDef.iterator();
					while (ite.hasNext()) {
						String now_def = (String) ite.next();

						int addr = symtabList.get(pbc).search(now_def);
						extDef.get(pbc).put(now_def, addr);
					}

					for (; lit_count < literaltabList.get(pbc).label.size(); lit_count++) {
						int add_val = 0;
						if (literaltabList.get(pbc).label.get(lit_count).charAt(1) == 'C') {
							literaltabList.get(pbc).locationList.add(locctr);
							literaltabList.get(pbc).size.add(literaltabList.get(pbc).label.get(lit_count).length() - 4);
							locctr += literaltabList.get(pbc).label.get(lit_count).length() - 4;
							literaltabList.get(pbc).type.add(0);

						} else {
							literaltabList.get(pbc).locationList.add(locctr);
							literaltabList.get(pbc).size.add(1);
							literaltabList.get(pbc).type.add(1);
							locctr += 1;
						}
						literaltabList.get(pbc).label.set(lit_count, literaltabList.get(pbc).label.get(lit_count)
								.substring(3, literaltabList.get(pbc).label.get(lit_count).length() - 1));
					}
					program_length.add(locctr - start_addr.get(pbc));
					pbc++;

					symtabList.add(new LabelTable());
					literaltabList.add(new LabelTable());
					TokenList.add(new TokenTable(symtabList.get(pbc), literaltabList.get(pbc), instTable));
					extDef.add(new HashMap<String, Integer>());
					extRef.add(new ArrayList<String>());
					TokenList.get(pbc).connect_exts(extDef.get(pbc), extRef.get(pbc));
					current_extDef.clear();

					locctr = 0;
					start_addr.add(0);
					lit_count = 0;

					if (token.label != null && token.label.length() > 0) {
						symtabList.get(pbc).putName(token.label, locctr);
					}

					s = i.next();
					continue;

				} else if (token.operator.equals("LTORG")) {
					for (; lit_count < literaltabList.get(pbc).label.size(); lit_count++) {
						int add_val = 0;
						if (literaltabList.get(pbc).label.get(lit_count).charAt(1) == 'C') {
							literaltabList.get(pbc).locationList.add(locctr);
							literaltabList.get(pbc).size.add(literaltabList.get(pbc).label.get(lit_count).length() - 4);
							locctr += literaltabList.get(pbc).label.get(lit_count).length() - 4;
							literaltabList.get(pbc).type.add(0);

						} else {
							literaltabList.get(pbc).locationList.add(locctr);
							literaltabList.get(pbc).size.add(1);
							literaltabList.get(pbc).type.add(1);
							locctr += 1;
						}
						literaltabList.get(pbc).label.set(lit_count, literaltabList.get(pbc).label.get(lit_count)
								.substring(3, literaltabList.get(pbc).label.get(lit_count).length() - 1));

					}
					s = i.next();
					continue;
				} else if (token.operator.equals("EQU")) {
					int addr = -1;
					if (token.operand[0].equals("*")) {
						addr = locctr;
					} else if (token.operand[0].contains("+")) {

						String[] val = token.operand[0].split("+");
						addr = symtabList.get(pbc).search(val[0]);
						for (int val_idx = 1; val_idx < val.length; val_idx++)
							addr += symtabList.get(pbc).search(val[val_idx]);
					} else if (token.operand[0].contains("-")) {
						String[] val = token.operand[0].split("-");
						addr = symtabList.get(pbc).search(val[0]);
						for (int val_idx = 1; val_idx < val.length; val_idx++)
							addr -= symtabList.get(pbc).search(val[val_idx]);
					} else {
						addr = symtabList.get(pbc).search(token.operand[0]);
					}
					symtabList.get(pbc).locationList.set(symtabList.get(pbc).label.indexOf(token.label), addr);
					s = i.next();
					continue;
				} else {
					System.out.println("Invalid OPCODE");
					System.exit(2);
				}

				TokenList.get(pbc).tokenList.add(token);
			}

			s = i.next();

		}

		Token token = new Token(s);
		
		end_operand = token.operand[0];

		Iterator ite = current_extDef.iterator();
		while (ite.hasNext()) {
			String now_def = (String) ite.next();

			int addr = symtabList.get(pbc).search(now_def);
			extDef.get(pbc).put(now_def, addr);
		}

		for (; lit_count < literaltabList.get(pbc).label.size(); lit_count++) {
			int add_val = 0;
			if (literaltabList.get(pbc).label.get(lit_count).charAt(0) == 'C') {
				literaltabList.get(pbc).locationList.add(locctr);
				literaltabList.get(pbc).size.add(literaltabList.get(pbc).label.get(lit_count).length() - 4);
				locctr += literaltabList.get(pbc).label.get(lit_count).length() - 4;
				literaltabList.get(pbc).type.add(0);

			} else {
				literaltabList.get(pbc).locationList.add(locctr);
				literaltabList.get(pbc).size.add(1);
				literaltabList.get(pbc).type.add(1);
				locctr += 1;
			}
			literaltabList.get(pbc).label.set(lit_count, literaltabList.get(pbc).label.get(lit_count).substring(3,
					literaltabList.get(pbc).label.get(lit_count).length() - 1));
		}

		program_length.add(locctr - start_addr.get(pbc));

		pbc++;
		// TODO Auto-generated method stub

	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub

		try {
			File f = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));

			for (int i = 0; i < pbc; i++) {
				Iterator ite = symtabList.get(i).label.iterator();
				int idx = 0;
				while (ite.hasNext()) {
					bw.write(String.format("%s\t\t%X\n", symtabList.get(i).label.get(idx),
							symtabList.get(i).locationList.get(idx)));
					idx++;
					ite.next();
				}
				bw.write("\n");
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		try {
			File f = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));

			for (int i = 0; i < pbc; i++) {
				Iterator ite = literaltabList.get(i).label.iterator();
				int idx = 0;
				while (ite.hasNext()) {
					bw.write(String.format("%s\t\t%X\n", literaltabList.get(i).label.get(idx),
							literaltabList.get(i).locationList.get(idx)));
					idx++;
					ite.next();
				}
				bw.write("\n");
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * pass2 과정을 수행한다.
	 * 
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {

		int pbc_i = 0;

		String obj_code = "";

		for (pbc_i = 0; pbc_i < pbc; pbc_i++) {
			// header record
			// init first text record

			for (int i = 0; i < TokenList.get(pbc_i).tokenList.size(); i++) {
				Token t = TokenList.get(pbc_i).tokenList.get(i);
				if (t.operator.equals("BASE")) {
					Assembler.base_register = TokenList.get(pbc_i).symTab.search(t.operand[0]);
				} else if (t.operator.equals("NOBASE")) {
					Assembler.base_register = -1;
				} else {
					TokenList.get(pbc_i).makeObjectCode(i);
				}

			}

		}

		for (pbc_i = 0; pbc_i < pbc; pbc_i++) {
			// header record
			// init first text record
			codeList.add(String.format("H%s\t%06X%06X", prog_name.get(pbc_i), start_addr.get(pbc_i),
					program_length.get(pbc_i)));
		//	System.out.println(String.format("H%s\t%06X%06X", prog_name.get(pbc_i), start_addr.get(pbc_i),
				//	program_length.get(pbc_i)));

			Iterator ite = extDef.get(pbc_i).keySet().iterator();

			String tmp = "D";
			boolean ext_avail = false;
			while (ite.hasNext()) {
				ext_avail = true;
				String key = (String) ite.next();
				tmp += String.format("%s%06X", key, extDef.get(pbc_i).get(key));
			}

			if (ext_avail) {
				codeList.add(tmp);
			//	System.out.println(tmp);
			}

			ite = extRef.get(pbc_i).iterator();
			tmp = "R";
			ext_avail = false;
			while (ite.hasNext()) {
				ext_avail = true;

				tmp += String.format("%-6s", ite.next());
			}
			if (ext_avail) {
				codeList.add(tmp);
				//System.out.println(tmp);
			}

			int written = 0;
			int begin_addr = 0;
			for (int i = 0; i < TokenList.get(pbc_i).tokenList.size(); i++) {

				Token t = TokenList.get(pbc_i).tokenList.get(i);

				// System.out.println(t.label+"\t"+t.operator+"\t"+t.objectCode+"\t"+t.location);

				if (written + t.byteSize > 0x1D) {
					codeList.add(String.format("T%06X%02X%s", begin_addr, written, tmp));
				//	System.out.println(String.format("T%06X%02X%s", begin_addr, written, tmp));
					written = 0;
				}

				if (written == 0) {
					begin_addr = t.location;
					tmp = "";
				}

				if (t.byteSize <= 3) {
					tmp += String.format("%s", t.objectCode);
				} else {
					tmp += String.format("%s", t.objectCode);
				}

				written += t.byteSize;

			}

			if (literaltabList.get(pbc_i).label.size() > 0) {
				for (int i = 0; i < literaltabList.get(pbc_i).label.size(); i++) {

					

					String label = literaltabList.get(pbc_i).label.get(i);
					int addr = literaltabList.get(pbc_i).locationList.get(i);

					int size = literaltabList.get(pbc_i).size.get(i);
					int type = literaltabList.get(pbc_i).type.get(i);

					if (written + size > 0x1D) {
						codeList.add(String.format("T%06X%02X%s", begin_addr, written, tmp));
						//System.out.println(String.format("T%06X%02X%s", begin_addr, written, tmp));
						written = 0;
					}

					if (written == 0) {
						begin_addr = addr;
						tmp = "";
					}
					if(type==0)
					{
						String ascii="";
						int string_i=0;
						for(string_i=0;string_i<label.length();string_i++)
						{
							ascii += String.format("%X", (int)label.charAt(string_i));
							
						}
						tmp += ascii;
					}
					else
					{
						tmp += label;
						
					}

					written += size;

				}

			}
			codeList.add(String.format("T%06X%02X%s", begin_addr, written, tmp));
		//	System.out.println(String.format("T%06X%02X%s", begin_addr, written, tmp));

			if(TokenList.get(pbc_i).modRecord.size()>0)
			{
				for (int i = 0; i < TokenList.get(pbc_i).modRecord.size(); i++) 
				{
					MRecord mr =  TokenList.get(pbc_i).modRecord.get(i);
					codeList.add(String.format("M%06X%02X%C%s",
							mr.addr,mr.length,mr.modification_flag,mr.symbol));
					
				//	System.out.println(String.format("M%06X%02X%C%s",
						//	mr.addr,mr.length,mr.modification_flag,mr.symbol));
				}
			}
			
			if(pbc_i==0)
			{
				
				int end_val = 0;
				for(int k=0;k<pbc;k++)
				{
					if(symtabList.get(k).search(end_operand)>=0)
					{
						end_val = symtabList.get(k).search(end_operand);
						break;
					}
						
				}
				codeList.add(String.format("E%06X",end_val));
			}
			else
			{
				codeList.add("E");
			}
			codeList.add("");

		}
		// TODO Auto-generated method stub

	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		
		
		// TODO Auto-generated method stub
		
		try {
			File f = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));

			Iterator i = codeList.iterator();
			while(i.hasNext())
			{
				String s = (String) i.next();
				bw.write(s);
				bw.write("\n");
			}
	
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
