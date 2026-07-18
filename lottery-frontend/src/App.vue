<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <nav>
      <RouterLink to="/">Biglietti</RouterLink> |
      <RouterLink to="/dashboard">Dashboard</RouterLink>
    </nav>
  </header>

  <RouterView />
</template>