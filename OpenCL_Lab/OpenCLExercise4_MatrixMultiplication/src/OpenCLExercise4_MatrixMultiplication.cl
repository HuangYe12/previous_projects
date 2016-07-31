#ifndef __OPENCL_VERSION__
#include <OpenCL/OpenCLKernel.hpp> // Hack to make syntax highlighting in Eclipse work
#endif

__kernel void matrixMulKernel1(__global float* d_inputA, __global float* d_inputB, __global float* d_outputC, uint countAX_BY, uint countAY, uint countBX) {
	int i = get_global_id(0);
	int j = get_global_id(1);

	float sum = 0;
	for (uint k = 0; k < countAX_BY; k++) {
		float a = d_inputA[k + j * countAX_BY];
		float b = d_inputB[i + k * countBX];
		sum += a * b;
	}
	d_outputC[i + j * countBX] = sum;
}

// The preprocessor constant WG_SIZE will contain the size of a work group in X/Y-direction

__attribute__((reqd_work_group_size(WG_SIZE, WG_SIZE, 1)))
__kernel void matrixMulKernel2(__global float* d_inputA, __global float* d_inputB, __global float* d_outputC, uint countAX_BY, uint countAY, uint countBX) {
	int i = get_global_id(0);
	int j = get_global_id(1);

	int tx = get_local_id(0);
	int ty = get_local_id(1);

	// sum is used to store the element of the matrix
	// that is computed by the work item
	float sum = 0;

	for (uint bs = 0; bs < countAX_BY; bs += WG_SIZE) { // loop over the submatrices
		// Local memory for the sub-matrix of M
		__local float l_A[WG_SIZE][WG_SIZE];
		// Local memory for the sub-matrix of N
		__local float l_B[WG_SIZE][WG_SIZE];

		// Load the matrices from global memory to shared memory;
		// each thread loads one element of each matrix
		l_A[ty][tx] = d_inputA[j * countAX_BY + (bs + tx)];
		l_B[ty][tx] = d_inputB[(bs + ty) * countBX + i];


		// Barrier to make sure the matrices are loaded
		barrier(CLK_LOCAL_MEM_FENCE);

		// Multiply the two matrices;
		// each work item computes one element of the sub-matrix
		for (uint k = 0; k < WG_SIZE; ++k) {
			sum += l_A[ty][k] * l_B[k][tx];
		}

		// Synchronize to make sure that the preceding
		// computation is done before loading two new
		// sub-matrices of M and N in the next iteration
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	// Write the block sub-matrix to global memory;
	// each thread writes one element
	d_outputC[j * countBX + i] = sum;
}

__kernel void matrixMulKernel3(__global float* d_inputA, __global float* d_inputB, __global float* d_outputC, uint countAX_BY, uint countAY, uint countBX, __local float* localMem) {
	int i = get_global_id(0);
	int j = get_global_id(1);

	int tx = get_local_id(0);
	int ty = get_local_id(1);

	uint wgSize = get_local_size(0);

	// sum is used to store the element of the matrix
	// that is computed by the work item
	float sum = 0;

	for (uint bs = 0; bs < countAX_BY; bs += wgSize) { // loop over the submatrices
		// Local memory for the sub-matrix of A
		__local float* l_A = localMem;
		// Local memory for the sub-matrix of B
		__local float* l_B = localMem + get_local_size(0) * get_local_size(0);

		// Load the matrices from global memory to shared memory;
		// each thread loads one element of each matrix
		l_A[ty * wgSize + tx] = d_inputA[j * countAX_BY + (bs + tx)];
		l_B[ty * wgSize + tx] = d_inputB[(bs + ty) * countBX + i];

		// Barrier to make sure the matrices are loaded
		barrier(CLK_LOCAL_MEM_FENCE);

		// Multiply the two matrices;
		// each work item computes one element of the sub-matrix
		for (uint k = 0; k < wgSize; ++k) {
			sum += l_A[ty * wgSize + k] * l_B[k * wgSize + tx];
		}

		// Synchronize to make sure that the preceding
		// computation is done before loading two new
		// sub-matrices of M and N in the next iteration
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	// Write the block sub-matrix to global memory;
	// each thread writes one element
	d_outputC[j * countBX + i] = sum;
}

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_NEAREST;
__kernel void matrixMulKernel4(__read_only image2d_t d_inputA, __read_only image2d_t d_inputB, __global float* d_outputC, uint countAX_BY, uint countAY, uint countBX) {
	int i = get_global_id(0);
	int j = get_global_id(1);

	float sum = 0;
	for (uint k = 0; k < countAX_BY; k++) {
		float a = read_imagef(d_inputA, sampler, (int2){k, j}).x;
		float b = read_imagef(d_inputB, sampler, (int2){i, k}).x;
		sum += a * b;
	}
	d_outputC[i + j * countBX] = sum;
}
