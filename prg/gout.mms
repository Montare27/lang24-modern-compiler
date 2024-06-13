%
%          @@
%         @@
%        @@     @@@@@@
%       @@    @@     @@     @&@@@@@       @@ @ @
%      @@   @@      @@    @@      @@    @@      @@
%     @@   @@       @@   @@       @@   @@       @@
%    @@   @@       @@   @@       @@   @@       @@
%    @@    @@@@@@@ @@@  @@       @@    @@@@@@ @@       @@@@@         @@@@
%                                             @@     @@@   @@@     .@@@@@
%                                      @@@@@@@@           @@@    @@  @@@
%                                                      @@@@    .@@   @@@
%                                                     @@@      @@@@@@@@@@@
%                                                   @@@@@@@@@         @@@
%

%%%%%%%%%%%%%%%%% STATIC %%%%%%%%%%%%%%%%%
		LOC	Data_Segment
OutBuf		BYTE	0,0
InSize		IS	2
InBuffer		BYTE	0
		LOC	InBuffer+InSize
InArgs		OCTA	InBuffer,InSize
_a		OCTA
		LOC	_a+8
exit		BYTE	"EXIT CODE: ",0
		LOC	exit+104
code		BYTE	1,10,0
		LOC	code+24
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%% MAIN %%%%%%%%%%%%%%%%%
%	Initial code starting with Main function
%	Put Global register delimiter into a special register rG
%	Allocate special registers for the program
		LOC	#100
Main		SWYM
		SET	$0,252
%	Set rG as global registers threshold
		PUT	rG,$0
		GREG	0
		GREG	0
		GREG	0
%	Set rL as local registers threshold
		SET	$0,8
		PUT	rL,$0

%	Global register $252
%	@Data_Segment (0x0000 0000 0000 0000 -> 0x0000 0007 1AFD 498D 0000)
		SETL	$0,160
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,8192
		SET	$0,$252

%	Frame Pointer ($253), Stack Pointer($254)
%	@Stack_Segment (0x0015 50F7 DCA7 0000 -> 0x7FFF FFFF FFFF FFF8)
		SETL	$0,65528
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,32767
		SET	$254,$0

%	Call the main function
		PUSHJ	$8,_main

%	Print exit code
		LDO	$0,$254,0
		LDA	$255,exit
		TRAP	0,Fputs,StdOut
		LDA	$255,code
		STB	$0,$255,0
		TRAP	0,Fputs,StdOut
%	Set return value
		SET	$255,$0

%	Exit the program
		TRAP	0,Halt,0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%% Prologue of _main %%%%%%%%%%%%%%%%%
_main		SWYM
%	$0 <- local variables offset
		SETL	$0,16
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
%	Store RA
		NEG	$0,$0
		SUB	$0,$0,8
		GET	$1,rJ
		STO	$1,$254,$0
%	Store FP
		SUB	$0,$0,8
		STO	$253,$254,$0
%	Move FP
		SET	$253,$254
%	Move SP ($0 <- frame size + temp size)
		SETL	$0,32
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		SUB	$254,$254,$0
%	Jump to body
		JMP	L0

%%%%%%%%%%%%%%%%% Body of _main %%%%%%%%%%%%%%%%%
L0		SWYM
		SET	$1,$253
		SETL	$0,65520
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,65535
		SET	$0,$0
		ADD	$0,$1,$0
		SET	$0,$0
		SET	$1,$0
		SETL	$0,10
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		STO	$0,$1,0
		LDA	$0,_a
		SET	$1,$0
		SETL	$0,0
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		STO	$0,$1,0
L2		SWYM
		LDA	$0,_a
		LDO	$0,$0,0
		SET	$1,$0
		SETL	$0,10
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		SET	$0,$0
		CMP	$0,$1,$0
		ZSN	$0,$0,1
		SET	$0,$0
		BNZ	$0,L3
L5		SWYM
		JMP	L4
L3		SWYM
		SET	$1,$253
		SETL	$0,65528
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,65535
		SET	$0,$0
		ADD	$0,$1,$0
		SET	$0,$0
		SET	$2,$0
		LDA	$0,_a
		LDO	$0,$0,0
		SET	$1,$0
		SETL	$0,1
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		SET	$0,$0
		ADD	$0,$1,$0
		SET	$0,$0
		STO	$0,$2,0
		SET	$1,$253
		SETL	$0,65520
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,65535
		SET	$0,$0
		ADD	$0,$1,$0
		SET	$0,$0
		SET	$1,$0
		SET	$2,$253
		SETL	$0,65520
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,65535
		SET	$0,$0
		ADD	$0,$2,$0
		SET	$0,$0
		LDO	$0,$0,0
		SET	$0,$0
		SET	$3,$253
		SETL	$2,65528
		INCML	$2,65535
		INCMH	$2,65535
		INCH	$2,65535
		SET	$2,$2
		ADD	$2,$3,$2
		SET	$2,$2
		LDO	$2,$2,0
		SET	$2,$2
		ADD	$0,$0,$2
		SET	$0,$0
		STO	$0,$1,0
		LDA	$0,_a
		SET	$1,$0
		SETL	$0,2
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		SET	$0,$0
		SET	$3,$253
		SETL	$2,65528
		INCML	$2,65535
		INCMH	$2,65535
		INCH	$2,65535
		SET	$2,$2
		ADD	$2,$3,$2
		SET	$2,$2
		LDO	$2,$2,0
		SET	$2,$2
		MUL	$0,$0,$2
		SET	$0,$0
		STO	$0,$1,0
		JMP	L2
L4		SWYM
		SET	$1,$253
		SETL	$0,65520
		INCML	$0,65535
		INCMH	$0,65535
		INCH	$0,65535
		SET	$0,$0
		ADD	$0,$1,$0
		SET	$0,$0
		LDO	$0,$0,0
		SET	$0,$0
		JMP	L1
		SETL	$0,0
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
		SET	$0,$0
		JMP	L1

%%%%%%%%%%%%%%%%% Epilogue of _main %%%%%%%%%%%%%%%%%
L1		SWYM
%	Store return value
		STO	$0,$253,0
%	SP <- FP
		SET	$254,$253
%	$0 <- local variables offset
		SETL	$0,16
		INCML	$0,0
		INCMH	$0,0
		INCH	$0,0
%	Restore RA
		NEG	$0,$0
		SUB	$0,$0,8
		ADD	$0,$253,$0
		LDO	$1,$0,0
		PUT	rJ,$1
%	Restore FP
		SUB	$0,$0,8
		LDO	$253,$0,0
%	Jump to RA
		POP	0,0
%%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%% STANDARD LIBRARY %%%%%%%%%%%%%%%%%
%	Print character
_putchar		SWYM
		LDO	$0,$254,8
		LDA	$255,OutBuf
		STB	$0,$255,0
		TRAP	0,Fputs,StdOut
		POP

%	Read character
_getchar		SWYM
		LDA	$255,InArgs
		TRAP	0,Fgets,StdIn
		LDA	$0,InBuffer
		LDB	$0,$0,0
		STO	$0,$254,0
		POP
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

