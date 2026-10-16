import { inject, Injectable, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { Observable, switchMap, tap } from "rxjs";
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from "./auth.models";
import { environment } from "../../environments/environment";

//keys to save things in the browser's local storage
const TOKEN_KEY = 'tc_token';
const EXPIRES_KEY = 'tc_expires_at';
const USER_KEY = 'tc_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router); 

  //encapsulate signals so that they can only be updated by the service
  private _token = signal<string | null>(null);
  private _user = signal<UserResponse | null>(null);
  private _expiresAt = 0;
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  readonly token = this._token.asReadonly();
  readonly user = this._user.asReadonly();

  constructor() {
    this.restoreSession();
  }

  //creates the account, logs in with the same credentials
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<UserResponse>(`${environment.apiUrl}/auth/register`, request).pipe(switchMap(()=> 
        this.login({ email: request.email, password: request.password })));
  }

  //user log in
    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
            tap((res) => this.startSession(res)),
        );
    }

    fetchMe(): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${environment.apiUrl}/users/me`);
    }

    logout(): void {
        this.clearSession();
        this.router.navigate(['login']);
    }

    isAuthenticated(): boolean {
        return this._token() !== null && Date.now() < this._expiresAt;
    }

    //private helpers ----------------------------------------------------
    
    private startSession(res: AuthResponse): void {
        this._expiresAt = Date.now() + res.expiresInSeconds * 1000;
        this._token.set(res.token);
        this._user.set(res.user);
        this.save(TOKEN_KEY, res.token);
        this.save(EXPIRES_KEY, String(this._expiresAt))
        this.save(USER_KEY, JSON.stringify(res.user));
        this.scheduleAutoLogout;
    }

    private restoreSession(): void {
        const token = this.load(TOKEN_KEY);
        const expires = Number(this.load(EXPIRES_KEY));
        const userJson = this.load(USER_KEY);

        //only restore if tokens are present and valid
        if(token && userJson && expires > Date.now()) {
            try {
                this._token.set(token);
                this._user.set(JSON.parse(userJson));
                this._expiresAt = expires;
                this.scheduleAutoLogout();
                return;
            } catch {

            }
        }
        this.clearSession();
    }

    private clearSession(): void {
        this._token.set(null);
        this._user.set(null);
        this._expiresAt = 0;
        if(this.expiryTimer) {
            clearTimeout(this.expiryTimer);
            this.expiryTimer = null;
        }
        [TOKEN_KEY, EXPIRES_KEY, USER_KEY].forEach(key => this.remove(key));
    }

    //automatically log user out when the token expires
    private scheduleAutoLogout(): void {
        if(this.expiryTimer) clearTimeout(this.expiryTimer);
        const msLeft = this._expiresAt - Date.now();
        this.expiryTimer = setTimeout(() => this.logout(), Math.max(msLeft, 0));
    }

    //local storage can throw errors in browser so every access is wrapped if storage falls login works until refresh
    private save(key: string, value: string): void {
        try { localStorage.setItem(key, value); } catch { }
    }

    private load(key: string): string |null {
        try {return localStorage.getItem(key);} catch { return null; }
    }

    private remove(key: string): void {
        try { localStorage.removeItem(key); } catch { }
    }


}