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

    const botonValidarIngreso =
        document.getElementById("botonValidarIngreso");

    const botonLimpiar =
        document.getElementById("botonLimpiar");

    const mensaje =
        document.getElementById("mensaje");

    const modalConfirmarValidacion =
        document.getElementById("modalConfirmarValidacion");

    const nombreUsuarioConfirmacion =
        document.getElementById("nombreUsuarioConfirmacion");

    const botonCerrarModalValidacion =
        document.getElementById("botonCerrarModalValidacion");

    const botonCancelarValidacion =
        document.getElementById("botonCancelarValidacion");

    const botonConfirmarValidacion =
        document.getElementById("botonConfirmarValidacion");

    const contenedorDetalleReserva =
        document.getElementById("contenedorDetalleReserva");

    const tituloAmbienteDetalle =
        document.getElementById("tituloAmbienteDetalle");

    const horaReservaDetalle =
        document.getElementById("horaReservaDetalle");

    const fechaRegistroDetalle =
        document.getElementById("fechaRegistroDetalle");

    const estadoReservaDetalle =
        document.getElementById("estadoReservaDetalle");

    const integrantesReservaBody =
        document.getElementById("integrantesReservaBody");

    const ubicacionReserva =
        document.getElementById("ubicacionReserva");

    const fechaReserva =
        document.getElementById("fechaReserva");

    const botonBuscarGrilla =
        document.getElementById("botonBuscarGrilla");

    const encabezadoGrillaReservas =
        document.getElementById("encabezadoGrillaReservas");

    const cuerpoGrillaReservas =
        document.getElementById("cuerpoGrillaReservas");

    inicializarGrillaReservas();

    let temporizadorIdentificacion;


    campoDni.addEventListener("input", () => {

        const dni =
            campoDni.value.trim();

        if (dni.length > 0) {
            campoCodigo.value = "";
            campoCodigo.disabled = true;
        } else {
            campoCodigo.disabled = false;
        }

        reiniciarIdentificacion();

        clearTimeout(
            temporizadorIdentificacion
        );

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

        const codigo =
            campoCodigo.value.trim();

        if (codigo.length > 0) {
            campoDni.value = "";
            campoDni.disabled = true;
        } else {
            campoDni.disabled = false;
        }

        reiniciarIdentificacion();

        clearTimeout(
            temporizadorIdentificacion
        );

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

        campoDni.disabled = false;
        campoCodigo.disabled = false;

        reiniciarIdentificacion();

        ocultarMensaje();

        campoDni.focus();
    });

    botonValidarIngreso.addEventListener("click", () => {

        if (botonValidarIngreso.disabled) {
            return;
        }

        nombreUsuarioConfirmacion.textContent =
            nombreUsuario.textContent;

        modalConfirmarValidacion.hidden = false;
    });


    botonCerrarModalValidacion.addEventListener(
        "click",
        cerrarModalValidacion
    );


    botonCancelarValidacion.addEventListener(
        "click",
        cerrarModalValidacion
    );


    modalConfirmarValidacion.addEventListener(
        "click",
        (event) => {

            if (event.target === modalConfirmarValidacion) {
                cerrarModalValidacion();
            }
        }
    );

    botonBuscarReserva.addEventListener(
        "click",
        async () => {

            const identificador =
                obtenerIdentificadorActual();

            if (!identificador) {
                return;
            }

            await cargarDetalleReserva(
                identificador.tipo,
                identificador.valor
            );
        }
    );


    document.addEventListener(
        "keydown",
        (event) => {

            if (
                event.key === "Escape"
                && !modalConfirmarValidacion.hidden
            ) {
                cerrarModalValidacion();
            }
        }
    );

    botonBuscarGrilla.addEventListener(
        "click",
        async () => {
            await cargarGrillaCatalogo();
        }
    );


    function cerrarModalValidacion() {

        modalConfirmarValidacion.hidden = true;
    }


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

            botonBuscarReserva.disabled = false;
            botonValidarIngreso.disabled = false;

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

        nombreUsuario.textContent = "—";

        botonBuscarReserva.disabled = true;
        botonValidarIngreso.disabled = true;

        contenedorDetalleReserva.hidden = true;
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

    function obtenerIdentificadorActual() {

        const dni =
            campoDni.value.trim();

        const codigo =
            campoCodigo.value.trim();

        if (dni.length > 0) {

            return {
                tipo: "DNI",
                valor: dni
            };
        }

        if (codigo.length > 0) {

            return {
                tipo: "CODIGO_UNIVERSITARIO",
                valor: codigo
            };
        }

        return null;
    }

    async function cargarDetalleReserva(
        tipo,
        valor
    ) {

        try {

            const parametros =
                new URLSearchParams({
                    tipo,
                    valor
                });

            const respuesta =
                await fetch(
                    "/api/validaciones-ingreso/manual/detalle?"
                    + parametros.toString()
                );

            const datos =
                await respuesta.json();

            if (!respuesta.ok) {

                throw new Error(
                    datos.mensaje
                    || "No fue posible localizar la reserva."
                );
            }

            mostrarDetalleReserva(
                datos
            );

            ocultarMensaje();

        } catch (error) {

            contenedorDetalleReserva.hidden = true;

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }

    function mostrarDetalleReserva(datos) {

        tituloAmbienteDetalle.textContent =
            `${datos.codigoAmbiente} - ${datos.nombreAmbiente}`;

        horaReservaDetalle.textContent =
            `${formatearHora(datos.fechaHoraInicio)} - ${formatearHora(datos.fechaHoraFin)}`;

        fechaRegistroDetalle.textContent =
            formatearFechaHora(
                datos.fechaHoraRegistro
            );

        estadoReservaDetalle.textContent =
            "Reservado";

        integrantesReservaBody.innerHTML =
            "";

        datos.integrantes.forEach(
            (integrante, indice) => {

                const fila =
                    document.createElement("tr");

                if (
                    integrante.idReservaUsuario
                    === datos.idReservaUsuarioBuscado
                ) {

                    fila.classList.add(
                        "integrante-buscado"
                    );
                }

                const estadoClase =
                    integrante.validado
                        ? "estado-validado"
                        : "estado-pendiente";

                const estadoTexto =
                    integrante.validado
                        ? "Validado"
                        : "Pendiente";

                const rolTexto =
                    integrante.rolEnReserva
                    === "RESPONSABLE"
                        ? "Responsable"
                        : "Integrante";

                fila.innerHTML = `
                <td>${indice + 1}</td>
                <td>${integrante.codigoUniversitario}</td>
                <td>${integrante.dni}</td>
                <td>${integrante.nombres} ${integrante.apellidos}</td>
                <td>${rolTexto}</td>
                <td>
                    <span class="estado-ingreso ${estadoClase}">
                        ${estadoTexto}
                    </span>
                </td>
            `;

                integrantesReservaBody.appendChild(
                    fila
                );
            }
        );

        contenedorDetalleReserva.hidden =
            false;

        contenedorDetalleReserva.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    function formatearHora(fechaHora) {

        const fecha =
            new Date(fechaHora);

        return fecha.toLocaleTimeString(
            "es-PE",
            {
                hour: "2-digit",
                minute: "2-digit",
                hour12: false
            }
        );
    }

    function formatearFechaHora(fechaHora) {

        const fecha =
            new Date(fechaHora);

        const dia =
            String(fecha.getDate())
                .padStart(2, "0");

        const mes =
            String(fecha.getMonth() + 1)
                .padStart(2, "0");

        const anio =
            fecha.getFullYear();

        const hora =
            String(fecha.getHours())
                .padStart(2, "0");

        const minutos =
            String(fecha.getMinutes())
                .padStart(2, "0");

        return `${dia}/${mes}/${anio} ${hora}:${minutos}`;
    }

    async function inicializarGrillaReservas() {

        establecerFechaActual();

        await cargarUbicaciones();

        if (ubicacionReserva.value) {
            await cargarGrillaCatalogo();
        }
    }

    function establecerFechaActual() {

        const hoy =
            new Date();

        const anio =
            hoy.getFullYear();

        const mes =
            String(hoy.getMonth() + 1)
                .padStart(2, "0");

        const dia =
            String(hoy.getDate())
                .padStart(2, "0");

        fechaReserva.value =
            `${anio}-${mes}-${dia}`;
    }

    async function cargarUbicaciones() {

        try {

            const respuesta =
                await fetch(
                    "/api/reservas/catalogo/ubicaciones"
                );

            const ubicaciones =
                await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(
                    "No fue posible cargar las ubicaciones."
                );
            }

            ubicacionReserva.innerHTML =
                "";

            ubicaciones.forEach(
                ubicacion => {

                    const opcion =
                        document.createElement("option");

                    opcion.value =
                        ubicacion.idUbicacion;

                    opcion.textContent =
                        ubicacion.nombre;

                    ubicacionReserva.appendChild(
                        opcion
                    );
                }
            );

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }

    async function cargarGrillaCatalogo() {

        const idUbicacion =
            ubicacionReserva.value;

        if (!idUbicacion) {
            return;
        }

        try {

            const respuesta =
                await fetch(
                    `/api/reservas/catalogo/ubicaciones/${idUbicacion}/ambientes`
                );

            const ambientes =
                await respuesta.json();

            if (!respuesta.ok) {

                throw new Error(
                    "No fue posible cargar los ambientes."
                );
            }

            construirGrilla(
                ambientes
            );

            ocultarMensaje();

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }

    function construirGrilla(ambientes) {

        encabezadoGrillaReservas.innerHTML = `
        <th class="encabezado-todos">
            Todos
        </th>
    `;

        ambientes.forEach(
            ambiente => {

                const th =
                    document.createElement("th");

                th.classList.add(
                    "encabezado-ambiente"
                );

                th.dataset.idAmbiente =
                    ambiente.idAmbiente;

                th.innerHTML = `
                <span>
                    ${ambiente.abreviatura}
                </span>

                <div class="tooltip-ambiente">

                    <strong>
                        Ambiente: ${ambiente.nombre}
                        - ${ambiente.piso}
                    </strong>

                    <br><br>

                    <strong>Mín.</strong>
                    ${ambiente.cantidadMinima}
                    personas

                    <br><br>

                    <strong>Máx.</strong>
                    ${ambiente.cantidadMaxima}
                    personas

                </div>
            `;

                encabezadoGrillaReservas.appendChild(
                    th
                );
            }
        );

        cuerpoGrillaReservas.innerHTML =
            "";

        for (let hora = 8; hora <= 20; hora++) {

            const fila =
                document.createElement("tr");

            const celdaHora =
                document.createElement("td");

            celdaHora.textContent =
                `${String(hora).padStart(2, "0")}:00`;

            fila.appendChild(
                celdaHora
            );

            ambientes.forEach(
                ambiente => {

                    const celda =
                        document.createElement("td");

                    celda.classList.add(
                        "celda-grilla-pendiente"
                    );

                    celda.dataset.idAmbiente =
                        ambiente.idAmbiente;

                    celda.dataset.hora =
                        hora;

                    fila.appendChild(
                        celda
                    );
                }
            );

            cuerpoGrillaReservas.appendChild(
                fila
            );
        }
    }

});