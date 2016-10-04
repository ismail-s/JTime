var updateTimestamp = require("../updateTimestamp");
var async = require('async');
var loopback = require('loopback');
var settings = require('../../settings');
var gmAPI = require("googlemaps");
var GoogleMapsAPI = new gmAPI({
    key: settings.googleMapsKey,
    secure: true
});

//Taken from http://stackoverflow.com/a/3561711
RegExp.escape = function(s) {
    return s.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
};

/**
 * Call the Google Maps api with to try and get a human-readable address for
 * the location data.location. When no address is obtained, cb is called with
 * the original data object, else data.humanReadableAddress is set to be the
 * human-readable address, and cb is called with the modified data object.
 *
 * @param {Object} data - The object to add the address to & pass to cb
 * @param {Function} cb - The callback to call with the data object
 */
function doReverseGeocode(data, cb) {
    var reverseGeocodeParams = {
        "latlng": data.location.lat.toString() + "," + data.location.lng.toString(),
        "language": "en"
    };
    GoogleMapsAPI.reverseGeocode(reverseGeocodeParams,
        function(err, result) {
            if (err || result.status !== "OK") {
                console.error("Didn't get at least 1 address.",
                    "Creating masjid without human-readable address", err, result);
                return cb(data);
            }
            console.log("got google maps reverse geocode results: ", result);
            var formattedAddress = result.results[0].formatted_address;
            data.humanReadableAddress = formattedAddress;
            return cb(data);
        });
}


