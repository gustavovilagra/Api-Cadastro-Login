        document.addEventListener("DOMContentLoaded", function () {
          new Vue({
              el: '#app',
              data: {
                 errorMessage: ''
                     },
                 methods: {

                      submitForm() {
                          const emailValue = this.$el.querySelector('#email').value;
                          const passwordValue = this.$el.querySelector('#password').value;

                          console.log("Email:", emailValue);
                          console.log("Password:", passwordValue);

                          const dataToSend = {
                              email: emailValue,
                              password: passwordValue
                          };

                         fetch('/webapp/submitI', {
                             method: 'POST',
                             headers: {
                                 'Content-Type': 'application/json'
                             },
                             body: JSON.stringify(dataToSend)
                         })
                         .then(response => response.text())
                         .then(data => {
                             if (data.includes('error')) {
                                 this.errorMessage = 'Contraseña incorrecta. Por favor, verificar.';
                             } else if(data.includes('usuario_no_existente')) {
                                 window.location.href = '/webapp/registro'; // Redireccionar a la página de registro
                             }else if(data.includes('usuario_existe')){
                                 window.location.href = "http://localhost:5501/principal.html";
                             }
                         })
                         .catch(error => {
                             console.error('There has been a problem with your fetch operation:', error);
                         });
                    }
                 }
             });

            var registrationForm = document.getElementById("registrationForm");
            var submitButton = document.getElementById("submitButton");
            var passwordInput = document.getElementById("password");
            var confirm_passwordInput = document.getElementById("confirm_password");
            var passwordError = document.getElementById("password_error");
            var togglePasswordButton = document.getElementById("togglePassword");


          togglePasswordButton.addEventListener("click", function () {
                 if (passwordInput.type === "password") {
                     passwordInput.type = "text";
                     togglePasswordButton.innerHTML = '<i class="fas fa-eye-slash"></i>'; // Cerrado
                 } else {
                     passwordInput.type = "password";
                      togglePasswordButton.innerHTML ='<i class="fas fa-eye"></i>'; // Abierto
                 }
             });


            if (confirm_passwordInput) {
                // Agregar el evento solo si confirm_passwordInput existe
                confirm_passwordInput.addEventListener("input", function () {
                    var password = passwordInput.value;
                    var confirmPassword = confirm_passwordInput.value;

                    if (password !== confirmPassword) {
                        passwordError.textContent = "Las contraseñas no coinciden. Por favor, verifica.";
                    } else {
                        passwordError.textContent = ""; // Borra el mensaje de error si las contraseñas coinciden
                    }

                    // Realiza la validación de tus campos aquí
                    var nombre = document.getElementById("nombre").value;
                    var email = document.getElementById("email").value;
                    var password = passwordInput.value;
                    var confirm_password = confirm_passwordInput.value;

                    if (password !== confirm_password) {
                        passwordError.textContent = "Las contraseñas no coinciden. Por favor, verifica.";
                    } else {
                        passwordError.textContent = ""; // Borra el mensaje de error si las contraseñas coinciden
                    }

                    // Verifica si todos los campos tienen valores y si la contraseña coincide con la confirmación
                    if (nombre && email && password && confirm_password && password === confirm_password) {
                        // Habilita el botón de registro si todos los campos están completados y la contraseña coincide
                        submitButton.disabled = false;
                    } else {
                        // Si falta algún campo o las contraseñas no coinciden, deshabilita el botón de registro
                        submitButton.disabled = true;
                    }
                });
            }

        });

