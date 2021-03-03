package com.app.siget.persistencia;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.stereotype.Repository;
import com.app.siget.dominio.Token;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Repository
public final class TokenDAO {

	public static final String TOKENS = "tokens";
	public static final String FECHA = "fecha";
	public static final String NAME = "name";
	public static final String TOKEN = "token";

	private TokenDAO() {
		super();
	}

	public static List<Token> leerTokensEliminar() {
		ArrayList<Token> tokens = new ArrayList<>();
		Document document;
		Token t;
		MongoCollection<Document> coleccion = AgenteDB.get().getBd(TOKENS);
		MongoCursor<Document> iter = coleccion.find().iterator();

		while ((iter.hasNext())) {
			document = iter.next();
			t = new Token(document.getString(NAME), document.getString(TOKEN), document.getString(FECHA));
			if ("nombre".equals(document.getString(NAME)) || t.isExpired()) {
				tokens.add(t);
			}
		}

		return tokens;
	}

	public static Token getToken(String name) {
		Document document;
		Token t;
		MongoCollection<Document> coleccion = AgenteDB.get().getBd(TOKENS);
		MongoCursor<Document> iter = coleccion.find().iterator();

		while ((iter.hasNext())) {
			document = iter.next();
			if (name.equals(document.get(NAME))) {
				t = new Token(document.getString(NAME), document.getString(TOKEN), document.getString(FECHA));
				return t;
			}

		}
		return null;
	}

	public static void insert(Token t) {
		Document document;
		MongoCollection<Document> coleccion;
		if (t != null) {
			
			while(getToken(t.getName())!=null) {
				eliminar(t);
			}
			coleccion = AgenteDB.get().getBd(TOKENS);
			document = new Document(NAME, t.getName());
			document.append(TOKEN, t.getToken());
			document.append(FECHA, t.getFecha());
			coleccion.insertOne(document);
		}
	}

	public static void eliminar(Token t) {
		Document document;
		MongoCollection<Document> coleccion;

		if (t != null) {
			coleccion = AgenteDB.get().getBd(TOKENS);
			document = new Document("name", t.getName());
			coleccion.findOneAndDelete(document);
		}
	}

}
