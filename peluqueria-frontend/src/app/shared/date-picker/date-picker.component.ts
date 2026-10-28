import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

interface CalendarDay {
  date: string;
  number: number;
  disabled: boolean;
  isToday: boolean;
}

@Component({
  selector: 'app-date-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.css']
})
export class DatePickerComponent implements OnChanges {
  @Input() date = '';
  @Input() minDate = this.toDateString(new Date());
  @Output() dateChange = new EventEmitter<string>();

  readonly weekdays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  visibleMonth = this.startOfMonth(new Date());

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
      days.push({
        date,
        number: day,
        disabled: date < this.minDate,
        isToday: date === today
      });
    }

    return days;
  }

  get previousMonthDisabled(): boolean {
    const minimumMonth = this.startOfMonth(this.parseDate(this.minDate) ?? new Date());
    return this.visibleMonth.getTime() <= minimumMonth.getTime();
  }

  changeMonth(offset: number): void {
    const nextMonth = new Date(
      this.visibleMonth.getFullYear(),
      this.visibleMonth.getMonth() + offset,
      1
    );

    if (offset < 0 && nextMonth < this.startOfMonth(this.parseDate(this.minDate) ?? new Date())) {
      return;
    }

    this.visibleMonth = nextMonth;
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
