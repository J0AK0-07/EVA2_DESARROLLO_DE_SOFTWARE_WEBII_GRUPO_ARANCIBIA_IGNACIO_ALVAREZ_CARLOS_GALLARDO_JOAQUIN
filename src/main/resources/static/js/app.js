/* Comportamientos progresivos: los formularios también se validan siempre en el servidor. */
(() => {
  const toggle = document.querySelector(".menu-toggle");
  const shade = document.querySelector(".nav-shade");
  const closeMenu = () => {
    document.body.classList.remove("menu-open");
    toggle?.setAttribute("aria-expanded", "false");
    if (shade) shade.hidden = true;
  };
  toggle?.addEventListener("click", () => {
    const open = !document.body.classList.contains("menu-open");
    document.body.classList.toggle("menu-open", open);
    toggle.setAttribute("aria-expanded", String(open));
    shade.hidden = !open;
  });
  shade?.addEventListener("click", closeMenu);
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeMenu();
  });
  document.querySelectorAll("form[data-confirm]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });

  const form = document.querySelector("[data-reserva-form]");
  if (!form) return;
  const mesa = form.querySelector("#mesaId");
  const status = form.querySelector("[data-disponibilidad-status]");
  let pending;
  let timer;
  async function actualizarMesas() {
    pending?.abort();
    const controller = new AbortController();
    pending = controller;
    const fecha = form.querySelector("#fecha");
    const hora = form.querySelector("#hora");
    const personas = form.querySelector("#cantidadPersonas");
    const duracion = form.querySelector("#duracionMinutos");
    const anterior = mesa.value;
    const boton = form.querySelector('button[type="submit"]');
    mesa.replaceChildren(new Option("Selecciona una mesa", ""));
    if (![fecha, hora, personas, duracion].every((input) => input.checkValidity())) {
      status.textContent = "Completa un turno válido para consultar mesas.";
      boton.disabled = false;
      return;
    }
    status.textContent = "Consultando mesas disponibles…";
    mesa.disabled = true;
    boton.disabled = true;
    const params = new URLSearchParams({
      fecha: fecha.value,
      hora: hora.value,
      personas: personas.value,
      duracion: duracion.value,
    });
    if (form.dataset.excluir) params.set("excluirReservaId", form.dataset.excluir);
    try {
      const response = await fetch(`/api/mesas/disponibles?${params}`, {
        signal: controller.signal,
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "No se pudo consultar la disponibilidad.");
      data.mesasDisponibles.forEach((item) =>
        mesa.add(
          new Option(
            `Mesa ${item.numero} · ${item.ubicacion} · ${item.capacidad} personas`,
            item.id,
          ),
        ),
      );
      if (data.mesasDisponibles.some((item) => String(item.id) === anterior)) mesa.value = anterior;
      status.textContent = data.mesasDisponibles.length
        ? `${data.mesasDisponibles.length} mesas disponibles para este turno.`
        : "No hay mesas disponibles. Prueba otro turno.";
    } catch (error) {
      if (error.name !== "AbortError") status.textContent = error.message;
    } finally {
      if (pending === controller) {
        mesa.disabled = false;
        boton.disabled = false;
      }
    }
  }
  ["fecha", "hora", "cantidadPersonas", "duracionMinutos"].forEach((id) => {
    form.querySelector(`#${id}`).addEventListener("input", () => {
      clearTimeout(timer);
      timer = setTimeout(actualizarMesas, 250);
    });
  });
})();
