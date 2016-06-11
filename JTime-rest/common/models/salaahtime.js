var updateTimestamp = require("../updateTimestamp");

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

    SalaahTime.getTimesForMasjidsForToday = function(salaahType, location, faveMasjidIds, cb) {
        if ((faveMasjidIds === null || faveMasjidIds === undefined) && (location === null || location === undefined)) {
            return cb(null, []);
        }
        var Masjid = SalaahTime.app.models.Masjid;
        faveMasjidIds = faveMasjidIds || [];

        // Create date objs for start and end of day
        var today = new Date();
        var end_date = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate(), 23, 59, 59, 999));
        var start_date = new Date(Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate(), 0, 0, 0, 0));

        var baseWhereQuery = {datetime: {between: [start_date, end_date]}};
        if (salaahType && inString(salaahType, "fzae")) {
            baseWhereQuery.type = salaahType
        }
        if (location) {
            //get the nearest masjids, add them to faveMasjidIds
            Masjid.find({limit: 10, fields: {id: true}, where: {location: {near: location}}}, function(err, masjids) {
                if (err) {
                    var msg = "Couldn't retrieve data from db";
                    console.error(msg, masjids, salaahType, location, faveMasjidIds);
                    return cb(new Error(msg));
                }
                faveMasjidIds = faveMasjidIds.concat(masjids.map(function(m){return m.id}));
                baseWhereQuery.masjidId = {inq: faveMasjidIds};
                SalaahTime.find({
                    fields: {type: true, masjidId: true, datetime: true},
                    where: baseWhereQuery
                }, function(err, instances) {
                    if (err) {
                        var msg = "Couldn't retrieve data from db";
                        console.error(msg, instances, salaahType, location, faveMasjidIds);
                        return cb(new Error(msg));
                    }
                    return cb(null, instances);
                })
            });
        } else {
            baseWhereQuery.masjidId = {inq: faveMasjidIds};
            SalaahTime.find({
                fields: {type: true, masjidId: true, datetime: true},
                where: baseWhereQuery
            }, function(err, instances) {
                if (err) {
                    var msg = "Couldn't retrieve data from db";
                    console.error(msg, instances, salaahType, location, faveMasjidIds);
                    return cb(new Error(msg));
                }
                return cb(null, instances);
            });
        }
    };

SalaahTime.remoteMethod(
    'getTimesForMasjidsForToday', {
        description: ["Get salaah times for today for masjids for a particular ",
                    "salaah type (optional), for certain masjids and for ",
                    "masjids near to a certain location"],
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
