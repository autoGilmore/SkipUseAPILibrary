package com.autogilmore.throwback.skipUsePackage.enums;

/* 
 * Enumerations for the PickQuery optional result modifications.
*/
public enum ResultOption {
    // ordering of Picks
    RAMP_NONE, RAMP_RATE_DOWN, RAMP_RATE_UP, RAMP_OLDEST, RAMP_NEWEST,
    // combining of Picks
    BLEND, MERGE,
    // include category info on returned Pick
    INCLUDE_CATEGORY_INFO
}
