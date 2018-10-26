import {Component} from '@angular/core';
import {TaskHelperService} from 'app/services/task.helper.service';
import {TaskWithDataEntries, DataEntry, Task} from 'tasklist';

@Component({
  selector: 'app-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: ['tasklist.component.scss']
})
export class TasklistComponent {

  tasks: Array<TaskWithDataEntries> = [];
  itemsPerPage: number;
  totalItems: any;
  page: any;
  previousPage: any;
  currentDataTab = 'description';

  constructor(private taskHelper: TaskHelperService) {
    this.loadData();
    this.page = 1;
  }


  loadPage(page: number) {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.loadData();
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


  loadData() {
    this.taskHelper.tasks.subscribe((tasks) => {
      this.tasks = tasks;
    });
  }

      /*
    this.dataService.query({
      page: this.page - 1,
      size: this.itemsPerPage,
    }).subscribe(
      (res: Response) => this.onSuccess(res.json(), res.headers),
      (res: Response) => this.onError(res.json())
      )
    */

}
