package com.app.siget.dominio;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.app.siget.excepciones.AccessNotGrantedException;
import com.app.siget.excepciones.CredencialesInvalidasException;
import com.app.siget.excepciones.FranjaHorariaOcupadaException;
import com.app.siget.persistencia.ActividadDAO;
import com.app.siget.persistencia.TokenDAO;
import com.app.siget.persistencia.UserDAO;
import java.util.Objects;

public class Manager {

	private WebSocketSession session;
	public static final String USUARIOS = "usuarios";
	public static final String ASISTENTE = "ASISTENTE";

	public Manager() {
		// Metodo constructor vacio (no hay atributos)
	}

	private static class ManagerHolder {
		private static Manager singleton = new Manager();
	}

	public static Manager get() {
		return ManagerHolder.singleton;
	}

	public void login(String name, String password) throws CredencialesInvalidasException, IOException {
		boolean login = false;

		ArrayList<User> usuarios = (ArrayList<User>) UserDAO.leerUsers();
		for (User u : usuarios) {
			login = checkCredenciales(u, name, password);
			if (login) {

				Token t = new Token(name);
				TokenDAO.insert(t);

				JSONObject jso = new JSONObject();
				jso.put("rol", u.getRol());
				jso.put("token", t.getToken());
				if (this.session != null) {
					this.session.sendMessage(new TextMessage(jso.toString()));
				}
				break;
			}
		}
		if (!login) {
			throw new CredencialesInvalidasException();
		}

	}

	public boolean checkCredenciales(User u, String name, String password) throws CredencialesInvalidasException {
		boolean aux = false;
		String pwdEncrypted;
		String pwdUser;
		if (u.getName().equals(name)) {
			pwdEncrypted = u.getPassword();
			pwdUser = encriptarMD5(password);
			if (!(pwdEncrypted.equals(pwdUser))) {

				throw new CredencialesInvalidasException();

			} else {
				aux = true;
			}
		}
		return aux;

	}

	public void register(String name, String email, String password, String rol) {
		User usuario = UserDAO.findUser(name);
		if(usuario==null)
		if ("ADMIN".equals(rol)) {
			UserDAO.insertar(new Admin(name, email, encriptarMD5(password)));
		} else {
			UserDAO.insertar(new Asistente(name, email, encriptarMD5(password)));
		}

	}

	public JSONObject leerUsuarios() {
		JSONArray jsa = new JSONArray();
		JSONObject jso = new JSONObject();
		List<User> usuarios = UserDAO.leerUsers();

		for (User user : usuarios) {

			jsa.put(user.toJSON());
		}
		jso.put(USUARIOS, jsa);

		return jso;

	}

	public JSONArray leerAsistentes() {
		JSONArray jsa = new JSONArray();
		List<User> usuarios = UserDAO.leerUsers(ASISTENTE);

		for (User user : usuarios) {
			jsa.put(user.toJSON());
		}

		return jsa;

	}

	public JSONArray leerReuniones() {
		JSONArray jsa = new JSONArray();
		List<Actividad> actividades = ActividadDAO.leerActividades(true);
		if (!actividades.isEmpty()) {
			for (Actividad act : actividades) {
				jsa.put(act.toJSON());
			}
		}

		return jsa;

	}
	
	public JSONObject filtrarPorSemana(String semana) {
		JSONObject jso = new JSONObject();
		jso.put("type","buscarPorSemana");
		JSONArray jsa = new JSONArray();
		List<Actividad> actividades = ActividadDAO.leerActividades(true);
		if (!actividades.isEmpty()) {
			for (Actividad act : actividades) {
				if (Objects.nonNull(act.getSemana()) && act.getSemana().equals(semana)) {
					jsa.put(act.toJSON());
				}
			}
		}
		jso.put("actividades", jsa);
		return jso;
	}

