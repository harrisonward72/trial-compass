import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { inject } from '@angular/core';
import { AuthService } from './core/auth.service';
import { Search } from './pages/search/search';
import { homeRedirectGuard } from './core/home.guard';

export const routes: Routes = [
    //auth service evaluates if logged in or not, if not goes to search page, if they are they go to their dashboard
    {path: '', pathMatch: 'full', canActivate:[homeRedirectGuard], children: []},
    {path: 'search', component: Search},
    {path: 'login', component: Login},
    {path: 'register', component: Register},
    //runs the authgaurd before activating this page to check if a user is logged in
    {path: 'dashboard', component: Dashboard, canActivate: [authGuard]},
    //unrecognized url sends people to the first path evaluation at the top
    {path: '**', redirectTo: ''},

];
