; Hardwarenahe Programmierung
; Project 3: LED blink
; Copyright 2009 by Parallele Systeme, Universität Stuttgart


NAME		LED_TIMER

st_data_s	SEGMENT IDATA	; define stack segment
data_s		SEGMENT DATA	; define data segment
code_s		SEGMENT	CODE	; defined code segment

			RSEG st_data_s	; begin stack segment
stack:		DS		10		; reserve 10 bytes for stack

			RSEG data_s		; begin data segment
delay_cnt:	DS		1		; reserve memory for your "variables"
delay_value:DS		1

			CSEG AT 0		; begin code segment at 0
startup:	JMP 	main	; jump to the program code

			ORG		0Bh		; initialize the timer interrupt
							; routine entry point
			JMP		t0_isr	; jump to the real ISR


timer0		EQU		0E5F4h	; timer start value for a 10ms delay
delay_init	EQU		64		; loop counter init value for 640 ms

			RSEG code_s
main:
			MOV SP, #stack			; configure the stack

			; initialize the counter
			MOV		delay_value, #delay_init
			MOV		delay_cnt, delay_value

			; timer configuration
			MOV 	TL0, #LOW timer0	; initilize the timer for
			MOV 	TH0, #HIGH timer0	; 10 ms @ 12MHz oscillator
			MOV 	TMOD, #01h			; 16-Bit timer controlled by run-bit (TCON)
			SETB	TR0					; let the timer run
			
			; interrupt configuration
			SETB	PT0					; set timer0 interrupt to high priority
			SETB	ET0					; enable timer0 interrupt
			SETB	EA					; make sure the interrupts are not disabled

endless:	ORL		PCON, #01h			; go into idle mode
			NOP							; make sure in case of hardware reset
			NOP							; there is enough time for it to work (2 cycles)
			JMP		endless

t0_isr:		PUSH	PSW					; save status
			MOV 	TL0, #LOW timer0	; reload the timer
			MOV 	TH0, #HIGH timer0 
			
			DJNZ	delay_cnt, t0_done		; check if it is time to switch the LED
			MOV		delay_cnt, delay_value	; reload the counter with the current
											; delay

			XRL		P3, #0FFh			; blink the LEDs
			XRL		P2, #0FFh			; blink the LEDs
			XRL		P1, #0FFh			; blink the LEDs
			XRL		P0, #0FFh			; blink the LEDs

t0_done:	POP PSW						; restore status
			RETI

END
