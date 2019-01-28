import { Injectable, Inject, NgZone, OnDestroy } from '@angular/core';
import { EventSourcePolyfill } from 'event-source-polyfill';
import { Observable } from 'rxjs/Observable';
import { TaskEvent } from 'cockpit/model/taskEvent';
import { BASE_PATH } from 'cockpit/variables';
import { BehaviorSubject } from 'rxjs';
import { Task } from 'cockpit';

@Injectable()
export class TaskEventReactiveService implements OnDestroy {

  private eventSource: EventSource;
  private events: BehaviorSubject<Array<TaskEvent>> = new BehaviorSubject<Array<TaskEvent>>([]);

  constructor(@Inject(BASE_PATH) private basePath: string, private zone: NgZone) {
    this.eventSource = this.createEventSource();
  }

  ngOnDestroy() {
    this.eventSource.close();
  }

  get tasks(): Observable<Array<DeletableTaskEvent>> {
    return this.events.asObservable().map((taskEvents: TaskEvent[]) => uniqueDeletable(sortedTaskEvents(taskEvents)));
  }

  get taskEvents(): Observable<Array<TaskEvent>> {
    return this.events.asObservable().map((taskEvents: TaskEvent[]) => sortedTaskEvents(taskEvents));
  }

  private createEventSource(): EventSourcePolyfill {
    const eventSource = new EventSourcePolyfill(`${this.basePath}/task-events`, { });
    eventSource.onmessage = (message: any) => {
      const event: TaskEvent = JSON.parse(message.data);
      const events = this.events.value;
      // console.log('Message:', message);
      this.zone.run(() => this.events.next(events.concat(event)));
    };
    eventSource.onerror = (err: any) => {
      console.log('Error in EventSource', err);
      this.events.error('Error in event source.');
    };
    return eventSource;
  }
}

export class DeletableTaskEvent implements TaskEvent {

  deletable: Boolean = false;
  id?: string;
  eventType?: string;
  created?: Date;
  task: Task;

  constructor(taskEvent: TaskEvent) {
    this.task = taskEvent.task;
    this.id = taskEvent.id;
    this.created = taskEvent.created;
    this.eventType = taskEvent.eventType;
    this.deletable = isDeletable(taskEvent);
  }
}

function sortedTaskEvents(taskEvents: TaskEvent[]): TaskEvent[] {
  return taskEvents.sort( (k1: TaskEvent, k2: TaskEvent) => getSeconds(k1.created) - getSeconds(k2.created) );
}

function uniqueDeletable(taskEvents: TaskEvent[]): DeletableTaskEvent[] {
  return taskEvents
    .map( (value: TaskEvent) => new DeletableTaskEvent(value))
    .filter( (value: DeletableTaskEvent, index: number, array: DeletableTaskEvent[]) => onlyUniqueDeletable(value, index, array));
}

function onlyUniqueDeletable(value: DeletableTaskEvent, index: Number, self: DeletableTaskEvent[]): Boolean {
  return self.findIndex((indexedTask: DeletableTaskEvent) => {
    // indexed event is the first event -> will be returned representing this task id
    // store the deletable information in it
    if (!isDeletable(value)) {
      indexedTask.deletable = false;
    }
    return indexedTask.id === value.id;
  }) === index;
}

function isDeletable(value: TaskEvent): Boolean {
  return value.eventType !== 'delete' && value.eventType !== 'complete';
}

function getSeconds(date?: Date): number {
  return date != null ? new Date(date.toString()).getTime() : 0;
}
