import { Component } from '@angular/core';
import { TaskEvent } from 'cockpit/model/taskEvent';
import { TaskEventHelperService } from 'app/services/taskeventhelper.service';

@Component({
  selector: 'app-taskeventlist',
  templateUrl: './taskeventlist.component.html',
  styleUrls: ['taskeventlist.component.scss']
})
export class TaskEventListComponent {

  tasks = new Map<string, Array<TaskEvent>>();
  collapseStatus = new Map<string, Boolean>();

  constructor(
    private taskEventHelper: TaskEventHelperService
  ) {
    this.subscribe();
  }

  taskEvent(taskId: string) {
    return this.tasks.get(taskId).sort(
      (event1: TaskEvent, event2: TaskEvent) => this.getTime(event2.created) - this.getTime(event1.created)
    );
  }

  reload() {
    this.taskEventHelper.reload();
  }

  deleteTask(taskId: string) {
    this.taskEventHelper.deleteTask(taskId);
  }

  deletable(taskId: string): Boolean {
    return this.tasks.get(taskId).filter((task) => task.eventType === 'delete').length === 0;
  }

  taskIds() {
    return Array.from(this.tasks.keys());
  }

  toggleCollapse(taskId: string) {
    this.collapseStatus.set(taskId, !this.collapseStatus.get(taskId));
  }

  collapseClass(event: TaskEvent) {
  if (this.collapseStatus.get(event.id)) {
      return '';
    } else {
      return 'collapse';
    }
  }

  class(event: TaskEvent) {
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
    }
  }


  toFieldSet(payload: any) {
    const payloadProps = Object.keys(payload);
    const result = [];
    for (const prop of payloadProps) {
      if (this.isValue(payload[prop])) {
        if (this.isObject(payload[prop])) {
          result.push({ name: prop, value: this.toFieldSet(payload[prop]) });
        } else {
          result.push({ name: prop, value: payload[prop] });
        }

      }
    }
    return result;
  }

  subscribe() {
    this.taskEventHelper.tasks.subscribe((taskEvents) => {
      this.tasks.clear();
      taskEvents.forEach(
        taskEvent => {
          let tasksForId = this.tasks.get(taskEvent.id);
          if (tasksForId === undefined) {
            tasksForId = new Array<TaskEvent>();
            this.tasks.set(taskEvent.id, tasksForId);
          }
          tasksForId.push(taskEvent);
        }
      );

      this.collapseStatus.clear();

      this.taskIds().forEach(
        (key) => {
          this.collapseStatus.set(key, this.deletable(key));
        }
      );

    });
  }

  private isValue(value): Boolean {
    return value && (!Array.isArray(value) || value.length > 0);
  }

  private getTime(date?: Date) {
    return date != null ? new Date(date.toString()).getTime() : 0;
  }

  private isObject(value): Boolean {
    if (value === null) {
      return false;
    }
    return ( (typeof value === 'function') || (typeof value === 'object') );
  }

}
