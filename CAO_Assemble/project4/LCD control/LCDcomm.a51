; Hardwarenahe Programmierung
; Computer Architecture and Organization
; Project 4: LCD control


NAME LCDcomm

LCD_RW		EQU		P1.0
LCD_RS		EQU		P1.1
LCD_E		EQU		P1.2
LCD_DATA	EQU		P1
LCD_DATA_SHIFT EQU	3
LCD_DATA_MASK EQU	87h

code_s		SEGMENT	CODE

$INCLUDE (wait.inc)
$INCLUDE (lcdhw.inc)

		RSEG code_s
;--------------------------------------------------------------------
; Low level communication routines
;--------------------------------------------------------------------

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; LCD_send_nibble routine sends the lower nibble (bits 0 to 3) of the 
; accumulator to the LCD and waits for the time specified in R3/PSW.1
; according to the format described in wait routine (in wait.inc)
; The routine also decides whether to send the data in data or 
; in command mode (see Table 6, p. 24 of the HD44780U data sheet)
; based on the value of R5: 1 - data mode, else - command mode
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

	; the data of the lower nibble (bits 3 to 0) of ACC will be 
	; written to port LCD_DATA on lines 
	; 3+LCD_DATA_SHIFT to 0+LCD_DATA_SHIFT

	; first we mask the data in A to only contain the lower nibble, then
	; we shift the contents by LCD_DATA_SHIFT bits, finally we output
	; the data on the 4 selected lines of port LCD_DATA by using ORL;
	; we need to make sure that the other bits are not affected though,
	; they may have a different function!
LCD_send_nibble:
			...							; mask out the high nibble
			MOV		R7, #LCD_DATA_SHIFT	; initialize the shift counter
shift_loop:	CJNE	R7, #0, shift_data	; enter the loop if we have
										; to shift
			JMP		write_data			; otherwise just skip it
shift_data:	
			RL		A					; shift the acc by one bit
			DJNZ	R7, shift_data		; repeat if necessary
write_data:	
			...							; set the lines corresponding
										; to our data to zero, but don't touch
										; the other lines
			...							; OR the data on LCD_DATA

	; decide whether we send a command or data to the LCD and set LCD_RS
	; accordingly
eval_R5:	...							; if R5 is zero we are in command 
										; mode, RS->0
			...					; send data to DDRAM, set RS
			JMP		enable_data
clear_rs:	...					; send a command, zero RS

	; now inform the LCD that new data is available at the inputs:
	; generate a pulse on LCD_E; the LCD latches the data when LCD_E
	; changes from 1 to 0
enable_data:SETB	LCD_E		; according to data sheet p. 49 the pulse
								; should be > 450 ns
			NOP
			NOP
			NOP
			CLR		LCD_E		; trigger data latching in the LCD

			JMP		wait		; LCD busy

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; LCD_send_byte routine sends the byte stored in R4 to the LCD
; by sending first the high, then the low nibble of R4. LCD_send_nibble
; routine is used.
; R3/PWS.1 and R5 have the same meaning as in LCD_send_nibble.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

LCD_send_byte:
			...						; copy R4 to acc
			...						; swap the high and the low nibble
			...						; send the low nibble (high nibble of R4) 
									; to the LCD
			...						; copy R4 to acc again
			...						; send the low nibble (low nibble of R4)
									; to the LCD


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; LCD_init routine initializes the LCD controller by setting
; the required parameters
; The magic involved is described on p.46 of the HD44780U data sheet
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;--------------------------------------------------------------------
; LCD control routines
;--------------------------------------------------------------------

