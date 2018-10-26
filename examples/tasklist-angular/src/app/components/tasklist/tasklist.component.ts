import {Component} from '@angular/core';
import {TaskHelperService} from 'app/services/task.helper.service';
import {TaskWithDataEntries, DataEntry, Task} from 'tasklist';
import { FilterService } from 'app/services/filter.service';

@Component({
  selector: 'app-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: ['tasklist.component.scss']
})
export class TasklistComponent {

  tasks: Array<TaskWithDataEntries> = [];
  itemsPerPage: number;
  totalItems: any;
  page: number;
  currentDataTab = 'description';

  constructor(private taskHelper: TaskHelperService, private filterService: FilterService) {
    this.subscribe();
    this.page = this.filterService.page + 1;
    this.itemsPerPage = this.filterService.itemsPerPage;
  }


  loadPage(page: number) {
    if (this.page - 1 !== this.filterService.page) {
      this.filterService.page = this.page - 1;
      this.taskHelper.reload();
    }
  }

  toFieldSet(payload: any) {
    const payloadProps = Object.keys(payload);
    const result = [];
    for (const prop of payloadProps) {
      result.push({ name: prop, value: payload[prop] });
    }
    return result;
  }


  subscribe() {
    this.taskHelper.tasksSubject.subscribe((tasks) => {
      this.tasks = tasks;
    });
    this.filterService.countSubject.subscribe((count: number) => {
      this.totalItems = count;
    });
  }
}
