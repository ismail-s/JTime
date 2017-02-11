var Promise = require('bluebird')
var request = require('request')
var moment = require('moment')
var cacheManager = require('cache-manager')
var memoryCache = cacheManager.caching({store: 'memory', max: 10000, ttl: 14 * 24 * 3600/* seconds */, promiseDependency: Promise})
var settings = require('../settings')

/**
 * Geonames has some weird style of returning results, and sometimes returns the
 * sunset time for the wrong day in one field and the right day in another field.
 * This function attempts to make sense of all this odd behaviour.
 */
function getSunsetFromBody (body, date) {
  if (!body || !body.sunset) {
    return null
  }
  var sunset1 = new Date(body.sunset)
  var sunset2 = body.dates && body.dates.length && body.dates[0] && body.dates[0].sunset && new Date(body.dates[0].sunset)
  if (moment(sunset1).format('YYYY-MM-DD') === moment(date).format('YYYY-MM-DD')) {
    return sunset1
  } else if (sunset2 && moment(sunset2).format('YYYY-MM-DD') === moment(date).format('YYYY-MM-DD')) {
    return sunset2
  } else {
    return null
  }
}

function getSunsetTime (location, date) {
    // Date is normalised (ie info about hour/min/seconds is thrown away) for
    // caching purposes
  var normalisedDate = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 0, 0, 0, 0))
  var key = [location.lat.toString(), location.lng.toString(), normalisedDate].join(',')
  return memoryCache.wrap(key, function () {
    var options = {
      uri: 'http://api.geonames.org/timezoneJSON',
      qs: {
        lat: location.lat,
        lng: location.lng,
        date: moment(date).format('YYYY-MM-DD'),
        username: settings.geoNamesUsername
      },
      json: true
    }
    return new Promise(function (resolve, reject) {
      request(options, function (error, response, body) {
        var sunset = getSunsetFromBody(body, date)
        if (error || response.statusCode !== 200 || body.status || !sunset) {
          console.warn('Error in geonames response', error, response, body)
          reject(new Error(error || body.status || 'Error obtaining sunset time'))
        } else {
          resolve(sunset)
        }
      })
    })
  })
}

/**
 * Get several sunset times for different masjids, for the same date. Returns
 * a promise that always resolves with a list of objects of the form eg
 * {masjidId: 1, datetime: sunsetDatetime, type: "m"}. Any sunset times that
 * could not be obtained are silently excluded from the resolved list.
 *
 * @param {Array} argList - A list of objects of the form eg
 *  {masjidId: 1, location: {lat: 1.2, lng: 3.4}}.
 * @param {Date} date - The date for which the sunset times is being obtained.
 */
function getSunsetTimes (argList, date) {
  var promises = []
  argList.forEach(function (elem) {
    promises.push(getSunsetTime(elem.location, date).then(function (sunset) {
      return {masjidId: elem.masjidId, datetime: sunset, type: 'm'}
    }))
  })
  return Promise.all(promises.map(function (promise) { return promise.reflect() })).filter(function (maybeSunset) {
    return maybeSunset.isFulfilled()
  }).map(function (sunset) { return sunset.value() })
}

module.exports = {
  getSunsetTime: getSunsetTime,
  getSunsetTimes: getSunsetTimes
}
