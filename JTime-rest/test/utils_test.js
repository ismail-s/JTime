/* global describe it */

var utils = require('../common/utils')
var expect = require('chai').expect
var jsc = require('jsverify')

describe('utils', function () {
  describe('getStartAndEndDatesForMonth', function () {
    it('returns the correct start date of a month for any date', function () {
      var prop = jsc.forall('datetime', function (testDate) {
        var res = utils.getStartAndEndDatesForMonth(testDate).startDate
        expect(res).to.exist
        expect(res.getFullYear()).to.equal(testDate.getFullYear())
        expect(res.getMonth()).to.equal(testDate.getMonth())
        expect(res.getDate()).to.equal(1)
        expect(res.getHours()).to.equal(0)
        expect(res.getMinutes()).to.equal(0)
        expect(res.getSeconds()).to.equal(0)
        expect(res.getMilliseconds()).to.equal(0)
        return true
      })
      jsc.assert(prop)
    })

    it('returns the correct end date of a month for any date', function () {
      var prop = jsc.forall('datetime', function (testDate) {
        var res = utils.getStartAndEndDatesForMonth(testDate).endDate
        expect(res).to.exist
        expect(res.getFullYear()).to.equal(testDate.getFullYear())
        expect(res.getMonth()).to.equal(testDate.getMonth())
        expect(res.getDate()).to.oneOf([28, 29, 30, 31])
        expect(res.getHours()).to.equal(23)
        expect(res.getMinutes()).to.equal(59)
        expect(res.getSeconds()).to.equal(59)
        expect(res.getMilliseconds()).to.equal(999)
        return true
      })
      jsc.assert(prop, {tests: 5000})
    })

    it('returns the correct end date for Feb 2016, a leap year', function () {
      var prop = jsc.forall(jsc.integer(1, 29),
                jsc.integer(0, 23), jsc.integer(0, 59), jsc.integer(0, 59),
                function (date, hour, min, sec) {
                  var testDate = new Date(2016, 1, date, hour, min, sec)
                  var res = utils.getStartAndEndDatesForMonth(testDate).endDate
                  expect(res).to.exist
                  expect(res.getFullYear()).to.equal(2016)
                  expect(res.getMonth()).to.equal(1)
                  expect(res.getDate()).to.equal(29)
                  expect(res.getHours()).to.equal(23)
                  expect(res.getMinutes()).to.equal(59)
                  expect(res.getSeconds()).to.equal(59)
                  expect(res.getMilliseconds()).to.equal(999)
                  return true
                })
      jsc.assert(prop)
    })
  })
})
