import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Environment } from 'process/models/environment';
import { EnvironmentService } from 'process/services/environment.service';

@Injectable()
export class EnvironmentHelperService {

  private environmentSubject: BehaviorSubject<Environment> = new BehaviorSubject<Environment>(this.none());

  constructor(private environmentService: EnvironmentService) {
    this.environmentService.getEnvironment().subscribe(
      (environment) => {
        this.environmentSubject.next(environment);
      },
      (error) => {
        console.log('Error loading environment', error);
      }
    );
  }

  env() {
    return this.environmentSubject.asObservable();
  }


  none(): Environment {
    return {
      applicationName: 'loading...',
      tasklistUrl: '#'
    };
  }
}
