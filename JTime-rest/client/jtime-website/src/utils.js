import Vue from 'vue'

export function dateToDateString (date) {
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
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

export function compareSalaahTypes (a, b) {
  const salaahTypes = ['f', 'z', 'a', 'm', 'e']
  const aIndex = salaahTypes.indexOf(a)
  const bIndex = salaahTypes.indexOf(b)
  if (aIndex === -1 || bIndex === -1) {
    return -2
  }
  if (aIndex < bIndex) {
    return -1
  } else if (aIndex > bIndex) {
    return 1
  } else {
    return 0
  }
}

function upgradeElement (elem) {
  Vue.nextTick(() => {
    // We call upgradeElements which recursively calls upgradeElement on all the
    // children of elem (as well as elem itself)
    // Note that we check that window.componentHandler is defined to avoid
    // errors whilst running unittests
    window.componentHandler && window.componentHandler.upgradeElements(elem)
  })
}

export const upgradeElementMixin = {
  mounted () { upgradeElement(this.$el) }
}
