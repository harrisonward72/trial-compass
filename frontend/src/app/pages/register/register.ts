import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink, Router } from '@angular/router';

import { AuthService } from '../../core/auth.service';

type FieldName = 'firstName' | 'email' | 'password';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private fb = inject(NonNullableFormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  //rules that mimic the backend validation rules
  //browser validation will prevent form from being submitted if rules are not met and provide instant feedback
  form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(70), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/),
      ],
    ],
  });

  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  serverErrors = signal<Record<string, string>>({}); //message from api

  //returns a helpful message to display under a field - or nothing if rules are met
  errorFor(name: FieldName): string | null {
    const server = this.serverErrors()[name];
    if (server) {
      return server;
    }

    const c = this.form.controls[name];
    //doesn't display errors until field has been touched
    if(!c.invalid || !(c.touched || c.dirty)) {
      return null;
    }

    if(c.hasError('required')) return 'This field is required';
    if(c.hasError('email')) return 'Enter a valid email address';
    if(c.hasError('minlenght')) return 'Password must be at least 8 characters';
    if(c.hasError('maxlength')) return 'Password must be no more than 70 characters';
    if(c.hasError('pattern')) return 'Password must contain at least one letter and one number';
    return 'Invalid value';
  }

  submit(): void {
    if(this.form.invalid) {
      this.form.markAllAsTouched(); //reveals all errors on the form
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.serverErrors.set({});

    const value = this.form.getRawValue();
    this.auth.register({firstName: value.firstName.trim(), email: value.email.trim(), password: value.password})
    .subscribe({
      next: () => this.router.navigate(['/search']),
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if(err.status === 400 && err.error?.errors) {
          //field by field messages from the Global Exception Handler
          this.serverErrors.set(err.error.errors);
        } else if (err.status === 409) {
          //email already exists
          this.serverErrors.set({email: err.error?.message ?? 'Email already exists'});
        } else {
          this.errorMessage.set('Something went wrong. Please try again');
        }
      },
    });
  }

}
