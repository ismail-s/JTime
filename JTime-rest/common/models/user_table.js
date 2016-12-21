var request = require('request')
var settings = require('../../settings')

module.exports = function (User) {
  User.googleId = function (googleId, cb) {
    var options = {
      uri: 'https://www.googleapis.com/oauth2/v3/tokeninfo',
      qs: {
        id_token: googleId
      },
      json: true
    }
    request(options, function (error, response, body) {
      if (!error && response.statusCode === 200 && body.aud === settings.clientId && !body.email.endsWith('cloudtestlabaccounts.com')) {
        var username = 'google.' + body.sub
        var query = {
          where: {
            or: [{username: username},
                            {email: body.email}]
          }
        }
        var data = {
          username: username,
          email: body.email,
          password: 'TODO'
        }
                // check if user exists on server-if not, createModel
        User.findOne(query, function (err, instance) {
          if (err) {
            var msg = 'Error when searching through db'
            console.error(msg, err, instance)
            cb(new Error(msg))
            return
          }
          if (!instance) {
            User.create(data, function (err, instance) {
              if (err) {
                var msg = 'DB Error'
                console.error(msg, err, instance)
                cb(new Error(msg))
                return
              } else {
                User.login({
                  email: body.email,
                  password: 'TODO'
                }, function (err, token) {
                  if (err) {
                    var msg = 'Login error'
                    console.error(msg, err, instance)
                    cb(new Error(msg))
                    return
                  } else {
                    cb(null, token.id, token.userId)
                    return
                  }
                })
              }
            })
            return
          } else {
            User.login({
              email: body.email,
              password: 'TODO'
            }, function (err, token) {
              if (err) {
                var msg = 'Login error'
                console.error(msg, err, instance)
                cb(new Error(msg))
                return
              } else {
                cb(null, token.id, token.userId)
                return
              }
            })
          }
        })
        return
      }
      cb(new Error('Token validation error'))
    })
  }

  User.remoteMethod(
        'googleId', {
          accepts: [{
            arg: 'id_token',
            type: 'string',
            required: true
          }],
          returns: [{
            arg: 'access_token',
            type: 'string'
          }, {
            arg: 'userId',
            type: 'number'
          }],
          http: {
            path: '/googleid',
            verb: 'get'
          }
        }
    )
}
