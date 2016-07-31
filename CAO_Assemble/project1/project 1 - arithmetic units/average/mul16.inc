; Computer structure and organization
; Project 1: arithmetic units
; Copyright 2008 by Parallele Systeme, Universität Stuttgart


E0		EQU 043h
E1		EQU 042h
E2		EQU 041h
E3		EQU 040h

MUL16:
		MOV E0, #0   ; zero the result space
		MOV E1, #0
		MOV E2, #0
		MOV E3, #0
		MOV A, @R0	; load the first pair of 8-bit numbers
		MOV B, @R1
		MUL AB		; perform the 8-bit multiplication
		MOV E0, A	; store the first partial product in E0/E1		
		MOV E1, B		
					; compute the second partial product
		DEC R1
		MOV A, @R0
		MOV B, @R1
		MUL AB
		ADD A, E1
		MOV E1, A
		XCH A, B
		ADDC A, E2
		MOV E2, A
		JNC PROD3
		INC E3
PROD3:	INC R1
		DEC R0
		MOV A, @R0
		MOV B, @R1
		MUL AB
		ADD A, E1
		MOV E1, A
		XCH A, B
		ADDC A, E2
		MOV E2, A
		JNC PROD4
		INC E3
PROD4:	DEC R1
		MOV A, @R0
		MOV B, @R1
		MUL AB
		ADD A, E2
		MOV E2, A
		XCH A, B
		ADDC A, E3
		MOV E3, A

		RET
