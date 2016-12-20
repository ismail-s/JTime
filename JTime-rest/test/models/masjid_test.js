var expect = require("chai").expect
var mockery = require("mockery")
var moment  = require("moment")

require('process').env['NO_DEPRECATION'] = "express-session"

describe('/Masjid', function () {

    var Masjid, request

    before(function() {
        this.timeout(3000)
        mockery.enable({useCleanCache: true, warnOnUnregistered: false})
        mockery.registerMock("googlemaps", require("../mocks/googleMapsMock"))
        mockery.registerMock("../sunsetTimes", require("../mocks/sunsetTimesMock"))
        mockery.registerMock("request", require("../mocks/requestMock"))
        var server = require('../../server/server')
        request = require('supertest')(server)
        Masjid = server.models.Masjid
    })

    beforeEach(function(done) {
        Masjid.destroyAll(done)
    })

    after(function() {
        mockery.disable()
    })

    it('starts with no masjids', function (done) {
        request.get('/api/Masjids').expect(200).expect([]).end(done)
     })

    it('can create new masjids', function(done) {
        this.timeout(3000)
        Masjid.app.models.UserTable.googleId('fakeGoogleId', function(err, access_token, userId) {
            if (err) return done(err)
            expect(access_token).to.exist
            expect(userId).to.exist
            request.post('/api/Masjids')
                .set('Authorization', access_token)
                .send({name: 'test', location: {lat: 0, lng: 0}})
                .expect(200)
                .expect(checkBodyOfResponse)
                .end(function(err, res) {
                    if (err) return done(err)
                    request.get('/api/Masjids').expect(200).expect(function(res) {
                        checkBodyOfResponse({body: res.body[0]})
                    }).end(done)
            })
        })
        function checkBodyOfResponse(res) {
            expect(res.body.name).to.equal('test')
            expect(res.body.location).to.eql({lat: 0, lng: 0})
            expect(res.body.humanReadableAddress).to.equal("Test Address")
            expect(res.body.id).to.equal(1)
            expect(res.body.createdAt).to.exist
            expect(res.body.lastModifiedAt).to.exist
        }
    })

    describe('getTimesForAMonth', function() {
        function padToTwoDigits(num) {
                return ('0' + num).substr(-2)
            }
        it('returns all magrib times for a month', function(done) {
            Masjid.create({name: 'test', location: {lat: 0, lng: 0}}, function(err, instance) {
                if (err) return done(err)
                var test_date = new Date()
                request.get('/api/Masjids/' + instance.id + '/times-for-a-month')
                    .query({date: test_date})
                    .expect(200).expect(function(res) {
                        var times = res.body.times
                        expect(times).to.have.lengthOf(moment(test_date).daysInMonth())
                        times.forEach(function(salaahTime, i) {
                            expect(salaahTime.type).to.equal('m')
                            var expected_datetime = [test_date.getFullYear(), '-',
                                padToTwoDigits(test_date.getMonth() + 1), '-',
                                padToTwoDigits(i + 1), 'T17:30:00.000Z'
                            ].join('')
                            expect(salaahTime.datetime).to.equal(expected_datetime)
                        })
                    }).end(done)
            })
        })

        it('returns salaah times that are in the db for the correct month', function(done) {
            Masjid.create({name: 'test', location: {lat: 0, lng: 0}}, function(err, instance) {
                if (err) return done(err)
                var fajrTime = moment().date(1).hour(6).minute(25).toDate()
                Masjid.app.models.SalaahTime.createOrUpdate(instance.id, 'f', fajrTime, function(err) {
                    if (err) return done(err)
                    var zoharTime = moment().date(2).hour(12).minute(15).toDate()
                    Masjid.app.models.SalaahTime.createOrUpdate(instance.id, 'z', zoharTime, function(err) {
                        if (err) return done(err)
                        var test_date = new Date()
                        request.get('/api/Masjids/' + instance.id + '/times-for-a-month')
                            .query({date: test_date})
                            .expect(200).expect(function(res) {
                                var times = res.body.times
                                expect(times).to.have.lengthOf(moment(test_date).daysInMonth() + 2)
                                times.filter(function(elem) {return elem.type != 'm'})
                                    .forEach(function(salaahTime, i) {
                                        expect(salaahTime.type).to.be.oneOf(['f', 'z'])
                                        var expected_datetime = [test_date.getFullYear(), '-',
                                            padToTwoDigits(test_date.getMonth() + 1), '-']
                                        if (salaahTime.type == 'f') {
                                            expected_datetime.push('01T06:25:00.000Z')
                                        } else {
                                            expected_datetime.push('02T12:15:00.000Z')
                                        }
                                        expect(salaahTime.datetime).to.equal(expected_datetime.join(''))
                                    })
                            }).end(done)
                    })
                })
            })
        })

        it('doesn\'t return salaah times from other months', function(done) {
            Masjid.create({name: 'test', location: {lat: 0, lng: 0}}, function(err, instance) {
                if (err) return done(err)
                var fajrTime = moment().add(1, 'months').date(1).hour(6).minute(25).toDate()
                Masjid.app.models.SalaahTime.createOrUpdate(instance.id, 'f', fajrTime, function(err) {
                    if (err) return done(err)
                    var test_date = new Date()
                    request.get('/api/Masjids/' + instance.id + '/times-for-a-month')
                        .query({date: test_date})
                        .expect(200).expect(function(res) {
                            var times = res.body.times
                            expect(times).to.have.lengthOf(moment(test_date).daysInMonth())
                            var filteredTimes = times.filter(function(elem) {return elem.type != 'm'})
                            expect(filteredTimes).to.eql([])
                        }).end(done)
                })
            })
        })
    })
})
