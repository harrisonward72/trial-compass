import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { Note, NoteRequest } from "./note.model";


//note logic, class that talks to notes backend, notes belong to a saved trial
 
@Injectable({ providedIn: 'root'})
export class NoteService {

    private http = inject(HttpClient);

    //CRUD options for notes
    
    private url(studyId: number): string {
        return `${environment.apiUrl}/saved-studies/${studyId}/notes`;
    }

    list(studyId: number): Observable<Note[]> {
        return this.http.get<Note[]>(this.url(studyId));
    }

    create(studyId: number, request: NoteRequest): Observable<Note>{
        return this.http.post<Note>(this.url(studyId), request);
    }

    update(studyId: number, noteId: number, request: NoteRequest): Observable<Note>{
        return this.http.put<Note>(`${this.url(studyId)}/${noteId}`, request);
    }

    remove(studyId: number, noteId: number): Observable<void> {
        return this.http.delete<void>(`${this.url(studyId)}/${noteId}`);
    }
}
