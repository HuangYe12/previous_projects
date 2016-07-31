; Computer structure and organization
; Project 1: arithmetic units
; Copyright 2008 by Parallele Systeme, Universität Stuttgart

NAME ADD32_DEMO

		ORG	0

; TASK=1: fixed address
; TASK=2: variable address
; TASK=3: packaging
$SET (TASK=3)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$IF TASK = 1
SUM1	EQU 043h
SUM2	EQU	053h

		MOV SUM1-3, #11h
		MOV SUM1-2, #22h
		MOV SUM1-1, #33h
		MOV SUM1, #44h
		
		MOV SUM2-3, #0CCh
		MOV SUM2-2, #0DDh
		MOV SUM2-1, #0EEh
		MOV SUM2, #0FFh
			
		MOV A, SUM1 ; copy contents of the cell at address SUM1
					; (43h in this case) to the accumulator
		ADD A, SUM2	; add the contents of the cell at address
					; SUM2 to the accumulator			
		MOV SUM1, A	; store the result back in SUM1

		MOV A, SUM1-1	; copy contents of the cell at address SUM1-1
						; (42h in this case) to the accumulator
		ADDC A, SUM2-1
		MOV SUM1-1, A

		MOV A, SUM1-2
		ADDC A, SUM2-2
		MOV SUM1-2, A
		MOV A, SUM1-3
		ADDC A, SUM2-3
		MOV SUM1-3, A

FINAL:	JMP FINAL


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSEIF TASK = 2
SUM1	EQU 043h
SUM2	EQU	053h

		MOV SUM1-3, #11h
		MOV SUM1-2, #22h
		MOV SUM1-1, #33h
		MOV SUM1, #44h
		
		MOV SUM2-3, #0CCh
		MOV SUM2-2, #0DDh
		MOV SUM2-1, #0EEh
		MOV SUM2, #0FFh

		MOV R0, #SUM1
		MOV R1, #SUM2

		MOV R2, #04h
		CLR C
LOOP:	MOV A, @R0
		ADDC A, @R1
		MOV @R0, A
		DEC R0
		DEC R1
		DJNZ R2, LOOP

FINAL:	JMP FINAL

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSE  ; TASK=3

SUM1	EQU 033h
SUM2	EQU	066h

		MOV SUM1-3, #0FEh
		MOV SUM1-2, #0EDh
		MOV SUM1-1, #0BEh
		MOV SUM1, #0EFh
		
		MOV SUM2-3, #0ABh
		MOV SUM2-2, #0BAh
		MOV SUM2-1, #0DEh
		MOV SUM2, #0ADh

		MOV R0, #SUM1
		MOV R1, #SUM2
		CALL ADD32


FINAL:	JMP FINAL

ADD32:
		MOV R2, #04h
		CLR C
LOOP:	MOV A, @R0
		ADDC A, @R1
		MOV @R0, A
		DEC R0
		DEc R1
		DJNZ R2, LOOP

		RET

$ENDIF

		END