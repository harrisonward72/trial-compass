import { DatePipe } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { NoteService } from '../../note.service';
import { Note } from '../../note.model';
import { HttpErrorResponse } from '@angular/common/http';

const MAX_LENGTH = 2000; //same as backend 

//this is one notes section for one trial - each trial gets its own

@Component({
  selector: 'app-notes-panel',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './notes-panel.html',
  styleUrl: './notes-panel.css',
})
export class NotesPanel implements OnInit{

  studyId = input.required<number>(); //passes input from parent component <app-notes-panel
  private noteService = inject(NoteService);
  readonly maxLength = MAX_LENGTH;

  notes = signal<Note[]>([]);
  loading = signal(true);
  busy = signal(false);
  errorMessage = signal<string | null>(null);
  edittingId = signal<number | null>(null); //allows ttracking for note that is being editted


  //note forms
  newNote = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(MAX_LENGTH)]
  });
  editNote = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(MAX_LENGTH)]
  });

  //OnInIt runs after inputs are processes so they won't be undefined
  ngOnInit(): void {
    this.noteService.list(this.studyId()).subscribe({
      next: (list) => {
        this.notes.set(list);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }



  //CREATE
  add(): void {
    const text = this.newNote.value.trim();
    if(!text) {
      this.errorMessage.set("You haven't written anything down");
      return;
    }

    if(this.newNote.invalid) {
      return;
    }

    this.busy.set(true);
    this.errorMessage.set(null);

    this.noteService.create(this.studyId(), {body: text}).subscribe ({
      next: (created) => {
        this.notes.update((list) => [created, ...list]);
        this.newNote.reset();
        this.busy.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  //UPDATE
  startEdit(note: Note): void {
    this.errorMessage.set(null);
    this.edittingId.set(note.id);
    this.editNote.setValue(note.body);
  }

  cancelEdit(): void {
    this.edittingId.set(null);
  }

  saveEdit(note: Note) {
    const text = this.editNote.value.trim();
    if(!text) {
      this.errorMessage.set('You cannot save an empty note');
      return;
    }

    if(this.editNote.invalid) {
      return;
    }

    this.busy.set(true);
    this.errorMessage.set(null);

    this.noteService.update(this.studyId(), note.id, {body: text}).subscribe({
      next: (updated) => {
        this.notes.update((list) => list.map((n) => (n.id === updated.id ? updated : n))) //swaps the edited note for the old version
        this.edittingId.set(null);
        this.busy.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  //DELETE
  remove(note: Note): void {
    //includes a confirmation
    if(!confirm('Are you sure you want to delete this note?')) {
      return;
    }

    this.busy.set(true);
    this.errorMessage.set(null);

    this.noteService.remove(this.studyId(), note.id).subscribe ({
      next: () => {
        this.notes.update((list) => list.filter((n) => n.id !== note.id));
        this.busy.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  //helper methods ====================================

  //returns true if the note was ever changed after it was created
  wasEdited(note: Note): boolean {
    return note.updatedAt !== note.createdAt;
  }

  //parses the json into something more readable
  private messageFrom(err: HttpErrorResponse): string {
    const fieldErrors = err.error?.errors as Record<string, string> | undefined;
    if(fieldErrors) {
      return Object.values(fieldErrors).join(' ');
    }
    if(typeof err.error?.message === 'string') {
      return err.error.message;
    }
    return 'An error occured please try again';
  }

}
