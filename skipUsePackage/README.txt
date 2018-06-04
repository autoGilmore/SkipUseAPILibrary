------------
Example Java code for using the www.SkipUseApi.com API.
------------
	About and API Documentation at: www.SkipUse.com


Configuration instructions:
	This demo was written as part of another demo project using the SkipUseAPI. This code was pulled out 
	of a project that is intended to be converted into a package in the future. 
	Currently it will not run on its own. 
	Still, you can place this folder structure with-in a Java project and use the SkipUseManager.java to test your
	SkipUse App project. There are JUnit tests for the SkipUseManager.java and SkipUseAPIService.java classes that 
	test some of the most common API functionality.

Operating instructions:
	The SkipUseManager is the intending access for communication with the server API. Examples of login
	and other feature usage are given in the SkipUseManagerTest.java class.

A file manifest:
	This example package/library includes:
		dataObjects folder: holds the JSON representation of various incoming server data which can be converted into
		other POJOs for normal usage.
		enums folder: for optional search and standard Skip Use Pass representations.
		exception folder: containing a custom exception for the SkipUse service.
		manager folder: containing the SkipUseManager and test files.
		service folder: containing the SkipUseAPIService used by the manager and test files.
		tokenData folder: containing a SkipUseToken and helper file used by the service for communication to the server.

Copyright and licensing information:
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.

Contact information for the distributor or programmer:
	TBA

Known bugs:
Troubleshooting:
Credits and acknowledgments

A changelog:
	060418: Initial demo files prepared.
