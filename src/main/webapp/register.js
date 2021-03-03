if("localhost:8080"== window.location.host){
	var url = 'ws://' + window.location.host + '/SIGETEquipo1';
}else{
	var url = 'wss://' + window.location.host + '/SIGETEquipo1';
}
var sws = new WebSocket(url);

sws.onopen = function(event) {
	var msg = {
		type: "infoUsuarios"
	};
	sws.send(JSON.stringify(msg));
};
sws.onmessage = function(event) {
	var data = event.data;
	data = JSON.parse(data);
	var users = [];

	for (var j = 0; j < data.usuarios.length; j++) {
		users[j] = data.usuarios[j].name;
	}

	sessionStorage.users = JSON.stringify(users);
};

window.onbeforeunload = function() {
	sessionStorage.removeItem("users");
};

let register = function() {

	document.getElementById("pwd1").style.backgroundColor = "transparent";
	document.getElementById("pwd2").style.backgroundColor = "transparent";
	document.getElementById("username").style.backgroundColor = "transparent";
	document.getElementById("email").style.backgroundColor = "transparent";

	if (usernameValido($('#username').val())) {
		if (contrasenaValida($('#pwd1').val())) {
			const info = {
				type: 'Register',
				userName: $('#username').val(),
				email: $('#email').val(),
				pwd: $('#pwd1').val(),
				pwd2: $('#pwd2').val(),
				rol: $('#rol').val()
			};
			const data = {
				data: JSON.stringify(info),
				url: 'register',
				type: 'post',
				contentType: 'application/json',
				success: function() {
					document.getElementById("pwd1").style.backgroundColor = "green";
					document.getElementById("pwd2").style.backgroundColor = "green";
					document.getElementById("username").style.backgroundColor = "green";
					document.getElementById("email").style.backgroundColor = "green";
					registroCorrecto();

				},
				error: function() {
					document.getElementById("pwd1").style.backgroundColor = "red";
					document.getElementById("pwd2").style.backgroundColor = "red";
				}
			};
			$.ajax(data);
		}
	} else {
		document.getElementById("username").style.backgroundColor = "red";
	}
};

function usernameValido(userName) {


	var users = JSON.parse('[' + sessionStorage.getItem('users') + ']');
	users = users[0];
	
	if(userName === ''){
		return false;
	}

	for (var j = 0; j < users.length; j++) {
		if (users[j] === userName) {
			return false;
		}
	}
	return true;
}

function contrasenaValida(pwd) {

	if (pwd.length > 4 && tiene_numeros(pwd) && tiene_minuscula_y_mayuscula(pwd)) {
		return true;
	} else {
		document.getElementById("pwd1").style.backgroundColor = "red";
		return false;
	}


}

function tiene_numeros(texto) {
	let numeros = "0123456789";
	for (i = 0; i < texto.length; i++) {
		if (numeros.indexOf(texto.charAt(i), 0) !== -1) {
			return true;
		}
	}
	return false;
}

function tiene_minuscula_y_mayuscula(pwd) {
	let m = false;
	let M = false;
	for (i = 0; i < pwd.length; i++) {
		if (!esNumero(pwd.charAt(i)) && pwd.charAt(i) === pwd.charAt(i).toUpperCase()) {
			M = true;
		}
		if (!esNumero(pwd.charAt(i)) && pwd.charAt(i) === pwd.charAt(i).toLowerCase()) {
			m = true;
		}
	}


	return M && m;

}

function esNumero(digito) {
	let numeros = "0123456789";
	if (numeros.indexOf(digito, 0) !== -1) {
		return true;
	}
	return false;


}

function volverAtras(){
	window.location.href = "index.html"
}


function registroCorrecto() {

	// When site loaded, load the Popupbox First
	loadPopupBox();
	volverAtras()
	$('#container').click(function() {
		unloadPopupBox();
	});

	function unloadPopupBox() {    // TO Unload the Popupbox
		$('#popup_box').fadeOut("slow");
		$("#container").css({ // this is just for style        
			"opacity": "1"
		});
	}

	function loadPopupBox() {    // To Load the Popupbox

		var counter = 5;
		var id;
		$('#popup_box').fadeIn("slow");
		$("#container").css({ // this is just for style
			"opacity": "0.3"
		});

		id = setInterval(function() {
			counter--;
			if (counter < 0) {
				clearInterval(id);

				unloadPopupBox();
			} else {
				$("#countDown").text("it closed  after " + counter.toString() + " seconds.");
			}
		}, 500);

	}
	
}





