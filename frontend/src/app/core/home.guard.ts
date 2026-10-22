import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { SavedStudyService } from '../saved-study.service';
import { catchError, map, of } from 'rxjs';


// determines how the user is directed based on whether or not they have saved trials
//this class decides where a '/' url path will send people
//determines from the back end if a user has saved trials or not
// guest -> search
// logged in with nothing saved -> search
// logged in with saved trials -> dashboard

export const homeRedirectGuard: CanActivateFn = () => { 
    const auth = inject(AuthService);
    const router = inject(Router);
    const savedService = inject(SavedStudyService);

    if(!auth.isAuthenticated()) {
        return router.createUrlTree(['/search']);
    }

    return savedService.list().pipe(
        map((list) => router.createUrlTree([list.length > 0 ? '/dashboard' : '/search'])),
        catchError(() => of(router.createUrlTree(['/dashboard'])))
    );

};