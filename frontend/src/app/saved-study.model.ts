//shape of the data going in and out from /api/saved-studies -- mirrors the java records


//saved trial on a users dashboard
export interface SavedStudy {
    id: number;
    nctId: string;
    title: string;
    lastKnownStatus: string | null;
    trackingStatusCode: string;
    trackingStatusLabel: string;
    followUpDate: string | null;
    savedAt: string;
}

//option in the tracking dropdown
export interface TrackingStatus {
    code: string;
    label: string;
}

//body of a save POST request
export interface SaveStudyRequest {
    nctId: string;
    title: string;
    lastKnownStatus?: string | null;
    trackingStatusCode?: string;
    followUpDate?: string | null;
}

//body of a PUT request -- only can change tracking status and follow up dates
export interface UpdateSavedStudyRequest {
    trackingStatusCode: string;
    followUpDate: string | null;
}