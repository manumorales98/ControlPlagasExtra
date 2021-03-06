package com.app.siget.ws;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.app.siget.dominio.Manager;

@Component
public class SpringWebSocket extends TextWebSocketHandler {

	private static final String NOMBRE = "nombre";
	private static final String TYPE = "type";
	private static final String VISTA = "vista";
	private static final String HF = "horaFinal";
	private static final String DIA = "dia";
	private static final String HI = "horaInicio";
	private static final String MF = "minutoFinal";
	private static final String MI = "minutoInicio";
	public static final String SEMANA = "semana";

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Manager.get().setSession(session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload().toString());
		switch (jso.getString(TYPE)) {
		case "ready":
			Manager.get().eliminarTests();
			break;

		case "convocarReunion":
			Manager.get().convocarReunion(jso.getString(NOMBRE), jso.getString(DIA), jso.getString(HI),
					jso.getString(MI), jso.getString(HF), jso.getString(MF), jso.get("usuarios").toString(), "true", jso.getString("semana"));
			break;

		case "check":
			session.sendMessage(
					new TextMessage(Manager.get().usuariosDisponibles(jso.getString(NOMBRE), jso.getString(DIA),
							jso.getString(HI), jso.getString(MI), jso.getString(HF), jso.getString(MF), jso.getString("semana")).toString()));
			break;
		case "leer":
			if (Manager.get().isAdmin(jso.getString(NOMBRE)) || "gestionUsuarios".equals(jso.getString(VISTA))) {
				session.sendMessage(new TextMessage(Manager.get().leer().toString()));
			} else {
				session.sendMessage(
						new TextMessage(Manager.get().leerActividades((String) jso.get(NOMBRE)).toString()));
			}
			break;
			
		case "buscarPorSemana":
			System.out.println("Buscar por semana");

			if (Manager.get().isAdmin(jso.getString(NOMBRE)) || "gestionUsuarios".equals(jso.getString(VISTA))) {
				session.sendMessage(new TextMessage(Manager.get().filtrarPorSemana(jso.getString(SEMANA)).toString()));
			} else {
				session.sendMessage(new TextMessage(Manager.get().filtrarPorSemanaUsuario(jso.getString(SEMANA),(String) jso.get(NOMBRE)).toString()));
			}
			break;
			
		case "insertar":
			Manager.get().insertarActividad((String) jso.get(NOMBRE), jso.getString(DIA), jso.getString(HI),
					jso.getString(MI), jso.getString(HF), jso.getString(MF), jso.getString("usuarios"), "false", jso.getString("semana"));
			break;
		case "eliminar":
			Manager.get().eliminarUsuario((String) jso.get(NOMBRE));
			break;
		case "register":
			Manager.get().register((String) jso.get(NOMBRE), jso.getString("email"), jso.getString("pwd1"),
					jso.getString("rol"));
			break;
		case "infoUsuarios":
			session.sendMessage(new TextMessage(Manager.get().leerUsuarios().toString()));
			break;
		case "modificar":
			// Misma condicion para modificar usuario tanto para Asistente como para Admin
			Manager.get().modificarUsuario(jso.getString(NOMBRE), jso.getString("email"), jso.getString("pwd"));
			break;
		case "ascender":
			Manager.get().ascenderUsuario(jso.getString(NOMBRE));
			break;
		case "aceptarReunion":
			Manager.get().aceptarReunion(jso.getString(NOMBRE), jso.getInt("id"));
			session.sendMessage(
					new TextMessage(Manager.get().cargarReunionesPendientes(jso.getString(NOMBRE)).toString()));
			break;
		case "rechazarReunion":
			Manager.get().rechazarReunion(jso.getString(NOMBRE), jso.getInt("id"));
			session.sendMessage(
					new TextMessage(Manager.get().cargarReunionesPendientes(jso.getString(NOMBRE)).toString()));
			break;
		case "reunionesPendientes":
			session.sendMessage(
					new TextMessage(Manager.get().cargarReunionesPendientes(jso.getString(NOMBRE)).toString()));
			break;
		default:
			break;
		}
	}
}
