<script setup>
import { ref, onMounted } from 'vue'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { getKpi } from '../services/ticketApi'

ChartJS.register(ArcElement, Tooltip, Legend)

const venduti = ref(0)
const distribuzione = ref({})
const loading = ref(false)
const errorMessage = ref('')

const filterFrom = ref('')
const filterTo = ref('')

const chartData = ref({
  labels: [],
  datasets: [{ data: [], backgroundColor: ['#4a6cf7', '#f7b84a', '#4af7a0', '#f74a4a'] }],
})

async function loadKpi() {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getKpi(filterFrom.value || undefined, filterTo.value || undefined)
    venduti.value = response.data.venduti
    distribuzione.value = response.data.distribuzionePerStato

    chartData.value = {
      labels: Object.keys(distribuzione.value),
      datasets: [
        {
          data: Object.values(distribuzione.value),
          backgroundColor: ['#4a6cf7', '#f7b84a', '#4af7a0', '#f74a4a'],
        },
      ],
    }
  } catch (error) {
    errorMessage.value = 'Errore nel caricamento dei KPI'
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadKpi()
})
</script>

<template>
  <div>
    <h1>Dashboard KPI</h1>

    <section>
      <h2>Filtro periodo (su data creazione)</h2>
      <input type="datetime-local" v-model="filterFrom" />
      <input type="datetime-local" v-model="filterTo" />
      <button @click="loadKpi">Applica</button>
    </section>

    <p v-if="errorMessage" style="color: red">{{ errorMessage }}</p>
    <p v-if="loading">Caricamento...</p>

    <div v-if="!loading" class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">Biglietti venduti</span>
        <span class="kpi-value">{{ venduti }}</span>
      </div>

      <div class="kpi-card chart-card">
        <span class="kpi-label">Distribuzione per stato</span>
        <div class="chart-wrapper">
          <Pie :data="chartData" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
div {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Roboto, sans-serif;
}

h1 {
  color: #2c3e50;
  margin-bottom: 24px;
}

section {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

section h2 {
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  margin-bottom: 12px;
}

input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
  margin-right: 10px;
}

button {
  padding: 8px 16px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

button:hover {
  background-color: #3854c9;
}

.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.kpi-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #6c757d;
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 48px;
  font-weight: 700;
  color: #2c3e50;
}

.chart-wrapper {
  max-width: 320px;
  margin: 0 auto;
}
</style>
