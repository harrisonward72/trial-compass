import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth.service';
import { UserResponse } from '../../core/auth.models';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  protected auth = inject(AuthService); //protected so the template can read it but other classes cannot
  serverUser = signal<UserResponse | null>(null);

  constructor() {
    //temporary to be replaced later
    //calls the protected endpoint to make intereptor visable - saved trials will replace this
    this.auth.fetchMe().subscribe((u) => this.serverUser.set(u));
  }
}
