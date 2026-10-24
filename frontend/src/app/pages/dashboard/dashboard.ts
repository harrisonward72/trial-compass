import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth.service';
import { UserResponse } from '../../core/auth.models';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { SavedStudyService } from '../../saved-study.service';
import { SavedStudy } from '../../saved-study.model';
import { TrackingStatus } from '../../saved-study.model';
import { forkJoin } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NotesPanel } from '../../components/notes-panel/notes-panel';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe, NotesPanel],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  protected auth = inject(AuthService); //protected so the template can read it but other classes cannot
  private savedService = inject(SavedStudyService);
  private today = new Date().toLocaleDateString('en-US');


  serverUser = signal<UserResponse | null>(null);
  items = signal<SavedStudy[]>([]);
  trackingStatuses = signal<TrackingStatus[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  notice = signal<string | null>(null);
  busyId = signal<number | null>(null);
  openNotes = signal<Set<number>>(new Set); //which trials have notes open - set holds the saved trials ids


  constructor() {
    forkJoin({
      items: this.savedService.list(),
      statuses: this.savedService.getTrackingStatuses(),
    }).subscribe({
      next: ({ items, statuses}) => {
        this.items.set(items);
        this.trackingStatuses.set(statuses);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  //UPDATE==============================================
  //status change will save as soon as its changed, rather than having a seperate save button

  onStatusChange(item: SavedStudy, event: Event): void {
    const element = event.target as HTMLSelectElement;
    //if the server refuses the last argument will put the control back how it was
    this.update(item, element.value, item.followUpDate, () => (element.value = item.trackingStatusCode));
  }

  onDateChange(item: SavedStudy, event: Event): void {
    const element = event.target as HTMLInputElement;
    //cleared date box will give a ""
    this.update(item, item.trackingStatusCode, element.value || null, () => (element.value = item.followUpDate ?? '')); 
  }

  private update(item: SavedStudy, code: string, date: string | null, revert: () => void): void {
    this.busyId.set(item.id);
    this.errorMessage.set(null);
    this.notice.set(null);

    //swap the changed row for the servers version of the row
    this.savedService.update(item.id, { trackingStatusCode: code, followUpDate: date }).subscribe({
      next:(updated) => {
        this.items.update((list) => list.map((i) => (i.id === updated.id ? updated : i)));
        this.busyId.set(null);
        this.notice.set('Changes saved');
      },
      error: (e: HttpErrorResponse) => {
        this.busyId.set(null);
        revert();
        this.errorMessage.set(this.messageFrom(e));
      }
    });
  }

  //DELETE =========================================
  //includes a confirmation step to eliminate accidental deletions

  remove(item: SavedStudy): void {
    if(!confirm(`Remove "${item.title}" from your saved trials?`)) return;

    this.busyId.set(item.id);
    this.errorMessage.set(null);
    this.notice.set(null);

    this.savedService.remove(item.id).subscribe({
      next: () => {
        this.items.update((list) => list.filter((i) => i.id != item.id));
        this.busyId.set(null);
        this.notice.set("Trial deleted");
      },
      error: (e: HttpErrorResponse) => {
        this.busyId.set(null);
        this.errorMessage.set(this.messageFrom(e));
      }
    });
  }


  //Helpers===============================

  //converts the json from the backend to a readable format to be displayed
  private messageFrom(err: HttpErrorResponse): string {
    const e = err.error?.errors as Record<string, string> | undefined;

    if(e){
       return Object.values(e).join(' ');
    }

    if(typeof err.error?.message === 'string'){
       return err.error.message;
    }

    return 'An error occured please try again';
  }

  //returns true when a user sets a follow up date but the date has already past
  //input validation
  isOverdue(item: SavedStudy): boolean {
    return !!item.followUpDate && item.followUpDate < this.today; //!! used for strict boolean conversion
  }

  //converts the field names in the database to a more readable format
  prettyStatus(code: string | null): string {
    if(!code) {
      return 'unknown'
    }
    const text = code.toLowerCase().replace(/_/g, ' '); //replace the underscores with spaces
    return text.charAt(0).toUpperCase() + text.slice(1); 
  }

  isNotesOpen(id: number): boolean {
    return this.openNotes().has(id);
  }

  toggleNotes(id: number): void {
    const next = new Set(this.openNotes());
    if(next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    this.openNotes.set(next);
  }
}
