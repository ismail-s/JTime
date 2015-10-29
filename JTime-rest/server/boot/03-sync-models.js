function updateModelIfNecessary(model, dataSource) {
    dataSource.isActual(model, function(err, actual) {
        if (err) throw err;
        if (!actual) {
            dataSource.autoupdate(model, function(err, result) {
                if (err) throw err;
            });
        }
    });
}

module.exports = function(app) {
    var customTables = ['Masjid', 'SalaahTime'];
    var builtinTables = ['User', 'AccessToken', 'ACL', 'RoleMapping', 'Role'];
    var tables = customTables.concat(builtinTables);
    var dataSource = app.dataSources.postgres;
    tables.forEach(function(table) {
        updateModelIfNecessary(table, dataSource);
    });
}
