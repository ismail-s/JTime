var Promise = require("bluebird");
var request = require('request');
var moment = require("moment");
var settings = require('../settings');

module.exports = function(location, date) {
    var options = {
        uri: 'http://api.geonames.org/timezoneJSON',
        qs: {
                lat: location.lat,
                lng: location.lng,
                date: moment(date).format("YYYY-MM-DD"),
                username: settings.geoNamesUsername
            },
            json: true
    };
    return new Promise(function(resolve, reject) {
        request(options, function(error, response, body) {
            if (error || response.statusCode != 200 || body.status || !body.sunset) {
                console.warn("Error in geonames response", error, response, body);
                reject(error || body.status);
            } else {
                resolve(new Date(body.sunset));
            }
        });
    });
};
