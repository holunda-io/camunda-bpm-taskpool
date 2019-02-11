import { Component } from '@angular/core';
import { TaskEvent } from 'cockpit/model/taskEvent';
import { TaskEventHelperService } from 'app/services/taskeventhelper.service';
import { TaskEventReactiveService, DeletableTaskEvent } from 'app/services/taskeventreactive.service';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/distinct';

@Component({
  selector: 'app-taskeventlist',
  templateUrl: './taskeventlist.component.html',
  styleUrls: ['taskeventlist.component.scss']
})
export class TaskEventListComponent {

  task$: Observable<Array<DeletableTaskEvent>>;

  private currentTask: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(
    private taskEventHelper: TaskEventHelperService,
    private taskEventReactiveService: TaskEventReactiveService
  ) {
    this.task$ = this.taskEventReactiveService.tasks;
  }

  deleteTask(taskId: string): void {
    this.show(taskId);
    this.taskEventHelper.deleteTask(taskId);
  }

  show(taskId: string): void {
    this.currentTask.next(taskId);
  }

  get currentTaskEvent$(): Observable<Array<TaskEvent>> {
    return combineLatest(
      this.taskEventReactiveService.taskEvents,
      this.currentTask.asObservable(),
      (taskEvents: TaskEvent[], currentTask: string) => taskEvents.filter((taskEvent: TaskEvent) => taskEvent.id === currentTask)
    );
  }

  class(event: TaskEvent): string {
    switch (event.eventType) {
      case 'create':
        return 'list-group-item-info';
      case 'delete':
        return 'list-group-item-danger';
      case 'complete':
        return 'list-group-item-success';
      case 'assign':
        return 'list-group-item-warning';
      case 'claim':
        return 'list-group-item-light';
      case 'unclaim':
        return 'list-group-item-secondary';
      case 'mark-complete':
        return 'list-group-item-primary';
      default:
        return '';
    }
  }

  toFieldSet(payload: any): object[] {
    return toFieldSet(payload);
  }
}

function toFieldSet(payload: any): object[] {
  const payloadProps = Object.keys(payload);
  const result = [];
  for (const prop of payloadProps) {
    if (isValue(payload[prop])) {
      if (isObject(payload[prop])) {
        result.push({ name: prop, value: toFieldSet(payload[prop]) });
      } else {
        result.push({ name: prop, value: payload[prop] });
      }

    }
  }
  return result;
}


function isValue(value?: any): Boolean {
  return value && (!Array.isArray(value) || value.length > 0);
}

function isObject(value?: any): Boolean {
  if (value === null) {
    return false;
  }
  return ( (typeof value === 'function') || (typeof value === 'object') );
}
