import { Component } from '@angular/core';
import { UserProfile, TaskWithDataEntries, Task } from 'tasklist/models';
import { TaskHelperService } from 'app/services/task.helper.service';
import { FilterService } from 'app/services/filter.service';
import { ProfileHelperService } from 'app/services/profile.helper.service';

@Component({
  selector: 'app-tasklist',
  templateUrl: './tasklist.component.html',
  styleUrls: ['tasklist.component.scss']
})
export class TasklistComponent {

  tasks: Array<TaskWithDataEntries> = [];
  userProfile: UserProfile;
  itemsPerPage: number;
  totalItems: any;
  page: number;
  currentDataTab = 'description';

  constructor(
    private taskHelper: TaskHelperService,
    private filterService: FilterService,
    private profileHelper: ProfileHelperService,
  ) {
    this.subscribe();
    this.page = this.filterService.page + 1;
    this.itemsPerPage = this.filterService.itemsPerPage;
  }

  claim($event, task: Task) {
    this.taskHelper.claim(task);
  }

  unclaim($event, task: Task) {
    this.taskHelper.unclaim(task);
  }

  reload() {
    this.taskHelper.reload();
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
    this.taskHelper.tasks.subscribe((tasks) => {
      this.tasks = tasks;
    });
    this.filterService.count.subscribe((count: number) => {
      this.totalItems = count;
    });
    this.profileHelper.userProfile.subscribe((profile: UserProfile) => {
      this.userProfile = profile;
    });
  }
}
