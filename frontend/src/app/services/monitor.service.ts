import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

export interface Event {
  id: string;
  type: 'WARNING' | 'ERROR' | 'INFO';
  service: Service;
  message: string;
  created_at?: string;
}

export interface Service {
  id?: string;
  name: string;
  environment: string;
  owner: string;
}

export interface Error {
  message: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class MonitorService {
  private http = inject(HttpClient);

  // Some statistics
  baseUrl: string = 'http://localhost:8080/api';

  getEventsByFilter(serviceName: string, errorType: string): Observable<Event[]> {
    const processedServiceName = (!serviceName || serviceName.trim() === '' || serviceName.trim() === '%')
      ? null
      : serviceName.trim();

    const graphQLQuery = {
      query: `
      query GetEvents($serviceName: String, $type: EventType) {
        getEvents(serviceName: $serviceName, type: $type) {
          id
          type
          message
          createdAt
          service {
            id
            name
            environment
            owner
          }
        }
      }
    `,
      variables: {
        serviceName: processedServiceName,
        type: errorType ? errorType : null
      }
    };

    return this.http.post<any>('http://localhost:8080/graphql', graphQLQuery).pipe(
      map(response => {
        if (response.errors) {
          console.error('GraphQL Internal Errors:', response.errors);
          throw new Error(response.errors.message);
        }

        const events = response.data.getEvents;
        return events.map((event: any) => ({
          ...event,
          created_at: event.createdAt
        }));
      })
    );
  }

  loadServices(): Observable<Service[]> {
    return this.http.get<Service[]>(`${this.baseUrl}/services`);
  }

  addService(service: Service): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/services`, service);
  }

}
