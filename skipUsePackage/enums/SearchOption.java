package com.autogilmore.throwback.skipUsePackage.enums;

/* 
 * Enumerations for the PickQuery for how Picks will be searched for.
*/
public enum SearchOption {
    // searching modes
    NORMAL, RANDOM, BALANCED, RACING, FAVORITE, WORST, STOP_USING_ONLY,
    // modifier for search modes
    GET_MORE_IF_SHORT, USE_TIME_OF_DAY, INCLUDE_STOP_USING, ENHANCE;
}