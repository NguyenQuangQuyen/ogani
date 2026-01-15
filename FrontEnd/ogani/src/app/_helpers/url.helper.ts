import { environment } from '../environments/environment';

// Export backend URL for use in components
export const BACKEND_URL = environment.apiUrl.replace('/api', '');

// Helper function to get image URL
export function getImageUrl(imageName: string): string {
  return `${environment.apiUrl}/image/file/${imageName}`;
}
