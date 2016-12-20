/**
 * Return an object with keys start_date and end_date, each of which is a Date
 * object for the start/end date of the month.
 *
 * @param {Date} date - Used to figure out which date to return date objs for
 */
function getStartAndEndDatesForMonth(date) {
    var start_date = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), 1, 0, 0, 0, 0));
    var end_date = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth() + 1, 0, 23, 59, 59, 999));
    return {start_date: start_date, end_date: end_date};
}

module.exports = {
    getStartAndEndDatesForMonth: getStartAndEndDatesForMonth
};
