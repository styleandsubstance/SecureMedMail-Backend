define("uploadshield/util/ajax", ["./_base", "dojo/_base/xhr", "dojo/json"], function(util, xhr, JSON){

    util.jsonPost = function(address, data, callback) {
        console.debug("Submitting json post to " + address + " with data:");
        console.debug(data);
        xhr.post({
            // The URL to request
            url: address,
            headers: { "Content-Type": "application/json"},
            handleAs: "json",
            postData: JSON.stringify(data),
            // The method that handles the request's successful result
            // Handle the response any way you'd like!
            load: function (result) {
                console.debug(result);
                callback(result);
            }
        });
    }

    util.get = function(address, callback) {
        console.debug("Submitting ajax request to " + address);

        xhr.get({
            // The URL to request
            url: address,
            handleAs: "json",
            load: function (result) {
                console.debug(result);
                callback(result);
            }
        });
    }

    util.jsonPostSync = function(address, data, callback) {
        console.debug("Submitting json post to " + address + " with data:");
        console.debug(data);
        xhr.post({
            // The URL to request
            url: address,
            headers: { "Content-Type": "application/json"},
            handleAs: "json",
            sync: true,
            postData: JSON.stringify(data),
            // The method that handles the request's successful result
            // Handle the response any way you'd like!
            load: function (result) {
                console.debug(result);
                callback(result);
            }
        });
    }


    return util;
});
