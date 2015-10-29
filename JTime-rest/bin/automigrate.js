var path = require('path');

var app = require(path.resolve(__dirname, '../server/server'));
var ds = app.datasources.postgres;
ds.automigrate('Masjid', function(err) {
  if (err) throw err;

  var masjids = [
    {
      name: 'test1',
      createdAt: new Date(),
      lastModifiedAt: new Date()
    },
    {
      name: 'test2',
      createdAt: new Date(),
      lastModifiedAt: new Date()
    }
  ];
  var count = masjids.length;
  masjids.forEach(function(masjid) {
    app.models.Masjid.create(masjid, function(err, model) {
      if (err) throw err;

      console.log('Created:', model);

      count--;
      if (count === 0)
        ds.disconnect();
    });
  });
});