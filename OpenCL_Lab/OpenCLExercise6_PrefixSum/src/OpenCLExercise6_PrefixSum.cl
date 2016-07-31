#ifndef __OPENCL_VERSION__
#include <OpenCL/OpenCLKernel.hpp> // Hack to make syntax highlighting in Eclipse work
#endif

__attribute__((reqd_work_group_size(WG_SIZE, 1, 1)))
__kernel void prefixSumKernel(__global int* d_input, __global int* d_output, __global int* d_blockSumsOut) {
	__local int ldata[WG_SIZE];

	uint li = get_local_id(0);
	uint gi = get_global_id(0);
	uint grpid = get_group_id(0);

	ldata[li] = d_input[gi];

	barrier(CLK_LOCAL_MEM_FENCE);

	for (uint offset = 1; offset < WG_SIZE; offset <<= 1) {
		int t = ldata[li];
		if (li >= offset)
			t += ldata[li - offset];
		barrier(CLK_LOCAL_MEM_FENCE);
		ldata[li] = t;
		barrier(CLK_LOCAL_MEM_FENCE);
	}

	d_output[gi] = ldata[li];

	if (d_blockSumsOut && li == WG_SIZE - 1)
		d_blockSumsOut[grpid] = ldata[li];
}

__attribute__((reqd_work_group_size(WG_SIZE, 1, 1)))
__kernel void blockAddKernel(__global int* d_blockSums, __global int* d_output) {
	uint gi = get_global_id(0);
	uint grpid = get_group_id(0);

	if (grpid == 0)
		return;

	int value = d_blockSums[grpid - 1];

	d_output[gi] = value + d_output[gi];
}
