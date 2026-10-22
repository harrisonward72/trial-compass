import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StudyService } from '../../study.service';
import { AuthService } from '../../core/auth.service';
import { RecruitmentStatus, SearchFilters, SexChoice, StudySummary } from '../../study.model';
import { HttpErrorResponse } from '@angular/common/http';
import { SavedStudyService } from '../../saved-study.service';


type FieldName = 'condition' | 'keyword' | 'zip' | 'radiusMiles' | 'age';
const PAGE_SIZE = 10;

@Component({
  selector: 'app-search',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './search.html',
  styleUrl: './search.css',
})
export class Search {
  private fb = inject(NonNullableFormBuilder);
  private studyService = inject(StudyService);
  private auth = inject(AuthService);
  private savedService = inject(SavedStudyService);


  //the form itself
  //rules that mirror the backend, this just allows for instant user feedback - the backend still is what actually does the validation
  form = this.fb.group({
    condition: ['', [Validators.maxLength(200)]],
    keyword: ['', [Validators.maxLength(200)]],
    zip: ['', [Validators.pattern(/^\d{5}$/)]], //5 digit zip
    radiusMiles: this.fb.control<number | null>(50, [Validators.min(1), Validators.max(2000)]),
    sex: this.fb.control<SexChoice>('ANY'), 
    age: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(120)])
  });

  

  //page state - signals will update the form automatically ==================================================
  statuses = signal<RecruitmentStatus[]>([]);
  selectedStatuses = signal<Set<string>>(new Set()); 
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  results = signal<StudySummary [] | null>(null); //if null = no search has been run
  totalCount = signal(0);
  savedIds =signal<Set<string>>(new Set()); //shows saved once trial is saved
  savingId = signal<string | null>(null); //allows saved button to be disabled when saved 

  //gov api gives a token for the next page - need to remember each page's token to support previous page
  //pagination features
  pageIndex = signal(0);
  nextToken = signal<string | null>(null);
  hasPrevious = computed(() => this.pageIndex() > 0); //computed used as a reactive value derived from other signals (pageIndex)
  private tokens: (string | null)[] = [null];  //tokens[i] will be a bookmark that opens page [i]
  private activeFilters: SearchFilters | null = null; //defines active filters for when seach is pressed

  //if a user is not logged in and trys to save a trial prompt them to log in
  loginPromptFor = signal<string | null>(null); //holds NCT ID
  notice = signal<string | null>(null);

  constructor() {
    //fill the check boxes from the DB
    this.studyService.getStatuses().subscribe({
      next: (list) => this.statuses.set(list),
      error: () => this.errorMessage.set('Could not load the status options')
    });

    //if logged in find out which trials are already saved
    if(this.auth.isAuthenticated()) {
      this.savedService.list().subscribe({
        next: (list) => this.savedIds.set(new Set(list.map((s) => s.nctId))),
        error: () => {
        }
      })
    }
  }

  //status checkboxes ======================================================
  
  isSelected(code: string): boolean {
    return this.selectedStatuses().has(code);
  }

  onStatusChange(code: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    //makes a new set of signals rather than editing the old one
    const next = new Set(this.selectedStatuses());
    if(checked) next.add(code);
    else next.delete(code);
    this.selectedStatuses.set(next);

  }

  //validation message =====================================================
  
  fieldError(name: FieldName): string | null {
    const c = this.form.controls[name];
    //doesn't display until the field has been touched
    if(!c.invalid || !(c.touched || c.dirty)) return null;

    switch(name) {
      case 'zip':
        return 'Enter a 5 digit zip code';
      case 'radiusMiles':
        return 'Radius must be between 1 and 2000 miles';
      case 'age':
        return 'Enter an age between 0 and 120';
      default:
        return 'Please user less than 200 characters';
    }
  }

  //search ===============================================================

  onSubmit(): void {
    if(this.form.invalid) {
      this.form.markAllAsTouched(); //displays all field errors at once
      return;
    }

    const v = this.form.getRawValue();
    //get a snapshot for the next/previous pagination so editing the form doesnt change whats on the page
    this.activeFilters = {
      condition: v.condition.trim(),
      keyword: v.keyword.trim(),
      zip: v.zip.trim(),
      radiusMiles: v.radiusMiles,
      sex: v.sex,
      age: v.age,
      statuses: [...this.selectedStatuses()],
      pageSize: PAGE_SIZE,
    };

    //new search starts on page 1 - user does not have to be logged in 
    this.tokens = [null];
    this.pageIndex.set(0);
    this.loginPromptFor.set(null);
    this.notice.set(null);
    this.fetchPage();

  }

  clearFilters(): void {
    this.form.reset({radiusMiles: 1000, sex: 'ANY'});
    this.selectedStatuses.set(new Set());
  }

  next(): void {
    const token = this.nextToken();
    if(!token) return;
    this.tokens[this.pageIndex() + 1] = token; //must remember so previous page will work
    this.pageIndex.update((i) => i + 1);
    this.fetchPage();
  }

  previous(): void { 
    if(!this.hasPrevious()) return;
    this.pageIndex.update((i) => i - 1);
    this.fetchPage();
  }

  private fetchPage(): void {
    if(!this.activeFilters) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.studyService.search(this.activeFilters, this.tokens[this.pageIndex()]).subscribe({
      next: (res) => {
        this.results.set(res.studies);
        this.totalCount.set(res.totalCount);
        this.nextToken.set(res.nextPageToken ?? null);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.results.set(null);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  //take the error message received from the back end and make it readable for the user
  private messageFrom(err: HttpErrorResponse): string {
    //messages for each field from the global exception handler
    const fieldErrors = err.error?.errors as Record<string, string> | undefined;
    if(fieldErrors) return Object.values(fieldErrors).join(' ');
    if(typeof err.error?.message === 'string') return err.error.message;
    return 'Something went wrong please try again';
  }

  //results helpers ======================================================

  sexLabel(sex: string | null) : string {
    switch(sex) {
      case 'ALL': return 'All sexes';
      case 'FEMALE': return 'Female only';
      case 'MALE': return 'Male only';
      default: return '';
    }
  }

  isSaved(nctId: string): boolean {
    return this.savedIds().has(nctId);
  }

  //option to save trial to dashboard
  saveTrial(study: StudySummary): void {
    if(!this.auth.isAuthenticated()) {
      //if youre a guest and want to save a trial you will be prompted to login
      this.loginPromptFor.set(study.nctId);
      return;
    }
    
    this.savingId.set(study.nctId);
    this.errorMessage.set(null);
    this.notice.set(null);

    this.savedService
      .save({nctId: study.nctId, title: study.title, lastKnownStatus:study.status})
      .subscribe({
        next:() => {
          this.markSaved(study.nctId);
          this.savingId.set(null);
          this.notice.set('Saved to your dashboard');
        },
        error: (err: HttpErrorResponse) => {
          this.savingId.set(null);
          if(err.status === 409) { //already saved mark it saved
            this.markSaved(study.nctId);
            return;
          }
          this.errorMessage.set(this.messageFrom(err))
        }
      });
  }

  private markSaved(nctId: string): void {
    const next = new Set(this.savedIds());
    next.add(nctId);
    this.savedIds.set(next);
  }
}
