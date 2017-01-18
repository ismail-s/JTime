<template>
  <div>
    <h2>Jamaat Times</h2>
    <p v-if="!locationApiIsAvailable">Finding your current location is not
                                      supported by your browser</p>
    <p v-else-if="locationOrError === null">Trying to get your location...</p>
    <p v-else-if="locationOrErrorIsError">Weren't able to get your location</p>
    <div class="mdl-grid" v-else>
        <div class="mdl-cell mdl-card mdl-shadow--2dp mdl-cell--4-col mdl-cell--4-col-tablet mdl-cell--6-col-phone" v-for="salaahTimes in sortedSalaahTimes">
          <div class="mdl-card__title">{{ salaahTypeCodeToString(salaahTimes[0]) }}</div>
          <div class="mdl-card__supporting-text">
            <table class="mdl-data-table mdl-js-data-table center">
              <tbody>
                <tr v-for="time in salaahTimes[1]">
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
import moment from 'moment'
import {salaahTypeCodeToString} from '../utils'

export default {
  name: 'home',
  data () {
    return {
      locationApiIsAvailable: true,
      locationOrError: null
    }
  },
  methods: {
    salaahTypeCodeToString
  },
  filters: {
    format_as_hh_mm: d => moment(d).format('HH-mm')
  },
  computed: {
    locationOrErrorIsError () {
      return !this.locationOrError ||
        this.locationOrError.code ||
        !this.locationOrError.coords ||
        !this.locationOrError.coords.latitude ||
        !this.locationOrError.coords.longitude
    },
    sortedSalaahTimes () {
      return this.$store.state.SalaahTimesModule.nearbySalaahTimes
    }
  },
  mounted () {
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
}
</script>

<style scoped>
h1 {
  font-weight: normal;
}

.center {
  margin: auto;
}
</style>
