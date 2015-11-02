var updateTimestamp = require("../updateTimestamp");

module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
    Masjid.validatesLengthOf('name', {
        max: 50
    });

    Masjid.getTodayTimes = function(id, cb) {
        var today = new Date();
        Masjid.getTimes(id, today, function(err, instances) {
            if (err != null) {
                console.error(err, instances, id);
                cb(500);
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
