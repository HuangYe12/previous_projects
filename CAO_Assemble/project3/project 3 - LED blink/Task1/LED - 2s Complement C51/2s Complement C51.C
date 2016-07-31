#include <REG51.H> // path: \Keil\C51\INC, define names of SFRs

void main (void)  {
  unsigned int UI;
  signed int SI;

  while (1) {                           // Loop forever
	UI = P1;							// Assign Port 1 to the lowe 8 bits of an unsigned 16-bit integer
	UI = UI << 8;						// Shift the unsigned integer 8 bits left, since Port 1 corrensponds to MSB
	UI = UI + P0;					    // Add Port0 to the lower 8 bits, since Port 0 corresponds to LSB
	SI = UI;
	UI = ~UI;							// Invert every bit of UI
	UI ++;								// and add 1 to it to compute it's two's complement
	SI = -SI;
	P2 = UI;					    	// The lower 8 bits of the unsigned integer is assigned to Port 2
	P3 = UI >> 8;				    	// Shift the unsiged integer 8 bits right so that they can be assigned to Port 3
  }
} 
