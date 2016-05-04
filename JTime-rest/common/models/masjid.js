var updateTimestamp = require("../updateTimestamp");
var async = require('async');
var loopback = require('loopback');
var settings = require('../../settings');

//Taken from http://stackoverflow.com/a/3561711
RegExp.escape= function(s) {
    return s.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
};

module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
    Masjid.validatesLengthOf('name', {
        max: 50
    });

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
            if (Array.isArray(data)) {
                // Undefined item will be skipped by async.map() which internally uses
                // Array.prototype.map(). The following loop makes sure all items are
                // iterated
                for (var i = 0, n = data.length; i < n; i++) {
                    if (data[i] === undefined) {
                        data[i] = {};
                    }
                }
                async.map(data, function(item, done) {
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
                    var data = [];
                    for (var i = 0, n = results.length; i < n; i++) {
                        if (results[i].err) {
                            if (!errors) {
                                errors = [];
                            }
                            errors[i] = results[i].err;
                        }
                        data[i] = results[i].result;
                    }
                    cb(errors, data);
                });
                return data;
            }
            else {
                //Duplicate names are allowed, but not within x distance of each other
                //Masjids not allowed within y distance of each other
                // var query = {
                //     where: {name: {regexp: new RegExp(RegExp.escape(data.name), 'i')}}};
                Masjid.find({fields: {name: true, location: true}}, function(err, instances) {
                    if (err != null) {
                        var msg = "Error when searching through db";
                        console.error(msg, err, instances);
                        cb(new Error(msg));
                        return;
                    }
                    if (instances != null && instances.length > 0) {
                        //Check if we have a conflicting instance. If no, then create.apply
                        console.log('am here');
                        for (var i = 0; i < instances.length; i++) {
                            var inst = instances[i];
                            var distance = loopback.GeoPoint.distanceBetween(data.location, inst.location, 'meters');
                            var match = inst.name.match(new RegExp(RegExp.escape(data.name), 'i'));
                            if (match != null) {
                                if (distance < settings.minDistanceBetweenMasjidsWithSameName) {
                                    var msg = "Too close to an existing masjid";
                                    console.error(msg, err, inst);
                                    cb(new Error(msg));
                                    return;
                                }
                            } else {
                                if (distance < settings.minDistanceBetweenMasjidsWithDiffName) {
                                    var msg = "Too close to an existing masjid";
                                    console.error(msg, err, inst);
                                    cb(new Error(msg));
                                    return;
                                }
                            }
                        }
                        return create.apply(originalThis, [data, cb]);
                    } else {
                        return create.apply(originalThis, [data, cb]);
                    }
                });
            }
        };
    });

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
    Masjid.getTimes = function(id, date, cb) {
        var SalaahTime = Masjid.app.models.SalaahTime;
        // Create date objs for start and end of day
        var end_date = new Date(date.getTime());
        end_date.setHours(23, 59, 59, 999);
        var start_date = new Date(date.getTime());
        start_date.setHours(0, 0, 0, 0);
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
};
