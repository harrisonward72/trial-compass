//shape of report objects to match java records

export interface ReportInfo {
    type: string; //follow ups - for example
    title: string;
    description: string;
}

export interface ReportResult {
    type: string;
    title: string;
    description: string;
    generatedAt: string;
    columns: string[];
    rows: string [][];
    rowCount: number;
}

export interface ReportFilters {
    keyword: string;
    trackingStatus: string;
    from: string;
    to: string; 
}
