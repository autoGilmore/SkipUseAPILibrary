# SkipUseAPI Library
This SkipUseAPI Library provides sample Java code for accessing the SkipUseAPI microservice. About and API documentation will be available at www.SkipUse.com in July 2018.

## Configuration:
This demo code uses the SkipUseAPI and was pulled out of another Spring Boot project that is still in development. Although the code will not run on its own, you can place this folder structure within your Java project (changing the package imports) and use the SkipUseManager class to test your SkipUse access. Be sure to note the import dependencies in the service class that use Spring for http calls and Jackson for serialization. There are JUnit tests for the SkipUseManager and SkipUseAPIService classes that provide insights into using some of the most common API functionality.

## Instructions:
The SkipUseManager is the intending access point for communication with the server API. Examples of login and other feature usage are given in the SkipUseManagerTest class.

## File manifest:
This example package/library includes:
* dataObjects folder: holds the JSON representation of various incoming server data which can be converted into other POJOs for your usage
* enums folder: contains a collection of constants and optional search parameters
* exception folder: contains a custom exception for the SkipUse service
* manager folder: contains the SkipUseManager and test files
* service folder: contains the SkipUseAPIService and test files used by the manager 
* tokenData folder: contains a SkipUseToken and helper methods required by the service for communication

## Copyright and licensing information:
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Contact information:
skipuseapi@gmail.com

### Known bugs:
### Troubleshooting:

## Changelog:

060618: Copy updates to the README.md, Manager, Service and test classes. 

060518: README Markdown conversion 
 
060418: Initial demo files prepared 

