

//interceptor class sits between code and the server - http requests will pass through it 

import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "./auth.service";
import { catchError, throwError } from "rxjs";
import { environment } from "../../environments/environment";

//write attach token logic once instead of every service call
export const authInterceptor: HttpInterceptorFn = (req, next) =>{
    const auth = inject(AuthService);
    const token = auth.token();

    //only send token to Trial Compass API
    const isOurApi = req.url.startsWith(environment.apiUrl);

    //request is immutable, must be cloned with new header
    const outGoing = 
    token && isOurApi ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

    return next(outGoing).pipe(catchError((error: HttpErrorResponse) => {
        //401 on a normal request means the token is invalid or expired, should trigger an error message
        if(error.status === 401 && token && !req.url.includes('/auth/')){
            auth.logout();
        }
        //pass the error along to the service that made the request
        return throwError(() => error);
    })
);



};