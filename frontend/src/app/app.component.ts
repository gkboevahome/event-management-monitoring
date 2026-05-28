import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms'; // ВАЖНО: Трябва ни за текстовите полета
import {Error, Event, MonitorService, Service} from './services/monitor.service';
import {Subscription} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Event Management Dashboard';

  private streamSubscription!: Subscription;
  inputServiceName = signal<string>('Payment-Service');
  inputErrorType = signal<string>('ERROR');

  newServiceName = signal<string>('');
  newServiceEnvironment = signal<string>('');
  newServiceOwner = signal<string>('');

  // Резултати от бекенда
  errorMessage = signal<Error | null>(null);
  successMessage = signal<string | null>(null);

  _services = signal<Service[]>([]);
  _events = signal<Event[]>([]);

  readonly services = this._services.asReadonly();
  readonly events = this._events.asReadonly();
  private http = inject(HttpClient);


  constructor(private monitorService: MonitorService) {
  }

  ngOnInit(): void {
    if (this.isLoggedIn()) {
      this.searchEvents();
      this.loadServices();
    }
  }

  searchEvents(): void {
    this.errorMessage.set(null);

    this.monitorService.getEventsByFilter(this.inputServiceName(), this.inputErrorType()).subscribe({
      next: (data) => this._events.set(data),
      error: (err) => this.handleError('Error while fetching events', err)
    });
  }

  loadServices(): void {
    this.monitorService.loadServices().subscribe({
      next: (data) => this._services.set(data),
      error: (err) => this.handleError('Error while fetching services', err)
    });

  }

  private handleError(message: string, error: Error): void {
    console.error(message, error.message);
    this.errorMessage.set(error);
  }

  // Validation state checks
  isLoggedIn(): boolean {
    return localStorage.getItem('access_token') !== null;
  }

  simulateLogin() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const systemAccount = { username: 'dashboard_system', password: 'system_secure_pass_2026' };

    this.http.post<{ token: string }>('http://localhost:8080/api/system-login', systemAccount).subscribe({
      next: (response) => {
        localStorage.setItem('access_token', response.token);
        this.successMessage.set('JWT tocken is successfully injected in the backend!');

        this.loadServices();
        this.searchEvents();
      },
      error: (err) => {
        this.handleError('Error with JWT', { message: 'Access is denied', status: '401' });
      }
    });

  }

  simulateLogout() {
    localStorage.removeItem('access_token');
    this._events.set([]);
    this._services.set([]);
  }

  submitNewService(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    let service: Service = {
      name: this.newServiceName(),
      environment: this.newServiceEnvironment(),
      owner: this.newServiceOwner(),
    };
    this.monitorService.addService(service).subscribe({
      next: (response) => {
        this.successMessage.set(`The "Service was created successfully!`);
        this.newServiceName.set('');
        this.newServiceEnvironment.set('');
        this.newServiceOwner.set('');
        this.loadServices();
      },
      error: (err) => this.handleError('Error in adding the service', err)
    });
  }

  ngOnDestroy() {
    if (this.streamSubscription) this.streamSubscription.unsubscribe();
  }
}
