; Hardwarenahe Programmierung
; Project 3: LED blink
; Copyright 2009 by Parallele Systeme, Universität Stuttgart


NAME	LED_BUSY

; TASK=1: toggle port P1
; TASK=2: generate PWM signal
$SET (TASK=1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$IF TASK = 1
		MOV P1, #0AAh
START1:
		MOV R5, #032h	
		XRL P1, #0FFh
WT:		CALL WAIT
		DJNZ R5, WT
		JMP START1

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSEIF TASK = 2
		MOV P1, #0h	; clear P1
		MOV P2, #0h	; clear P2
START2:	MOV A, P2	; read the value of P2 to A
		ANL A, #0Fh	; get the 4 lower bits of P2
		MOV R1, A	; and store it to R1 for the time with high voltage
		MOV A, #0Fh
		CLR C	 
		SUBB A, R1	; get the difference for the time with low voltage
		MOV R2, A
		CJNE R1, #0h, HIGH_V ; if R1 is not equal to 0, set high voltage
		JMP LOW_V
HIGH_V:	SETB P1.0
WAIT_H:	CALL WAIT
		DJNZ R1, WAIT_H
		CJNE R2, #0h, LOW_V	; if R2 is not equal to 0, set low voltage
		JMP START2
LOW_V:	CLR P1.0
WAIT_L:	CALL WAIT
		DJNZ R2, WAIT_L		
		JMP START2
		
$ENDIF

WAIT:
		PUSH 3
		PUSH 4
		MOV R3, #04h
		MOV R4, #0FFh
LOOP:	NOP
		NOP
		NOP
		NOP
		NOP
		NOP
		NOP
		DJNZ R4, LOOP
		DJNZ R3, LOOP
		POP 4
		POP 3
		RET

END
