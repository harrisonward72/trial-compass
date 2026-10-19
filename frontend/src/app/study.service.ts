import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { RecruitmentStatus, SearchFilters, StudySearchResponse } from "./study.model";
import { environment } from "../environments/environment";



//class that allows front end to talk to study endpoints to build the URLs
//builds search params onto url
@Injectable({providedIn: 'root'})
export class StudyService {
    private http = inject(HttpClient);

    //loads the status checkboxes from SQL
    getStatuses(): Observable<RecruitmentStatus[]> {
        return this.http.get<RecruitmentStatus[]>(`${environment.apiUrl}/studies/statuses`);
    }

    //builds the query string from only filters being used
    search(filters: SearchFilters, pageToken: string | null): Observable<StudySearchResponse> {
        let params = new HttpParams().set('pageSize', filters.pageSize);

        //checking the filters - set the params if input is present
        if(filters.condition) params = params.set('condition', filters.condition);
        if(filters.keyword) params = params.set('keyword', filters.keyword);

        //radius is only applied if a zipcode is entered - apply together
        if(filters.zip) {
            params = params.set('zip', filters.zip);
            if (filters.radiusMiles !== null) params = params.set('radiusMiles', filters.radiusMiles);
        }

        if(filters.sex !== 'ANY') params = params.set('sex', filters.sex);
        if(filters.age !== null) params = params.set('age', filters.age);

        //appending statuses field with code field
        for(const code of filters.statuses) {
            params = params.append('statuses', code);
        }

        if(pageToken) params = params.set('pageToken', pageToken);

        return this.http.get<StudySearchResponse>(`${environment.apiUrl}/studies/search`, {params});

    }
}