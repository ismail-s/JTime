var updateTimestamp = require("../updateTimestamp");

module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
    Masjid.validatesLengthOf('name', {max: 50});
};
