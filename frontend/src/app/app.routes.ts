import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';

export const routes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'dashboard'},
    {path: 'login', component: Login},
    {path: 'register', component: Register},
    //runs the authgaurd before activating this page to check if a user is logged in
    {path: 'dashboard', component: Dashboard, canActivate: [authGuard]},
    //unrecognized url sends people home
    {path: '**', redirectTo: 'dashboard'},

];
