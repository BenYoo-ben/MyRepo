package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다.
 * 
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다. - program code를 메모리에 적재시키기 - 주어진 공간만큼 메모리에 빈
 * 공간 할당하기 - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;

	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * 
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr = resourceManager;
	}

	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록
	 * 한다. load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * 
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode) {
		FileInputStream fis;

		char[] data = new char[(int) objectCode.length()];
		int prog_start_addr = 0;
		int last_addr = 0;
		if (objectCode != null)
			try {
				fis = new FileInputStream(objectCode);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String s;
				while ((s = br.readLine()) != null) {
					if (s.length() <= 0)
						continue;
					char first_char = s.charAt(0);

					switch (first_char) {
					case 'H':
						prog_start_addr = last_addr;
						String prog_name = s.substring(1, 7);
						int start_addr = Integer.parseInt(s.substring(7, 13), 16);
						int prog_length = Integer.parseInt(s.substring(13, 19), 16);

						System.out.println(prog_name + "^");
						System.out.println(start_addr + "^");
						System.out.println(prog_length + "^");
						System.out.println();
						rMgr.program_names.add(prog_name);
						rMgr.program_lengths.add(prog_length);
						rMgr.starting_addresses.add(start_addr);

						break;
					case 'D':
						int num = (s.length()-1)/12;
						
						int cursor=1;
						for(int i=0;i<num;i++)
						{
							String symbol_name = s.substring(cursor,cursor+6);
							cursor+=6;
							int symbol_addr = Integer.parseInt(s.substring(cursor,cursor+6),16);
							cursor+=6;
							
							System.out.println(symbol_name);
							System.out.println(symbol_addr);
							System.out.println();
							rMgr.symtabList.putSymbol(symbol_name, symbol_addr);
						}
						
					
						break;
					case 'R':
						break;
					case 'M':
						int modify_addr = Integer.parseInt(s.substring(1,7),16);
						int modify_len = Integer.parseInt(s.substring(7,9),16);
						boolean modify_flag = true;
							if(s.charAt(9)=='-')
								modify_flag = false;
						int modify_value = rMgr.symtabList.search(s.substring(10,16));
						/*
						 * need modification on here. Decode Value and do arithmetic on addr.
						 */
							
						
						break;
					case 'T':
						int start_addr_T = Integer.parseInt(s.substring(1, 7), 16);
						

						int len = Integer.parseInt(s.substring(7, 9), 16);
						System.out.println("length : "+len);
						
						char[]data_T = s.substring(9).toCharArray();
						rMgr.setMemory(prog_start_addr+start_addr_T,data_T,len);
						
						last_addr = start_addr_T;

						break;
					case 'E':
						if (s.length() > 1)
							rMgr.first_executable_addr = Integer.parseInt(s.substring(1), 16);
						break;
					default:
						break;

					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
