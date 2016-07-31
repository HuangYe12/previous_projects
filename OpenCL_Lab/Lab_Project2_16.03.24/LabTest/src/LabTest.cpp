//////////////////////////////////////////////////////////////////////////////
// OpenCL LabTest
//////////////////////////////////////////////////////////////////////////////

// includes
#include <stdlib.h>
#include <sys/stat.h>
#include "hdf5.h"
#include <string>

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

using namespace std;

/////////Define some initial values
#define DIMENSIONX 6  												// element dimensions in HDF5 file
#define DIMENSIONY 6
#define DIMENSIONZ 6
#define EXPONENT 11    												//parameter m = 2^k in this case k should be less than 16
#define FILE1 "/home/huangye/cart_Jet_3D_32x24x32_State_000000.000100000000.h5"
#define FILE2 "/home/huangye/cart_Jet_3D_32x24x32_State_000000.000110000000.h5"
#define LOOPNUM 10   												//the number of files need to be processed
#define QFACTOR 1													//The quantization factor could be 1  10  100
const int num_element = 24576;										//The number of elements in each HDF5 file
typedef double data_vec[DIMENSIONX][DIMENSIONY][DIMENSIONZ][5];		//Define an input type to read the data from HDF5 files
const int num_point = 5308416; 										// num_element*6*6*6)

//////////////////////////////////////////////////////////////////////////////
unsigned char buff;
int filled;
unsigned long int index_out;
vector<unsigned char> out_buf(num_point*8);		// To store the final output which are in sequential bits form
vector<unsigned int> overflow_buf(num_point); 	// 32 bit space for overflow value

//Function to attach output into sequential bits
void put_bit(unsigned char b)
{
	buff = buff | ((b & 1) << filled);
	if (filled == 0)
	  {
	    out_buf[index_out]= buff;
	    index_out ++;
	    buff = 0;
	    filled = 7;
	}
	else
	     filled--;
}
//////////////////////////////////////////////////////////////////////////////
// CPU implementation
//////////////////////////////////////////////////////////////////////////////
unsigned int golombcode(const int x, int k, int m, const size_t index, unsigned int &count_of){
	//int m = 1 << k = 2^k ; k is the chosen parameter
	int q = x / m;	//Quotient
	unsigned int output = 0;
	int count_bit = 0;
	//input Quotient Code
	if (q){
		count_bit = q+k+1;
		if(count_bit>16){
			output = 65535; // Set 16 bit escape symbols as 0xffff
			overflow_buf[index] = x;
			count_of ++;
			return output;
		}
		output = ((1 << q)-1) << (k+1);
	}
	else
		count_bit = k+1;

	//input Remainder Code as M is power of 2.
	int r = x % m;
	output += r;
	return output;
}

void compressionHost (const vector<double>& buffer_p1, const vector<double>& buffer_p2, const vector<double>& buffer_p3, vector<cl_uint>& h_output){

	double q_factor = QFACTOR; // 1  10 100
	double predictor;
	int  k = EXPONENT; //parameter m = 2^k
	int m= 1 << k;
	int output_error;
	unsigned int count_of=0; /////////////////
	for (size_t index=0; index<num_point; index++){
		//Prediction and Quantization
		predictor = 2*buffer_p2[index]-buffer_p1[index];
		output_error = (int) 2*((buffer_p3[index]-predictor)/q_factor);
		if(output_error < 0)
			output_error = abs(output_error)-1;
		h_output[index]= golombcode(output_error,k,m,index, count_of); // GolombEncoder
	}
	cout << "overflow rate =" << (double)count_of/num_point << endl;
}

void readData(std::vector<double> &buffer_p, size_t p){
	string filename;
	switch(p){
	case 1:
	{
		filename=FILE1;
		break;
	}
	case 2:
	{
		filename=FILE2;
		break;
	}
	default:
	{
		//filename = "/home/huangye/cart_Jet_3D_32x24x32_State_000000.000120000000.h5";
		cout << "Please input next file path:";
		getline(cin, filename);
		//filename = cin.get();
	}
	}
	const char* fname = filename.data();
	hid_t file_id = H5Fopen(fname, H5F_ACC_RDONLY, H5P_DEFAULT);
	hid_t dataset_id = H5Dopen2(file_id, "/DG_Solution", H5P_DEFAULT);
    double* input_data;
    input_data = (double*) malloc (num_element * sizeof (data_vec));
    herr_t status = H5Dread(dataset_id, H5T_NATIVE_DOUBLE, H5S_ALL, H5S_ALL, H5P_DEFAULT, input_data);
    int param = 4;
    int index = 0;
    for (int element=0; element < num_element; element++) {
    	for (int z=0; z < DIMENSIONZ; z++) {
    		for (int y=0; y < DIMENSIONY; y++) {
    			for (int x=0; x < DIMENSIONX; x++) {
    				//buffer_p[index] = input_data[element][x][y][z][param];
    				buffer_p[index] = input_data[(((element*DIMENSIONX+x)*DIMENSIONY+y)*DIMENSIONZ+z)*5+param];
    				//std::cout <<  buffer_p[index] << std::endl;
    				//std::cout << std::flush;
    				index = index + 1;
    			}
    		}
    	}
    }
    free(input_data);
    H5Dclose(dataset_id);
    H5Fclose(file_id);
    cout << "ReadHDF5Finish" << endl;
}



