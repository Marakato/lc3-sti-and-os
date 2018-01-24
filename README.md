# lc3-sti-and-os

## 1. 简介

*Introduction to Computing System from bits&gates to C&beyond* (《计算机系统概论》) 一书里面提出的机器。

Java语言编写。

## 2. 文件说明

.asm后缀的为lc3汇编。.obj是编译出来的二进制代码。

os为Java模拟器启动后立刻读入的程序。os会调用_trapvec与_intvec来初始化陷阱向量与中断向量。初始化完毕后会读入命令并且执行。逻辑是不停读取字符串然后与所有命令比较。

USERINT(用户使用键盘中断功能) USERPROG1(Hello World与异常中断) USERPROG2(基于栈与递归的字符数组的快速排序) 是演示用的小程序。通过往os内输入命令来读入、运行。

LC3EnumType.java 存储所有常量值。

LC3GUI.java 负责界面部分。所有的按键将写到虚拟机指定的内存地址内(IO-Map)

LC3VirtualMachine.java 一开始读入os到内存，然后不断开始执行字节码。

## 3. ISA

DR: 3位，目标寄存器

SR(1/2)：3位，源寄存器

imm：立即数，带符号；后跟的数字表示比特数。

(PC)offset：偏移量，带符号；后跟的数字表示比特数。

ADD:
0001 DR SR1 000 SR2
0001 DR SR1 1 imm5

AND:
0101 DR SR1 000 SR2
0101 DR SR1 1 imm5

BR:(Branch if negative/zero/positive) 将PSR标志寄存器对应位与此处nzp进行与操作，如果为1则跳转。
0000 nzp PCoffset9

JMP:
1100 000 SR 000000

JSR:(reg[7]=pc'; pc<-pc'+PCoffset11) 相当于函数调用，要把返回地址存在R7内
0100 1 PCoffset11

JSRR:(temp=reg[SR]; reg[7]=pc'; pc<-temp) 相当于函数调用，要把返回地址存在R7内。需要temp的原因是有可能 JSRR R7
0100 000 SR 000000

LD: (reg[DR]=mem[pc'+PCoffset9])
0010 DR PCoffset9

LDI:(reg[DR]=mem[ mem[pc'+PCoffset9] ])
1010 DR PCoffset9

LDR:(reg[DR]=mem[SR+offset6])
0110 DR SR offset6

LEA:(reg[DR]=pc'+PCoffset9)
1110 DR PCoffset9

NOT:
1001 DR SR 111111

RET:(JMP的特殊版)
1100 000 111 000000

RTI:(中断返回)
1000 000000000000

ST: (store系列指令，与上面load系列相对应)
0011 SR PCoffset9

STI:
1011 SR PCoffset9

STR:
0111 SR BaseR offset6

TRAP:
1111 0000 trapvect8

RESERVED: 未定义，不使用。
1101 xxxxxxxxxxxx

x20 getc 获取字符，不回显
x21 out 将reg[0]低8位打印到屏幕
x22 puts 将reg[0]指向的字符串打印到屏幕。每个字长(16字节)存储一个ASCII字符。
x23 in 获取字符并回显
x24 putsp 将reg[0]指向的字符串打印到屏幕。每个字长(16字节)存储两个ASCII字符。
x25 halt 停机

Mem[0xFE00] KBSR 键盘状态寄存器(最高位指示是否有字符，次高位指示是否允许中断)

Mem[0xFE02] KBDR 键盘数据寄存器

Mem[0xFE04] DSR (输出给屏幕)状态寄存器

Mem[0xFE06] DSR (输出给屏幕)数据寄存器

Mem[0xFFFE] Clock 是否停机

<br><br><br>

## 4. 与原书不同的地方

Trap 不使用reg[7]当作返回地址寄存器，而是使用内核栈。

Trap后会使权限从用户级变为系统级(相当于陷回操作系统)，所以需要RTI返回。

PSR标志寄存器布局：

15		14	13	12	11	10	9	8	7	6	5	4	3	2	1	0

user/sys						ErrCode-----	IntLevel	N	Z	P

15为用户级(1)系统级(0)，8765为错误码，432为中断权限级，210为nzp(上次数值运算后正负还是0，置相应位为1)


## 5. 实现情况：

操作系统指令：

cls  清屏

halt 停机

run  执行程序(先输入run，回车，再输入文件名，回车。)

操作系统内存布局：

0x0000~0x00FF 陷阱矢量

0x0100~0x01FF 中断矢量

0x0200~0x1000 操作系统实现部分

0x8000~0xFFFF io-map

Trap x24未实现

Trap x25 具体实现是程序执行完毕后重新将执行权力交还操作系统。

一开始的权限是系统级，操作系统初始化完毕后，当用户要求执行用户程序时，操作系统先构造PSR和"返回地址"，PSR当然是权限为用户级权限，然后通过RTI进入用户级程序。用户执行完后，调用Trap x25回到操作系统。




其他实现：

Trap x0 本地方法。reg[0]为调用哪个本地方法。

reg[0]=0 loadfilea 读取reg[1]指向的字符串为路径的ASCII文件，每一行为4个16进制的ASCII字符。该文件在内存位置的首地址存到reg[2]中。

reg[0]=1 loadfileb 读取reg[1]指向的字符串为路径的二进制文件。该文件在内存位置的首地址存到reg[2]中。

reg[0]=2 changertitype 中断原本返回到中断指令的下一条指令。使用这个指令会返回到下条指令的上一条指令。

reg[0]=3 cls 指示GUI，清除屏幕。

reg[0]=4 kernelpush 将reg[1]的数据压入内核栈。

reg[0]=5 kernelpop 将内核栈顶元素弹到reg[1]内。

reg[0]=6 print error string 将虚拟机内部产生的错误信息的Java字符串打印到屏幕上。

中断矢量：

0x0100 留给kernel panic(未使用)

0x0101 UserRTI 用户级权限使用RTI时触发

0x0102 OpenFileError 打开文件失败时触发
