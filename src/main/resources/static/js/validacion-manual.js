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

    const vistaDetalleAmbienteGeneral =
        document.getElementById(
            "vistaDetalleAmbienteGeneral"
        );

    const celdaDetalleAmbienteGeneral =
        document.getElementById(
            "celdaDetalleAmbienteGeneral"
        );

    const tituloAmbienteGeneral =
        document.getElementById(
            "tituloAmbienteGeneral"
        );

    const cuerpoDetalleAmbienteGeneral =
        document.getElementById(
            "cuerpoDetalleAmbienteGeneral"
        );

    const plantillaIntegrantesReservaGrilla =
        document.getElementById(
            "plantillaIntegrantesReservaGrilla"
        );


    let ambientesCargados = [];
    let reservasCargadas = [];
    let temporizadorIdentificacion;


    inicializarGrillaReservas();


    campoDni.addEventListener(
        "input",
        () => {

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
        }
    );


    campoCodigo.addEventListener(
        "input",
        () => {

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
        }
    );


    botonLimpiar.addEventListener(
        "click",
        () => {

            campoDni.value = "";
            campoCodigo.value = "";

            campoDni.disabled = false;
            campoCodigo.disabled = false;

            reiniciarIdentificacion();

            ocultarMensaje();

            campoDni.focus();
        }
    );


    botonValidarIngreso.addEventListener(
        "click",
        () => {

            if (botonValidarIngreso.disabled) {
                return;
            }

            nombreUsuarioConfirmacion.textContent =
                nombreUsuario.textContent;

            modalConfirmarValidacion.hidden =
                false;
        }
    );


    botonCerrarModalValidacion.addEventListener(
        "click",
        cerrarModalValidacion
    );


    botonCancelarValidacion.addEventListener(
        "click",
        cerrarModalValidacion
    );


    botonConfirmarValidacion.addEventListener(
        "click",
        confirmarValidacionIngreso
    );


    modalConfirmarValidacion.addEventListener(
        "click",
        (event) => {

            if (
                event.target
                === modalConfirmarValidacion
            ) {

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
                &&
                !modalConfirmarValidacion.hidden
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
    ubicacionReserva.addEventListener(
        "change",
        async () => {

            await cargarGrillaCatalogo();
        }
    );


    function cerrarModalValidacion() {

        modalConfirmarValidacion.hidden =
            true;
    }


    async function confirmarValidacionIngreso() {

        const identificador =
            obtenerIdentificadorActual();

        if (!identificador) {

            cerrarModalValidacion();
            return;
        }

        botonConfirmarValidacion.disabled =
            true;

        try {

            const parametros =
                new URLSearchParams({
                    tipo: identificador.tipo,
                    valor: identificador.valor
                });

            const respuestaDetalle =
                await fetch(
                    "/api/validaciones-ingreso/manual/detalle?"
                    + parametros.toString()
                );

            const detalle =
                await respuestaDetalle.json();

            if (!respuestaDetalle.ok) {

                throw new Error(
                    detalle.mensaje
                    || "No fue posible localizar la reserva."
                );
            }


            const respuestaValidacion =
                await fetch(
                    "/api/validaciones-ingreso/manual/confirmar",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            idReservaUsuario:
                            detalle.idReservaUsuarioBuscado,

                            tipoIdentificador:
                            identificador.tipo
                        })
                    }
                );

            const validacion =
                await respuestaValidacion.json();

            if (!respuestaValidacion.ok) {

                throw new Error(
                    validacion.mensaje
                    || "No fue posible validar el ingreso."
                );
            }


            const respuestaActualizada =
                await fetch(
                    "/api/validaciones-ingreso/manual/detalle?"
                    + parametros.toString()
                );

            const detalleActualizado =
                await respuestaActualizada.json();

            if (!respuestaActualizada.ok) {

                throw new Error(
                    detalleActualizado.mensaje
                    || "El ingreso fue validado, pero no fue posible actualizar la vista."
                );
            }


            cerrarModalValidacion();

            await mostrarReservaEncontrada(
                detalleActualizado
            );

            mostrarMensaje(
                "Ingreso validado correctamente.",
                "exito"
            );

        } catch (error) {

            cerrarModalValidacion();

            mostrarMensaje(
                error.message,
                "error"
            );

        } finally {

            botonConfirmarValidacion.disabled =
                false;
        }
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

            botonBuscarReserva.disabled =
                false;

            botonValidarIngreso.disabled =
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

        nombreUsuario.textContent =
            "—";

        botonBuscarReserva.disabled =
            true;

        botonValidarIngreso.disabled =
            true;
    }


    function mostrarMensaje(
        texto,
        tipo
    ) {

        mensaje.textContent =
            texto;

        mensaje.className =
            `mensaje mensaje-${tipo}`;

        mensaje.hidden =
            false;
    }


    function ocultarMensaje() {

        mensaje.textContent =
            "";

        mensaje.className =
            "mensaje";

        mensaje.hidden =
            true;
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

            await mostrarReservaEncontrada(
                datos
            );

            ocultarMensaje();

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }


    async function mostrarReservaEncontrada(
        datos
    ) {

        ubicacionReserva.value =
            String(
                datos.idUbicacion
            );

        fechaReserva.value =
            datos.fechaHoraInicio.substring(
                0,
                10
            );


        await cargarGrillaCatalogo();


        const ambiente =
            ambientesCargados.find(
                ambiente =>
                    ambiente.idAmbiente
                    === datos.idAmbiente
            );

        if (!ambiente) {

            throw new Error(
                "No fue posible localizar el ambiente de la reserva."
            );
        }


        mostrarDetalleAmbienteGeneral(
            ambiente,
            ambientesCargados
        );


        const filaReserva =
            cuerpoDetalleAmbienteGeneral
                .querySelector(
                    `tr[data-id-reserva="${datos.idReserva}"]`
                );

        if (!filaReserva) {

            throw new Error(
                "No fue posible localizar la reserva en el ambiente."
            );
        }


        await mostrarIntegrantesReservaGrilla(
            datos.idReserva,
            filaReserva,
            datos.idReservaUsuarioBuscado
        );
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
            String(
                fecha.getDate()
            ).padStart(
                2,
                "0"
            );

        const mes =
            String(
                fecha.getMonth() + 1
            ).padStart(
                2,
                "0"
            );

        const anio =
            fecha.getFullYear();

        const hora =
            String(
                fecha.getHours()
            ).padStart(
                2,
                "0"
            );

        const minutos =
            String(
                fecha.getMinutes()
            ).padStart(
                2,
                "0"
            );

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
                        document.createElement(
                            "option"
                        );

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

        const fecha =
            fechaReserva.value;

        if (!idUbicacion || !fecha) {
            return;
        }

        try {

            const [
                respuestaAmbientes,
                respuestaReservas
            ] = await Promise.all([

                fetch(
                    `/api/reservas/catalogo/ubicaciones/${idUbicacion}/ambientes`
                ),

                fetch(
                    `/api/reservas?idUbicacion=${idUbicacion}&fecha=${fecha}`
                )
            ]);


            if (!respuestaAmbientes.ok) {

                throw new Error(
                    "No fue posible cargar los ambientes."
                );
            }

            if (!respuestaReservas.ok) {

                throw new Error(
                    "No fue posible cargar las reservas."
                );
            }


            const ambientes =
                await respuestaAmbientes.json();

            const reservas =
                await respuestaReservas.json();


            ambientesCargados =
                ambientes;

            reservasCargadas =
                reservas;


            construirGrilla(
                ambientes,
                reservas
            );

            ocultarMensaje();

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }


    function construirGrilla(
        ambientes,
        reservas
    ) {

        encabezadoGrillaReservas.innerHTML = `
            <th class="encabezado-todos">
                Todos
            </th>
        `;

        encabezadoGrillaReservas
            .querySelector(
                ".encabezado-todos"
            )
            .addEventListener(
                "click",
                mostrarGrillaGeneral
            );


        ambientes.forEach(
            ambiente => {

                const th =
                    document.createElement(
                        "th"
                    );

                th.classList.add(
                    "encabezado-ambiente"
                );

                th.dataset.idAmbiente =
                    ambiente.idAmbiente;

                th.addEventListener(
                    "click",
                    () => {

                        mostrarDetalleAmbienteGeneral(
                            ambiente,
                            ambientes
                        );
                    }
                );

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


        for (
            let hora = 8;
            hora <= 20;
            hora++
        ) {

            const fila =
                document.createElement(
                    "tr"
                );

            const celdaHora =
                document.createElement(
                    "td"
                );

            celdaHora.textContent =
                `${String(hora).padStart(2, "0")}:00`;

            fila.appendChild(
                celdaHora
            );


            ambientes.forEach(
                ambiente => {

                    const celda =
                        document.createElement(
                            "td"
                        );

                    celda.classList.add(
                        "estado-libre"
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


        pintarReservasEnGrilla(
            reservas
        );

        vistaDetalleAmbienteGeneral.hidden =
            true;

        cuerpoGrillaReservas.hidden =
            false;
    }


    function pintarReservasEnGrilla(
        reservas
    ) {

        reservas.forEach(
            reserva => {

                const celdasAmbiente =
                    cuerpoGrillaReservas
                        .querySelectorAll(
                            `td[data-id-ambiente="${reserva.idAmbiente}"]`
                        );

                celdasAmbiente.forEach(
                    celda => {

                        const hora =
                            Number(
                                celda.dataset.hora
                            );

                        if (
                            !reservaOcupaHora(
                                reserva,
                                hora
                            )
                        ) {
                            return;
                        }


                        const ocupada =
                            reserva.periodosOcupados
                                .some(
                                    periodo =>
                                        periodoOcupaHora(
                                            periodo,
                                            hora
                                        )
                                );


                        celda.classList.remove(
                            "estado-libre",
                            "estado-reservado",
                            "estado-ocupado"
                        );


                        if (ocupada) {

                            celda.classList.add(
                                "estado-ocupado"
                            );

                        } else {

                            celda.classList.add(
                                "estado-reservado"
                            );
                        }


                        celda.dataset.idReserva =
                            reserva.idReserva;

                        celda.dataset.codigoReserva =
                            reserva.codigoReserva;

                        celda.title =
                            reserva.codigoReserva;
                    }
                );
            }
        );
    }


    function reservaOcupaHora(
        reserva,
        hora
    ) {

        const fecha =
            fechaReserva.value;

        const horaTexto =
            String(
                hora
            ).padStart(
                2,
                "0"
            );

        const inicioCelda =
            new Date(
                `${fecha}T${horaTexto}:00:00`
            );

        const finCelda =
            new Date(
                inicioCelda.getTime()
                + 60 * 60 * 1000
            );

        const inicioReserva =
            new Date(
                reserva.fechaHoraInicio
            );

        const finReserva =
            new Date(
                reserva.fechaHoraFin
            );

        return (
            inicioReserva < finCelda
            &&
            finReserva > inicioCelda
        );
    }

    function periodoOcupaHora(
        periodo,
        hora
    ) {

        const fecha =
            fechaReserva.value;

        const horaTexto =
            String(
                hora
            ).padStart(
                2,
                "0"
            );

        const inicioCelda =
            new Date(
                `${fecha}T${horaTexto}:00:00`
            );

        const finCelda =
            new Date(
                inicioCelda.getTime()
                + 60 * 60 * 1000
            );

        const inicioPeriodo =
            new Date(
                periodo.fechaHoraInicio
            );

        const finPeriodo =
            new Date(
                periodo.fechaHoraFin
            );


        return (
            inicioPeriodo < finCelda
            &&
            finPeriodo > inicioCelda
        );
    }


    function mostrarDetalleAmbienteGeneral(
        ambienteSeleccionado,
        ambientes
    ) {

        cuerpoGrillaReservas.hidden =
            true;

        vistaDetalleAmbienteGeneral.hidden =
            false;


        celdaDetalleAmbienteGeneral.colSpan =
            ambientes.length + 1;


        tituloAmbienteGeneral.textContent =
            `${ambienteSeleccionado.codigo} - `
            + `${ambienteSeleccionado.nombre} - `
            + `${ambienteSeleccionado.piso}`;


        marcarAmbienteSeleccionado(
            ambienteSeleccionado.idAmbiente
        );


        construirHorasAmbienteGeneral(
            ambienteSeleccionado
        );
    }


    function construirHorasAmbienteGeneral(
        ambienteSeleccionado
    ) {

        cuerpoDetalleAmbienteGeneral.innerHTML =
            "";


        for (
            let hora = 8;
            hora <= 20;
            hora++
        ) {

            const fila =
                document.createElement(
                    "tr"
                );

            const horaInicio =
                `${String(hora).padStart(2, "0")}:00`;

            const horaFin =
                `${String(hora + 1).padStart(2, "0")}:00`;


            const reserva =
                reservasCargadas.find(
                    reserva =>

                        reserva.idAmbiente
                        === ambienteSeleccionado.idAmbiente

                        &&

                        reservaOcupaHora(
                            reserva,
                            hora
                        )
                );


            if (reserva) {

                fila.classList.add(
                    "fila-reserva-clickeable"
                );

                fila.dataset.idReserva =
                    reserva.idReserva;

                fila.dataset.hora =
                    hora;


                const periodoOcupado =
                    reserva.periodosOcupados
                        .some(
                            periodo =>
                                periodoOcupaHora(
                                    periodo,
                                    hora
                                )
                        );


                const claseEstado =
                    periodoOcupado
                        ? "estado-ocupado-detalle"
                        : "estado-reservado-detalle";

                const textoEstado =
                    periodoOcupado
                        ? "Ocupado"
                        : "Reservado";


                fila.innerHTML = `
                <td class="celda-hora-detalle">
                    ${horaInicio} - ${horaFin}
                </td>

                <td class="estado-detalle ${claseEstado}">
                    ${textoEstado}
                </td>

                <td>
                    ${formatearFechaHora(
                    reserva.fechaHoraRegistro
                )}
                </td>

                <td>
                    <button
                        class="accion-tabla boton-ocupar-reserva"
                        type="button"
                        title="Ocupar horario"
                        ${periodoOcupado ? "disabled" : ""}>
                        ✓
                    </button>
                </td>

                <td>
                    <button
                        class="accion-tabla"
                        type="button"
                        title="Cancelar reserva">
                        ✕
                    </button>
                </td>

                <td>
                    <button
                        class="accion-tabla"
                        type="button"
                        title="Liberar reserva">
                        🗑
                    </button>
                </td>

                <td>—</td>

                <td>—</td>

                <td>—</td>

                <td>
                    ${reserva.responsable}
                </td>
            `;


                const botonOcupar =
                    fila.querySelector(
                        ".boton-ocupar-reserva"
                    );


                if (
                    botonOcupar
                    &&
                    !periodoOcupado
                ) {

                    botonOcupar.addEventListener(
                        "click",
                        async evento => {

                            evento.stopPropagation();

                            await ocuparReservaManual(
                                reserva.idReserva,
                                ambienteSeleccionado,
                                hora
                            );
                        }
                    );
                }


                fila.addEventListener(
                    "click",
                    evento => {

                        if (
                            evento.target.closest(
                                "button"
                            )
                        ) {
                            return;
                        }

                        mostrarIntegrantesReservaGrilla(
                            reserva.idReserva,
                            fila
                        );
                    }
                );

            } else {

                fila.innerHTML = `
                <td class="celda-hora-detalle">
                    ${horaInicio} - ${horaFin}
                </td>

                <td class="estado-detalle estado-libre-detalle">
                    Libre
                </td>

                <td>—</td>
                <td>—</td>
                <td>—</td>
                <td>—</td>
                <td>—</td>
                <td>—</td>
                <td>—</td>
                <td>—</td>
            `;
            }


            cuerpoDetalleAmbienteGeneral.appendChild(
                fila
            );
        }
    }


    function mostrarGrillaGeneral() {

        vistaDetalleAmbienteGeneral.hidden =
            true;

        cuerpoGrillaReservas.hidden =
            false;

        limpiarAmbienteSeleccionado();
    }


    function marcarAmbienteSeleccionado(
        idAmbiente
    ) {

        const encabezados =
            encabezadoGrillaReservas
                .querySelectorAll(
                    ".encabezado-ambiente"
                );

        encabezados.forEach(
            encabezado => {

                encabezado.classList.toggle(
                    "encabezado-ambiente-activo",
                    Number(
                        encabezado.dataset.idAmbiente
                    ) === idAmbiente
                );
            }
        );
    }


    function limpiarAmbienteSeleccionado() {

        encabezadoGrillaReservas
            .querySelectorAll(
                ".encabezado-ambiente-activo"
            )
            .forEach(
                encabezado => {

                    encabezado.classList.remove(
                        "encabezado-ambiente-activo"
                    );
                }
            );
    }


    async function mostrarIntegrantesReservaGrilla(
        idReserva,
        filaReserva,
        idReservaUsuarioBuscado = null
    ) {

        const detalleActual =
            filaReserva.nextElementSibling;

        if (
            filaReserva.classList.contains(
                "reserva-resaltada"
            )
            &&
            detalleActual
            &&
            detalleActual.classList.contains(
                "fila-informacion-reserva-grilla"
            )
        ) {

            detalleActual.remove();

            filaReserva.classList.remove(
                "reserva-resaltada"
            );

            ocultarMensaje();

            return;
        }


        try {

            const respuesta =
                await fetch(
                    `/api/reservas/${idReserva}`
                );

            const datos =
                await respuesta.json();

            if (!respuesta.ok) {

                throw new Error(
                    datos.message
                    || "No fue posible cargar la reserva."
                );
            }


            cuerpoDetalleAmbienteGeneral
                .querySelectorAll(
                    ".fila-informacion-reserva-grilla"
                )
                .forEach(
                    fila => {

                        fila.remove();
                    }
                );


            cuerpoDetalleAmbienteGeneral
                .querySelectorAll(
                    ".reserva-resaltada"
                )
                .forEach(
                    fila => {

                        fila.classList.remove(
                            "reserva-resaltada"
                        );
                    }
                );


            filaReserva.classList.add(
                "reserva-resaltada"
            );


            const fragmento =
                plantillaIntegrantesReservaGrilla
                    .content
                    .cloneNode(
                        true
                    );


            const filaInformacion =
                fragmento.querySelector(
                    ".fila-informacion-reserva"
                );

            filaInformacion.classList.add(
                "fila-informacion-reserva-grilla"
            );


            const cuerpoIntegrantes =
                fragmento.querySelector(
                    "#integrantesReservaBody"
                );


            cuerpoIntegrantes.id =
                "integrantesReservaGrillaBody";


            datos.integrantes.forEach(
                (integrante, indice) => {

                    const fila =
                        document.createElement(
                            "tr"
                        );


                    if (
                        idReservaUsuarioBuscado !== null
                        &&
                        integrante.idReservaUsuario
                        === idReservaUsuarioBuscado
                    ) {

                        fila.classList.add(
                            "integrante-buscado"
                        );
                    }


                    const rolTexto =
                        integrante.rolEnReserva
                        === "RESPONSABLE"
                            ? "Responsable"
                            : "Integrante";


                    const estadoClase =
                        integrante.validado
                            ? "estado-validado"
                            : "estado-pendiente";


                    const estadoTexto =
                        integrante.validado
                            ? "Validado"
                            : "Pendiente";


                    fila.innerHTML = `
                        <td>
                            ${indice + 1}
                        </td>

                        <td>
                            ${integrante.codigoUniversitario}
                        </td>

                        <td>
                            ${integrante.dni}
                        </td>

                        <td>
                            ${integrante.nombres}
                            ${integrante.apellidos}
                        </td>

                        <td>
                            ${rolTexto}
                        </td>

                        <td>
                            <span
                                class="estado-ingreso ${estadoClase}">
                                ${estadoTexto}
                            </span>
                        </td>
                    `;


                    cuerpoIntegrantes.appendChild(
                        fila
                    );
                }
            );


            filaReserva.after(
                fragmento
            );

            ocultarMensaje();

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }


    async function ocuparReservaManual(
        idReserva,
        ambienteSeleccionado,
        hora
    ) {

        const horaInicio =
            `${String(hora).padStart(2, "0")}:00`;

        const horaFin =
            `${String(hora + 1).padStart(2, "0")}:00`;


        const confirmado =
            window.confirm(
                `¿Está seguro de ocupar el horario `
                + `${horaInicio} - ${horaFin}? `
                + `Todos los integrantes de la reserva `
                + `serán registrados como validados.`
            );


        if (!confirmado) {
            return;
        }


        const fechaHoraInicioPeriodo =
            `${fechaReserva.value}T`
            + `${String(hora).padStart(2, "0")}`
            + ":00:00";


        try {

            const respuesta =
                await fetch(
                    `/api/reservas/${idReserva}/ocupar`,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({
                            fechaHoraInicioPeriodo:
                            fechaHoraInicioPeriodo
                        })
                    }
                );


            const datos =
                await respuesta.json();


            if (!respuesta.ok) {

                throw new Error(
                    datos.mensaje
                    || datos.message
                    || "No fue posible ocupar el horario."
                );
            }


            await cargarGrillaCatalogo();


            const ambienteActual =
                ambientesCargados.find(
                    ambiente =>
                        ambiente.idAmbiente
                        === ambienteSeleccionado.idAmbiente
                );


            if (ambienteActual) {

                mostrarDetalleAmbienteGeneral(
                    ambienteActual,
                    ambientesCargados
                );


                const filaActual =
                    cuerpoDetalleAmbienteGeneral
                        .querySelector(
                            `tr[data-id-reserva="${idReserva}"]`
                            + `[data-hora="${hora}"]`
                        );


                if (filaActual) {

                    await mostrarIntegrantesReservaGrilla(
                        idReserva,
                        filaActual
                    );
                }
            }


            mostrarMensaje(
                `Horario ${horaInicio} - ${horaFin} `
                + `ocupado correctamente. `
                + `Todos los integrantes fueron `
                + `registrados como validados.`,
                "exito"
            );

        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );
        }
    }

});