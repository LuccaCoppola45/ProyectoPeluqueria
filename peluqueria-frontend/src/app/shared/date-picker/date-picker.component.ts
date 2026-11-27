import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';

interface CalendarDay {
  date: string;
  number: number;
  disabled: boolean;
  isToday: boolean;
}

/** Cuántos meses seguidos ocultos se saltean como mucho al cambiar de mes. */
const MAX_MESES_SALTEADOS = 24;

@Component({
  selector: 'app-date-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.css']
})
export class DatePickerComponent implements OnInit, OnChanges {
  @Input() date = '';
  @Input() minDate = this.toDateString(new Date());
  /**
   * Días que se pueden elegir (yyyy-MM-dd). Los demás no se muestran, así nadie pierde tiempo tocando
   * días cerrados o llenos. Con null se muestran todos los días desde minDate (como en el panel).
   */
  @Input() availableDates: string[] | null = null;
  /** Mientras se consultan los días libres del mes que se está viendo. */
  @Input() loading = false;
  /** Meses que no se muestran en el calendario (yyyy-MM): al cambiar de mes se saltean. */
  @Input() hiddenMonths: string[] = [];
  /** Avisa el primer día (yyyy-MM-dd) del mes que quedó a la vista, para consultar sus días libres. */
  @Output() monthChange = new EventEmitter<string>();
  @Output() dateChange = new EventEmitter<string>();

  readonly weekdays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  visibleMonth = this.startOfMonth(new Date());

  ngOnInit(): void {
    if (this.isHidden(this.visibleMonth)) {
      this.visibleMonth = this.nextVisibleMonth(this.visibleMonth, 1) ?? this.visibleMonth;
    }
    // Se avisa después de este ciclo para que el padre pueda actualizar su estado sin errores de Angular
    const month = this.toDateString(this.visibleMonth);
    Promise.resolve().then(() => this.monthChange.emit(month));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['date'] && this.date) {
      const selectedDate = this.parseDate(this.date);
      if (selectedDate) {
        this.visibleMonth = this.startOfMonth(selectedDate);
      }
    }
  }

  get monthLabel(): string {
    return new Intl.DateTimeFormat('es-AR', {
      month: 'long',
      year: 'numeric'
    }).format(this.visibleMonth);
  }

  get selectedDateLabel(): string {
    const selectedDate = this.parseDate(this.date);
    return selectedDate
      ? new Intl.DateTimeFormat('es-AR', {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        }).format(selectedDate)
      : '';
  }

  get calendarDays(): (CalendarDay | null)[] {
    const firstDay = this.visibleMonth.getDay();
    const mondayFirstOffset = (firstDay + 6) % 7;
    const daysInMonth = new Date(
      this.visibleMonth.getFullYear(),
      this.visibleMonth.getMonth() + 1,
      0
    ).getDate();
    const days: (CalendarDay | null)[] = Array(mondayFirstOffset).fill(null);
    const today = this.toDateString(new Date());

    for (let day = 1; day <= daysInMonth; day++) {
      const date = this.toDateString(new Date(
        this.visibleMonth.getFullYear(),
        this.visibleMonth.getMonth(),
        day
      ));
      if (!this.isShown(date)) {
        days.push(null);
        continue;
      }
      days.push({
        date,
        number: day,
        disabled: date < this.minDate,
        isToday: date === today
      });
    }

    return this.withoutEmptyWeeks(days);
  }

  /** Las semanas sin ningún día para elegir no se muestran: si no, quedan huecos grandes en el calendario. */
  private withoutEmptyWeeks(days: (CalendarDay | null)[]): (CalendarDay | null)[] {
    const weeks: (CalendarDay | null)[][] = [];
    for (let i = 0; i < days.length; i += 7) {
      weeks.push(days.slice(i, i + 7));
    }
    return weeks.filter((week) => week.some((day) => day !== null)).flat();
  }

  /** Se están filtrando días y en el mes a la vista no quedó ninguno para elegir. */
  get noDaysThisMonth(): boolean {
    return this.availableDates !== null && !this.loading
      && this.calendarDays.every((day) => day === null || day.disabled);
  }

  private isShown(date: string): boolean {
    if (this.availableDates === null) {
      return true;
    }
    return !this.loading && this.availableDates.includes(date);
  }

  get previousMonthDisabled(): boolean {
    return this.nextVisibleMonth(this.visibleMonth, -1) === null;
  }

  changeMonth(offset: number): void {
    const nextMonth = this.nextVisibleMonth(this.visibleMonth, offset);
    if (!nextMonth) {
      return;
    }

    this.visibleMonth = nextMonth;
    this.monthChange.emit(this.toDateString(nextMonth));
  }

  /** El mes siguiente (o anterior) que se puede mostrar, salteando los ocultos; null si no hay. */
  private nextVisibleMonth(from: Date, offset: number): Date | null {
    const minimumMonth = this.startOfMonth(this.parseDate(this.minDate) ?? new Date());
    let month = from;
    for (let i = 0; i < MAX_MESES_SALTEADOS; i++) {
      month = new Date(month.getFullYear(), month.getMonth() + offset, 1);
      if (month < minimumMonth) {
        return null;
      }
      if (!this.isHidden(month)) {
        return month;
      }
    }
    return null;
  }

  private isHidden(month: Date): boolean {
    return this.hiddenMonths.includes(this.toDateString(month).substring(0, 7));
  }

  selectDate(date: string): void {
    this.dateChange.emit(date);
  }

  accessibleDate(date: string): string {
    const parsedDate = this.parseDate(date);
    return parsedDate
      ? new Intl.DateTimeFormat('es-AR', {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        }).format(parsedDate)
      : date;
  }

  private parseDate(value: string): Date | null {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) {
      return null;
    }

    const [, year, month, day] = match;
    const parsed = new Date(Number(year), Number(month) - 1, Number(day));
    return this.toDateString(parsed) === value ? parsed : null;
  }

  private startOfMonth(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }

  private toDateString(date: Date): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
