document.addEventListener("DOMContentLoaded", () => {

    const codigoPunto =
        document.getElementById(
            "codigoPunto"
        );

    const dniEscaneado =
        document.getElementById(
            "dniEscaneado"
        );

    const botonEscanear =
        document.getElementById(
            "botonEscanear"
        );

    const botonLimpiarEscaneo =
        document.getElementById(
            "botonLimpiarEscaneo"
        );

    const resultadoEscaneo =
        document.getElementById(
            "resultadoEscaneo"
        );

    const estadoEscaneo =
        document.getElementById(
            "estadoEscaneo"
        );

    const resultadoDni =
        document.getElementById(
            "resultadoDni"
        );

    const resultadoIdValidacion =
        document.getElementById(
            "resultadoIdValidacion"
        );

    const resultadoPunto =
        document.getElementById(
            "resultadoPunto"
        );

    const resultadoMedio =
        document.getElementById(
            "resultadoMedio"
        );

    const resultadoFechaHora =
        document.getElementById(
            "resultadoFechaHora"
        );

    const mensajeEscaneo =
        document.getElementById(
            "mensajeEscaneo"
        );


    codigoPunto.addEventListener(
        "input",
        actualizarEstadoBoton
    );


    dniEscaneado.addEventListener(
        "input",
        () => {

            dniEscaneado.value =
                dniEscaneado.value.replace(
                    /\D/g,
                    ""
                );

            actualizarEstadoBoton();
        }
    );


    dniEscaneado.addEventListener(
        "keydown",
        async event => {

            if (
                event.key === "Enter"
                &&
                !botonEscanear.disabled
            ) {

                await ejecutarEscaneo();
            }
        }
    );


    botonEscanear.addEventListener(
        "click",
        ejecutarEscaneo
    );


    botonLimpiarEscaneo.addEventListener(
        "click",
        () => {

            /*
             * Conservamos el punto de validación,
             * ya que representa el dispositivo
             * instalado en un ambiente concreto.
             */
            dniEscaneado.value =
                "";

            resultadoEscaneo.hidden =
                true;

            ocultarMensaje();

            actualizarEstadoBoton();

            dniEscaneado.focus();
        }
    );


    function actualizarEstadoBoton() {

        const puntoValido =
            codigoPunto.value
                .trim()
                .length > 0;

        const dniValido =
            dniEscaneado.value
                .trim()
                .length === 8;

        botonEscanear.disabled =
            !puntoValido
            ||
            !dniValido;
    }


    async function ejecutarEscaneo() {

        const dni =
            dniEscaneado.value.trim();

        const punto =
            codigoPunto.value.trim();


        botonEscanear.disabled =
            true;

        ocultarMensaje();

        resultadoEscaneo.hidden =
            true;


        try {

            const respuesta =
                await fetch(
                    "/api/validaciones-ingreso/escaneo",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            dni: dni,
                            codigoPunto: punto
                        })
                    }
                );


            const datos =
                await respuesta.json();


            if (!respuesta.ok) {

                throw new Error(
                    datos.mensaje
                    || datos.message
                    || "La validación fue rechazada."
                );
            }


            estadoEscaneo.textContent =
                "VALIDACIÓN ACEPTADA";

            resultadoDni.textContent =
                dni;

            resultadoIdValidacion.textContent =
                datos.idValidacion;

            resultadoPunto.textContent =
                datos.codigoPunto;

            resultadoMedio.textContent =
                datos.medioValidacion;

            resultadoFechaHora.textContent =
                formatearFechaHora(
                    datos.fechaHoraValidacion
                );


            resultadoEscaneo.hidden =
                false;


            mostrarMensaje(
                "Ingreso validado correctamente.",
                "exito"
            );


            /*
             * Dejamos el punto configurado
             * para permitir el siguiente escaneo.
             */
            dniEscaneado.value =
                "";

            dniEscaneado.focus();

        } catch (error) {

            estadoEscaneo.textContent =
                "VALIDACIÓN RECHAZADA";

            resultadoDni.textContent =
                dni;

            resultadoIdValidacion.textContent =
                "—";

            resultadoPunto.textContent =
                punto;

            resultadoMedio.textContent =
                "—";

            resultadoFechaHora.textContent =
                "—";

            resultadoEscaneo.hidden =
                false;


            mostrarMensaje(
                error.message,
                "error"
            );

        } finally {

            actualizarEstadoBoton();
        }
    }


    function formatearFechaHora(
        fechaHora
    ) {

        const fecha =
            new Date(
                fechaHora
            );

        return fecha.toLocaleString(
            "es-PE",
            {
                day: "2-digit",
                month: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
                hour12: false
            }
        );
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensajeEscaneo.textContent =
            texto;

        mensajeEscaneo.className =
            `mensaje mensaje-${tipo}`;

        mensajeEscaneo.hidden =
            false;
    }


    function ocultarMensaje() {

        mensajeEscaneo.textContent =
            "";

        mensajeEscaneo.className =
            "mensaje";

        mensajeEscaneo.hidden =
            true;
    }


    codigoPunto.focus();

});