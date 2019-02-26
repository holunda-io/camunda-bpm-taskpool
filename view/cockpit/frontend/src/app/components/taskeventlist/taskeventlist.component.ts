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

  toFieldSet(payload: any): PayloadEntry[] {
    return toFieldSet(payload);
  }
}

interface PayloadEntry {
  readonly name: string;
  readonly value: any;
  readonly type: string;
}

class ScalarPayloadEntry implements PayloadEntry {
  readonly type = 'scalar';

  constructor(readonly name: string, readonly value: any) {
  }
}

class ComplexPayloadEntry implements PayloadEntry {
  readonly type = 'complex';

  constructor(readonly name: string, readonly value: PayloadEntry[]) {
  }
}

function toFieldSet(payload: any): PayloadEntry[] {
  const payloadProps = Object.keys(payload);
  const result: PayloadEntry[] = [];
  for (const prop of payloadProps) {
    const value = payload[prop];
    if (isValue(value)) {
      if (isObject(value)) {
        result.push(new ComplexPayloadEntry(prop, toFieldSet(value)));
      } else {
        result.push(new ScalarPayloadEntry(prop, value));
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
