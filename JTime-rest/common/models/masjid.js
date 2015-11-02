var updateTimestamp = require("../updateTimestamp");

module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
    Masjid.validatesLengthOf('name', {
        max: 50
    });

    Masjid.getTodayTimes = function(id, cb) {
        var SalaahTime = Masjid.app.models.SalaahTime;
        // Create date objs for start and end of day
        var end_of_today = new Date();
        end_of_today.setHours(23, 59, 59, 999);
        var start_of_today = new Date();
        start_of_today.setHours(0, 0, 0, 0);
        // Find salaah times for today for specified masjid
        SalaahTime.find({
                where: {
                    masjidId: id,
                    datetime: {
                        between: [start_of_today, end_of_today]
                    }
                },
                fields: {
                    type: true,
                    datetime: true
                }
            },
            function(err, instances) {
                if (err != null) throw err;
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
                path: '/:id/today-times',
                verb: 'get'
            }
        }
    );
};
