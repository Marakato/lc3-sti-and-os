	.ORIG x3000
	AND R0,R0,#0

	LEA R0, STRS

	TRAP x22
	
	TRAP x0
	RTI

	HALT

STRS	.STRINGZ "user program!\n"
	.END