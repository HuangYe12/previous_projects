; Computer structure and organization
; Project 2: memset
; Copyright 2008,2010 by Parallele Systeme, Universität Stuttgart

NAME	MEMSET_DEMO
	; define code memory segment
	; ?PR?_CAO_MEMSET?MEMSET_DEMO is the name of the segment
?PR?_CAO_MEMSET?MEMSET_DEMO segment code

	; Put the follwing code in the previously declared segment
	; PR?_CAO_MEMSET?MEMSET_DEMO
	; the segment should be relocatable (i.e. the linker has the freedom
	; to put it at any memory adress to avoid clashes with other modules)
	RSEG ?PR?_CAO_MEMSET?MEMSET_DEMO

	; declare the memset subroutine label as public, i.e. visible outside
	; of this module (e.g. from the c-file)
	PUBLIC _CAO_MEMSET

	; now follows the actual memset implementation
_cao_memSET:
	; void *memset(void *s, int c, size_t n);
	; according to Keil argument passing table 
	; (see Cx51 Compiler User's Guide):
	; s -> R6/R7   (MSB in R6)
	; c -> R5
	; n -> R2/R3    (MSB in R2)
	; return value: R6/R7
		MOV DPL, R7
		MOV DPH, R6
		MOV A, R5		

WHILE:
		CJNE R3, #0, COPY
		CJNE R2, #0, DECR2
		JMP DONE

DECR2:	DEC R2
COPY:	MOVX @DPTR, A
		INC DPTR
		DEC R3
		JMP WHILE

DONE:	RET
	END
