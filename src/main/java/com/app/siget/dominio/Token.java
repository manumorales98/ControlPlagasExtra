package com.app.siget.dominio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Token {

	private String name;
	private final String uuid;
	private String fecha;
	
	public Token(String name, String uuid, String fecha) {
		this.name = name;
		this.uuid = uuid;
		this.fecha = fecha;
	}

	public Token(String name) {
		this.name = name;
		this.uuid = UUID.randomUUID().toString().replace("-", "");
		this.fecha = LocalDateTime.now().toString();
	}

	public String getName() {
		return name;
	}

	public String getToken() {
		return uuid;
	}

	public String getFecha() {
		return fecha;
	}
	
	public boolean isExpired() {
		Boolean res = false;
		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime fechaL = LocalDateTime.parse(this.getFecha());
		Duration diference = Duration.between(fechaL, ahora);
		if(diference.toMinutes()>59) {
			res = true;
		}
		return res;
	}

}