//////////////////////////////////////////////////////////////////////////////
// Main function
//////////////////////////////////////////////////////////////////////////////
int main(int argc, char** argv) {

	// Create a context
	cl::Context context(CL_DEVICE_TYPE_GPU);

	// Get the first device of the context
	std::cout << "Context has " << context.getInfo<CL_CONTEXT_DEVICES>().size() << " devices" << std::endl;
	cl::Device device = context.getInfo<CL_CONTEXT_DEVICES>()[0];
	std::vector<cl::Device> devices;
	devices.push_back(device);
	OpenCL::printDeviceInfo(std::cout, device);

	// Create a command queue
	cl::CommandQueue queue(context, device, CL_QUEUE_PROFILING_ENABLE);
	
	// Load the source code
	cl::Program program = OpenCL::loadProgramSource(context, "src/LabTest.cl");

	// Compile the source code. This is similar to program.build(devices) but will print more detailed error messages
	OpenCL::buildProgram(program, devices);

	// Create a kernel object
	cl::Kernel compressionKernel(program, "compressionKernel");

	// Declare some values
	size_t wgSize = 64; // Number of work items per work group
	size_t count = wgSize * 82944;	// Overall number of work items = Number of elements
	size_t size_input = count * sizeof (double); // Size of data in bytes
	size_t size_output = count * sizeof (cl_uint); // Size of data in bytes
	size_t size_outbuf = count * sizeof (double); // Size of data in bytes

	// Allocate space for input data and output data from CPU and GPU on the host
	vector<double> buffer_p1 (count);
	vector<double> buffer_p2 (count);
	vector<double> buffer_p3 (count);
	vector<cl_uint> h_outputCpu (count);
	vector<cl_uint> h_outputGpu (count);

	// Allocate space for input and output data on the device
	cl::Buffer d_input1 (context, CL_MEM_READ_WRITE, size_input);
	cl::Buffer d_input2 (context, CL_MEM_READ_WRITE, size_input);
	cl::Buffer d_input3 (context, CL_MEM_READ_WRITE, size_input);
	cl::Buffer d_output (context, CL_MEM_READ_WRITE, size_output);

	// Start a loop for inputing files sequentially

	for(size_t loop=0; loop<LOOPNUM; loop++){
		if(loop==0){
			// Initialize memory to 0xff (useful for debugging because otherwise GPU memory will contain information from last execution)
			memset(buffer_p1.data(), 255, size_input);
			memset(buffer_p2.data(), 255, size_input);
		}
		memset(buffer_p3.data(), 255, size_input);
		memset(h_outputCpu.data(), 255, size_output);
		memset(h_outputGpu.data(), 255, size_output);
		queue.enqueueWriteBuffer(d_input1, true, 0, size_input, buffer_p1.data());
		queue.enqueueWriteBuffer(d_input2, true, 0, size_input, buffer_p2.data());
		queue.enqueueWriteBuffer(d_input3, true, 0, size_input, buffer_p3.data());
		queue.enqueueWriteBuffer(d_output, true, 0, size_output, h_outputGpu.data());



		// Read input data
		Core::TimeSpan readStart = Core::getCurrentTime();
		if(loop==0){
			readData(buffer_p1,1);
			readData(buffer_p2,2);
		}
		readData(buffer_p3,3);
		Core::TimeSpan readEnd = Core::getCurrentTime();



		// Do compression on the host side
		Core::TimeSpan cpuStart = Core::getCurrentTime();
		cout << "CPU compute start" << endl;
		compressionHost (buffer_p1, buffer_p2, buffer_p3, h_outputCpu);
		cout << "CPU compute finished" << endl;
		Core::TimeSpan cpuEnd = Core::getCurrentTime();

		// Copy input data to device
		cl::Event copy1;
		queue.enqueueWriteBuffer(d_input1, true, 0, size_input, buffer_p1.data(), NULL, &copy1);

		cl::Event copy2;
		queue.enqueueWriteBuffer(d_input2, true, 0, size_input, buffer_p2.data(), NULL, &copy2);

		cl::Event copy3;
		queue.enqueueWriteBuffer(d_input3, true, 0, size_input, buffer_p3.data(), NULL, &copy3);

		std::cout << "GPU Computing start" << std::endl;

		// Launch kernel on the device
		cl::Event execution;
		compressionKernel.setArg<cl::Buffer>(0, d_input1);
		compressionKernel.setArg<cl::Buffer>(1, d_input2);
		compressionKernel.setArg<cl::Buffer>(2, d_input3);
		compressionKernel.setArg<cl::Buffer>(3, d_output);
		queue.enqueueNDRangeKernel(compressionKernel, cl::NullRange, count, wgSize, NULL, &execution);

		// Copy output data back to host
		cl::Event copy4;
		queue.enqueueReadBuffer(d_output, true, 0, size_output, h_outputGpu.data(), NULL, &copy4);

		std::cout << "GPU Computing Finished" << std::endl;

		// Print performance data
		Core::TimeSpan readTime = readEnd - readStart;
		Core::TimeSpan cpuTime = cpuEnd - cpuStart;
		Core::TimeSpan gpuTime = OpenCL::getElapsedTime(execution);
		Core::TimeSpan copyTime1 = OpenCL::getElapsedTime(copy1)+OpenCL::getElapsedTime(copy2)+OpenCL::getElapsedTime(copy3);
		Core::TimeSpan copyTime2 = OpenCL::getElapsedTime(copy4);
		Core::TimeSpan copyTime = copyTime1 + copyTime2;
		Core::TimeSpan overallGpuTime = gpuTime + copyTime;
		std::cout << "READ HDF5 Time: " << readTime.toString() << std::endl;
		std::cout << "CPU Time: " << cpuTime.toString() << std::endl;
		std::cout << "Memory copy Time: " << copyTime.toString() << std::endl;
		std::cout << "GPU Time w/o memory copy: " << gpuTime.toString() << " (speedup = " << (cpuTime.getSeconds() / gpuTime.getSeconds()) << ")" << std::endl;
		std::cout << "GPU Time with memory copy: " << overallGpuTime.toString() << " (speedup = " << (cpuTime.getSeconds() / overallGpuTime.getSeconds()) << ")" << std::endl;

		// Check whether results are correct
		std::size_t errorCount = 0;
		for (std::size_t i = 0; i < count; i++) {
			// Allow small differences between CPU and GPU results (due to different rounding behavior)
			if (!(std::abs (h_outputCpu[i] - h_outputGpu[i]) <= 1e-5)) {
				if (errorCount < 15)
					std::cout << "Result for " << i << " is incorrect: GPU value is " << h_outputGpu[i] << ", CPU value is " << h_outputCpu[i] << std::endl;
				else if (errorCount == 15)
					std::cout << "..." << std::endl;
				errorCount++;
			}
		}
		if (errorCount != 0) {
			std::cout << "Found " << errorCount << " incorrect results" << std::endl;
			return 1;
		}

//// To attach the output from GPU into a bits sequence
		size_t check_q = 0;
		size_t check_r = 0;
		index_out = 0;
		filled = 7;
		buff = 0;
		memset(out_buf.data(), 0, size_outbuf); //Reset the output buffer, at every end of a loop, this buffer should be pop out.
		for (size_t index=0; index<num_point; index++){
			if(h_outputGpu[index]==65535){		//Recognize the escape symbol for overflow values
				for(size_t i=0; i<16; i++)
					put_bit(1);

				unsigned int check_of = overflow_buf[index];
				for(int i=0; i<32 ; i++){
					put_bit((check_of>>(32-i))%2);
					//////////////////////////
				}

			}
			else{
				check_q = (h_outputGpu[index] >> (EXPONENT+1));		// To recognize golombcode from 16 bits memory space
				if(check_q!=0){
					for(size_t i= (check_q+1); i>1 ; i=(i>>1))		// The Rice code part (without suffix zero)
						put_bit(1);
				}

				for(size_t j= 0; j<= EXPONENT; j++){				// The Reminder part k+1 bits
					check_r = (h_outputGpu[index]>>(EXPONENT-j))%2;
					put_bit(check_r);
				}
			}

		}

	//if there is remaining bits in buff
		if(filled!=7){
			index_out = index_out+1;
			out_buf[index_out] = buff;
		}
		double compression_ratio = 0;
		size_t original_bytes = num_point*sizeof(double);
		compression_ratio = original_bytes / (double) index_out;
		cout << "compressed_size =" << index_out << endl;
		cout << "compression_ratio =" << compression_ratio << endl;

		buffer_p1 = buffer_p2;	// Transfer the pointer for next file process.
		buffer_p2 = buffer_p3;
		std::cout << "Success" << std::endl;

	}
	return 0;
}
