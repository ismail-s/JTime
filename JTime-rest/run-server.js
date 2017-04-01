var fs = require('fs')
var path = require('path')
var forever = require('forever-monitor')

var flagFile = path.resolve(__dirname, 'restart-rest-api.flag')

var child = new (forever.Monitor)('server.js', {
  sourceDir: path.resolve(__dirname, 'server'),
  max: 3,
  silent: false,
  killTree: true,
  env: {NODE_ENV: 'production'},
  watch: false
})

child.on('exit', function () {
  console.log('rest api has exited after 3 restarts')
})

child.start()

setInterval(function () {
  fs.access(flagFile, function (err) {
    if (err) {
      // File doesn't exist, nothing to do
      return
    }
    console.log('Flag file found, will delete flag file and restart rest api')
    // Delete file
    fs.unlink(flagFile, function (err) {
      if (err) {
        console.error('Unable to delete flag file, won\'t restart as a precaution', err)
        return
      }
      console.log('Restarting rest api')
      child.restart()
    })
  })
}, 60/* secs */ * 1000/* milliseconds */)
