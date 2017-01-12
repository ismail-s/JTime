<template>
  <div>
    <h2>Edit Salaah times</h2>
    <div id="handsontable" class="center"></div>
  </div>
</template>

<script>
import {commonComputedProperties, sortSalaahTimes} from '../masjid-utils'
import Handsontable from 'handsontable/dist/handsontable.full'

export default {
  name: 'edit-salaah-times',
  data () {
    return {
      data: [['Date', 'Day', 'Fajr', 'Zohar', 'Asr', 'Magrib', 'Esha']
      ],
      handsontable: undefined,
      changesToSave: []
    }
  },
  computed: commonComputedProperties,
  mounted () {
    const elem = this.$el.querySelector('#handsontable')
    this.handsontable = new Handsontable(elem, {
      data: this.data,
      height: 800,
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
    if (this.masjidId && this.year && this.month + 1) {
      this.$store.dispatch({
        type: 'getSalaahTimesForMonth',
        masjidId: this.masjidId,
        year: this.year,
        month: this.month
      })
    }
  },
  watch: {
    '$store.state.SalaahTimesModule.salaahTimes': {
      handler (salaahTimes) {
        const finalResult = sortSalaahTimes(salaahTimes[this.masjidId], this.year, this.month)
          .map(t => [t.date, t.dayOfWeek, t.fajrTime || '', t.zoharTime || '', t.asrTime || '', t.magribTime || '', t.eshaTime || ''])
        this.data.splice(1, this.data.length - 1)
        this.data.push(...finalResult)
        this.handsontable.render()
        this.$el.querySelector('#handsontable').querySelector('.wtHolder').style.width = '100%'
        this.$el.querySelector('#handsontable').querySelector('.wtHolder').style.height = '100%'
      },
      deep: true
    }
  }
}
</script>

<style scoped>
@import "~handsontable/dist/handsontable.full.css";

.center {
  display: inline-block;
}
</style>
