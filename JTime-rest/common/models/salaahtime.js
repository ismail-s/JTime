var updateTimestamp = require("../updateTimestamp");

module.exports = function(salaahTime) {
    salaahTime.observe('before save', updateTimestamp);
    salaahTime.validatesInclusionOf('type', { in : 'fzamej'.split(''),
            message: 'is not an allowed salaah type.'
    });
    salaahTime.validatesLengthOf('type', {
        is: 1
    });
};
