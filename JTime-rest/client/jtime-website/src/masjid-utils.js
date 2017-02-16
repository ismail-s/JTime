import {compareSalaahTypes} from './utils'
import router from './router'
import moment from 'moment'

export function sortSalaahTimes (salaahTimes, year, month) {
  const times = salaahTimes.filter(t => t.datetime.getFullYear() === year && t.datetime.getMonth() === month)
  times.sort((a, b) => {
    const aDate = a.datetime.getDate()
    const bDate = b.datetime.getDate()
    if (aDate < bDate) {
      return -1
    } else if (aDate > bDate) {
      return 1
    } else {
      return compareSalaahTypes(a.type, b.type)
    }
  })
  const daysInMonth = moment().year(year).month(month).daysInMonth()
  var finalResult = []
  for (var i = 1; i <= daysInMonth; i++) {
    var obj = {date: i, dayOfWeek: moment().year(year).month(month).date(i).format('ddd')}
    while (times[0] && times[0].datetime.getDate() === i) {
      const time = times.shift()
      const datetime = moment(time.datetime).format('HH-mm')
      switch (time.type) {
        case 'f':
          obj.fajrTime = datetime
          break
        case 'z':
          obj.zoharTime = datetime
          break
        case 'a':
          obj.asrTime = datetime
          break
        case 'm':
          obj.magribTime = moment(time.datetime).add(5, 'minutes').format('HH-mm')
          break
        case 'e':
          obj.eshaTime = datetime
          break
      }
    }
    finalResult.push(obj)
  }
  return finalResult
}

export const commonComputedProperties = {
  masjidId () {
    return parseInt(this.$route.params.id)
  },
  year () {
    return parseInt(this.$route.params.year)
  },
  month () {
    return parseInt(this.$route.params.month)
  }
}

export const commonMethods = {
  goToNextMonth () {
    let [newYear, newMonth] = [this.year, this.month]
    if (this.month < 0 || this.month > 11) {
      // Invalid month, return early
      return
    } else if (this.month === 11) {
      newMonth = 0
      newYear = this.year + 1
    } else {
      newMonth = this.month + 1
    }
    router.push({name: this.name,
      params: {id: this.masjidId, year: newYear, month: newMonth}})
  },
  goToPrevMonth () {
    let [newYear, newMonth] = [this.year, this.month]
    if (this.month < 0 || this.month > 11) {
      // Invalid month, return early
      return
    } else if (this.month === 0) {
      newMonth = 11
      newYear = this.year - 1
    } else {
      newMonth = this.month - 1
    }
    router.push({name: this.name,
      params: {id: this.masjidId, year: newYear, month: newMonth}})
  },
  getSalaahTimesForMonth () {
    if (this.masjidId && this.year && this.month + 1) {
      this.$store.dispatch({
        type: 'getSalaahTimesForMonth',
        masjidId: this.masjidId,
        year: this.year,
        month: this.month
      })
    }
  }
}
