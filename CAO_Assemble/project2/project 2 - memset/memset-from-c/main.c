/* Computer architecture and organization
   Project 2: memset
   Copyright 2008,2010 by Parallele Systeme, Universität Stuttgart */

/* Declare the function prototype
   unsigned char xdata *s is a pointer to a memory cell in
   the XRAM (xdata) */
void cao_memset (unsigned char xdata *s, unsigned char, unsigned int n);

/* This is the starting point of our program. It is called automatically
	when the controller is turned on. The compiler inserts the appropriate
	code at the address 0000. */
void main (){

	/* Call the function declared above.
	   The implementation is not here.
	   The linker looks for an implementation in other modules.
	   It finds _CAO_MEMSET in the MEMSET_DEMO module.
	   It knows that the '_' at the beginning signifies that the
	   parameters passed in the call to cao_memset should be passed
	   to the assembly implementation via registers (this is a 
	   convention of the Keil C51 compiler). 
	   The code to put the values into the correct registers is inserted
	   automatically. You can check that in the disassembly window 
	   after building.  */

	cao_memset (0x0010, 'a', 0x20); /* call the function cao_memset */

	while (1);   /* endless loop */
}