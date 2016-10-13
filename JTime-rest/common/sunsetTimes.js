var sunCalc = require("suncalc");
var tzwhere = require("tzwhere");
tzwhere.init();

module.exports = function(location, date) {
    var utcSunset = sunCalc.getTimes(date, location.lat, location.lng).sunset;
    var tzOffset = tzwhere.tzOffsetAt(location.lat, location.lng, utcSunset.getTime());
    var sunset = new Date(utcSunset.getTime() + tzOffset);
    return sunset;
};