package jkw_lc3;

import java.io.*;

public class LC3VirtualMachine {
	
	
	private int unsign(short a) {
		int temp=a&0x0000FFFF;
		return temp;
	}
/*
 * 
 * IO&Screen
 * 
 */
	// screen input
	private LC3GUI gui = new LC3GUI();
	//////
	
	private void IO() {
		if(gui.keydown) {
			gui.keydown=false;
			short temp=memory[ unsign(LC3EnumType.IOPort.KBSR) ]|=0x8000;
			memory[ unsign(LC3EnumType.IOPort.KBDR) ]=(short)gui.inputchar;
			if( (temp&0x4000) != 0) {
				this.interrupt(1, LC3EnumType.Int.KEYBOARD );
				//mark
			}
		}
		
		if(memory[ unsign(LC3EnumType.IOPort.DSR) ] < 0) {
			memory[ unsign(LC3EnumType.IOPort.DSR) ]&=0x7FFF;
			gui.printc( (char) memory[ unsign(LC3EnumType.IOPort.DDR) ] );
		}
		
	}
	
/*
 * 
 *	Kernel Stack 
 *
*/
	private short kernelstack[];
	private short ksp;
	private void kernel_push(short a) {
		++ksp;
		kernelstack[ ksp ]=a;
	}
	private short kernel_pop() {
		--ksp;
		return kernelstack[ ksp+1 ];
	}
	private void kernel_call(short vec) {
		kernel_push(psr);
		kernel_push(pc);
		pc=memory[unsign(vec)];
	}
	private void kernel_ret() {
		pc=kernel_pop();
		psr=kernel_pop();
	}
	private void kernel_change_ret_type() {
		--kernelstack[ksp];
	}
/*
 * 
 * Trap0
 * 
 */
	private void trap0() {
		short id=reg[0];
		StringBuilder filepath=new StringBuilder();
		switch(id) {
		case LC3EnumType.Trap0.LOADFILEA:
		case LC3EnumType.Trap0.LOADFILEB:
			short file_addr=reg[1];
			while(memory[ unsign(file_addr) ]>0) {
				filepath.append( (char)(memory[ unsign(file_addr) ]));
				++file_addr;
			}
			try {
				if(id==1)
					reg[2]=LoadBinFile(filepath.toString());
				else
					reg[2]=LoadASCIIFile(filepath.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				reg[2]=0;
				seterrorcode((short)1, "OpenFile: "+filepath.toString()+" Error!\n");
				interrupt(2, LC3EnumType.Int.OPENFILEERROR);
				//e.printStackTrace();
			}
			break;
		case LC3EnumType.Trap0.CHANGERTITYPE:
			kernel_change_ret_type();
			break;
		case LC3EnumType.Trap0.CLS:
			gui.cls();
			break;
		case LC3EnumType.Trap0.KERNELPUSH:
			kernel_push(reg[1]);
			break;
		case LC3EnumType.Trap0.KERNELPOP:
			reg[1]=kernel_pop();
			break;
		case LC3EnumType.Trap0.PRINT_ERROR:
			gui.printstr(ErrorStr);
			break;
		}
		return;	
	}

/*
 * 
 *	Normal Regs and Mems and Flags 
 *
*/
	
	private short memory[];
	private short reg[];
	
	private short pc;
	private short psr; //  15	14	13	12	11	10	9	8	7	6	5	4	3	2	1	0
					 //user/sys						ErrCode-----	IntLevel	N	Z	P

	private String ErrorStr;
	
/*
 * 
 * Get Opcodes, Operands, Offset, etc.
 * 
 */
	private short getargOp(short ir) {
		int temp=ir&(0xF000);
		temp=temp>>>12;
		return (short)temp;
	}
	// Calc
	private short getargDes(short ir) {
		short temp=(short)(ir&(0x0E00));
		temp=(short)(temp>>>9);
		return temp;
	}
	private short getargSrc1(short ir) {
		short temp=(short)(ir&(0x01C0));
		temp=(short)(temp>>>6);
		return temp;
	}
	private short getargSrc2(short ir) {
		short temp=(short)(ir&(0x0007));
		return temp;
	}
	private boolean is_rtype(short ir) {
		short temp=(short)(ir&(0x0020));
		return temp==0;
	}
	private short get_imm5(short ir) {
		short temp=(short)(ir&0x001F);
		temp=(short) (temp<<11);
		temp=(short) (temp>>11);
		return temp;
	}
	// Load/Store
	private short get_offset9(short ir) {
		short temp=(short)(ir&0x01FF);
		temp=(short) (temp<<7);
		temp=(short) (temp>>7);
		return temp;
	}
	private short get_offset6(short ir) {
		short temp=(short)(ir&0x003F);
		temp=(short) (temp<<10);
		temp=(short) (temp>>10);
		return temp;
	}
	//Branch & Jump
	private boolean will_jump(short ir) {
		short nzp=(short) (psr&(0x07));
		short to_check=(short)(ir&(0x0E00));
		to_check=(short) (to_check>>>9);
		short temp=(short) (nzp&to_check);
		if(temp==0) return false;
		else return true;
	}
	private short get_jump_reg(short ir) {
		short temp=(short)(ir&0x01C0);
		temp=(short) (temp>>>6);
		return temp;
	}
	private boolean is_jsr(short ir) {
		short temp=(short)(ir&0x0800);
		return temp!=0;
	}
	private short get_jsr_offset11(short ir) {
		short temp=(short)(ir&0x07FF);
		temp=(short)(temp<<5);
		temp=(short)(temp>>5);
		return temp;
	}
	//Trap
	private short get_trap_vec(short ir) {
		short temp=(short)(ir&0x00FF);
		return temp;
	}
	

/*
 * 
 * Error Codes
 * 	
 */
	
	private int geterrorcode() {
		int tp=psr&0x03B0;
		tp=tp>>>6;
		return tp;
	}
	private void seterrorcode(short code,String str) {
		short tp=(short) (code&0x0F);
		
		tp=(short)(tp<<6);
		psr=(short)(psr&0xFC3F);
		psr=(short)(psr|tp);

		ErrorStr=str;
		return;
	}
	
	
/*
 * 
 * User Or System
 * 	
 */	
	private boolean is_super() {
		return psr>=0;
	}
	private void set_super() {
		psr=(short) (psr&0x7FFF);
	}
	private void set_user() {
		psr=(short)(psr|0x8000);
	}
/*
 * 
 * Interrupt
 * 
 */
	
	public int getintlevel() {
		int tp=psr&0x0038;
		tp=tp>>>3;
		return tp;
	}
	public void setintlevel(int level) {
		int tp=psr&0xffc7;
		level=level&7;
		level=level<<3;
		tp=tp|level;
		psr=(short)(tp);
	}
	
	public void interrupt(int level,short vector) {
		if(level==7) {
			gui.printstr(ErrorStr.toString());
			memory[ unsign(LC3EnumType.IOPort.CLOCK) ]&=0x7FFF;
			return;
		}
		if(getintlevel()>level) {
			return;
		}
		setintlevel(level);
		kernel_call(vector);
		set_super();
		return;
	}
	

/*
 * 
 * SetCC
 * 
 */
	private void setcc(short ans) {
		if(ans>0) {
			psr=(short) (psr&0xFFF8);
			psr=(short) (psr|0x1);
		} else if(ans==0) {
			psr=(short) (psr&0xFFF8);
			psr=(short) (psr|0x2);			
		} else {
			psr=(short) (psr&0xFFF8);
			psr=(short) (psr|0x4);			
		}
		return;
	}	
	
/*
 * 
 * Constructor
 * 
 */
	private short LoadBinFile(String path) throws IOException {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(".\\src\\jkw_lc3\\"+path)));
		
