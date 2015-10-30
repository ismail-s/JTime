var updateTimestamp = require("../updateTimestamp");

module.exports = function(salaahTime) {
    salaahTime.observe('before save', updateTimestamp);
};
