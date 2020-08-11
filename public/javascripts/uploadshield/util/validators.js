define("uploadshield/util/validators", ["./_base"], function(util){

    util.validateUsername = function(value, constraints){
        console.debug("Here");
        console.debug(value);
        console.debug(constraints);
        return false;
    }

    util.validatePassword = function(value, constraints) {
        return false;
    }

    return util;
});
