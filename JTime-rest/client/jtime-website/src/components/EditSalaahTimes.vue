<template>
  <div>
    <h2>Edit Salaah times</h2>
    <h3>Help</h3>
    <p>This webpage is designed to be used on a computer. Add and change salaah
      times in the same way as you would use any spreadsheet program. If a cell
      turns red, it is not a valid time and will not be saved. To save changes,
      press the "Save Changes" button. Until you press this button, any changes
      you make will be lost if you close the web browser or navigate away from
      this page.</p>
    <div id="handsontable" class="center"></div>
    <div class="pad-5px">
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToPrevMonth">Previous month</button>
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToNextMonth">Next month</button>
    </div>
    <div class="padBottom">
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect" v-on:click="saveChanges" v-bind:disabled="saveButtonIsDisabled">Save changes</button>
    </div>
  </div>
</template>

<script>
import {commonComputedProperties, commonMethods, sortSalaahTimes} from '../masjid-utils'
import {baseUrl} from '../store/utils'
import Handsontable from 'handsontable/dist/handsontable.full'
import moment from 'moment'
import Vue from 'vue'

export default {
  name: 'edit-salaah-times',
  data () {
    return {
      data: [],
      handsontable: undefined,
      changesToSave: [],
      saveButtonIsDisabled: false
    }
  },
  computed: commonComputedProperties,
  methods: {
    setUpComponent () {
      // Reset everything in-case this method is called after changing routes,
      // when this component is reused
      this.handsontable && this.handsontable.destroy()
      this.changesToSave = []
      this.data = []
      this.saveButtonIsDisabled = false
      const elem = this.$el.querySelector('#handsontable')
      this.handsontable = new Handsontable(elem, {
        data: this.data,
        colHeaders: ['Date', 'Day', 'Fajr', 'Zohar', 'Asr', 'Esha'],
        columns: [
          {type: 'numeric',
            readOnly: true
          },
          {type: 'text',
            readOnly: true
          },
          {type: 'time',
            timeFormat: 'HH-mm',
            correctFormat: true,
            allowEmpty: false
          },
          {type: 'time',
            timeFormat: 'HH-mm',
            correctFormat: true,
            allowEmpty: false
          },
          {type: 'time',
            timeFormat: 'HH-mm',
            correctFormat: true,
            allowEmpty: false
          },
          {type: 'time',
            timeFormat: 'HH-mm',
            correctFormat: true,
            allowEmpty: false
          }],
        afterInit () {
          elem.querySelector('.wtHolder').style.width = '100%'
        },
        afterChange: function (changes, source) {
          if (changes && source !== 'loadData') {
            this.changesToSave.push(...changes)
          }
          elem.querySelector('.wtHolder').style.width = '100%'
          elem.querySelector('.wtHolder').style.height = '100%'
        }.bind(this)
      })
      this.getSalaahTimesForMonth()
    },
    saveChanges () {
      this.saveButtonIsDisabled = true
      const daysInMonth = moment().year(this.year).month(this.month).daysInMonth()
      const changesToMake = {} // map from eg "01-f" to new time
      for (const change of this.changesToSave) {
        let [dayOfMonth, salaahTypeNum, oldVal, newVal] = change
        dayOfMonth += 1
        if (!this.isValidTime(newVal) || oldVal === newVal ||
          dayOfMonth < 1 || dayOfMonth > daysInMonth) {
          continue
        }
        const salaahTypeCode = this.salaahTypeNumToCode(salaahTypeNum)
        const key = `${dayOfMonth}-${salaahTypeCode}`
        changesToMake[key] = newVal
      }
      const newOrUpdatedTimes = []
      for (const key in changesToMake) {
        const [dayOfMonth, salaahType] = key.split('-')
        const date = moment(changesToMake[key], 'HH-mm', true).utc()
          .year(this.year).month(this.month).date(parseInt(dayOfMonth)).toISOString()
        newOrUpdatedTimes.push({date, type: salaahType})
      }
      // Don't save changes if there are no changes to save
      if (newOrUpdatedTimes.length === 0) {
        this.saveButtonIsDisabled = false
        return
      }
      const options = {
        headers: {
          Authorization: this.$store.state.LoggedInUserModule.loggedInUser.accessToken
        }}
      const body = {masjidId: this.masjidId, newOrUpdatedTimes}
      Vue.http.post(`${baseUrl}/SalaahTimes/create-or-update-multiple`, body, options).then(response => {
        return response.json()
      }).then(res => {
        this.$store.commit('toast', 'Times were successfully saved')
      }).catch(err => {
        this.$store.commit('toast', `Times weren't successfully saved: ${err}`)
      }).then(() => {
        this.changesToSave = []
        this.saveButtonIsDisabled = false
      })
    },
    isValidTime (timeStr) {
      return moment(timeStr, 'HH-mm', true).isValid()
    },
    salaahTypeNumToCode (num) {
      switch (num) {
        case 2:
          return 'f'
        case 3:
          return 'z'
        case 4:
          return 'a'
        case 5:
          return 'e'
        default:
          throw Error(`Invalid num parameter passed to salaahTypeNumToCode: ${num}`)
      }
    },
    ...commonMethods
  },
  mounted () { this.setUpComponent() },
  watch: {
    '$store.state.SalaahTimesModule.salaahTimes': {
      handler (salaahTimes) {
        const finalResult = sortSalaahTimes(salaahTimes[this.masjidId], this.year, this.month)
          .map(t => [t.date, t.dayOfWeek, t.fajrTime || '', t.zoharTime || '', t.asrTime || '', t.eshaTime || ''])
        this.data.splice(0, this.data.length - 1)
        this.data.push(...finalResult)
        this.handsontable.render()
        this.$el.querySelector('#handsontable').querySelector('.wtHolder').style.width = '100%'
        this.$el.querySelector('#handsontable').querySelector('.wtHolder').style.height = '100%'
      },
      deep: true
    },
    '$route' () { this.setUpComponent() }
  }
}
</script>

<style scoped>
@import "~handsontable/dist/handsontable.full.css";

.center {
  display: inline-block;
}

.padBottom {
  padding-bottom: 10px;
}

.pad-5px {
  padding: 5px;
}
</style>
