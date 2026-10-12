import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PingResponse {
  status: string;
  message: string;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  ping(): Observable<PingResponse> {
    return this.http.get<PingResponse>(`${this.baseUrl}/ping`);
  }
}