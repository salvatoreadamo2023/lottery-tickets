<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <nav>
      <RouterLink to="/">Biglietti</RouterLink>
      <RouterLink to="/dashboard">Dashboard</RouterLink>
    </nav>
  </header>

  <RouterView />
</template>

<style scoped>
header {
  background-color: #2c3e50;
  padding: 16px 24px;
}

nav {
  display: flex;
  gap: 24px;
}

nav a {
  color: white;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: border-color 0.15s;
}

nav a:hover,
nav a.router-link-active {
  border-bottom-color: #4a6cf7;
}
</style>
