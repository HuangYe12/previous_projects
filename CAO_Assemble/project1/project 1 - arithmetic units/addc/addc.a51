; Computer structure and organization
; Project 1: arithmetic units
; Copyright 2008 by Parallele Systeme, Universität Stuttgart

SUM1	EQU 043h

NAME ADD32_DEMO
		ORG	0
		MOV 10h, #0FFh
		MOV 20h, #0FFh

		MOV R0, #10h
		MOV R1, #20h
		MOV R3, #1
		MOV	A, @R1
		ADD A, R3
		ADD A, @R0

FINAL:	JMP FINAL

ADD32:

		RET

		END
