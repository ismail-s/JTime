var updateTimestamp = require("../updateTimestamp");

module.exports = function(Masjid) {
    Masjid.observe('before save', updateTimestamp);
};
