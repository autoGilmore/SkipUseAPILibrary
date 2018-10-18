# SkipUseAPI Library
This SkipUseAPI Library provides sample Java code for accessing the SkipUseAPI microservice. About and API documentation will be available at www.SkipUse.com in July 2019.

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
101718:   
* Update: A Pick ID may not contain ' @@@ ' characters
* New: MemberListPickIDList and PickList data objects
* Added: Last updated timestamp for Pick ID collection 
* Added: Undo for the last collection change
* Added: Get a list of all Picks from a list of members
* Added: properties now can include test user credentials
* Improved: test members more defined usages
* Improved: API more detailed error messages
* Sample Music Project: improved time-out recovery for automatically logging back in
* Sample Music Project: added converter for listening user IDs to SkipUse members

082718:
* Update to show that splitting Pick IDs in a collection is done by comma + space
* Updated tests to use the CSV comma + space split
* Now using system out vs. system err for error feedback
* FIXED: Music selection demo issue when marking a Song ID with a category to create the category if it does not exist
* Added additional error check in music selection demo to log-in if needed

080318:   
* Maximum collection size increased from 5,000 to 50,000 Pick IDs
* Manager now logs-out before attempting to log-in
* Updated tests to verify locally-stored members are properly retrieved 
* Updated API test to remove test members for testing consistency
* Sample Music Project: added a category list and reference to the properties file for name and password
* FIXED: a problem when getting locally-stored members were not found. Now will automatically get the member list from the service when referenced 

071218:   
* Added properties file for settings
* Added an example project for a music selection helper
* Updated test and Manager files to reference property settings
* FIXED: an issue with the timestamp not being serialized because of a leading underscore

062818:
* Updated standards for boolean variables and renamed.
* Added a last updated timestamp field to Pick.
* Added SkipUseParameters file for API constants.
* Ramp mode added 'newest' and 'oldest' for PickQuery usage.
* Moved the server API calls from the SkipUseAPIService to a new SkipUseAPI file.
* FIXED: a problem when getting one pick for a member and it was a new pick. A new Pick is returned with member ID of -1. Now adding the member ID when getting a new Pick and then updating it using the Manager.

060618: 
* Copy updates to the README.md, Manager, Service and test classes. 

060518: 
* README Markdown conversion 
 
060418: 
* Initial demo files prepared 

