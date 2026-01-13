import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ReportApiService {
  private readonly REPORT_API = 'http://hgr0a62zxby.sn.mynetname.net:2003/api/report';

  constructor(private http: HttpClient) {}

  downloadDashboardReport(options?: {
    title?: string;
    exportedBy?: string;
  }): void {
    const params = new HttpParams()
      .set('title', options?.title || 'Báo cáo tổng quan hệ thống')
      .set('exportedBy', options?.exportedBy || 'admin');

    this.http
      .get(`${this.REPORT_API}/dashboard`, {
        params,
        responseType: 'blob' as const,
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'bao_cao_dashboard.xlsx';
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Failed to download report', err);
        },
      });
  }

  downloadDashboardReportSync(payload: any): void {
    this.http
      .post(`${this.REPORT_API}/dashboard-sync`, payload, {
        responseType: 'blob' as const,
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'bao_cao_dashboard.xlsx';
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Failed to download synced report', err);
        },
      });
  }
}