	public JSONObject leerActividades(String nombre) {
		JSONObject jso = new JSONObject();
		JSONArray jsa = new JSONArray();

		for (Actividad a : ActividadDAO.leerActividades(nombre)) {
			jsa.put(a.toJSON());
		}
		jso.put("actividades", jsa);
		return jso;
	}

	public void insertarActividad(String nombre, String dia, String horaI, String minutosI, String horaF,
			String minutosF, String usuario, String reunion, String semana) throws FranjaHorariaOcupadaException {

		List<User> users = UserDAO.leerUsers();

		LocalTime horaIni = LocalTime.of(Integer.parseInt(horaI), Integer.parseInt(minutosI));
		LocalTime horaFin = LocalTime.of(Integer.parseInt(horaF), Integer.parseInt(minutosF));
		boolean reunionB = Boolean.parseBoolean(reunion);

		for (User user : users) {
			if (usuario.equals(user.getName()) && ASISTENTE.equals(user.getRol())) {
				ActividadDAO.insertarActividad((Asistente) user,
						new Actividad(nombre, DiaSemana.valueOf(dia), horaIni, horaFin, reunionB, semana));
			}
		}
	}

	public void actualizar(String string, boolean boolean1) {
		// sustituir este metodo por su equivalente de los de arriba
	}

	public void eliminarUsuario(String usuario) {
		for (User u : UserDAO.leerUsers()) {
			if (usuario.equals(u.getName()) && ASISTENTE.equals(u.getRol())) {
				UserDAO.eliminar(u, true);
			}
		}
	}

	public void error() {
		// sustituir este metodo por su equivalente de los de arriba
	}

	public JSONObject leer() {
		JSONObject jso = new JSONObject();
		jso.put(USUARIOS, Manager.get().leerAsistentes());
		jso.put("actividades", Manager.get().leerReuniones());
		jso.put("type", "leer");
		return jso;
	}

	public void eliminarTests() {
		for (User u : UserDAO.leerUsers()) {
			if ("nombre".equals(u.getName())) {
				UserDAO.eliminar(u, true);
			}
			if ("asistente".equals(u.getName())) {
				UserDAO.eliminar(u, true);
			}
			if ("admin".equals(u.getName())) {
				UserDAO.eliminar(u, true);
			}
			if ("admin2".equals(u.getName())) {
				UserDAO.eliminar(u, true);
			}
		}
		for (Actividad a : ActividadDAO.leerActividades(true)) {
			if ("nombre periodo no laborable".equals(a.getName())) {
				ActividadDAO.eliminar(a);
			}
		}
		for (Token t : TokenDAO.leerTokensEliminar()) {
			TokenDAO.eliminar(t);
		}
	}

	public void setSession(WebSocketSession session) {
		this.session = session;
	}

	public boolean isAdmin(String nombre) {
		for (User u : UserDAO.leerUsers()) {
			if (nombre.equals(u.getName()) && "ADMIN".equals(u.getRol())) {
				return true;
			}
		}
		return false;
	}

	public void convocarReunion(String nombre, String dia, String horaI, String minutosI, String horaF, String minutosF,
			String usuarios, String reunion, String semana) {

		JSONArray jsa = new JSONArray(usuarios);
		LocalTime horaIni = LocalTime.of(Integer.parseInt(horaI), Integer.parseInt(minutosI));
		LocalTime horaFin = LocalTime.of(Integer.parseInt(horaF), Integer.parseInt(minutosF));
		Actividad reunionPendiente = new Actividad(nombre, DiaSemana.valueOf(dia), horaIni, horaFin,
				Boolean.parseBoolean(reunion), semana);

		for (int i = 0; i < jsa.length(); i++) {
			for (User u : UserDAO.leerUsers()) {
				if (u.getName().equals(jsa.get(i))) {
					ActividadDAO.insertarReunionPend((Asistente) u, reunionPendiente);

				}
			}

		}

	}

