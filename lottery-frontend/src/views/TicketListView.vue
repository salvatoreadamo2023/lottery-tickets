<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTickets, createTicket, updateTicketStatus, deleteTicket } from '../services/ticketApi'

const tickets = ref([])
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const filterStatus = ref('')
const filterFrom = ref('')
const filterTo = ref('')

const newExtractAt = ref('')
const creating = ref(false)

const currentPage = ref(1)
const pageSize = 20

const totalPages = computed(() => Math.max(1, Math.ceil(tickets.value.length / pageSize)))

const paginatedTickets = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return tickets.value.slice(start, start + pageSize)
})

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// restituisce la data/ora corrente nel formato richiesto da <input type="datetime-local">
function nowForInput() {
  const d = new Date()
  d.setSeconds(0, 0)
  const offset = d.getTimezoneOffset()
  const local = new Date(d.getTime() - offset * 60000)
  return local.toISOString().slice(0, 16)
}

// trasforma "2025-12-04T23:00:00" in "04/12/2025 23:00" per la visualizzazione in tabella
function formatDateTime(value) {
  if (!value) return ''
  const [datePart, timePart] = value.split('T')
  if (!datePart || !timePart) return value
  const [year, month, day] = datePart.split('-')
  const time = timePart.substring(0, 5)
  return `${day}/${month}/${year} ${time}`
}

