package jkw_lc3;

public class LC3EnumType {
	public static class IOPort {
		// keyboard
		public final static short KBSR=(short) 0xFE00; //FE00
		public final static short KBDR=(short) 0xFE02; //FE02
		// screen output
		public final static short DSR=(short) 0xFE04; //FE04
		public final static short DDR=(short) 0xFE06; // FE06
		
		public final static short CLOCK=(short) 0xFFFE;
	}
	
	public static class ErrCode {
		public final static short Normal=0;
		public final static short OpenfileError=1;
		public final static short User_RTI=2;
	}
	
	public static class Trap0 {
		public final static short LOADFILEA=0;
		public final static short LOADFILEB=1;
		public final static short CHANGERTITYPE=2;
		public final static short CLS=3;
		public final static short KERNELPUSH=4;
		public final static short KERNELPOP=5;
		public final static short PRINT_ERROR=6;
	}
	
	public static class Int {
		private final static short beg=0x0100;
		public final static short KERNEL_PANIC=beg+0;
		public final static short USER_RTI=beg+1;
		public final static short KEYBOARD=beg+0x80;
		public final static short OPENFILEERROR=beg+2;
	}
	
	public static class Operand {
		public final static short ADD=1;
		public final static short AND=5;
		public final static short BR=0;
		public final static short JMP=12;
		public final static short JSR_R=4;
		public final static short LD=2;
		public final static short LDI=10;
		public final static short LDR=6;
		public final static short LEA=14;
		public final static short NOT=9;
		public final static short RTI=8;
		public final static short ST=3;
		public final static short STI=11;
		public final static short STR=7;
		public final static short TRAP=15;
		
		// CoCalc
		public final static short FCALC=13;
	}
	
}
