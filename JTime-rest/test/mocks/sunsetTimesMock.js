var Promise = require('bluebird')

function getSunsetTime (location, date) {
  return Promise.resolve(new Date(date.getFullYear(), date.getMonth(), date.getDate(), 17, 30))
}

module.exports = {
  getSunsetTime: getSunsetTime
}
