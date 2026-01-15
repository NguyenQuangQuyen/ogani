import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const CONTACT_API = 'https://hgr0a62zxby.sn.mynetname.net:2003/api/contact/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class ContactService {
  constructor(private http: HttpClient) {}

  sendMessage(data: any): Observable<any> {
    return this.http.post(CONTACT_API + 'send', data, httpOptions);
  }
}
