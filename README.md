# SkipUseAPI Library
This SkipUseAPI Library provides sample Java code for accessing the SkipUseAPI microservice. About page and the API documentation will be available at https://www.skipuse.com/rest-api-documentation/. NOTE: Accounts are currently limited to Beta users. Non-API users; look for a WordPress plugin to use with your site content in the future.

## Configuration:
This demo code uses the SkipUseAPI and was pulled out of another Spring Boot project that is still in development. Although the code will not run on its own, you can place this folder structure within your Java project (changing the package imports) and use the SkipUseManager class to test your SkipUse access. Be sure to note the import dependencies in the service class that use Spring for http calls and Jackson for serialization. There are JUnit tests for the SkipUseManager and SkipUseAPIService classes that provide insights into using some of the most common API functionality.

## Instructions:
The SkipUseManager is the intended access point for communication with the server API. Examples of login and other feature usage are given in the SkipUseManagerTest class.

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
120222:
* Added: SearchOption modifiers: PERCENT_GREATER_THAN, PERCENT_LESS_THAN.
* Updated: SearchOption Lists to String type to handle post-fix percentages.
* Removed: RAMP_SEARCH SearchOption.
* Fix: Manager changed to only get one Pick instead of the whole collection to update a single Pick StopUsing flag.
* Clean-up: API token helper, manager, service and tests.
* Sample Music Project: Add StopUsing flag helper.

122121:
* Naming update: MemberPickIDCollection to MemberCollection.
* Naming update: SearchOption: NORMAL to SearchOption: QUEUE. (Still the default search method)
* Added: SearchOption: PICK_INFO. (replacement for 'PickID' in the PickQuery)
* Added: pickIDList in PickQuery. Which can override the MemberCollection  Pick IDs that are used for searches. This allows for Pick ID changes on-the-fly or limiting searches to a select set of Picks without having to update the MemberCollection first before using a PickQuery.
* Removed: Ramp, Search and Play mode enums. (now handled by the ResultOptions)
* Added: a demo test to show the updated PickQuery 'NOT' CategoryOption. For including categories before the modifier key word but excluding ones after.
* Added: a demo test to show the new searching with multiple members and their categories.
* Sample Music Project: added ability for multiple listening members to select their own and other member categories.

051421:
* Naming update: Search Mode changed to SearchOption.
* Fix: Added missing MemberCollectionID for marking a Pick with a Category.
* Removed: SkipUseManager auto-login option. Login must now be called initially. The manager will continue to re-login if needed until logout is used.
* Added: A searchOrigin field to the Pick object which can be used to determine how a Pick was chosen from a PickQuery's SearchOptionList.
* Added: SearchOption: RANDOM: Returning Picks randomly by their weighted auto-percentage rating.
* Updated: SkipUseManager to be an Enum Singleton.
* Sample Music Project: improved auto-login by reducing extra calls to check if the proxy is still valid.
* Sample Music Project: add additional date check to ignore if a song is offered before the song exclusion period has ended.
* Sample Music Project: Fixed a missing CollectionID for a member when marking a Pick with a category.

081820:
* Added: PickIDCountAdvance object which can be used in batch and delayed updates that change a member's Pick Skip and Use counts advancement values 
* Added: SkipUseManager using new PickIDCountAdvanceList object for batch member Pick updates
* Updated: Pick Query searching options were condensed into 3 lists to set options and modifiers for selection, result and advanced changes
* Sample Music Project: updates to use more of the Pick Query search options

111119:
* Added: RESET modifier for the NORMAL search mode for resetting the Pick Query queue
* Updated: the ignoreRecentlyUpdated to use number of hours instead of true/false
* Sample Music Project: added Category list modifiers for "ANY, NONE, NOT"

070619:
* Added: new search modes: BALANCED and RACING
* Added: 2 search modes selected will be blended into a single Pick list
* Added: useTimeOfDay option to PickQuery for TEMPORAL replacement
* Added: category modes: NOT to exclude Picks marked with a category
* Update: TEMPORAL search mode replaced by RACING search mode
* Update: change collectionID to memberID for collections
* Update: searchMode to searchModeList to handle multiple search modes
* Update: PickQuery to use String List vs Object type list for API calls
* Sample Music Project: updated to handle multiple search modes and use NOT category mode

031419:
* Added: Member's can now have their own collection
* Added: Pick Query can now search from a member's collection by using the member ID
* Added: Temporal search mode for Pick Query
* Added: Owner profile object for changing email, name and password
* Removed: Trending percentage meta from Pick
* Update: search mode enum options for Pick Query
* Update: default and unset values were switched from '-1' to now use '0'
* Update: IDs for collections, members, accounts, now use Long data type
* Sample Music Project: improved server connection testing and recovery
* Sample Music Project: now using the new Temporal search mode option in the regular Pick Query
* Sample Music Project: added some tests

121918:  
* Fix: Missing memberID when marking stop-using Picks
* Update: SkipUseAPI changes for Put calls vs Patch
* Update: Debug option for PickQuery feedback
* Update: SkipUseManagerTest where get user Picks only returns current collection Picks
* Added: More SkipUseManager automatic log-ins
* Added: SkipUseManagerTest example to show how to only get a few Picks from a large collection
* Sample Music Project: break-up getNextSong method
* Sample Music Project: added reference for MemberCategoryList
* Sample Music Project: remove Picks from current list that might have been updated

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
* FIXED: a problem when getting one pick for a member and it was a new pick. A new Pick is returned with a member ID of -1. Now adding the member ID when getting a new Pick and then updating it using the Manager.

060618: 
* Copy updates to the README.md, Manager, Service and test classes. 

060518: 
* README Markdown conversion 
 
060418: 
* Initial demo files prepared 

