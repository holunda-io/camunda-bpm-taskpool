import { Injectable, OnInit, OnDestroy } from '@angular/core';
import { EnvironmentService } from 'process/api/environment.service';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Environment } from 'process/model/environment';

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
