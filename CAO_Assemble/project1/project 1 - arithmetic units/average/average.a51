; Computer structure and organization
; Project 1: arithmetic units
; Copyright 2008 by Parallele Systeme, Universität Stuttgart

NAME AVERAGE_DEMO

;DATA1 		EQU 08899h
;DATA2		EQU 0AABBh
;DATA3		EQU 0CCDDh
;DATA4		EQU 0EEFFh

DATA1 		EQU 0ADEDh
DATA2		EQU 0BEEFh
DATA3		EQU 0FEEDh
DATA4		EQU 0ABBAh

MULDATA		EQU 031h	; address of the LSB of the input for MUL16
RESULTLSB	EQU 054h	; area to store the final result
RESULTMSB	EQU 050h

			ORG     0

			; clear result space
			MOV R0, #RESULTLSB
CLEAR:		MOV @R0, #0
			DEC R0
			CJNE R0, #RESULTMSB-1, CLEAR

			; compute and sum up the squares of DATA1 to DATA4
			; use R6 and R7 to transfer the data to the subroutine

			; DATA1
			MOV R6, #HIGH DATA1
			MOV R7, #LOW DATA1
			CALL SUMUP

			; DATA2
			MOV R6, #HIGH DATA2
			MOV R7, #LOW DATA2
			CALL SUMUP

			; DATA3
			MOV R6, #HIGH DATA3
			MOV R7, #LOW DATA3
			CALL SUMUP

			; DATA4
			MOV R6, #HIGH DATA4
			MOV R7, #LOW DATA4
			CALL SUMUP

			; divide the result by 4
			MOV R2, #02h ; repeat twice, because every shift is division by 2
AVERAGE:	MOV R1, #RESULTLSB-RESULTMSB+1
			MOV R0, #RESULTMSB
			CLR C
SHIFT:		MOV A, @R0
			RRC A
			MOV @R0, A
			INC R0
			DJNZ R1, SHIFT
			DJNZ R2, AVERAGE

			; The result is now in RESULTMSB+1 to RESULTLSB and 32-bits long


FINAL:		JMP FINAL

SUMUP:
			; compute the square
			MOV MULDATA-1, R6
			MOV MULDATA, R7
			MOV R0, #MULDATA
			MOV R1, #MULDATA
			CALL MUL16

			; add the square to the sum
			MOV R0, #RESULTLSB	; Summand1: current val of total sum of squares
			MOV R1, #E0			; Summand2: result of the multiplication
			CALL ADD32
			; don't forget to take care of carry signals
			JNC NOCARRY
			INC RESULTMSB
NOCARRY:	RET


$INCLUDE (ADD32.INC)
$INCLUDE (MUL16.INC)

	END
