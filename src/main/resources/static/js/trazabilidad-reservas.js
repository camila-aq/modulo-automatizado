console.log("trazabilidad-reservas.js cargado");

document.addEventListener("DOMContentLoaded", () => {

    const estudiante =
        document.getElementById(
            "estudiante"
        );

    const codigoUniversitario =
        document.getElementById(
            "codigoUniversitario"
        );

    const dni =
        document.getElementById(
            "dni"
        );

    const fechaDesde =
        document.getElementById(
            "fechaDesde"
        );

    const fechaHasta =
        document.getElementById(
            "fechaHasta"
        );

    const ubicacion =
        document.getElementById(
            "ubicacion"
        );

    const ambiente =
        document.getElementById(
            "ambiente"
        );

    const estadoOcupacion =
        document.getElementById(
            "estadoOcupacion"
        );

    const botonBuscar =
        document.getElementById(
            "botonBuscar"
        );

    const botonLimpiar =
        document.getElementById(
            "botonLimpiar"
        );

    const mensaje =
        document.getElementById(
            "mensaje"
        );

    const cantidadResultados =
        document.getElementById(
            "cantidadResultados"
        );

    const cuerpoReservas =
        document.getElementById(
            "cuerpoReservas"
        );

    const panelHistorial =
        document.getElementById(
            "panelHistorial"
        );

    const cuerpoIntegrantes =
        document.getElementById(
            "cuerpoIntegrantes"
        );

    const detalleCodigoReserva =
        document.getElementById(
            "detalleCodigoReserva"
        );

    const detalleFechaReserva =
        document.getElementById(
            "detalleFechaReserva"
        );

    const detalleHorarioReserva =
        document.getElementById(
            "detalleHorarioReserva"
        );

    const detalleUbicacionReserva =
        document.getElementById(
            "detalleUbicacionReserva"
        );

    const detalleAmbienteReserva =
        document.getElementById(
            "detalleAmbienteReserva"
        );

    const detalleEstadoReserva =
        document.getElementById(
            "detalleEstadoReserva"
        );

    const detalleLimiteTolerancia =
        document.getElementById(
            "detalleLimiteTolerancia"
        );

    const detalleCantidadMinima =
        document.getElementById(
            "detalleCantidadMinima"
        );

    const cuerpoEventos =
        document.getElementById(
            "cuerpoEventos"
        );

    const botonCerrarHistorial =
        document.getElementById(
            "botonCerrarHistorial"
        );


    let reservasEncontradas = [];


    const fechaHoy =
        obtenerFechaLocalHoy();

    fechaDesde.value =
        fechaHoy;

    fechaHasta.value =
        fechaHoy;


    cargarUbicaciones();


    ubicacion.addEventListener(
        "change",
        cargarAmbientes
    );


    botonBuscar.addEventListener(
        "click",
        buscarReservas
    );


    botonLimpiar.addEventListener(
        "click",
        () => {

            estudiante.value = "";
            codigoUniversitario.value = "";
            dni.value = "";

            fechaDesde.value =
                fechaHoy;

            fechaHasta.value =
                fechaHoy;

            ubicacion.value = "";

            reiniciarAmbientes();

            estadoOcupacion.value = "";

            reservasEncontradas = [];

            ocultarHistorial();
            ocultarMensaje();

            mostrarReservas(
                []
            );

            estudiante.focus();
        }
    );


    botonCerrarHistorial.addEventListener(
        "click",
        ocultarHistorial
    );


    async function cargarUbicaciones() {

        try {

            const respuesta =
                await fetch(
                    "/api/reservas/catalogo/ubicaciones"
                );


            if (!respuesta.ok) {

                throw new Error(
                    "No fue posible cargar las ubicaciones."
                );
            }


            const ubicaciones =
                await respuesta.json();


            for (const item of ubicaciones) {

                const opcion =
                    document.createElement(
                        "option"
                    );

                opcion.value =
                    item.idUbicacion;

                opcion.textContent =
                    item.nombre;

                ubicacion.appendChild(
                    opcion
                );
            }

        } catch (error) {

            mostrarError(
                error.message
            );
        }
    }


    async function cargarAmbientes() {

        reiniciarAmbientes();


        if (!ubicacion.value) {
            return;
        }


        try {

            const respuesta =
                await fetch(
                    "/api/reservas/catalogo/ubicaciones/"
                    + ubicacion.value
                    + "/ambientes"
                );


            if (!respuesta.ok) {

                throw new Error(
                    "No fue posible cargar los ambientes."
                );
            }


            const ambientes =
                await respuesta.json();


            for (const item of ambientes) {

                const opcion =
                    document.createElement(
                        "option"
                    );

                opcion.value =
                    item.idAmbiente;

                opcion.textContent =
                    item.abreviatura
                    + " - "
                    + item.nombre;

                ambiente.appendChild(
                    opcion
                );
            }


            ambiente.disabled =
                false;

        } catch (error) {

            mostrarError(
                error.message
            );
        }
    }


    function reiniciarAmbientes() {

        ambiente.innerHTML =
            "";

        const opcion =
            document.createElement(
                "option"
            );

        opcion.value = "";

        opcion.textContent =
            "Todos";

        ambiente.appendChild(
            opcion
        );

        ambiente.disabled =
            !ubicacion.value;
    }


    async function buscarReservas() {

        ocultarMensaje();
        ocultarHistorial();


        if (
            fechaDesde.value
            &&
            fechaHasta.value
            &&
            fechaDesde.value > fechaHasta.value
        ) {

            mostrarError(
                "La fecha inicial no puede ser posterior a la fecha final."
            );

            return;
        }


        const parametros =
            new URLSearchParams();


        agregarParametro(
            parametros,
            "estudiante",
            estudiante.value
        );

        agregarParametro(
            parametros,
            "codigoUniversitario",
            codigoUniversitario.value
        );

        agregarParametro(
            parametros,
            "dni",
            dni.value
        );

        agregarParametro(
            parametros,
            "fechaDesde",
            fechaDesde.value
        );

        agregarParametro(
            parametros,
            "fechaHasta",
            fechaHasta.value
        );

        agregarParametro(
            parametros,
            "idUbicacion",
            ubicacion.value
        );

        agregarParametro(
            parametros,
            "idAmbiente",
            ambiente.value
        );

        agregarParametro(
            parametros,
            "estado",
            estadoOcupacion.value
        );


        botonBuscar.disabled =
            true;


        try {

            const respuesta =
                await fetch(
                    "/api/trazabilidad/reservas?"
                    + parametros.toString()
                );


            if (!respuesta.ok) {

                const mensajeError =
                    await obtenerMensajeError(
                        respuesta
                    );

                throw new Error(
                    mensajeError
                );
            }


            reservasEncontradas =
                await respuesta.json();


            mostrarReservas(
                reservasEncontradas
            );

        } catch (error) {

            reservasEncontradas = [];

            mostrarReservas(
                []
            );

            mostrarError(
                error.message
            );

        } finally {

            botonBuscar.disabled =
                false;
        }
    }


    function agregarParametro(
        parametros,
        nombre,
        valor) {

        if (
            valor
            &&
            valor.trim()
        ) {

            parametros.set(
                nombre,
                valor.trim()
            );
        }
    }


    function mostrarReservas(
        reservas) {

        cuerpoReservas.innerHTML =
            "";


        cantidadResultados.textContent =
            reservas.length === 1
                ? "1 resultado"
                : reservas.length
                + " resultados";


        if (reservas.length === 0) {

            const fila =
                document.createElement(
                    "tr"
                );

            fila.className =
                "fila-sin-resultados";


            const celda =
                document.createElement(
                    "td"
                );

            celda.colSpan = 7;

            celda.textContent =
                "No se encontraron reservas con los criterios indicados.";


            fila.appendChild(
                celda
            );

            cuerpoReservas.appendChild(
                fila
            );

            return;
        }


        for (const reserva of reservas) {

            const fila =
                document.createElement(
                    "tr"
                );


            agregarCelda(
                fila,
                reserva.codigoReserva
            );

            agregarCelda(
                fila,
                formatearFecha(
                    reserva.fechaHoraInicio
                )
            );

            agregarCelda(
                fila,
                formatearHorario(
                    reserva.fechaHoraInicio,
                    reserva.fechaHoraFin
                )
            );

            agregarCelda(
                fila,
                reserva.ubicacion
            );

            agregarCelda(
                fila,
                reserva.ambiente
                + " - "
                + reserva.piso
            );


            const celdaEstado =
                document.createElement(
                    "td"
                );

            celdaEstado.appendChild(
                crearEstado(
                    reserva.estadoActual
                )
            );

            fila.appendChild(
                celdaEstado
            );


            const celdaAccion =
                document.createElement(
                    "td"
                );

            const boton =
                document.createElement(
                    "button"
                );

            boton.type =
                "button";

            boton.className =
                "boton-ver-historial";

            boton.textContent =
                "Ver historial";

            boton.addEventListener(
                "click",
                () =>
                    cargarHistorial(
                        reserva
                    )
            );


            celdaAccion.appendChild(
                boton
            );

            fila.appendChild(
                celdaAccion
            );


            cuerpoReservas.appendChild(
                fila
            );
        }
    }


    async function cargarHistorial(
        reserva) {

        ocultarMensaje();


        try {

            const respuesta =
                await fetch(
                    "/api/trazabilidad/reservas/"
                    + reserva.idReserva
                );


            if (!respuesta.ok) {

                const mensajeError =
                    await obtenerMensajeError(
                        respuesta
                    );

                throw new Error(
                    mensajeError
                );
            }


            const trazabilidad =
                await respuesta.json();


            mostrarHistorial(
                reserva,
                trazabilidad
            );

        } catch (error) {

            mostrarError(
                error.message
            );
        }
    }


    function mostrarHistorial(
        reserva,
        trazabilidad) {

        detalleCodigoReserva.textContent =
            reserva.codigoReserva;

        detalleFechaReserva.textContent =
            formatearFecha(
                reserva.fechaHoraInicio
            );

        detalleHorarioReserva.textContent =
            formatearHorario(
                reserva.fechaHoraInicio,
                reserva.fechaHoraFin
            );

        detalleUbicacionReserva.textContent =
            reserva.ubicacion;

        detalleAmbienteReserva.textContent =
            reserva.ambiente
            + " - "
            + reserva.piso;


        detalleEstadoReserva.innerHTML =
            "";

        detalleEstadoReserva.appendChild(
            crearEstado(
                trazabilidad.estadoActual
            )
        );


        detalleLimiteTolerancia.textContent =
            trazabilidad.fechaHoraLimiteTolerancia
                ? formatearFechaHora(
                    trazabilidad.fechaHoraLimiteTolerancia
                )
                : "—";

        detalleCantidadMinima.textContent =
            trazabilidad.cantidadMinimaRequerida != null
                ? trazabilidad.cantidadMinimaRequerida
                : "—";

        mostrarIntegrantes(
            trazabilidad.integrantes
        );

        mostrarEventos(
            trazabilidad.eventos
        );


        panelHistorial.hidden =
            false;


        panelHistorial.scrollIntoView(
            {
                behavior: "smooth",
                block: "start"
            }
        );
    }


    function mostrarEventos(
        eventos) {

        cuerpoEventos.innerHTML =
            "";


        if (
            !eventos
            ||
            eventos.length === 0
        ) {

            const fila =
                document.createElement(
                    "tr"
                );

            fila.className =
                "fila-sin-resultados";


            const celda =
                document.createElement(
                    "td"
                );

            celda.colSpan =
                4;

            celda.textContent =
                "No existen eventos registrados para esta reserva.";


            fila.appendChild(
                celda
            );

            cuerpoEventos.appendChild(
                fila
            );

            return;
        }


        for (const evento of eventos) {

            const fila =
                document.createElement(
                    "tr"
                );


            agregarCelda(
                fila,
                formatearFecha(
                    evento.fechaHora
                )
            );

            agregarCelda(
                fila,
                formatearHora(
                    evento.fechaHora
                )
            );


            const celdaTipo =
                document.createElement(
                    "td"
                );

            celdaTipo.className =
                "tipo-evento";

            celdaTipo.textContent =
                obtenerNombreEvento(
                    evento.tipoEvento
                );

            fila.appendChild(
                celdaTipo
            );


            agregarCelda(
                fila,
                evento.detalle
            );


            cuerpoEventos.appendChild(
                fila
            );
        }
    }


    function crearEstado(
        estado) {

        const elemento =
            document.createElement(
                "span"
            );

        elemento.className =
            "estado";


        if (estado === "PENDIENTE") {

            elemento.classList.add(
                "estado-pendiente"
            );

        } else if (estado === "OCUPADO") {

            elemento.classList.add(
                "estado-ocupado"
            );

        } else if (estado === "LIBERADO") {

            elemento.classList.add(
                "estado-liberado"
            );
        }


        elemento.textContent =
            estado || "—";


        return elemento;
    }


    function agregarCelda(
        fila,
        contenido) {

        const celda =
            document.createElement(
                "td"
            );

        celda.textContent =
            contenido || "—";

        fila.appendChild(
            celda
        );
    }


    function ocultarHistorial() {

        panelHistorial.hidden =
            true;

        cuerpoEventos.innerHTML =
            "";

        cuerpoIntegrantes.innerHTML =
            "";
    }


    function obtenerNombreEvento(
        tipoEvento) {

        if (
            tipoEvento
            === "VALIDACION_INGRESO"
        ) {

            return "Validación de ingreso";
        }


        if (
            tipoEvento
            === "ESTADO_OCUPACION"
        ) {

            return "Estado de ocupación";
        }


        if (
            tipoEvento
            === "CORREO_ELECTRONICO"
        ) {

            return "Correo electrónico";
        }


        return tipoEvento;
    }


    function obtenerFechaLocalHoy() {

        const hoy =
            new Date();

        const anio =
            hoy.getFullYear();

        const mes =
            String(
                hoy.getMonth() + 1
            ).padStart(
                2,
                "0"
            );

        const dia =
            String(
                hoy.getDate()
            ).padStart(
                2,
                "0"
            );


        return anio
            + "-"
            + mes
            + "-"
            + dia;
    }


    function formatearFecha(
        fechaHora) {

        if (!fechaHora) {
            return "—";
        }


        const fecha =
            new Date(
                fechaHora
            );


        return fecha.toLocaleDateString(
            "es-PE",
            {
                day: "2-digit",
                month: "2-digit",
                year: "numeric"
            }
        );
    }


    function formatearHora(
        fechaHora) {

        if (!fechaHora) {
            return "—";
        }


        const fecha =
            new Date(
                fechaHora
            );


        return fecha.toLocaleTimeString(
            "es-PE",
            {
                hour: "2-digit",
                minute: "2-digit",
                hour12: false
            }
        );
    }


    function formatearHorario(
        inicio,
        fin) {

        return formatearHora(inicio)
            + " - "
            + formatearHora(fin);
    }


    function formatearFechaHora(
        fechaHora) {

        return formatearFecha(fechaHora)
            + " "
            + formatearHora(fechaHora);
    }


    async function obtenerMensajeError(
        respuesta) {

        try {

            const error =
                await respuesta.json();


            if (error.message) {
                return error.message;
            }

        } catch (error) {

            /*
             * La respuesta no contenía
             * un cuerpo JSON utilizable.
             */
        }


        return "No fue posible completar la operación.";
    }


    function mostrarError(
        texto) {

        mensaje.textContent =
            texto;

        mensaje.className =
            "mensaje mensaje-error";

        mensaje.hidden =
            false;
    }


    function ocultarMensaje() {

        mensaje.hidden =
            true;

        mensaje.textContent =
            "";

        mensaje.className =
            "mensaje";
    }

    function mostrarIntegrantes(
        integrantes) {

        cuerpoIntegrantes.innerHTML =
            "";


        for (const integrante of integrantes) {

            const fila =
                document.createElement(
                    "tr"
                );


            agregarCelda(
                fila,
                integrante.codigoUniversitario
            );


            agregarCelda(
                fila,
                integrante.dni
            );


            agregarCelda(
                fila,
                integrante.nombres
                + " "
                + integrante.apellidos
            );


            agregarCelda(
                fila,
                integrante.rolEnReserva
            );


            agregarCelda(
                fila,
                integrante.validado
                    ? "Validado"
                    : "No validado"
            );


            cuerpoIntegrantes.appendChild(
                fila
            );
        }
    }
});