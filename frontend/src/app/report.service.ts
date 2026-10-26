import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ReportFilters, ReportInfo, ReportResult } from "./report.model";


//front end talks to the backend with this report service class by building urls with the report params
@Injectable({ providedIn: 'root'})
export class ReportService {
    private http = inject(HttpClient);
    private base = `${environment.apiUrl}/reports`;

    //find what reports exist to populate the report picker
    list(): Observable<ReportInfo[]> {
        return this.http.get<ReportInfo[]>(this.base);
    }

    //runs a report - uses filters if they are specified
    run(type: string, filters: ReportFilters): Observable<ReportResult> {
        let params = new HttpParams();
        if(filters.keyword) {
            params = params.set('keyword', filters.keyword);
        }
        if(filters.trackingStatus){
            params = params.set('trackingStatus', filters.trackingStatus);
        }
        if(filters.from) {
            params = params.set('from', filters.from);
        }
        if(filters.to) {
            params = params.set('to', filters.to);
        }

        return this.http.get<ReportResult>(`${this.base}/${type}`, {params});
    }
}