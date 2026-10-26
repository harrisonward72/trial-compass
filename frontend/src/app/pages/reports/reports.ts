import { TrackingStatus } from './../../saved-study.model';
import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReportService } from '../../report.service';
import { SavedStudyService } from '../../saved-study.service';
import { ReportInfo, ReportResult } from '../../report.model';
import { forkJoin } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-reports',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports {
  private fb = inject(NonNullableFormBuilder);
  private reportService = inject(ReportService);
  private savedService = inject(SavedStudyService);

  //filter form=====================
  form = this.fb.group({
    type: [''],
    keyword: ['', [Validators.maxLength(100)]],
    trackingStatus: [''],
    from: [''],
    to: ['']
  });

  //signals =========================
  reports = signal<ReportInfo[]>([]);
  trackingStatuses = signal<TrackingStatus[]>([]);
  result = signal<ReportResult | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  constructor() {
    //loads the picker and status dropdown together then runs the first report
    forkJoin({
      reports: this.reportService.list(),
      statuses: this.savedService.getTrackingStatuses(),
    }).subscribe({
      next: ({ reports, statuses }) => {
        this.reports.set(reports);
        this.trackingStatuses.set(statuses);
        if(reports.length > 0 ) {
          this.form.patchValue({ type: reports[0].type });
          this.run();
        }
        else {
          this.loading.set(false);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    })
  }

  //gives description of the current chosen reports
  description(): string {
    const type = this.form.controls.type.value;
    return this.reports().find((r) => r.type === type)?.description ?? '';
  }

  run(): void {
    const v = this.form.getRawValue();

    if(!v.type) {
      return;
    }

    if(v.from && v.to && v.from > v.to) {
      this.errorMessage.set('The start date must be on or before the end date');
      return;
    }

    if(this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(false);
    this.errorMessage.set(null);

    this.reportService.run(v.type, {
      keyword: v.keyword.trim(),
      trackingStatus: v.trackingStatus,
      from: v.from,
      to: v.to,
    })
    .subscribe ({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.messageFrom(err));
      }
    });
  }

  clearFilters(): void {
    this.form.patchValue({ keyword: '', trackingStatus: '', from: '', to: ''});
    this.run();
  }

  //export and print functions=================================================

  downloadCsv(): void {
    const r = this.result();
    if(!r){
      return;
    }

    const lines: string[][] = [
      [r.title],
      [`Generated: ${new Date(r.generatedAt).toLocaleString()}`],
      [],
      r.columns,
      ...r.rows
    ];

    const csv = lines.map((row) => row.map((cell) => this.csvCell(cell)).join(',')).join('\r\n');

    //\uFEFF talks to excel and makes it read the file as UTF-8
    const blob = new Blob(['\uFEFF' + csv], {type: 'text/csv;charset=utf=8;'});
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${r.type}-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  print(): void {
    window.print();
  }

  private csvCell(value: string): string {
    let text = value ?? '';

    //need to wrap text in quotes so excel doesn't mess with the formatting
    if(/^[=+\-@\t\r]/.test(text)) {
      text = "'" + text;
    }
    if (/[",\r\n]/.test(text)) {
      text = '"' + text.replace(/"/g, '""') + '"';
    }

    return text;
  }

  private messageFrom(err: HttpErrorResponse): string {
    const fieldErrors = err.error?.errors as Record<string, string> | undefined;
    if(fieldErrors) {
      return Object.values(fieldErrors).join(' ');
    }
    if(typeof err.error?.message === 'string') {
      return err.error.message;
    }
    return 'An error has occured please try again';
  }

}
