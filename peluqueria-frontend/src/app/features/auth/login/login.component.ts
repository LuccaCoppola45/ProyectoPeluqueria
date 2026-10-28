import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  readonly cargando = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly mostrarPassword = signal(false);

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      dni: ['', [Validators.required, Validators.pattern(/^\d{7,8}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    if (this.route.snapshot.queryParamMap.get('sesion') === 'vencida') {
      this.errorMsg.set('Tu sesión venció. Iniciá sesión nuevamente.');
    }
  }

  onDniInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const dni = input.value.replace(/\D/g, '').slice(0, 8);
    input.value = dni;
    this.form.get('dni')?.setValue(dni, { emitEvent: false });
  }

  togglePassword(): void {
    this.mostrarPassword.update((visible) => !visible);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMsg.set('Revisá los datos que están en rojo antes de continuar.');
      return;
    }

    this.cargando.set(true);
    this.errorMsg.set(null);

    this.authService.login(this.form.getRawValue() as LoginRequest).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigateByUrl(this.destinoTrasIngresar());
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMsg.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor. Verificá que el backend esté iniciado e intentá de nuevo.'
            : err.status === 401
              ? 'El DNI o la contraseña no son correctos. Revisá los datos e intentá de nuevo.'
              : 'No se pudo iniciar sesión. Intentá de nuevo.'
        );
      }
    });
  }

  /**
   * Si el usuario fue enviado a iniciar sesión desde otra página (por ejemplo
   * al querer reservar un turno), al ingresar vuelve a esa misma página
   * en lugar de perder el trabajo que estaba haciendo.
   */
  private destinoTrasIngresar(): string {
    const regreso = this.route.snapshot.queryParamMap.get('regreso');
    // Solo se aceptan rutas internas para evitar redirecciones externas.
    return regreso && regreso.startsWith('/') && !regreso.startsWith('//')
      ? regreso
      : '/servicios';
  }
}
