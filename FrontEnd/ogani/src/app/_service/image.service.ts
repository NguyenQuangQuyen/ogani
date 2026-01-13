import {
  HttpClient,
  HttpHeaders,
  HttpErrorResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

const IMAGE_API = 'http://hgr0a62zxby.sn.mynetname.net:2003/api/image/';

@Injectable({
  providedIn: 'root',
})
export class ImageService {
  constructor(private http: HttpClient) {}

  /**
   * Upload image to server
   * @param file Image file to upload
   * @returns Observable containing image information after upload
   */
  upload(file: File): Observable<any> {
    // Log file information before upload
    console.log(
      'Preparing to upload file:',
      file.name,
      'size:',
      file.size,
      'type:',
      file.type
    );

    // Create FormData object to send file
    const formData = new FormData();
    formData.append('file', file);

    // Call new API to upload image
    return this.http.post(IMAGE_API + 'upload', formData).pipe(
      catchError((error) => {
        console.error('Error uploading image:', error);
        return this.handleError(error);
      })
    );
  }

  /**
   * Get list of images from server
   * @returns Observable containing list of images
   */
  getList(): Observable<any> {
    return this.http
      .get(IMAGE_API, httpOptions)
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP error
   * @param error HTTP error
   * @returns Observable containing error
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = '';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage =
        `Error code: ${error.status}, ` +
        `Content: ${error.error?.message || error.message || 'Unknown error'}`;
    }

    console.error('ImageService error details:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
