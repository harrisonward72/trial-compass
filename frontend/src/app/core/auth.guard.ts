import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

//guard function runs before the route is activated. Returns true if user is authenticated
//returning URL tree will redirect them 

export const authGuard: CanActivateFn = (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if(auth.isAuthenticated()) {
        return true;
    }
    //user is not authenticated, redirect to login page with returnUrl query param
    return router.createUrlTree(['/login'], {queryParams: {returnUrl: state.url}});
};