		if(in.available()==0) {
			in.close();
			return 0;
		}
		
		short s,t;
		s=in.readShort();
		t=s;
		while(in.available()!=0) {
			memory[ unsign(s) ]=in.readShort();
			++s;
		}
		in.close();
		return t;	
	}
	private short LoadASCIIFile(String path) throws IOException,NumberFormatException {
		BufferedReader in = new BufferedReader(new FileReader(".\\src\\jkw_lc3\\"+path));
		String line=in.readLine();
		short s=0;
		s=Short.parseShort(line);
		if(s==0) {
			in.close();
			return 0;
		}
		short t=s;
		
		line=in.readLine();
		while(line!=null) {
			short ir=Short.valueOf(line,16);
			memory[ unsign(s) ]=ir;
			++s;
			line=in.readLine();
		}
		in.close();
		return t;
	}
	
	private LC3VirtualMachine(boolean DEBUG) {
		short st=0;
		
		memory=new short[(1<<16)];
		reg=new short[8];
		kernelstack=new short[1<<8];
		ksp=0;
		
		setcc((short) 0);
		set_super();
		
		if(!DEBUG) {
			try {
				st=LoadBinFile("os.obj");
			} catch(IOException e) {
				//e.printStackTrace();
				seterrorcode(LC3EnumType.ErrCode.OpenfileError,"Can't find OS!\n");
				interrupt(7,LC3EnumType.Int.KERNEL_PANIC);
				return;
			}
			if(st==0) {
				seterrorcode(LC3EnumType.ErrCode.OpenfileError,"OS File Error!\n");
				interrupt(7,LC3EnumType.Int.KERNEL_PANIC);
				return;
			}
			pc=st;
			
			memory[ unsign(LC3EnumType.IOPort.CLOCK) ]=(short) 0x8000;
			run();
		}
	}
	
	
	public void interpret_one(short ir) {
		short oper=getargOp(ir);
		short reg1,reg2,regimm,regdes,regbase;
		short pctemp;
		
		
		switch(oper) {
		case LC3EnumType.Operand.ADD:
			reg1=getargSrc1(ir);
			if(is_rtype(ir)) {
				regdes=getargDes(ir);
				reg2=getargSrc2(ir);
				reg[regdes]=(short) (reg[reg1]+reg[reg2]);
				setcc(reg[regdes]);
				break;
			}
			else {
				regdes=getargDes(ir);
				regimm=get_imm5(ir);
				reg[regdes]=(short) (reg[reg1]+regimm);
				setcc(reg[regdes]);
				break;
			}
		case LC3EnumType.Operand.AND:
			reg1=getargSrc1(ir);
			if(is_rtype(ir)) {
				regdes=getargDes(ir);
				reg2=getargSrc2(ir);
				reg[regdes]=(short) (reg[reg1]&reg[reg2]);
				setcc(reg[regdes]);
				break;
			}
			else {
				regdes=getargDes(ir);
				regimm=get_imm5(ir);
				reg[regdes]=(short) (reg[reg1]&regimm);
				setcc(reg[regdes]);
				break;
			}
		case LC3EnumType.Operand.BR:
			if(will_jump(ir)) {
				pc=(short) (pc+get_offset9(ir));
			}
			break;
			
		case LC3EnumType.Operand.JMP:
			short reg_target=get_jump_reg(ir);
			pc=reg[reg_target];
			break;
			
		case LC3EnumType.Operand.JSR_R:
			if(is_jsr(ir)) {
				short reg_off=get_jsr_offset11(ir);
				reg[7]=pc;
				pc=(short) (pc+reg_off);
			}
			else {
				short jsr_target=get_jump_reg(ir);
				short targg=reg[jsr_target];
				reg[7]=pc;
				pc=targg;
			}
			break;
		case LC3EnumType.Operand.LD:
			regdes=getargDes(ir);
			pctemp=(short) (pc+get_offset9(ir));
			reg[regdes]=memory[unsign(pctemp)];
			break;
		case LC3EnumType.Operand.ST:
			regdes=getargDes(ir);
			pctemp=(short) (pc+get_offset9(ir));
			memory[(int)pctemp]=reg[regdes];
			break;
			
		case LC3EnumType.Operand.LDI:
			regdes=getargDes(ir);
			pctemp=(short) (pc+get_offset9(ir));
			reg[regdes]=memory[ unsign(memory[ unsign(pctemp)]) ];
			break;
		case LC3EnumType.Operand.STI:
			regdes=getargDes(ir);
			pctemp=(short) (pc+get_offset9(ir));
			memory[ unsign(memory[unsign(pctemp)]) ]=reg[regdes];
			break;
			
		case LC3EnumType.Operand.LDR:
			regdes=getargDes(ir);
			regbase=getargSrc1(ir);
			reg[regdes]=memory[ unsign((short) (reg[regbase] +get_offset6(ir))) ];
			break;
		case LC3EnumType.Operand.STR:
			regdes=getargDes(ir);
			regbase=getargSrc1(ir);
			memory[ unsign((short) (reg[regbase] +get_offset6(ir))) ]=reg[regdes];
			break;
		case LC3EnumType.Operand.LEA:
			regdes=getargDes(ir);
			reg[regdes]= (short) (pc+get_offset9(ir));
			break;
		case LC3EnumType.Operand.NOT:
			regdes=getargDes(ir);
			reg1=getargSrc1(ir);
			reg[regdes]=(short) ~reg[reg1];
			setcc(reg[regdes]);
			break;
			
			
		case LC3EnumType.Operand.RTI:
			if(is_super()) {
				//MARK
				kernel_ret();
			}
			else {
				seterrorcode(LC3EnumType.ErrCode.User_RTI,"Cannot RTI From User Level!\n");
				interrupt(6,LC3EnumType.Int.USER_RTI);
			}
			break;
		case LC3EnumType.Operand.TRAP:
			short trapvec=get_trap_vec(ir);
			if(trapvec==0) {
				//MARK
				if( is_super() ) {
					trap0();
				}
				else {
					gui.printstr("User Cannot Use Trap0!\n");
				}
			}
			else {
				kernel_call(trapvec);
				set_super();
			}
		}
		
	}
	
	
	public void run() {
		while(memory[ unsign(LC3EnumType.IOPort.CLOCK) ]<0) {
			/*
			 * Firstly, run code
			 */
			short ir=memory[ unsign(pc) ];
			++pc;
			//

			interpret_one(ir);
			/*
			 * Secondly, deal with interrupt and IO
			 */
			IO();
			
		}
	}
	
	public static void main(String[] arg) {
		LC3VirtualMachine lc3 = new LC3VirtualMachine(false);
		lc3.run();
	}
}
