var Promise = require("bluebird");
var updateTimestamp = require("../updateTimestamp");
var getSunsetTimes = require("../sunsetTimes").getSunsetTimes;

function inString(char, str) {
    var list = str.split('');
        for (var i = 0; i < list.length; i++) {
            if (list[i] === char) {
            return true;
            }
        }
        return false;
}

module.exports = function(SalaahTime) {
    SalaahTime.observe('before save', updateTimestamp);
    //Magrib times are not to be stored in the db, but computed
    SalaahTime.validatesInclusionOf('type', { in : 'fzaej'.split(''),
            message: 'is not an allowed salaah type.'
    });
    SalaahTime.validatesLengthOf('type', {
        is: 1
    });

    SalaahTime.createOrUpdate = function(masjidId, type, datetime, cb) {
        //Make sure type is valid
        //Check if there exists a masjid with masjidId
        //Check if there is a salaah time with matching masjidId & type
        //  If yes, then update this instance with the new datetime
        //  If no, then create a new instance
        console.log("creating/updating", masjidId, type, datetime);

        var badTypeError = new Error("type should be one of f, z, a, e or j");
        if (type.length !== 1) {
            cb(badTypeError);
            return;
        }
        if (!inString(type, "fzae")) {
            cb(badTypeError);
            return;
        }
        var Masjid = SalaahTime.app.models.Masjid;
        Masjid.findById(masjidId, function(err, mInstance) {
                if (err != null) {
                    var msg = "Error when querying the db";
                    console.error(msg, err, mInstance, masjidId, type, datetime);
                    cb(new Error(msg));
                    return;
                }
                if (mInstance === null) {
                    var msg = "No masjid exists with that id";
                    console.error(msg, err, mInstance, masjidId, type, datetime);
                    cb(new Error(msg));
                    return;
                }
                // Create date objs for start and end of day
                var end_date = new Date(datetime.getTime());
                end_date.setHours(23, 59, 59, 999);
                var start_date = new Date(datetime.getTime());
                start_date.setHours(0, 0, 0, 0);
                var query = {
                    where: {
                        masjidId: masjidId,
                        type: type,
                        datetime: {between: [start_date, end_date]}
                    }
                };
                SalaahTime.findOne(query, function(err, sInstance) {
                    if (err != null) {
                        var msg = "Error when querying the db";
                        console.error(msg, err, sInstance, query);
                        cb(new Error(msg));
                        return;
                    }
                    if (sInstance) {
                        sInstance.updateAttribute('datetime', datetime, function(err, newInstance) {
                            if (err != null) {
                                var msg = "Error modifying db entry";
                                console.error(msg, err, newInstance, datetime);
                                cb(new Error(msg));
                                return;
                            }
                            cb(null, newInstance);
                        });
                    } else {
                        var newTime = {
                            masjidId: masjidId,
                            type: type,
                            datetime: datetime
                        };
                        SalaahTime.create(newTime, function(err, newInstance) {
                            if (err != null) {
                                var msg = "Error creating entry in db";
                                console.error(msg, err, newInstance, newTime);
                                cb(new Error(msg));
                                return;
                            }
                            cb(null, newInstance);
                        });
                    }
                });
        });
};
SalaahTime.remoteMethod(
    'createOrUpdate', {
        description: ["Check if a salaah time for the same salaah, ",
                    "masjid & date exists. If it does, update it with ",
                    "the new salaah time. Else, create a new db entry."],
        accepts: [{
            arg: 'masjidId',
            type: 'number',
            required: true
        }, {
            arg: 'type',
            type: 'string',
            required: true
        }, {
            arg: 'datetime',
            type: 'Date',
            required: true
        }],
        returns: {
            arg: 'instance',
            type: 'Object'
        },
        http: {
            path: '/create-or-update',
            verb: 'post'
        }
    }
);

    SalaahTime.getTimesForMultipleMasjids = function(date, salaahType, location, faveMasjidIds, cb) {
        SalaahTime.findAsync = Promise.promisify(SalaahTime.find, {context: SalaahTime});
        if ((faveMasjidIds === null || faveMasjidIds === undefined) && (location === null || location === undefined)) {
            return cb(null, []);
        }
        var Masjid = SalaahTime.app.models.Masjid;
        Masjid.findAsync = Promise.promisify(Masjid.find, {context: Masjid});
        faveMasjidIds = faveMasjidIds || [];

        var handleDBError = function(err) {
                var msg = "Couldn't retrieve data from db";
                console.error(msg, err, salaahType, location, faveMasjidIds);
                return cb(new Error(msg));
            };
        var addMasjidNamesAndLocsToSalaahTimes = function(salaahTimes, masjidNameLocMap) {
                return salaahTimes.map(function(s) {
                    s.masjidName = masjidNameLocMap[s.masjidId][0];
                    s.masjidLocation = masjidNameLocMap[s.masjidId][1];
                    return s;
                });
            };

        // Create date objs for start and end of day
        var today = date;
        var end_date = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate(), 23, 59, 59, 999));
        var start_date = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate(), 0, 0, 0, 0));

        Masjid.findAsync({fields: {id: true, name: true, location: true}, where: {id: {inq: faveMasjidIds}}})
            .then(function(faveMasjidNames) {
            var masjidNameLocMap = {};
            faveMasjidNames.forEach(function(m){masjidNameLocMap[m.id] = [m.name, m.location]});

            var fieldsToReturn = {type: true, masjidId: true, datetime: true};
            var baseWhereQuery = {datetime: {between: [start_date, end_date]}};
            if (salaahType && inString(salaahType, "fzae")) {
                baseWhereQuery.type = salaahType;
            }
            if (location) {
                //get the nearest masjids, add them to faveMasjidIds
                Masjid.findAsync({limit: 10, fields: {id: true, name: true, location: true}, where: {location: {near: location}}})
                    .then(function(masjids) {
                        faveMasjidIds = faveMasjidIds.concat(masjids.map(function(m){return m.id}));
                        masjids.forEach(function(m){masjidNameLocMap[m.id] = [m.name, m.location]});
                        baseWhereQuery.masjidId = {inq: faveMasjidIds};
                        return SalaahTime.findAsync({
                            fields: fieldsToReturn,
                            where: baseWhereQuery});
                    }).then(function(instances) {
                        //Get magrib times, add them to instances
                        if (!salaahType || salaahType == "m") {
                            //Get magrib times, add them to instances
                            var argList = Object.keys(masjidNameLocMap).map(function(k) {return {masjidId: k, location: masjidNameLocMap[k][1]}});
                            return getSunsetTimes(argList, date).then(function(sunsets) {
                                var result = addMasjidNamesAndLocsToSalaahTimes(instances.concat(sunsets), masjidNameLocMap);
                                return cb(null, result);
                            });
                        } else {
                            var result = addMasjidNamesAndLocsToSalaahTimes(instances, masjidNameLocMap);
                            return cb(null, result);
                        }
                    }).catch(handleDBError);
            } else {
                baseWhereQuery.masjidId = {inq: faveMasjidIds};
                SalaahTime.findAsync({
                    fields: fieldsToReturn,
                    where: baseWhereQuery
                }).then(function(instances) {
                    if (!salaahType || salaahType == "m") {
                        //Get magrib times, add them to instances
                        var argList = Object.keys(masjidNameLocMap).map(function(k) {return {masjidId: k, location: masjidNameLocMap[k][1]}});
                        return getSunsetTimes(argList, date).then(function(sunsets) {
                            var result = addMasjidNamesAndLocsToSalaahTimes(instances.concat(sunsets), masjidNameLocMap);
                            return cb(null, result);
                        });
                    } else {
                        var result = addMasjidNamesAndLocsToSalaahTimes(instances, masjidNameLocMap);
                        return cb(null, result);
                    }
                }).catch(handleDBError);
            }
        }).catch(handleDBError);
    };

    SalaahTime.remoteMethod(
        'getTimesForMultipleMasjids', {
            description: ["Get salaah times for today for masjids for a particular ",
                        "salaah type (optional), for certain masjids and for ",
                        "masjids near to a certain location"],
            accepts: [{
                arg: 'date',
                type: 'Date',
                required: true
            },{
                arg: 'salaahType',
                type: 'string',
                required: false
            }, {
                arg: 'location',
                type: 'GeoPoint',
                required: false
            }, {
                arg: 'faveMasjidIds',
                type: ['number'],
                required: false
            }],
            returns: {
                arg: 'res',
                type: 'array'
            },
            http: {
                path: '/times-for-multiple-masjids',
                verb: 'get'
            }
        }
    );

    SalaahTime.getTimesForMasjidsForToday = function(salaahType, location, faveMasjidIds, cb) {
        return SalaahTime.getTimesForMultipleMasjids(new Date(), salaahType, location, faveMasjidIds, cb);
    };

    SalaahTime.remoteMethod(
        'getTimesForMasjidsForToday', {
            description: ["Deprecated method, due to be removed in version 2.0.0. ",
                        "Get salaah times for today for masjids for a particular ",
                        "salaah type (optional), for certain masjids and for ",
                        "masjids near to a certain location. The determination ",
                        "of today is done by server time."],
            accepts: [{
                arg: 'salaahType',
                type: 'string',
                required: false
            }, {
                arg: 'location',
                type: 'GeoPoint',
                required: false
            }, {
                arg: 'faveMasjidIds',
                type: ['number'],
                required: false
            }],
            returns: {
                arg: 'res',
                type: 'array'
            },
            http: {
                path: '/times-for-masjids-for-today',
                verb: 'get'
            }
        }
    );
};
