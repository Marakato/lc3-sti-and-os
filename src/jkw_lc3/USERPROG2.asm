	.ORIG x3000

PROGSTART
	AND R0,R0,#0
	AND R1,R1,#0
	ST R0,BUFFID
	LD R6,STACKB
	LD R5,STACKB ; SP=BP=STACKB

;READSTR
AGAIN	IN
	LEA R3,BUFFER
	ADD R1,R0,#0
	ADD R1,R1,#-10
	BRz IS_ENTER
	ADD R1,R0,#0
	ADD R1,R1,#-8
	BRz IS_BACK
	LD R2,BUFFID
	ADD R2,R3,R2
	STR R0,R2,#0
	LD R2,BUFFID
	ADD R2,R2,#1
	ST R2,BUFFID
	BRnzp AGAIN

IS_BACK:
	LD R2,BUFFID
	ADD R2,R2,#0
	BRnz CANTBACK
	ADD R2,R2,#-1
	ST R2,BUFFID
CANTBACK
	BRnzp AGAIN

IS_ENTER
	; NOW START SORTING
	LD R2,BUFFID
	LEA R3,BUFFER
	ADD R2,R2,R3
	AND R0,R0,#0
	STR R0,R2,#0

	ADD R0,R0,R3
	LD R1,BUFFID
	ADD R0,R0,R1
	STR R0,R6,#-1 ;PUSH BUFFER+LEN
	AND R0,R0,#0
	ADD R0,R0,R3

	STR R0,R6,#-2 ;PUSH BUFFER
	ADD R6,R6,#-2 ; SP-=2
	LD R0,QUICK_SORT_ADDR
	JSRR R0
	LEA R0,BUFFER
	PUTS
	LEA R0,AGAINSTR
	PUTS
	IN
	ADD R0,R0,#-10
	BRz PROGSTART
	LEA R0,EXITSTR
	PUTS
	HALT

QUICK_SORT_ADDR .FILL QUICK_SORT
	
STACKB	.FILL x6000

BUFFER	.BLKW #100
BUFFID	.FILL x0
AGAINSTR .STRINGZ "\nInput again? Enter(Continue) or Other(Exit)\n"

EXITSTR	.STRINGZ "Exit, Thank you~\n"


; ARGUMENT: BEGIN, END
;	    BP(R5)+2,+3
PARTITION
	STR R7,R6,#-1
	STR R5,R6,#-2
	ADD R6,R6,#-2
	ADD R5,R6,#0 ; PUSH PC,BP MOV BP,SP
	ADD R6,R6,#-3 ; ITER,PIVOT,TEMP
		      ; BP-1,-2,-3
	LDR R0,R5,#2
	ADD R0,R0,#-1
	STR R0,R5,#-1
	
	LDR R0,R5,#3
	ADD R0,R0,#-1
	LDR R1,R0,#0
	STR R1,R5,#-2
WHILELOOP	
	LDR R0,R5,#2
	LDR R1,R5,#3
	ADD R1,R1,#-1
	NOT R1,R1
	ADD R1,R1,#1
	ADD R0,R0,R1
	BRzp ENDWHILE
; WHILE
	LDR R0,R5,#2
	LDR R1,R0,#0
	LDR R2,R5,#-2
	NOT R2,R2
	ADD R2,R2,#1
	ADD R1,R1,R2
	BRzp NOTIF
	LDR R3,R5,#-1
	ADD R3,R3,#1
	STR R3,R5,#-1
	; SWAP *R3,*R0
	LDR R2,R0,#0
	LDR R1,R3,#0
	STR R1,R0,#0
	STR R2,R3,#0

NOTIF	ADD R0,R0,#1
	STR R0,R5,#2
	BRnzp WHILELOOP

ENDWHILE
	LDR R0,R5,#-1
	LDR R1,R0,#1
	LDR R2,R5,#3
	STR R1,R2,#-1
	LDR R3,R5,#-2
	STR R3,R0,#1
	ADD R0,R0,#1

LEAVEPART
	LDR R7,R5,#1
	ADD R6,R5,#2
	LDR R5,R5,#0
	RET
	

QUICK_SORT
	STR R7,R6,#-1
	STR R5,R6,#-2 
	ADD R6,R6,#-2 ; PUSH PC,BP
	ADD R5,R6,#0
	; CONTROL: OLD_BP: R5+0 PC: R5+1
	; ARGUMENT: R5+2,begin   R5+3,end
	ADD R6,R6,#-1
	;LOCAL TEMP R5,#-1

	LDR R0,R5,#2
	LDR R1,R5,#3
	NOT R1,R1
	ADD R1,R1,#1
	ADD R0,R0,R1
	BRz OUT_RECUR ; BEGIN==END
	ADD R0,R0,#1
	BRz OUT_RECUR ; BEGIN==END-1
	
	LDR R0,R5,#2
	LDR R1,R5,#3
	STR R1,R6,-1
	STR R0,R6,-2
	ADD R6,R6,#-2
	JSR PARTITION ;RETURN TO R0
	STR R0,R5,#-1
	LDR R1,R5,#2 ;LOAD BEGIN
	STR R0,R6,#1 ;STORE TEMP
	STR R1,R6,#0 ;STORE BEGIN
	JSR QUICK_SORT

	LDR R0,R5,#3 ;LOAD END
	LDR R1,R5,#-1 ;LOAD TEMP
	ADD R1,R1,#1

	STR R1,R6,#0 ;STORE TEMP+1
	STR R0,R6,#1 ;STORE END
	JSR QUICK_SORT


OUT_RECUR
	LDR R7,R5,#1
	ADD R6,R5,#2
	LDR R5,R5,#0
	RET

	.END