var settings = require('../../settings')

function request(options, cb) {
    if (options.uri === 'https://www.googleapis.com/oauth2/v3/tokeninfo') {
        // make sure login succeeds
        setTimeout(function() {
            cb(null, {statusCode: 200}, {aud: settings.clientId, sub: 12345, email: 'test@example.com'})
        }, 0)
    }
}

module.exports = request
