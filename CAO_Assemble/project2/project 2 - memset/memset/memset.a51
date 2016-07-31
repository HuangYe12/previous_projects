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

; TASK=1: internal RAM
; TASK=2: external RAM
; TASK=3: 16-bit long byte count
; TASK=4: subroutine
$SET (TASK=3)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$IF TASK = 1
START	EQU	048h
COUNT	EQU	012h
CHAR	EQU	055h

		MOV R7, #START		; set the beginning of the memory area
							; where the byte CHAR should be stored
		MOV R3, #COUNT		; set the number of bytes that should
							; be filled with CHAR
		MOV R5, #CHAR		; load CHAR

		;;;;; End of initialization

		MOV B, R7			; move the address pointer to R0 as
		MOV R0, B			; @Ri can only be used with R0 and R1
		MOV A, R5

WHILE:
		CJNE R3, #0, COPY
		JMP DONE

COPY:	MOV @R0, A
		INC R0
		DEC R3
		JMP WHILE
DONE:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSEIF TASK = 2
START	EQU	01234h
COUNT	EQU	012h
CHAR	EQU	055h

		MOV R7, #LOW START		; set the beginning of the memory area
		MOV R6, #HIGH START		; where the byte CHAR should be stored
		MOV R3, #COUNT			; set the number of bytes that should
								; be filled with CHAR
		MOV R5, #CHAR			; load CHAR

		;;;;; End of initialization

		MOV DPH, R6
		MOV DPL, R7
		MOV A, R5

WHILE:
		CJNE R3, #0, COPY
		JMP DONE

COPY:	MOVX @DPTR, A
		INC DPTR
		DEC R3
		JMP WHILE
DONE:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSEIF TASK = 3
START	EQU	01234h
COUNT	EQU	00202h
CHAR	EQU	055h

		MOV R7, #LOW START		; set the beginning of the memory area
		MOV R6, #HIGH START		; where the byte CHAR should be stored
		MOV R3, #LOW COUNT		; set the number of bytes that should
		MOV R2, #HIGH COUNT		; be filled with CHAR
		MOV R5, #CHAR			; load CHAR

		;;;;; End of initialization

		MOV DPL, R7
		MOV DPH, R6
		MOV A, R5

WHILE:
		CJNE R3, #0, COPY
		CJNE R2, #0, DECR2
		JMP DONE

DECR2:  DEC R2
COPY:   MOVX @DPTR, A
		INC DPTR
		DEC R3
		JMP WHILE

DONE:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
$ELSEIF TASK = 4
START	EQU	01234h
COUNT	EQU	00246h
CHAR	EQU	055h

		MOV R7, #LOW START		; set the beginning of the memory area
		MOV R6, #HIGH START		; where the byte CHAR should be stored
		MOV R3, #LOW COUNT		; set the number of bytes that should
		MOV R2, #HIGH COUNT		; be filled with CHAR
		MOV R5, #CHAR			; load CHAR

		CALL MEMSET_SUB

ENDLESS:JMP ENDLESS
		
MEMSET_SUB:
		MOV DPL, R7
		MOV DPH, R6
		MOV A, R5

WHILE:
		CJNE R3, #0, COPY
		CJNE R2, #0, DECR2
		JMP DONE

DECR2:  DEC R2
COPY:   MOVX @DPTR, A
		INC DPTR
		DEC R3
		JMP WHILE

DONE:	RET
$ENDIF

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

FINAL:	JMP FINAL
	END
