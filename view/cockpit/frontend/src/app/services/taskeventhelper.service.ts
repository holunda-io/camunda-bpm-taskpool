import { Injectable } from '@angular/core';
import { TaskEventService, TaskEvent } from 'cockpit';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { HttpResponse } from '@angular/common/http';

@Injectable()
export class TaskEventHelperService {

  private tasksSubject: BehaviorSubject<Array<TaskEvent>> = new BehaviorSubject<Array<TaskEvent>>([]);

  constructor(private taskEventService: TaskEventService) {
    this.reload();
  }

  get tasks() {
    return this.tasksSubject.asObservable();
  }

  deleteTask(taskId: string): void {
    this.taskEventService.sendCommand(taskId, 'delete').subscribe(
      (response) => {
        this.reload();
      },
      (error) => {
        console.log('Error deleting task', error);
      }
    );
  }

  reload(): void {
    this.taskEventService.getTaskEvents().subscribe((events: Array<TaskEvent>) => {
      this.tasksSubject.next(events);
    }, (error) => {
      console.log('Error loading tasks', error);
    });
  }
}
