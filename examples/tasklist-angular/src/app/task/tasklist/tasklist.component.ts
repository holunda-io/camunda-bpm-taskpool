import {Component} from '@angular/core';
import {Task, TaskWithDataEntries} from 'tasklist/models';
import {TaskHelperService} from 'app/services/task.helper.service';
import {FilterService} from 'app/services/filter.service';
import {UserStoreService} from 'app/user/state/user.store-service';
import {UserProfile} from 'app/user/state/user.reducer';
import {TaskStoreService} from 'app/task/state/task.store-service';
import {Observable} from 'rxjs';

@Component({
  selector: 'tasks-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: ['tasklist.component.scss']
})
export class TasklistComponent {

  itemsPerPage: number;
  totalItems: any;
  page: number;
  currentDataTab = 'description';
  currentProfile: UserProfile;
  tasks: Observable<TaskWithDataEntries[]>;

  constructor(
    private taskStore: TaskStoreService,
    private taskHelper: TaskHelperService,
    private filterService: FilterService,
    private userStore: UserStoreService
  ) {
    this.subscribe();
    this.page = this.filterService.page + 1;
    this.itemsPerPage = this.filterService.itemsPerPage;
    this.tasks = this.taskStore.tasks();
  }

  claim($event, task: Task) {
    this.taskHelper.claim(task);
  }

  unclaim($event, task: Task) {
    this.taskHelper.unclaim(task);
  }

  reload() {
    this.taskStore.loadTasks();
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
    this.filterService.count.subscribe((count: number) => {
      this.totalItems = count;
    });
    this.userStore.currentUserProfile$().subscribe(userProfile => {
      this.currentProfile = userProfile;
    });
  }
}
