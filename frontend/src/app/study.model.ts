//shape of the data going to the backend
//each of these mirror their corresponding java records


export interface RecruitmentStatus {
    code: string; //what gets sent to the API
    label: string; //what the user will see

}

export interface StudySummary {
    nctId: string;
    title: string;
    status: string;
    statusLabel: string;
    summary: string | null;
    conditions: string [];
    organization: string | null;
    sex: string | null;
    ageRange: string | null;
    locations: string[];
    totalLocations: number;
    lastUpdated: string | null;
}

export interface StudySearchResponse {
    studies: StudySummary[];
    totalCount: number;
    nextPageToken: string | null; //last page of results will have a next page token of null
}

export type SexChoice = 'ANY' | 'FEMALE' | 'MALE';

export interface SearchFilters {
    condition: string;
    keyword: string;
    zip: string;
    radiusMiles: number | null;
    sex: SexChoice;
    age: number | null;
    statuses: string[];
    pageSize: number;
}