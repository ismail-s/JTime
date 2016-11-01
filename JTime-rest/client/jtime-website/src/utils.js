import Vue from 'vue'

export function dateToDateString (date) {
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}

function padNumToTwoDigits (n) {
  return ('0' + n).slice(-2)
}

export function dateTimeStringToTimeString (dateStr) {
  const date = new Date(dateStr)
  return `${padNumToTwoDigits(date.getHours())}-${padNumToTwoDigits(date.getMinutes())}`
}

export function salaahTypeCodeToString (c) {
  switch (c) {
    case 'f':
      return 'Fajr'
    case 'z':
      return 'Zohar'
    case 'a':
      return 'Asr'
    case 'm':
      return 'Magrib'
    case 'e':
      return 'Esha'
    default:
      return ''
  }
}

function upgradeElement (elem) {
  Vue.nextTick(() => {
    // We call upgradeElements which recursively calls upgradeElement on all the
    // children of elem (as well as elem itself)
    window.componentHandler.upgradeElements(elem)
  })
}

export const upgradeElementMixin = {
  mounted () { upgradeElement(this.$el) }
}
