import { Injectable } from '@angular/core';
import { TaskService, TaskWithDataEntries } from 'tasklist';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { HttpResponse } from '@angular/common/http';
import { FilterService } from './filter.service';

@Injectable()
export class TaskHelperService {

  tasksSubject: BehaviorSubject<Array<TaskWithDataEntries>> = new BehaviorSubject<Array<TaskWithDataEntries>>([]);

  constructor(private taskService: TaskService, private filterService: FilterService) {
    this.reload();
  }

  get tasks() {
    return this.tasksSubject.asObservable();
  }

  reload(): void {
    this.taskService.getTasks(
      this.filterService.filter,
      this.filterService.page,
      this.filterService.itemsPerPage,
      this.filterService.sort,
      'response').subscribe((response: HttpResponse<Array<TaskWithDataEntries>>) => {
      this.tasksSubject.next(response.body);
      this.filterService.countSubject.next(Number(response.headers.get('X-ElementCount')));
    }, (error) => {
      console.log('Error loading tasks', error);
    });
  }
}
