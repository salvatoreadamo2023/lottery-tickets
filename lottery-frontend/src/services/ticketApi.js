import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  auth: {
    username: import.meta.env.VITE_API_USERNAME,
    password: import.meta.env.VITE_API_PASSWORD
  }
})

export function getTickets(filters = {}) {
  return apiClient.get('/tickets', { params: filters })
}

export function getTicket(ticketId) {
  return apiClient.get(`/tickets/${ticketId}`)
}

export function createTicket(extractAt) {
  return apiClient.post('/tickets', { extractAt })
}

export function updateTicketStatus(ticketId, status) {
  return apiClient.put(`/tickets/${ticketId}/status`, { status })
}

export function deleteTicket(ticketId) {
  return apiClient.delete(`/tickets/${ticketId}`)
}

export function getKpi(from, to) {
  return apiClient.get('/tickets/kpi', { params: { from, to } })
}