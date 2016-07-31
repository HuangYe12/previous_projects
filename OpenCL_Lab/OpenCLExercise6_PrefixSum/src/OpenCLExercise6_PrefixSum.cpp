//////////////////////////////////////////////////////////////////////////////
// OpenCL exercise 6: Prefix sum (Scan)
//////////////////////////////////////////////////////////////////////////////

// includes
#include <stdio.h>

#include <Core/Assert.hpp>
#include <Core/Time.hpp>
#include <Core/Image.hpp>
#include <OpenCL/cl-patched.hpp>
#include <OpenCL/Program.hpp>
#include <OpenCL/Event.hpp>
#include <OpenCL/Device.hpp>

#include <fstream>
#include <sstream>
#include <iostream>
#include <cmath>
#include <iomanip>
#include <sstream>

#include <boost/lexical_cast.hpp>

//////////////////////////////////////////////////////////////////////////////
// CPU implementation
//////////////////////////////////////////////////////////////////////////////
void prefixSumHost(const std::vector<cl_int>& h_input, std::vector<cl_int>& h_output) {
	if (h_input.size () == 0)
		return;
	cl_int sum = h_input[0];
	h_output[0] = sum;
	for (std::size_t i = 1; i < h_input.size (); i++) {
		sum += h_input[i];
		h_output[i] = sum;
	}
}

