package com.app.siget.persistencia;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import com.app.siget.dominio.Actividad;
import com.app.siget.dominio.Admin;
import com.app.siget.dominio.Asistente;
import com.app.siget.dominio.Horario;
import com.app.siget.dominio.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Repository
public final class UserDAO {
	public static final String USUARIO = "users";
	public static final String EMAIL = "email";
	public static final String PASSWORD = "password";
	public static final String NAME = "name";
	public static final String ADMIN = "ADMIN";
	public static final String HORARIO = "horario";
	public static final String REUNIONESPENDIENTES = "reunionesPendientes";

	private UserDAO() {
		super();
	}

	public static User findUser(String name) {
		for (User u : UserDAO.leerUsers()) {
			if (name.equals(u.getName())) {
				return u;
			}
		}
		return null;
	}

	public static List<User> leerUsers() {
		ArrayList<User> usuarios = new ArrayList<>();
		Document document;
		User u;
		MongoCollection<Document> coleccion = AgenteDB.get().getBd(USUARIO);
		MongoCursor<Document> iter = coleccion.find().iterator();

		while ((iter.hasNext())) {
			document = iter.next();
			if ((ADMIN).equals(document.getString("rol"))) {
				u = new Admin(document.getString(NAME), document.getString(EMAIL), document.getString(PASSWORD));
			} else {
				u = new Asistente(document.getString(NAME), document.getString(EMAIL), document.getString(PASSWORD));
				((Asistente) u).setReunionesPendientes(document.getString(REUNIONESPENDIENTES));
				((Asistente) u).setHorario(Horario.string2Horario(document.getString(HORARIO)));

			}

			usuarios.add(u);
		}

		return usuarios;
	}

	public static List<User> leerUsers(String rol) {
		ArrayList<User> usuarios = new ArrayList<>();
		Document document;
		User u;
		MongoCollection<Document> coleccion = AgenteDB.get().getBd(USUARIO);
		MongoCursor<Document> iter = coleccion.find().iterator();

		while ((iter.hasNext())) {
			document = iter.next();
			if ((rol).equals(document.getString("rol"))) {
				if (ADMIN.equals(rol)) {
					u = new Admin(document.getString(NAME), document.getString(EMAIL), document.getString(PASSWORD));
				} else {
					u = new Asistente(document.getString(NAME), document.getString(EMAIL),
							document.getString(PASSWORD));
					((Asistente) u).setHorario(Horario.string2Horario(document.getString(HORARIO)));
					((Asistente) u).setReunionesPendientes(document.getString(REUNIONESPENDIENTES));

				}

				usuarios.add(u);
			}
		}
		return usuarios;
	}

	public static void insertar(User user) {
		Document document;
		MongoCollection<Document> coleccion;
		if (user != null) {
			coleccion = AgenteDB.get().getBd(USUARIO);
			document = new Document(NAME, user.getName());
			document.append(EMAIL, user.getEmail());
			document.append(PASSWORD, user.getPassword());
			document.append("rol", user.getRol());
			if (user.getRol().equals("ASISTENTE")) {
				document.append(HORARIO, ((Asistente) user).getHorario().toString());
				document.append(REUNIONESPENDIENTES, ((Asistente) user).getReunionesPendientes().toString());
			}
			coleccion.insertOne(document);
		}
	}

	public static void eliminar(User user, boolean permanente) {

		Document document;
		MongoCollection<Document> coleccion;

		if (user != null) {
			coleccion = AgenteDB.get().getBd(USUARIO);
			document = new Document("name", user.getName());
			if (permanente && "ASISTENTE".equals(user.getRol())) {
				for (Actividad a : ActividadDAO.leerActividades(user.getName())) {
					if (!a.isReunion()) {
						ActividadDAO.eliminar(a);
					}
				}
			}
			coleccion.findOneAndDelete(document);
		}
	}

	public static void modificar(User u) {
		// Mismo metodo para modificar usuario tanto para Asistente como para Admin
		UserDAO.eliminar(u, false);
		UserDAO.insertar(u);
	}

}
