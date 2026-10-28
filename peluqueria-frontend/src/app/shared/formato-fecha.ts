/**
 * Utilidades de formato pensadas para que la información sea fácil de leer:
 * fechas en texto claro ("jueves, 22 de octubre de 2026") y precios en
 * pesos argentinos con separador de miles ("$5.000").
 */

/** Convierte "yyyy-MM-dd" a un Date local (sin desfase de zona horaria). */
export function aFechaLocal(fecha: string): Date | null {
  const coincidencia = /^(\d{4})-(\d{2})-(\d{2})$/.exec(fecha ?? '');
  if (!coincidencia) {
    return null;
  }

  const [, anio, mes, dia] = coincidencia;
  const fechaParseada = new Date(Number(anio), Number(mes) - 1, Number(dia));
  return fechaParseada.getFullYear() === Number(anio) ? fechaParseada : null;
}

/** "jueves, 22 de octubre de 2026" */
export function formatearFechaLarga(fecha: string): string {
  const fechaParseada = aFechaLocal(fecha);
  if (!fechaParseada) {
    return fecha;
  }

  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  }).format(fechaParseada);
}

/** "jue 22 oct" (para listados compactos) */
export function formatearFechaCorta(fecha: string): string {
  const fechaParseada = aFechaLocal(fecha);
  if (!fechaParseada) {
    return fecha;
  }

  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  }).format(fechaParseada);
}

/** "$5.000" (precio en pesos argentinos, sin decimales) */
export function formatearPrecio(precio: number | null | undefined): string {
  const valor = Number(precio ?? 0);
  return `$${valor.toLocaleString('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })}`;
}

/** "30 minutos" / "1 hora 30 minutos" */
export function formatearDuracion(minutos: number | null | undefined): string {
  const total = Math.max(0, Math.round(Number(minutos ?? 0)));
  if (total === 0) {
    return 'Sin duración';
  }

  const horas = Math.floor(total / 60);
  const resto = total % 60;

  if (horas === 0) {
    return `${resto} minutos`;
  }

  if (resto === 0) {
    return `${horas} ${horas === 1 ? 'hora' : 'horas'}`;
  }

  return `${horas} ${horas === 1 ? 'hora' : 'horas'} y ${resto} minutos`;
}

/**
 * El backend manda los días sin tilde ("MIERCOLES", "SABADO").
 * Acá se convierten a español correcto ("Miércoles", "Sábado") para que
 * se lean bien y no parezcan errores de escritura.
 */
const DIAS_CON_TILDE: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  MIERCÓLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  SÁBADO: 'Sábado',
  DOMINGO: 'Domingo'
};

/** "LUNES" -> "Lunes", "MIERCOLES" -> "Miércoles". Si no lo reconoce, lo devuelve tal cual. */
export function formatearDiaSemana(dia: string | null | undefined): string {
  const valor = (dia ?? '').trim();
  return DIAS_CON_TILDE[valor.toUpperCase()] ?? valor;
}

/**
 * "1122334455" -> "11 2233-4455"
 * Los números quedan con el formato que se usa en Argentina, que se lee
 * mucho mejor al teléfono que un bloque de dígitos pegados.
 */
export function formatearTelefono(telefono: string | null | undefined): string {
  const digitos = (telefono ?? '').replace(/\D/g, '');

  if (digitos.length === 10) {
    return `${digitos.slice(0, 2)} ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
  }

  if (digitos.length === 11 && digitos.startsWith('11')) {
    return `${digitos.slice(0, 2)} ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
  }

  return (telefono ?? '').trim();
}
