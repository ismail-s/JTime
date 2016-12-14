// https://stackoverflow.com/questions/1215392
// Allow enabling/disabling console.log
module.exports = function()
{
    var oldConsoleLog = null
    var pub = {}
    pub.enableLogger =  function enableLogger()
                        {
                            if(oldConsoleLog == null)
                                return
                            console['log'] = oldConsoleLog
                        }
    pub.disableLogger = function disableLogger()
                        {
                            oldConsoleLog = console.log
                            console['log'] = function() {}
                        }
    return pub
}()
