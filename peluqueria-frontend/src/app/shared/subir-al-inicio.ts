/**
 * Lleva la página arriba de todo con un desplazamiento suave.
 * Se usa después de una acción para que la persona vea el aviso de éxito o de error,
 * que siempre se muestra al principio de la página.
 */
export function subirAlInicio(): void {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
