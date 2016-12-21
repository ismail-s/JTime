function updateModelIfNecessary (model, dataSource) {
  dataSource.isActual(model, function (err, actual) {
    if (err) throw err
    if (!actual) {
      dataSource.autoupdate(model, function (err, result) {
        if (err) throw err
      })
    }
  })
}

module.exports = function (app) {
  var customTables = ['Masjid', 'SalaahTime']
  var builtinTables = ['user_table', 'accessToken', 'ACL', 'RoleMapping', 'Role']
  var tables = customTables.concat(builtinTables)
  var dataSource = app.dataSources.postgres
    // This line prevents too many connections to the db being created.
    // See https://github.com/strongloop/loopback-datasource-juggler/issues/805
    // for more info.
  dataSource.setMaxListeners(0)

  tables.forEach(function (table) {
    updateModelIfNecessary(table, dataSource)
  })
}