	// Este metodo comprueba si la reunion que se quiere convocar se solapa con
	// otras actividades de usuarios. Devuelve el listado de usuarios disponibles
	public JSONArray usuariosDisponibles(String nombre, String dia, String horaI, String minutosI, String horaF,
			String minutosF, String semana) {
		JSONArray jsa = new JSONArray();
		LocalTime horaIni = LocalTime.of(Integer.parseInt(horaI), Integer.parseInt(minutosI));
		LocalTime horaFin = LocalTime.of(Integer.parseInt(horaF), Integer.parseInt(minutosF));

		for (User u : UserDAO.leerUsers()) {
			if (!isAdmin(u.getName()) && !((Asistente) u).getHorario()
					.estaOcupado(new Actividad(nombre, DiaSemana.valueOf(dia), horaIni, horaFin, true, semana))) {

				jsa.put(u.toJSON());
			}

		}
		return jsa;
	}

	public void modificarUsuario(String nombre, String emailNuevo, String passwordNueva) {
		// Mismo metodo para modificar usuario tanto para Asistente como para Admin

		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(nombre)) {
				u.setEmail(emailNuevo);
				u.setPassword(encriptarMD5(passwordNueva));
				UserDAO.modificar(u);
			}
		}
	}

	public void ascenderUsuario(String nombre) {
		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(nombre)) {
				Admin user = new Admin(u.getName(), u.getEmail(), u.getPassword());
				UserDAO.modificar(user);
			}
		}
	}

	public JSONArray leerInfoUsuario(String nombre) {
		JSONArray jsa = new JSONArray();
		JSONObject jso = new JSONObject();
		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(nombre)) {
				jsa.put(u.toJSON());
			}
		}
		jso.put(USUARIOS, jsa);

		return jsa;
	}

	public JSONArray cargarReunionesPendientes(String usuario) {
		JSONArray jsa = new JSONArray();
		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(usuario)) {
				for (int id : ((Asistente) u).getReunionesPendientes()) {
					for (Actividad actv : ActividadDAO.leerActividades(true)) {
						if (id == actv.getId()) {
							jsa.put(actv.toJSON());
						}

					}
				}

			}
		}

		return jsa;
	}

	public void aceptarReunion(String usuario, int id) throws FranjaHorariaOcupadaException {
		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(usuario)) {
				((Asistente) u).quitarReunionPendiente(id);

				for (Actividad actv : ActividadDAO.leerActividades(true)) {
					if (actv.getId() == id) {
						((Asistente) u).insertarActividad(actv);
						UserDAO.modificar(u);

					}
				}

			}
		}
	}

	public void rechazarReunion(String usuario, int id) {
		for (User u : UserDAO.leerUsers()) {
			if (u.getName().equals(usuario)) {
				((Asistente) u).quitarReunionPendiente(id);
				UserDAO.modificar(u);

			}
		}
	}

	public void cerrarSesion(String name) {
		TokenDAO.eliminar(new Token(name));
	}

	public void checkAccess(String name, String token, String page) throws AccessNotGrantedException {

		boolean adminPages = (page.contains("admin.html") || page.contains("gestion.html"));
		boolean adminRole = UserDAO.findUser(name).isAdmin();

		if (token.equals(TokenDAO.getToken(name).getToken())) {
			if (adminPages != adminRole) {
				cerrarSesion(name);
				throw new AccessNotGrantedException();
			}
		} else {
			throw new AccessNotGrantedException();
		}

	}

	public String encriptarMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);

			int diff = 32 - hashtext.length();
			StringBuilder bld = new StringBuilder();

			while (diff > 1) {
				bld.append("0");
				diff--;
			}

			return bld.toString() + hashtext;

		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}
	
	public Object filtrarPorSemanaUsuario(String semana, String usuario) {
		JSONObject jso = new JSONObject();
		JSONArray jsa = new JSONArray();
		
		for (Actividad a : ActividadDAO.leerActividades(usuario)) {
			if(a.getSemana().equals(semana)) {
			jsa.put(a.toJSON());
			}
		}

		jso.put("actividades", jsa);
		return jso;
	}


}
