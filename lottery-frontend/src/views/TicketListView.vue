<script setup>
import { ref, onMounted } from 'vue'
import { getTickets, createTicket, updateTicketStatus, deleteTicket } from '../services/ticketApi'

const tickets = ref([])
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const filterStatus = ref('')
const filterFrom = ref('')
const filterTo = ref('')

const newExtractAt = ref('')

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
  loading.value = true
  try {
    const filters = {}
    if (filterStatus.value) filters.status = filterStatus.value
    if (filterFrom.value) filters.from = filterFrom.value
    if (filterTo.value) filters.to = filterTo.value

    const response = await getTickets(filters)
    tickets.value = response.data
  } catch (error) {
    showError('Errore nel caricamento dei biglietti')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newExtractAt.value) {
    showError('Inserisci una data di estrazione')
    return
  }
  try {
    await createTicket(newExtractAt.value)
    newExtractAt.value = ''
    showSuccess('Biglietto creato con successo')
    await loadTickets()
  } catch (error) {
    showError('Errore nella creazione del biglietto')
    console.error(error)
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
      <input type="datetime-local" v-model="newExtractAt" />
      <button @click="handleCreate">Crea</button>
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
      <input type="datetime-local" v-model="filterFrom" placeholder="Da" />
      <input type="datetime-local" v-model="filterTo" placeholder="A" />
      <button @click="loadTickets">Applica filtri</button>
    </section>

    <p v-if="successMessage" class="success-banner">{{ successMessage }}</p>
    <p v-if="errorMessage" class="error-banner">{{ errorMessage }}</p>
    <p v-if="loading">Caricamento...</p>

    <table v-if="!loading">
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
        <tr v-for="ticket in tickets" :key="ticket.ticketId">
          <td>{{ ticket.ticketId }}</td>
          <td>{{ ticket.status }}</td>
          <td>{{ ticket.createdAt }}</td>
          <td>{{ ticket.extractAt }}</td>
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

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

thead {
  background-color: #2c3e50;
  color: white;
}

th,
td {
  padding: 12px 14px;
  text-align: left;
  font-size: 14px;
}

tbody tr {
  border-bottom: 1px solid #eee;
}

tbody tr:hover {
  background-color: #f5f7ff;
}

tbody tr:last-child {
  border-bottom: none;
}

td select {
  margin-right: 8px;
}

td button {
  background-color: #dc3545;
}

td button:hover {
  background-color: #b02a37;
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
</style>
