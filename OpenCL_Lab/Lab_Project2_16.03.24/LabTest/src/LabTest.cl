#ifndef __OPENCL_VERSION__
#include <OpenCL/OpenCLKernel.hpp> // Hack to make syntax highlighting in Eclipse work
#endif
#define EXPONENT 11
#define QFACTOR 1

__constant int num_point = 5308416;

unsigned int golombcode(const int x, int k, int m){

	//int m = 1 << k = 2^k ; k is the chosen parameter
	int q = x / m;	//Quotient
	unsigned int output = 0;
	int count_bit = 0;
	//input Quotient Code
	if (q){
		count_bit = q+k+1;
		output = ((1 << q)-1) << (k+1);
		if(count_bit>16){
			output = 65535; // Set 16 bit escape symbols as 0xffff
			 return output;
		}

	}
	//input Remainder Code as M is power of 2.
	int r = x % m;
	output += r;
	return output;
}

__kernel void compressionKernel (__global const double* d_input1, __global const double* d_input2, __global const double* d_input3 , __global uint* d_output) {
	size_t index = get_global_id(0);
	double q_factor = QFACTOR;
	double predictor;
	int  k = EXPONENT; //parameter m = 2^k
	int m= 1 << k;
	int output_error;
	//Prediction and Quantization
	predictor = 2*d_input2[index]-d_input1[index];
	output_error = (int) 2*((d_input3[index]-predictor)/q_factor);
	if(output_error < 0)
		output_error = abs(output_error)-1;
	d_output[index]= golombcode(output_error,k,m); // GolombEncoder
	}
