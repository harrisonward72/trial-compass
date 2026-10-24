//shape of the note object - mirrors java record
export interface Note {
    id: number;
    body: string;
    createdAt: string;
    updatedAt: string;
}

export interface NoteRequest {
    body: string;
}