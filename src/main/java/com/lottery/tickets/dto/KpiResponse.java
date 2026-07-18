package com.lottery.tickets.dto;

import java.util.Map;

public class KpiResponse {

	private long venduti;
	private Map<String, Long> distribuzionePerStato;

	public long getVenduti() {
		return venduti;
	}

	public void setVenduti(long venduti) {
		this.venduti = venduti;
	}

	public Map<String, Long> getDistribuzionePerStato() {
		return distribuzionePerStato;
	}

	public void setDistribuzionePerStato(Map<String, Long> distribuzionePerStato) {
		this.distribuzionePerStato = distribuzionePerStato;
	}
}