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
const applying = ref(false)

const filterFrom = ref('')
const filterTo = ref('')

const statusColors = {
  CREATO: '#2563eb',
  SOSPESO: '#b45309',
  VENDUTO: '#15803d',
  SCADUTO: '#b91c1c',
}

const chartData = ref({
  labels: [],
  datasets: [{ data: [], backgroundColor: [] }],
})

// restituisce la data/ora corrente nel formato richiesto da <input type="datetime-local">
function nowForInput() {
  const d = new Date()
  d.setSeconds(0, 0)
  const offset = d.getTimezoneOffset()
  const local = new Date(d.getTime() - offset * 60000)
  return local.toISOString().slice(0, 16)
}

// evita richieste multiple se l'utente clicca più volte velocemente su "Applica"
async function loadKpi() {
  if (applying.value) return

  if (filterFrom.value && filterTo.value && new Date(filterFrom.value) > new Date(filterTo.value)) {
    errorMessage.value = 'La data di inizio non può essere successiva alla data di fine'
    return
  }

  applying.value = true
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getKpi(filterFrom.value || undefined, filterTo.value || undefined)
    venduti.value = response.data.venduti
    distribuzione.value = response.data.distribuzionePerStato

    const labels = Object.keys(distribuzione.value)
    chartData.value = {
      labels,
      datasets: [
        {
          data: Object.values(distribuzione.value),
          backgroundColor: labels.map(status => statusColors[status] || '#9ca3af'),
        },
      ],
    }
  } catch (error) {
    errorMessage.value = 'Errore nel caricamento dei KPI'
    console.error(error)
  } finally {
    loading.value = false
    applying.value = false
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
      <input type="datetime-local" v-model="filterFrom" :max="filterTo || nowForInput()" />
      <input type="datetime-local" v-model="filterTo" :min="filterFrom" :max="nowForInput()" />
      <button @click="loadKpi" :disabled="applying">
        {{ applying ? 'Applico...' : 'Applica' }}
      </button>
    </section>

    <p v-if="errorMessage" class="error-banner">{{ errorMessage }}</p>
    <p v-if="loading">Caricamento...</p>

    <div v-if="!loading" class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">Biglietti venduti</span>
        <span class="kpi-value">{{ venduti }}</span>
      </div>

      <div class="kpi-card chart-card">
        <span class="kpi-label">Distribuzione per stato</span>
        <div class="chart-wrapper">
          <Pie v-if="chartData.labels.length > 0" :data="chartData" />
          <p v-else class="empty-state-small">Nessun dato disponibile per il periodo selezionato.</p>
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

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-banner {
  background: #fdecea;
  color: #dc3545;
  padding: 10px 14px;
  border-radius: 6px;
  border-left: 4px solid #dc3545;
  margin-bottom: 16px;
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

.empty-state-small {
  text-align: center;
  color: #6c757d;
  font-size: 13px;
  padding: 40px 0;
}
</style>