function showSuccess(message) {
  successMessage.value = message
  errorMessage.value = ''
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

function showError(message) {
  errorMessage.value = message
  successMessage.value = ''
}

async function loadTickets() {
  if (filterFrom.value && filterTo.value && new Date(filterFrom.value) > new Date(filterTo.value)) {
    showError('La data di inizio non può essere successiva alla data di fine')
    return
  }

  loading.value = true
  try {
    const filters = {}
    if (filterStatus.value) filters.status = filterStatus.value
    if (filterFrom.value) filters.from = filterFrom.value
    if (filterTo.value) filters.to = filterTo.value

    const response = await getTickets(filters)
    tickets.value = response.data
    currentPage.value = 1
  } catch (error) {
    showError('Errore nel caricamento dei biglietti')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// evita doppie creazioni se l'utente clicca più volte velocemente
async function handleCreate() {
  if (creating.value) return

  if (!newExtractAt.value) {
    showError('Inserisci una data di estrazione')
    return
  }

  creating.value = true
  try {
    await createTicket(newExtractAt.value)
    newExtractAt.value = ''
    showSuccess('Biglietto creato con successo')
    await loadTickets()
  } catch (error) {
    showError('Errore nella creazione del biglietto')
    console.error(error)
  } finally {
    creating.value = false
  }
}

async function handleStatusChange(ticketId, newStatus) {
  try {
    await updateTicketStatus(ticketId, newStatus)
    showSuccess(`Stato di ${ticketId} aggiornato a ${newStatus}`)
    await loadTickets()
  } catch (error) {
    showError('Errore nel cambio stato (verifica che il biglietto non sia SCADUTO)')
    console.error(error)
    await loadTickets()
  }
}

async function handleDelete(ticketId) {
  if (!confirm(`Sei sicuro di voler eliminare il biglietto ${ticketId}?`)) {
    return
  }
  try {
    await deleteTicket(ticketId)
    showSuccess(`Biglietto ${ticketId} eliminato`)
    await loadTickets()
  } catch (error) {
    showError('Errore nella cancellazione')
    console.error(error)
  }
}

onMounted(() => {
  loadTickets()
})
</script>

<template>
  <div>
    <h1>Lista Biglietti</h1>

    <section>
      <h2>Nuovo biglietto</h2>
      <input type="datetime-local" v-model="newExtractAt" :min="nowForInput()" />
      <button @click="handleCreate" :disabled="creating">
        {{ creating ? 'Creazione...' : 'Crea' }}
      </button>
    </section>

    <section>
      <h2>Filtri</h2>
      <select v-model="filterStatus">
        <option value="">Tutti gli stati</option>
        <option value="CREATO">CREATO</option>
        <option value="SOSPESO">SOSPESO</option>
        <option value="VENDUTO">VENDUTO</option>
        <option value="SCADUTO">SCADUTO</option>
      </select>
      <input type="datetime-local" v-model="filterFrom" placeholder="Da" :max="filterTo || nowForInput()" />
      <input type="datetime-local" v-model="filterTo" placeholder="A" :min="filterFrom" :max="nowForInput()" />
      <button @click="loadTickets">Applica filtri</button>
    </section>

    <p v-if="successMessage" class="success-banner">{{ successMessage }}</p>
    <p v-if="errorMessage" class="error-banner">{{ errorMessage }}</p>
    <p v-if="loading">Caricamento...</p>

    <table v-if="!loading && tickets.length > 0">
      <thead>
        <tr>
          <th>Ticket ID</th>
          <th>Stato</th>
          <th>Creato il</th>
          <th>Estrazione</th>
          <th>Azioni</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in paginatedTickets" :key="ticket.ticketId">
          <td>{{ ticket.ticketId }}</td>
          <td>
            <span class="badge" :class="`badge-${ticket.status.toLowerCase()}`">{{ ticket.status }}</span>
          </td>
          <td>{{ formatDateTime(ticket.createdAt) }}</td>
          <td>{{ formatDateTime(ticket.extractAt) }}</td>
          <td>
            <select
              :value="ticket.status"
              @change="handleStatusChange(ticket.ticketId, $event.target.value)"
            >
              <option value="CREATO">CREATO</option>
              <option value="SOSPESO">SOSPESO</option>
              <option value="VENDUTO">VENDUTO</option>
              <option value="SCADUTO">SCADUTO</option>
            </select>
            <button @click="handleDelete(ticket.ticketId)">Elimina</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-if="!loading && tickets.length === 0" class="empty-state">
      Nessun biglietto trovato con i filtri selezionati.
    </p>

    <div class="pagination" v-if="!loading && tickets.length > 0">
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1">Precedente</button>
      <span>Pagina {{ currentPage }} di {{ totalPages }} ({{ tickets.length }} biglietti)</span>
      <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages">Successiva</button>
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

input,
select {
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
  transition: background-color 0.15s;
}

button:hover {
  background-color: #3854c9;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

thead {
  background-color: #2c3e50;
  color: white;
}

thead th {
  padding: 14px 16px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
  text-align: left;
}

tbody td {
  padding: 14px 16px;
  color: #374151;
  font-size: 14px;
}

tbody tr {
  border-bottom: 1px solid #eee;
}

tbody tr:nth-child(even) {
  background-color: #fafbfc;
}

tbody tr:hover {
  background-color: #eef2ff;
}

tbody tr:last-child {
  border-bottom: none;
}

td select {
  margin-right: 8px;
  padding: 6px 10px;
  font-size: 13px;
}

td button {
  padding: 6px 14px;
  font-size: 13px;
  background-color: #dc3545;
}

td button:hover {
  background-color: #b02a37;
}

.badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.badge-creato {
  background-color: #e7f1ff;
  color: #2563eb;
}

.badge-sospeso {
  background-color: #fef3c7;
  color: #b45309;
}

.badge-venduto {
  background-color: #d1fae5;
  color: #15803d;
}

.badge-scaduto {
  background-color: #fee2e2;
  color: #b91c1c;
}

.error-banner {
  background: #fdecea;
  color: #dc3545;
  padding: 10px 14px;
  border-radius: 6px;
  border-left: 4px solid #dc3545;
  margin-bottom: 16px;
}

.success-banner {
  background: #eafaf1;
  color: #1e7e34;
  padding: 10px 14px;
  border-radius: 6px;
  border-left: 4px solid #28a745;
  margin-bottom: 16px;
}

.empty-state {
  text-align: center;
  color: #6c757d;
  background: #f8f9fa;
  border: 1px dashed #ccc;
  border-radius: 8px;
  padding: 30px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
  font-size: 14px;
  color: #6c757d;
}
</style>