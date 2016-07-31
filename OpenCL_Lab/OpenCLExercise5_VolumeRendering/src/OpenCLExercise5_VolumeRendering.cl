#ifndef __OPENCL_VERSION__
#include <OpenCL/OpenCLKernel.hpp> // Hack to make syntax highlighting in Eclipse work
#endif

int intersectBox(float3 r_o, float3 r_d, float3 boxmin, float3 boxmax, float *tnear, float *tfar) {
	// compute intersection of ray with all six bbox planes
	float3 invR = (float3)(1.0f, 1.0f, 1.0f) / r_d;
	float3 tbot = invR * (boxmin - r_o);
	float3 ttop = invR * (boxmax - r_o);

	// re-order intersections to find smallest and largest on each axis
	float3 tmin = min(ttop, tbot);
	float3 tmax = max(ttop, tbot);

	// find the largest tmin and the smallest tmax
	float largest_tmin = max(max(tmin.x, tmin.y), tmin.z);
	float smallest_tmax = min(min(tmax.x, tmax.y), tmax.z);

	*tnear = largest_tmin;
	*tfar = smallest_tmax;

	return smallest_tmax > largest_tmin;
}

const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_LINEAR;
//const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;
__kernel void renderKernel (__read_only image3d_t d_input, __global float* d_output, __constant float* invViewMatrix, float tstep, float brightness) {

	uint outX = get_global_size(0);
	uint outY = get_global_size(1);

	uint x = get_global_id(0);
	uint y = get_global_id(1);

	uint index = (y * outX) + x;

	float u = (x / (float) (outX - 1))*2.0f-1.0f;
	float v = (y / (float) (outY - 1))*2.0f-1.0f;

	float3 boxMin = (float3)(0, 0, 0);
	float3 boxMax = (float3)(get_image_width(d_input), get_image_height(d_input), get_image_depth(d_input));

	// calculate eye ray in world space
	float3 eyeRay_o;
	float3 eyeRay_d;

	eyeRay_o = (float3)(invViewMatrix[3], invViewMatrix[7], invViewMatrix[11]);

	float3 temp = normalize((float3)(u, v, -2.0f));
	eyeRay_d.x = dot(temp, ((float3)(invViewMatrix[0],invViewMatrix[1],invViewMatrix[2])));
	eyeRay_d.y = dot(temp, ((float3)(invViewMatrix[4],invViewMatrix[5],invViewMatrix[6])));
	eyeRay_d.z = dot(temp, ((float3)(invViewMatrix[8],invViewMatrix[9],invViewMatrix[10])));

	// find intersection with box
	float tnear, tfar;
	int hit = intersectBox(eyeRay_o, eyeRay_d, boxMin, boxMax, &tnear, &tfar);
	if (!hit) {
		// set output to 0
		d_output[index] = 0;
		return;
	}
	if (tnear < 0.0f)
		tnear = 0.0f;     // clamp to near plane

	// march along ray from back to front, accumulating color
	float sum = 0;
	for (float t = tfar; t >= tnear; t -= tstep) {
		float3 pos = eyeRay_o + eyeRay_d*t;

		// read from 3D texture
		float sample = read_imagef(d_input, sampler, (float4)(pos, 0)).x;

		// accumulate result
		sum += sample;
	}

	// write output value
	d_output[index] = sum * brightness;
}
