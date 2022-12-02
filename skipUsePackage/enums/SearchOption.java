package com.autogilmore.throwback.skipUsePackage.enums;

/* 
 * Enumerations for the PickQuery for how Picks will be searched for.
*/
public enum SearchOption {
    // searching modes
    QUEUE, RANDOM, BALANCED, RACING, FAVORITE, WORST, STOP_USING_ONLY, PICK_INFO,
    // modifier for search modes
    GET_MORE_IF_SHORT, USE_TIME_OF_DAY, PERCENT_GREATER_THAN, PERCENT_LESS_THAN, INCLUDE_STOP_USING, ENHANCE;
}