//////////////////////////////////////////////////////////////////////////////
// Main function
//////////////////////////////////////////////////////////////////////////////
int main(int argc, char** argv) {
	// Create a context
	cl::Context context(CL_DEVICE_TYPE_GPU);

	// Get a device of the context
	int deviceNr = argc < 2 ? 1 : atoi(argv[1]);
	std::cout << "Using device " << deviceNr << " / " << context.getInfo<CL_CONTEXT_DEVICES>().size() << std::endl;
	ASSERT (deviceNr > 0);
	ASSERT ((size_t) deviceNr <= context.getInfo<CL_CONTEXT_DEVICES>().size());
	cl::Device device = context.getInfo<CL_CONTEXT_DEVICES>()[deviceNr - 1];
	std::vector<cl::Device> devices;
	devices.push_back(device);
	OpenCL::printDeviceInfo(std::cout, device);

	// Create a command queue
	cl::CommandQueue queue(context, device, CL_QUEUE_PROFILING_ENABLE);

	// Declare some values
	std::size_t wgSize = 256; // Number of work items per work group
	std::size_t count = wgSize * wgSize * wgSize; // Number of values

	std::size_t size = count * sizeof (cl_int);

	// Load the source code
	cl::Program program = OpenCL::loadProgramSource(context, "src/OpenCLExercise6_PrefixSum.cl");
	// Compile the source code. This is similar to program.build(devices) but will print more detailed error messages
	// This will pass the value of wgSize as a preprocessor constant "WG_SIZE" to the OpenCL C compiler
	OpenCL::buildProgram(program, devices, "-DWG_SIZE=" + boost::lexical_cast<std::string>(wgSize));

	// Allocate space for output data from CPU and GPU on the host
	std::vector<cl_int> h_input (count);
	std::vector<cl_int> h_outputCpu (count);
	std::vector<cl_int> h_temp1 (wgSize * wgSize);
	std::vector<cl_int> h_temp2 (wgSize);
	std::vector<cl_int> h_outputGpu (count);

	// Allocate space for input and output data on the device
	cl::Buffer d_input (context, CL_MEM_READ_WRITE, size);
	cl::Buffer d_temp1 (context, CL_MEM_READ_WRITE, wgSize * wgSize * sizeof (cl_int));
	cl::Buffer d_temp2 (context, CL_MEM_READ_WRITE, wgSize * sizeof (cl_int));
	cl::Buffer d_output (context, CL_MEM_READ_WRITE, size);

	// Initialize memory to 0xff (useful for debugging because otherwise GPU memory will contain information from last execution)
	memset(h_input.data(), 255, size);
	memset(h_temp1.data(), 255, wgSize * wgSize * sizeof (cl_int));
	memset(h_temp2.data(), 255, wgSize * sizeof (cl_int));
	memset(h_outputCpu.data(), 255, size);
	memset(h_outputGpu.data(), 255, size);
	//TODO: GPU
	queue.enqueueWriteBuffer(d_input, true, 0, size, h_input.data());
	queue.enqueueWriteBuffer(d_temp1, true, 0, wgSize * wgSize * sizeof (cl_int), h_temp1.data());
	queue.enqueueWriteBuffer(d_temp2, true, 0, wgSize * sizeof (cl_int), h_temp1.data());
	queue.enqueueWriteBuffer(d_output, true, 0, size, h_outputCpu.data());
	queue.enqueueWriteBuffer(d_output, true, 0, size, h_outputGpu.data());

	//////// Generate input data ////////////////////////////////
	// Use random input data
	for (std::size_t i = 0; i < count; i++)
		h_input[i] = rand() % 100 - 40;
	// Or: Use consecutive integer numbers as data
	/*
	for (std::size_t i = 0; i < count; i++)
		h_input[i] = i;
	// */

	// Do calculation on the host side
	Core::TimeSpan cpuStart = Core::getCurrentTime();
	prefixSumHost(h_input, h_outputCpu);
	Core::TimeSpan cpuEnd = Core::getCurrentTime();

	// Create kernels
	cl::Kernel prefixSumKernel(program, "prefixSumKernel");
	cl::Kernel blockAddKernel(program, "blockAddKernel");

	// Copy input data to device
	cl::Event copyToDev;
	queue.enqueueWriteBuffer(d_input, true, 0, size, h_input.data(), NULL, &copyToDev);

	// Call the kernels
	cl::Event kernelExecution[5];

	prefixSumKernel.setArg<cl::Buffer>(0, d_input);
	prefixSumKernel.setArg<cl::Buffer>(1, d_output);
	prefixSumKernel.setArg<cl::Buffer>(2, d_temp1);
	queue.enqueueNDRangeKernel(prefixSumKernel, cl::NullRange, wgSize * wgSize * wgSize, wgSize, NULL, &kernelExecution[0]);

	prefixSumKernel.setArg<cl::Buffer>(0, d_temp1);
	prefixSumKernel.setArg<cl::Buffer>(1, d_temp1);
	prefixSumKernel.setArg<cl::Buffer>(2, d_temp2);
	queue.enqueueNDRangeKernel(prefixSumKernel, cl::NullRange, wgSize * wgSize, wgSize, NULL, &kernelExecution[1]);

	prefixSumKernel.setArg<cl::Buffer>(0, d_temp2);
	prefixSumKernel.setArg<cl::Buffer>(1, d_temp2);
	prefixSumKernel.setArg<cl::Buffer>(2, cl::Buffer ());
	queue.enqueueNDRangeKernel(prefixSumKernel, cl::NullRange, wgSize, wgSize, NULL, &kernelExecution[2]);

	blockAddKernel.setArg<cl::Buffer>(0, d_temp2);
	blockAddKernel.setArg<cl::Buffer>(1, d_temp1);
	queue.enqueueNDRangeKernel(blockAddKernel, cl::NullRange, wgSize * wgSize, wgSize, NULL, &kernelExecution[3]);

	blockAddKernel.setArg<cl::Buffer>(0, d_temp1);
	blockAddKernel.setArg<cl::Buffer>(1, d_output);
	queue.enqueueNDRangeKernel(blockAddKernel, cl::NullRange, wgSize * wgSize * wgSize, wgSize, NULL, &kernelExecution[4]);


	// Copy output data back to host
	cl::Event copyToHost;
	queue.enqueueReadBuffer(d_output, true, 0, size, h_outputGpu.data(), NULL, &copyToHost);

	// Print performance data
	Core::TimeSpan cpuTime = cpuEnd - cpuStart;
	Core::TimeSpan gpuTime = Core::TimeSpan::fromSeconds (0);
	for (std::size_t i = 0; i < sizeof (kernelExecution) / sizeof (*kernelExecution); i++)
		gpuTime = gpuTime + OpenCL::getElapsedTime (kernelExecution[i]);
	Core::TimeSpan copyTime = OpenCL::getElapsedTime(copyToDev) + OpenCL::getElapsedTime(copyToHost);
	Core::TimeSpan overallGpuTime = gpuTime + copyTime;
	std::cout << "CPU Time: " << cpuTime.toString() << ", " << (count / cpuTime.getSeconds() / 1e9) << " GVal/s" << std::endl;;
	std::cout << "Memory copy Time: " << copyTime.toString() << std::endl;
	std::cout << "GPU Time w/o memory copy: " << gpuTime.toString() << " (speedup = " << (cpuTime.getSeconds() / gpuTime.getSeconds()) << ", " << (count / gpuTime.getSeconds() / 1e9) << " GVal/s)" << std::endl;
	std::cout << "GPU Time with memory copy: " << overallGpuTime.toString() << " (speedup = " << (cpuTime.getSeconds() / overallGpuTime.getSeconds()) << ", " << (count / overallGpuTime.getSeconds() / 1e9) << " GVal/s)" << std::endl;

	// Check whether results are correct
	std::size_t errorCount = 0;
	for (size_t i = 0; i < count; i = i + 1) {
		if (h_outputCpu[i] != h_outputGpu[i]) {
			if (errorCount < 15)
				std::cout << "Result at " << i << " is incorrect: GPU value is " << h_outputGpu[i] << ", CPU value is " << h_outputCpu[i] << std::endl;
			else if (errorCount == 15)
				std::cout << "..." << std::endl;
			errorCount++;
		}
	}
	if (errorCount != 0) {
		std::cout << "Found " << errorCount << " incorrect results" << std::endl;
		return 1;
	}

	std::cout << "Success" << std::endl;

	return 0;
}
