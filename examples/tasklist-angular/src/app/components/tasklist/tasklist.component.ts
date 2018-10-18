import {Component} from '@angular/core';
import {TaskHelperService} from 'app/services/task.helper.service';
import {TaskWithDataEntries} from 'tasklist';

@Component({
  selector: 'app-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: []
})
export class TasklistComponent {

  tasks: Array<TaskWithDataEntries> = [];

  constructor(private taskHelper: TaskHelperService) {
    this.taskHelper.tasks.subscribe((tasks) => {
      this.tasks = tasks;
    });
  }
}
