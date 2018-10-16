import {Injectable} from '@angular/core';
import {TaskService, TaskWithDataEntries} from 'tasklist';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class TaskHelperService {

  tasks: BehaviorSubject<Array<TaskWithDataEntries>> = new BehaviorSubject<Array<TaskWithDataEntries>>([]);

  constructor(private taskService: TaskService) {
    this.loadTasks();
  }

  private loadTasks(): void {
    const filter = [];
    this.taskService.getTasks(filter).subscribe((tasks: Array<TaskWithDataEntries>) => {
      this.tasks.next(tasks);
    }, (error) => {
      console.log('Error loading tasks', error);
    });
  }
}
