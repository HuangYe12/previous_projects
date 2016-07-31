; Computer structure and organization
; Project 2: memset
; Copyright 2008 by Parallele Systeme, Universität Stuttgart

NAME	MEMSET_DEMO

	; void *memset(void *s, int c, size_t n);
	; according to Keil argument passing table 
	; (see Cx51 Compiler User's Guide):
	; s -> R6/R7   (MSB in R6)
	; c -> R5
	; n -> R2/R3    (MSB in R2)
	; return value: R6/R7

MEMSET	MACRO S, CH, N
		; setup all parameters to be passed to the function
		; Keil uses big-endian ordering here
		MOV R6, #HIGH S
		MOV R7, #LOW S
		MOV R5, #CH
		MOV R3, #LOW N
		MOV R2, #HIGH N
		; now actually do the job
		CALL MEMSET_SUB
		; the return value is already in the R6/R7
		ENDM

		ORG		0
START:	
		; call the macro with parameters like you would 
		; call the C-function
		; Fill in 55h 0246h times starting from address 1234h
		MEMSET 1234h, 55h, 0246h
FINAL:	JMP FINAL

MEMSET_SUB:
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
