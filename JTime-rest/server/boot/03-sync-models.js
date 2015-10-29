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
    var dataSource = app.dataSources.postgres;
    updateModelIfNecessary('Masjid', dataSource);
    updateModelIfNecessary('SalaahTime', dataSource);
}