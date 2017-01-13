var logger = require('./logger')

// Mock the googlemaps module. Only function mocked is the reverseGeocode one.
// The mocked version of the function always succeeds and returns "Test Address".
var googleMapsMock = function () {}

googleMapsMock.prototype.reverseGeocode = function (params, cb) {
  setTimeout(function () {
    logger.disableLogger()
    cb(null, {
      status: 'OK',
      results: [{formatted_address: 'Test Address'}]
    })
    logger.enableLogger()
  }, 0)
}

module.exports = googleMapsMock
