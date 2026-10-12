import { Component, inject, signal } from '@angular/core';
import { ApiService, PingResponse } from './api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private api = inject(ApiService);

  result = signal<PingResponse | null>(null);
  error = signal<string | null>(null);

  constructor() {
    this.api.ping().subscribe({
      next: (res) => this.result.set(res),
      error: () => this.error.set('Could not reach the Trial Compass API.'),
    });
  }
}
