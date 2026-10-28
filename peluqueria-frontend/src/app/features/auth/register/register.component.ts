import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  readonly cargando = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly exito = signal(false);
  readonly mostrarPassword = signal(false);
  readonly paisCode = '+54';

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      apellido: ['', [Validators.required, Validators.minLength(3)]],
      dni: ['', [Validators.required, Validators.pattern(/^\d{7,8}$/)]],
      telefono: ['', [Validators.required, Validators.pattern(/^\d{8,12}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onDniInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const dni = input.value.replace(/\D/g, '').slice(0, 8);
    input.value = dni;
    this.form.get('dni')?.setValue(dni, { emitEvent: false });
  }

  onTelefonoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = this.normalizarDigitosTelefono(input.value);
    input.value = digits;
    this.form.get('telefono')?.setValue(digits, { emitEvent: false });
  }

  togglePassword(): void {
    this.mostrarPassword.update((visible) => !visible);
  }

  private normalizarTelefono(valor: string): string {
    return `${this.paisCode}${this.normalizarDigitosTelefono(valor)}`;
  }

  private normalizarDigitosTelefono(valor: string): string {
    const digits = (valor ?? '').replace(/\D/g, '');
    return digits.startsWith('54') ? digits.slice(2) : digits;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMsg.set('Revisá los datos que están en rojo antes de continuar.');
      return;
    }

    const valores = this.form.getRawValue();
    const payload: RegisterRequest = {
      nombre: valores.nombre.trim(),
      apellido: valores.apellido.trim(),
      dni: valores.dni,
      telefono: this.normalizarTelefono(valores.telefono),
      password: valores.password
    };

    this.cargando.set(true);
    this.errorMsg.set(null);

    this.authService.registrar(payload).subscribe({
      next: () => {
        this.cargando.set(false);
        this.exito.set(true);
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMsg.set(
          err.status === 409
            ? 'Ese DNI ya está registrado. Probá iniciar sesión con tus datos.'
            : err.status === 400
              ? 'Revisá los datos ingresados e intentá de nuevo.'
              : 'No se pudo completar el registro. Intentá de nuevo.'
        );
      }
    });
  }
}
