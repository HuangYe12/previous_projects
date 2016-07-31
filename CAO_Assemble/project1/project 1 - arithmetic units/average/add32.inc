; Computer structure and organization
; Project 1: arithmetic units
; Copyright 2008 by Parallele Systeme, Universität Stuttgart


ADD32:
		MOV R2, #04h
		CLR C
ALOOP:	MOV A, @R0
		ADDC A, @R1
		MOV @R0, A
		DEC R0
		DEC R1
		DJNZ R2, ALOOP

		RET
