var loopback = require('loopback');
var boot = require('loopback-boot');
var request = require('request');
var settings = require('../settings');

var app = module.exports = loopback();

// Enable http session
app.use(loopback.session({
  secret: 'some secret...TODO-change this'
}));

boot(app, __dirname);


app.get('/auth/googleid', function(req, res) {
  var options = {
    uri: 'https://www.googleapis.com/oauth2/v3/tokeninfo',
    qs: {id_token: req.query.id_token},
    json: true
  };
  request(options, function (error, response, body) {
    if (!error && response.statusCode == 200 && body.aud == settings.clientId) {
      var userModel = app.models.user;
      var username = "google." + body.sub;
      var query = {where: {or: [{username: username}, {email: body.email}]}};
      var data = {username: username, email: body.email, password: "TODO"};
      //check if user exists on server-if not, createModel
      userModel.findOne(query, function(err, instance) {
          if (err) {res.json({error: "DB error"});return;}
          if (!instance) {
              userModel.create(data, function(err, instance) {
                  if (err) {
                      res.json("DB Error");
                      return;
                  } else {
                      userModel.login({email: body.email, password: "TODO"}, function(err, token) {
                          if (err) {
                              console.log('Login error:', err);
                              res.json({error: "Login error"});
                              return;
                          } else {
                              console.log(token);
                              res.json({access_token: token.id, userId: token.userId});
                          }
                      });
                  }
              });
              return;
          } else {
              userModel.login({email: body.email, password: "TODO"}, function(err, token) {
                  if (err) {res.json({error: "Login error"}); return;} else {
                      console.log(token);
                      res.json({access_token: token.id, userId: token.userId});
                  }
              });
          }
      });
      return;
    }
    res.status(404).json({error: "Token validation error"});
  });
});




app.start = function() {
  // start the web server
  return app.listen(function() {
    app.emit('started');
    var baseUrl = app.get('url').replace(/\/$/, '');
    console.log('Web server listening at: %s', baseUrl);
    if (app.get('loopback-component-explorer')) {
      var explorerPath = app.get('loopback-component-explorer').mountPath;
      console.log('Browse your REST API at %s%s', baseUrl, explorerPath);
    }
  });
};





// start the server if `$ node server.js`
if (require.main === module)
  app.start();
