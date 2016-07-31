; Hardwarenahe Programmierung
; Computer Architecture and Organization
; Project 4: LCD control
; Copyright 2009,2010 by Parallele Systeme, Universität Stuttgart
; Author: Marek Wroblewski

NAME		LCDcontrol

...						; enable access to routines from the wait module
...						; enable access to routines from the LCDcomm module
$INCLUDE (lcdhw.inc)	; load constants from LCDhw

idata_s		SEGMENT IDATA		; define data segment for stack
code_s		SEGMENT	CODE		; define data segment for code

			RSEG idata_s		; use stack segment
stack:		DS		10			; allocate 10 bytes for stack


			...					; begin a code segment at address 0
startup:	JMP	main			; jump to the real code

			...					; begin a relocatable code segment

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; main entry
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

main:
			MOV	SP, #stack			; configure stack
			CALL	LCD_init		; initialize and configure the LCD
			CALL	transfer_text	; transfer the text for the first and
									; second line on the LCD diplay to
									; LCD's memory (DDRAM)
			JMP		do_scrolling	; scroll the text to the left and right
									; in an endless loop

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; set the contents of LCD's memory (DDRAM)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
transfer_text:
			...						; inform the LCD that we are going
									; to write to the memory area of
									; the first line
			...						; initilize DPTR to point to the text 
									; of the first line		
			...						; now transfer the text to the LCD

			; repeat the above for the second line
			...
			...
			JMP		LCD_send_text	; transfer the text and the we're done, so
									; no CALL just JMP																			  	


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; change the text displayed on the LCD screen to achieve the scrolling
; effect
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

SCROLL_LEFT	EQU		1

do_scrolling:
			MOV		R1, #SCROLL_LEFT ; use R1 to indicate if we're
									; scrolling left or right

			; use R2 as a counter how many characters to scroll
			; we want to scroll that far to the left (i.e. move the 
			; text to the left) that all text of line 1 can be seen, 
			; then we scroll back
scroll_init:MOV		R2, #TXT_LINE2-TXT_LINE1-1-LCD_DISPLAY_SIZE
			; is this safe-^ ? Think of situations when this does
			; not work. Test what happens then.

scroll:		...					 ; copy left-right flag to ACC
			JZ		scroll_right ; decide whether we go left or right
			...					 ; tell the LCD to scroll left
			JMP		scroll_dec	 ; go to the delay loop
scroll_right:
			...					 ; tell the LCD to scroll left
scroll_dec:
			...					; set millisecond resolution
			...					; delay loop 250 ms
			...					; do the wait

			...					; continue to scroll in the same 
									; direction as long as not all 
									; characters are shown
			; switch direction by invertig the direction flag
			MOV		A, #01h
			XRL		A, R1
			MOV		R1, A

			JMP		scroll_init     ; start again

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; text data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; define the text display on the first and the second line of the LCD
; Only LCD_DDRAM_SIZE bytes will be copied to the LCD. If the text
; is shorter it must be terminated with a NULL-byte (0)
TXT_LINE1:	DB		'COMPUTER ARCHITECTURE AND ORGANIZATION',0
TXT_LINE2:	DB		'            -- Project 4 --',0

END