module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
    Masjid.validatesLengthOf('name', {
        max: 50
    });

    /**
     * Check to see if any of the masjids in instances conflict with masjid.
     * Calls cb with null if no conflicts occur, else calls cb with an Error if
     * there were any conflicts.
     *
     * @param {Object} masjid - the masjid to compare other masjids to
     * @param {Object[]} instances - a list of existing masjids to compare to
     * @param {Fuction} cb - the callback to call with results of conflict check
     */
    Masjid.checkForConflictingMasjids = function(masjid, instances, cb) {
        //Duplicate names are allowed, but not within x distance of each other
        //Masjids not allowed within y distance of each other
        if (instances == null || instances.length <= 0) {
            //no instances to conflict with, so no conflicts
            return cb(null);
        }
        //iterate through instances, check if any of them conflict with masjid
        for (var i = 0; i < instances.length; i++) {
            var inst = instances[i];
            var distance = loopback.GeoPoint.distanceBetween(masjid.location, inst.location, {
                type: 'meters'
            });
            var match = inst.name.match(new RegExp(RegExp.escape(masjid.name), 'i'));
            if (match != null && distance < settings.minDistanceBetweenMasjidsWithSameName) {
                var msg = "Too close to an existing masjid";
                console.error(msg, inst);
                cb(new Error(msg));
                return;
            }
            else if (distance < settings.minDistanceBetweenMasjidsWithDiffName) {
                var msg = "Too close to an existing masjid";
                console.error(msg, inst);
                cb(new Error(msg));
                return;
            }
        }
        //no instances conflict with masjid
        return cb(null);
    };

    /**
     * Create several masjids, as specified in arr. Calls cb with any errors
     * that occurred whilst creating the masjids, and with the array of created
     * masjids
     *
     * @param {Object[]} arr - the array of masjids to create
     * @param {Function} cb - the callback to invoke with all errors & results
     */
    Masjid.handleCreatingArrayOfMasjids = function(arr, cb) {
        //Note that this code was originally taken from the loopback source
        //code itself, and then modified slightly.
        //See https://github.com/strongloop/loopback-datasource-juggler/blob/3113333cb2ab62ad3863fc5ceee856a4facb37cb/lib/dao.js#L242
        //for the original code.
        // Undefined item will be skipped by async.map() which internally uses
        // Array.prototype.map(). The following loop makes sure all items are
        // iterated
        for (var i = 0, n = arr.length; i < n; i++) {
            if (arr[i] === undefined) {
                arr[i] = {};
            }
        }
        async.map(arr, function(item, done) {
            Masjid.create(item, function(err, result) {
                // Collect all errors and results
                done(null, {
                    err: err,
                    result: result || item
                });
            });
        }, function(err, results) {
            if (err) {
                return cb(err, results);
            }
            // Convert the results into two arrays
            var errors = null;
            var arr = [];
            for (var i = 0, n = results.length; i < n; i++) {
                if (results[i].err) {
                    if (!errors) {
                        errors = [];
                    }
                    errors[i] = results[i].err;
                }
                arr[i] = results[i].result;
            }
            cb(errors, arr);
        });
        return arr;
    };

    /**
     * Wrapper around the builtin create method that performs some validation
     * to make sure no existing masjid exists that is too close to the masjid
     * to be created.
     *
     */
    Masjid.on('dataSourceAttached', function(obj) {
        var create = Masjid.create;
        Masjid.create = function(data, cb) {
            var originalThis = this;

            // We only want the name and location of a masjid. All other
            // properties of the masjid model are computed
            if (data.humanReadableAddress || data.createdAt || data.lastModifiedAt || data.id) {
                var msg = "Only a name and location should be provided, nothing else";
                cb(new Error(msg));
                return;
            }

            if (Array.isArray(data)) {
                return Masjid.handleCreatingArrayOfMasjids(data, cb);
            }
            Masjid.find({
                fields: {
                    name: true,
                    location: true
                }
            }, function(err, instances) {
                if (err != null) {
                    var msg = "Error when searching through db";
                    console.error(msg, err, instances);
                    cb(new Error(msg));
                    return;
                }
                Masjid.checkForConflictingMasjids(data, instances, function(err) {
                    if (err != null) {
                        return cb(err);
                    }
                    return doReverseGeocode(data, function(data) {
                        create.apply(originalThis, [data, cb]);
                    });
                });
            });
        };
    });

    Masjid.getTimes = function(id, date, cb) {
        var SalaahTime = Masjid.app.models.SalaahTime;
        // Create date objs for start and end of day
        var end_date = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 23, 59, 59, 999));
        var start_date = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 0, 0, 0, 0));
        console.info("Getting times for masjid id", id, "start date is", start_date, "end date is", end_date);
        // Find salaah times for the specified masjid for the specified day
        SalaahTime.find({
                where: {
                    masjidId: id,
                    datetime: {
                        between: [start_date, end_date]
                    }
                },
                fields: {
                    type: true,
                    datetime: true
                }
            },
            function(err, instances) {
                if (err != null) {
                    console.error(err, instances, id, date);
                    cb(500);
                    return;
                }
                cb(null, instances);
            });
    };
    Masjid.remoteMethod(
        'getTimes', {
            accepts: [{
                arg: 'id',
                type: 'number',
                required: true
            }, {
                arg: 'date',
                type: 'Date',
                required: true
            }],
            returns: {
                arg: 'times',
                type: 'Array'
            },
            http: {
                path: '/:id/times',
                verb: 'get'
            }
        }
    );

    Masjid.getTodayTimes = function(id, cb) {
         var today = new Date();
         Masjid.getTimes(id, today, function(err, instances) {
             if (err != null) {
                 console.error(err, instances, id);
                 cb(500);
                 return;
             }
             cb(null, instances);
         });
     };
     Masjid.remoteMethod(
         'getTodayTimes', {
             description: ["Deprecated method, due to be removed in v2.0.0. ",
                            "Returns salaah times for a given masjid, for ",
                            "today, where today is determined by server time."],
             accepts: [{
                 arg: 'id',
                 type: 'number',
                 required: true
             }],
             returns: {
                 arg: 'times',
                 type: 'Array'
             },
             http: {
                 path: '/:id/times-for-today',
                 verb: 'get'
             }
         }
     );
};
