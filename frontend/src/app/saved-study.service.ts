//this class talks to the backend and makes the CRUD options possible on the front end
//the interceptor send the validation token, not the user ID, the backend validates the token

import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { SaveStudyRequest, UpdateSavedStudyRequest, SavedStudy, TrackingStatus } from "./saved-study.model";

@Injectable({providedIn: 'root'})
export class SavedStudyService {

    private http = inject(HttpClient);
    private base = `${environment.apiUrl}/saved-studies`;

    list(): Observable<SavedStudy[]> {
        return this.http.get<SavedStudy[]>(this.base);
    }

    getTrackingStatuses():Observable<TrackingStatus[]> {
        return this.http.get<TrackingStatus[]>(`${this.base}/tracking-statuses`);
    }

    save(request: SaveStudyRequest): Observable<SavedStudy> {
        return this.http.post<SavedStudy>(this.base, request);
    }

    update(id: number, request: UpdateSavedStudyRequest): Observable<SavedStudy> {
        return this.http.put<SavedStudy>(`${this.base}/${id}`, request);
    }

    remove(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/${id}`);
    }
}