package com.app.siget.http;

import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.siget.dominio.Manager;
import com.app.siget.excepciones.AccessNotGrantedException;
import com.app.siget.excepciones.CredencialesInvalidasException;
import com.app.siget.excepciones.DiferentesContrasenasException;


@RestController
@CrossOrigin(origins= {"http://localhost:8080","https://sige-equipo1-mantenimiento.herokuapp.com"},allowedHeaders="*")
public class Controller {

	private static final String PASS = "pwd";
	private static final String USERNAME = "userName";

	@PostMapping("/login")
	public void login(@RequestBody Map<String, Object> credenciales)
			throws CredencialesInvalidasException, IOException {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString(USERNAME);
		String password = jso.getString(PASS);
		Manager.get().login(name, password);
	}

	@PostMapping("/register")
	public void register(@RequestBody Map<String, Object> credenciales) throws DiferentesContrasenasException {
		JSONObject jso = new JSONObject(credenciales);
		String password = jso.getString(PASS);
		String passwordConfirmacion = jso.getString("pwd2");

		if (!password.equals(passwordConfirmacion)) {
			throw new DiferentesContrasenasException();
		}

		String name = jso.getString(USERNAME);
		String email = jso.getString("email");
		String rol = jso.getString("rol");

		Manager.get().register(name, email, password, rol);
	}

	@PostMapping("/cerrarSesion")
	public void cerrarSesion(@RequestBody Map<String, Object> credenciales) {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString(USERNAME);
		Manager.get().cerrarSesion(name);
	}

	@PostMapping("/checkAccess")
	public void checkAccess(@RequestBody Map<String, Object> credenciales) throws AccessNotGrantedException {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString(USERNAME);
		String token = jso.getString("token");
		String page = jso.getString("page");
		String[] parts = page.split("/");
		Manager.get().checkAccess(name, token, parts[parts.length - 1]);
	}

}
