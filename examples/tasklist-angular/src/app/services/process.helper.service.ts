import {Injectable} from '@angular/core';
import {ProcessService} from 'tasklist/services';
import {ProcessDefinition} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';


@Injectable()
export class ProcessHelperService {

  private processesSubject: BehaviorSubject<Array<ProcessDefinition>> = new BehaviorSubject<Array<ProcessDefinition>>([]);

  constructor(private processService: ProcessService) {
    this.reload();
  }

  get processes() {
    return this.processesSubject.asObservable();
  }

  reload(): void {
    this.processService.getStartableProcesses().subscribe((response: Array<ProcessDefinition>) => {
      this.processesSubject.next(response);
    }, (error) => {
      console.log('Error loading processes', error);
    });
  }
}
