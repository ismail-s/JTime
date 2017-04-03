<template>
  <div>
    <h2>Jamaat Times</h2>
    <p v-if="!locationApiIsAvailable">Finding your current location is not
                                      supported by your browser</p>
    <p v-else-if="locationOrError === null">Trying to get your location...</p>
    <p v-else-if="locationOrErrorIsError">Weren't able to get your location</p>
    <p v-else-if="!sortedSalaahTimes">No nearby salaah times found</p>
    <div class="mdl-grid" v-else>
        <div class="mdl-cell mdl-card mdl-shadow--2dp mdl-cell--4-col mdl-cell--4-col-tablet mdl-cell--6-col-phone" v-for="salaahTimes in sortedSalaahTimes">
          <div class="mdl-card__title">{{ salaahTypeCodeToString(salaahTimes[0]) }}</div>
          <div class="mdl-card__supporting-text horizontal-scroll">
            <table class="mdl-data-table mdl-js-data-table center">
              <tbody>
                <tr v-for="time in salaahTimes[1]" v-bind:class="time === closestTime ? 'highlight-row' : ''">
                  <td class="mdl-data-table__cell--non-numeric">{{ time.masjidName }}</td>
                  <td>{{ time.datetime | format_as_hh_mm }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
    </div>
  </div>
</template>

<script>
import haversine from 'haversine'
import moment from 'moment'
import {salaahTypeCodeToString} from '../utils'

export default {
  name: 'home',
  data () {
    return {
      locationApiIsAvailable: true,
      locationOrError: null,
      timeoutId: null
    }
  },
  methods: {
    salaahTypeCodeToString,
    sortByDistanceFromCurrentLocation (a, b) {
      const currentLoc = {latitude: this.locationOrError.coords.latitude,
        longitude: this.locationOrError.coords.longitude}
      const aLoc = {latitude: a.masjidLocation.lat, longitude: a.masjidLocation.lng}
      const bLoc = {latitude: b.masjidLocation.lat, longitude: b.masjidLocation.lng}
      const aDist = haversine(currentLoc, aLoc)
      const bDist = haversine(currentLoc, bLoc)
      if (aDist < bDist) {
        return -1
      } else if (aDist > bDist) {
        return 1
      } else {
        return 0
      }
    },
    getLocationAndNearbyMasjids () {
      if ('geolocation' in window.navigator) {
        window.navigator.geolocation.getCurrentPosition((position) => {
          this.locationOrError = position
          this.$store.dispatch({
            type: 'getTimesForNearbyMasjids',
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          })
        }, (error) => {
          this.locationOrError = error
        }, {
          timeout: 15000,
          maximumAge: 15000
        })
      } else {
        this.locationApiIsAvailable = false
      }
    }
  },
  filters: {
    format_as_hh_mm: d => moment.utc(d).format('HH-mm')
  },
  computed: {
    locationOrErrorIsError () {
      return !this.locationOrError ||
        this.locationOrError.code ||
        !this.locationOrError.coords ||
        this.locationOrError.coords.latitude == null ||
        this.locationOrError.coords.longitude == null
    },
    sortedSalaahTimes () {
      const times = this.$store.state.SalaahTimesModule.nearbySalaahTimes
        // Group by salaah type
        .reduce((acc, elem) => {
          if (acc[elem.type]) {
            acc[elem.type].push(elem)
            return acc
          }
        }, {'f': [], 'z': [], 'a': [], 'm': [], 'e': []})
      var timesAsList = [['f', times['f']], ['z', times['z']],
        ['a', times['a']], ['m', times['m']], ['e', times['e']]]
          .map(([salaahType, times]) => {
            times.sort((a, b) => {
              // sort by time, then by distance from current position
              const [aTime, bTime] = [a.datetime.getTime(), b.datetime.getTime()]
              if (aTime < bTime) {
                return -1
              } else if (aTime > bTime) {
                return 1
              } else {
                return this.sortByDistanceFromCurrentLocation(a, b)
              }
            })
            return [salaahType, times]
          }).filter(([salaahType, times]) => times.length > 0)
      return timesAsList.length === 0 ? null : timesAsList
    },
    closestTime () {
      const utcOffset = moment().utcOffset() * 60 * 1000 // in milliseconds
      const nowish = moment().subtract(3, 'minutes')
      const sortedTimes = this.$store.state.SalaahTimesModule.nearbySalaahTimes
        .map(elem => {
          elem.keyForTime = -nowish.diff(elem.datetime) - utcOffset
          return elem
        })
        .sort((aElem, bElem) => {
          const [a, b] = [aElem.keyForTime, bElem.keyForTime]
          if (a < b) {
            return -1
          } else if (a > b) {
            return 1
          } else return 0
        })
      const filteredTimes = sortedTimes.filter(elem => elem.keyForTime >= 0)
      let res
      if (filteredTimes.length > 0) {
        // filter all times equal to first one
        res = filteredTimes
          .filter(elem => elem.keyForTime === filteredTimes[0].keyForTime)
      } else {
        // get most recent time
        res = sortedTimes.filter(elem => elem.keyForTime === sortedTimes[sortedTimes.length - 1].keyForTime)
      }
      // sort by distance from current loc
      res.sort(this.sortByDistanceFromCurrentLocation)
      // return closest time
      const finalRes = res[0]
      finalRes && delete finalRes.keyForTime
      return finalRes || null
    },
    updateTime () {
      if (!this.closestTime) {
        return null
      }
      const utcOffset = moment().utcOffset()
      /* If closestTime is at least 3 mins in the past, then we can assume that
         there are no more salaah times for today, so we should next update the
         salaah times at midnight */
      if (moment().diff(this.closestTime.datetime, 'minutes', true) + utcOffset >= 3) {
        return moment().add(1, 'days').startOf('day').diff(moment())
      }
      return moment(this.closestTime.datetime).clone()
        .add(3, 'minutes').add(1, 'millisecond').diff(moment()) - (utcOffset * 60 * 1000)
    }
  },
  mounted () {
    this.getLocationAndNearbyMasjids()
  },
  beforeDestroy () {
    clearTimeout(this.timeoutId)
  },
  watch: {
    updateTime (t) {
      clearTimeout(this.timeoutId)
      this.timeoutId = setTimeout(this.getLocationAndNearbyMasjids, t)
    }
  }
}
</script>

<style scoped>
h1 {
  font-weight: normal;
}

.center {
  margin: auto;
}

.highlight-row {
  background-color: #f48fb1; /*material design pink 200*/
}

.horizontal-scroll {
  overflow-x: auto;
}
</style>
