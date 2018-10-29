import { Injectable, OnInit, OnDestroy } from '@angular/core';
import { TaskService, TaskWithDataEntries } from 'tasklist';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { HttpResponse } from '@angular/common/http';
import { FilterService, Field } from './filter.service';
import { Subscribable, Subscription } from 'rxjs';

@Injectable()
export class TaskHelperService implements OnDestroy {

  private tasksSubject: BehaviorSubject<Array<TaskWithDataEntries>> = new BehaviorSubject<Array<TaskWithDataEntries>>([]);
  private sortSubscription: Subscription;

  constructor(private taskService: TaskService, private filterService: FilterService) {
    this.sortSubscription = this.filterService.columnSorted$.subscribe( (fieldEvent: Field) => {
      this.reload();
    });

    this.reload();
  }

  ngOnDestroy() {
    this.sortSubscription.unsubscribe();
  }

  get tasks() {
    return this.tasksSubject.asObservable();
  }

  reload(): void {
    this.taskService.getTasks(
      this.filterService.filter,
      this.filterService.page,
      this.filterService.itemsPerPage,
      this.filterService.getSort(),
      'response').subscribe((response: HttpResponse<Array<TaskWithDataEntries>>) => {
      this.tasksSubject.next(response.body);
      this.filterService.countUpdate(Number(response.headers.get('X-ElementCount')));
    }, (error) => {
      console.log('Error loading tasks', error);
    });
  }
}
