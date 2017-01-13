/**
 * Return an object with keys start_date and end_date, each of which is a Date
 * object for the start/end date of the month.
 *
 * @param {Date} date - Used to figure out which date to return date objs for
 */
function getStartAndEndDatesForMonth (date) {
  var startDate = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), 1, 0, 0, 0, 0))
  var endDate = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth() + 1, 0, 23, 59, 59, 999))
  return {startDate: startDate, endDate: endDate}
}

module.exports = {
  getStartAndEndDatesForMonth: getStartAndEndDatesForMonth
}
