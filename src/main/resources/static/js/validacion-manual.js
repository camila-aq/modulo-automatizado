console.log("validacion-manual.js cargado");

document.addEventListener("DOMContentLoaded", () => {

    const campoDni =
        document.getElementById("dni");

    const campoCodigo =
        document.getElementById("codigoUniversitario");

    const nombreUsuario =
        document.getElementById("nombreUsuario");

    const botonBuscarReserva =
        document.getElementById("botonBuscarReserva");

    const botonLimpiar =
        document.getElementById("botonLimpiar");

    const mensaje =
        document.getElementById("mensaje");

    let temporizadorIdentificacion;

    let usuarioIdentificado = false;


    campoDni.addEventListener("input", () => {

        campoCodigo.value = "";

        reiniciarIdentificacion();

        clearTimeout(
            temporizadorIdentificacion
        );

        const dni =
            campoDni.value.trim();

        if (dni.length === 8) {

            temporizadorIdentificacion =
                setTimeout(
                    () => identificarUsuario(
                        "DNI",
                        dni
                    ),
                    350
                );
        }
    });


    campoCodigo.addEventListener("input", () => {

        campoDni.value = "";

        reiniciarIdentificacion();

        clearTimeout(
            temporizadorIdentificacion
        );

        const codigo =
            campoCodigo.value.trim();

        if (codigo.length > 0) {

            temporizadorIdentificacion =
                setTimeout(
                    () => identificarUsuario(
                        "CODIGO_UNIVERSITARIO",
                        codigo
                    ),
                    600
                );
        }
    });


    botonLimpiar.addEventListener("click", () => {

        campoDni.value = "";
        campoCodigo.value = "";

        reiniciarIdentificacion();

        ocultarMensaje();

        campoDni.focus();
    });


    async function identificarUsuario(
        tipo,
        valor
    ) {

        try {

            const parametros =
                new URLSearchParams({
                    tipo: tipo,
                    valor: valor
                });

            const respuesta =
                await fetch(
                    "/api/validaciones-ingreso/manual/identificar?"
                    + parametros.toString()
                );

            const datos =
                await respuesta.json();

            if (!respuesta.ok) {

                throw new Error(
                    datos.mensaje
                    || "No fue posible identificar al usuario."
                );
            }

            const nombreCompleto =
                `${datos.nombres} ${datos.apellidos}`;

            nombreUsuario.textContent =
                nombreCompleto;

            usuarioIdentificado = true;

            botonBuscarReserva.disabled =
                false;

            ocultarMensaje();

        } catch (error) {

            reiniciarIdentificacion();

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }


    function reiniciarIdentificacion() {

        usuarioIdentificado = false;

        nombreUsuario.textContent = "—";

        botonBuscarReserva.disabled = true;
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent = texto;

        mensaje.className =
            `mensaje mensaje-${tipo}`;

        mensaje.hidden = false;
    }


    function ocultarMensaje() {

        mensaje.textContent = "";

        mensaje.className = "mensaje";

        mensaje.hidden = true;
    }

});