LCD_init:
			...						; configure R/W# line for writing

			...						; select millisecond resultion
			...						; configure 40 ms wait
									; (according to HD44780U data sheet p. 46)
			CALL	wait			; now really wait
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		; using LCD_send_nibble, only the lower nibble of ACC
		; is sent to the LCD
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			; send funtion set, 8bit interface
			; and wait > 4.1 ms
			; (according to HD44780U data sheet p. 46)
			...						; configure 5 ms wait
			...						; configure LCD_send_nibble to use command mode
									; when talking to the LCD (see Table 6, p. 24 of
									; the HD44780U data sheet)
			MOV		A, #03h			; set the data that will be sent to the LCD
			CALL	LCD_send_nibble ; call LCD_send_nibble routine

			; repeat with 100 us wait (see HD44780U data sheet p. 46)
			...						; switch to microsecond resolution
			...						; configure 100 us wait
			...						; set the data that will be sent to the LCD
			...						; call LCD_send_nibble routine
			; send again ...
			MOV		R3, #100
			MOV		A, #03h
			CALL	LCD_send_nibble

			; ... and switch to 4bit interface
			...						; set the data that will be sent to the LCD
									; (see HD44780U data sheet p. 46)
			...						; call LCD_send_nibble routine

		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		; The LCD is now in the 4 bit mode
		; This is the end of LCD_send_nibble calls, 
		; from now on both nibbles are transferred one at a time
		; using the LCD_send_byte routine
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

			; configure the LCD for operation
			; see page 46 and 28 of the data sheet
			...						; function set: 4-bit interface 
									; and 2 display lines
			CALL	LCD_send_byte	; send the command

			...						; display on, no cursor, no blinking
			...						; send the command

			...						; clear display (wait > 1.52 ms, see p. 24)
			...						; enable millisecond resolution
			...						; configure 3 ms wait			
			...						; send the command

			...						; entry mode:
									; increment address when writing, no shift
			...						; send the command

			; end of initialization

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; LCD_activate_1stline and LCD_activate_2ndline routines
; position the memory pointer of the LCD controller so that writing 
; to the first or second display line, respectively, is possible.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

LCD_activate_1stline:
			...					; set DDRAM address to 0
			JMP		LCD_line12	; jump to the part common to both 1st and
								; 2nd line
LCD_activate_2ndline:
			...					; set DDRAM address to 40h
LCD_line12:
			...					; configure LCD_send_nibble to use command mode
								; when talking to the LCD (see Table 6, p. 24 of
								; the HD44780U data sheet)

			...					; wait 100 us after the transfer
			...					; select microsecond resolution
			...					; send the command


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; LCD_sroll_left and LCD_sroll_right scroll the display of the LCD 
; by one character to the left or right, respectively.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

LCD_scroll_left:
			...					; command the LCD to scroll by one to the left
			JMP		LCD_scroll	; jump to the part common to both left
								; and right scrolling
LCD_scroll_right:
			...					; command the LCD to scroll by one to the right
LCD_scroll:
			...					; configure LCD_send_nibble to use command mode
								; when talking to the LCD (see Table 6, p. 24 of
								; the HD44780U data sheet)

			...					; wait 100 us after the transfer
			...					; select microsecond resolution
			...					; send the command

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; the LCD_send_text routine sends up to LCD_DDRAM_SIZE bytes of text 
; stored at DPTR (MSB/LSB) to the LCD.
; DPTR is expected to be pointing to the beginning of the text.
; If a Null-byte (= 0) is enconutered, this is interpreted as the of 
; the string. The remaining memory of the LCD is filled with spaces (' ').
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
LCD_send_text:
			...					; configure LCD_send_nibble to use data mode
								; when talking to the LCD (see Table 6, p. 24 of
								; the HD44780U data sheet)
			...					; wait 100 us after the transfer
			...					; configure microsecond resolution

			; read the text from the code memory starting at DPTR
			MOV		R0, #00h	; initialize offset from DPTR
next_char:	...					; copy offset to ACC
			...					; copy contents of @A+DPTR to ACC
			JZ		fill_spaces	; break if NULL-byte found
			...					; otherwise transmit the byte to the LCD
			...					; send the data to the LCD
			...					; increase offset from DPTR

								; continue the next_char loop as long as not all 
								; LCD_DDRAM_SIZE memory bytes are written
			...
			JMP		LCD_send_text_done ; LCD_DDRAM_SIZE bytes written, done

			; the text from DPTR is copied completely, but it was shorter than
			; LCD_DDRAM_SIZE bytes, fill up the rest of the DDRAM (LCD's text 
			; memory) with white space
fill_spaces:
			MOV		R4, #' '	; specify that space (' ') should be transferred 
								; to the LCD
fill_space:	...					; send the data to the LCD
			...					; increase the offset (pointer into DDRAM)

								; continue the fill_space loop as long as not all 
								; LCD_DDRAM_SIZE memory bytes are written					
			...
LCD_send_text_done:
			RET

; make the routines available to other modules by exporting them
...
...
...


END