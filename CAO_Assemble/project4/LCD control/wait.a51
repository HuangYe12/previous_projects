; Hardwarenahe Programmierung
; Computer Architecture and Organization
; Project 4: LCD control
; Copyright 2009,2010 by Parallele Systeme, Universität Stuttgart
; Author: Marek Wroblewski

NAME WAIT

MILLISEC	EQU		...

data_s		SEGMENT DATA
code_s		SEGMENT	CODE

			RSEG data_s
delay_cnt:	DS		1
TL_init:	DS		1
TH_init:	DS		1


			...				; initialize the timer0 interrupt
							; routine entry point
			...				; jump to the ISR


			RSEG code_s

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; the wait routine waits at least the specified number of milliseconds
; (if PSW.1 is set) or microseconds (if PWS.1 is cleared).
; Times below approx. 50 us are not feasible.
; The number of time units requested is passed in R3.
; The waiting is done mostly in idle mode.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
wait:
			...					; test if we are in millisecond
								; or microsecond mode; when PSW.1 is set
								; we use millisecond resolution

	; for microsecond resolution we just count the specified number of cycles
	; using timer, i.e. we assume that one timer clock is 1 us. In reality it
	; depends on the oscillator frequency.
us_config:
			MOV		delay_cnt, #1	; how often do we need to restart the timer
									; in microsecond mode, we only need to do it
									; once as we only wait the specified number 
									; of timer cycles
			; compute the initial value of the timer
			...
			...
			...

			MOV		TL_init, A				; initilize the timer for
			MOV		TH_init, #0FFh			; R3 us @ 12MHz oscillator
			JMP		time_config_done		; jump to timer start

	; For millisecond resolution we restart the timer R3 times. Every timer
	; run (from start to the interrupt) takes 1 ms. The value MILLISEC
	; must be computed depending on the oscillator frequency.

ms_config:
			MOV		delay_cnt, ...	; specify how often we restart the timer
									; we restart once for each millisecond
			MOV		TL_init, #LOW MILLISEC	; initilize the timer for
			MOV		TH_init, #HIGH MILLISEC	; 1000 us

time_config_done:
			; timer configuration
			...
			...
			...							; 16-Bit timer controlled by run-bit (TCON)
			...							; let the timer run

			; interrupt configuration
			...							; set timer0 interrupt to high priority
			...							; enable timer0 interrupt
			...							; make sure the interrupts are not disabled

wait_loop:	...							; go into idle mode
			NOP							; make sure in case of hardware reset
			NOP							; there is enough time for it to work (2 cycles)
			...							; we got woken up, but the time is not yet up
										; so go to idle mode again
			RET

; interrupt service routine for wait
timer0:		...							; save PSW to the stack
			MOV		TL0, TL_init		; re-initialize the timer for
			...							; 1000 us
			...							; check if the requested number of timer
										; interrupts occured
			...							; if yes disable the interrupt

t0_done:	...							; restore PSW
			RETI

; make the routine available to other modules by exporting it
PUBLIC		wait						; export wait routine

END
