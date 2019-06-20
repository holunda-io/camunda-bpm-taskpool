import {Component, OnInit} from '@angular/core';
import {Task, TaskWithDataEntries} from 'tasklist/models';
import {UserStoreService} from 'app/user/state/user.store-service';
import {UserProfile} from 'app/user/state/user.reducer';
import {TaskStoreService} from 'app/task/state/task.store-service';
import {Observable} from 'rxjs';
import {itemsPerPage} from 'app/task/state/task.selectors';

@Component({
  selector: 'tasks-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: ['tasklist.component.scss']
})
export class TasklistComponent implements OnInit {

  itemsPerPage: number;
  totalItems: Observable<number>;
  page: Observable<number>;
  currentDataTab = 'description';
  currentProfile$: Observable<UserProfile>;
  tasks: Observable<TaskWithDataEntries[]>;

  constructor(
    private taskStore: TaskStoreService,
    private userStore: UserStoreService
  ) {}

  ngOnInit(): void {
    this.totalItems = this.taskStore.taskCount$();
    this.itemsPerPage = itemsPerPage;
    this.page = this.taskStore.selectedPage$();
    this.tasks = this.taskStore.tasks();
    this.currentProfile$ = this.userStore.currentUserProfile$();
  }

  claim($event, task: Task) {
    this.taskStore.claim(task);
  }

  unclaim($event, task: Task) {
    this.taskStore.unclaim(task);
  }

  reload() {
    this.taskStore.loadTasks();
  }

  loadPage(page: number) {
    this.taskStore.selectPage(page);
  }

  toFieldSet(payload: any) {
    return Object.keys(payload)
      .map(prop => ({name: prop, value: payload[prop]}));
  }
}
