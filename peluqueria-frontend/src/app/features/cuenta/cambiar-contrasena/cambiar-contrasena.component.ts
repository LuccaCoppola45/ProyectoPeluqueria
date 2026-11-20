import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../../../core/services/usuario.service';

/** Las dos contraseñas nuevas tienen que coincidir */
function contrasenasIguales(grupo: AbstractControl): ValidationErrors | null {
  const nueva = grupo.get('nueva')?.value;
  const repetida = grupo.get('repetida')?.value;
  return nueva && repetida && nueva !== repetida ? { distintas: true } : null;
}

@Component({
  selector: 'app-cambiar-contrasena',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cambiar-contrasena.component.html',
  styleUrls: ['./cambiar-contrasena.component.css']
})
export class CambiarContrasenaComponent {
  private readonly usuarioService = inject(UsuarioService);

  readonly cargando = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly listo = signal(false);
  readonly mostrar = signal(false);

  readonly form = inject(FormBuilder).nonNullable.group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
      repetida: ['', Validators.required]
    },
    { validators: contrasenasIguales }
  );

  guardar(): void {
    this.errorMsg.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMsg.set('Revisá los datos que están en rojo antes de continuar.');
      return;
    }
    const { actual, nueva } = this.form.getRawValue();
    this.cargando.set(true);
    this.usuarioService.cambiarMiContrasena(actual, nueva).subscribe({
      next: () => {
        this.cargando.set(false);
        this.listo.set(true);
        this.form.reset();
      },
      error: (err) => {
        this.cargando.set(false);
        this.errorMsg.set(err.error?.message ?? 'No se pudo cambiar la contraseña. Probá de nuevo.');
      }
    });
  }
}
