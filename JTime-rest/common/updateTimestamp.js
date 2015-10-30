module.exports = function(ctx, next) {
    if (ctx.instance) {
        ctx.instance.lastModifiedAt = new Date();
    }
    else {
        ctx.data.lastModifiedAt = new Date();
    }
    next();
};
