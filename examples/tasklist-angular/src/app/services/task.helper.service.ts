import {Injectable, OnDestroy} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Task, TaskWithDataEntries} from 'tasklist/models';
import {BehaviorSubject, Subscription} from 'rxjs';
import {UserStoreService} from 'app/user/state/user.store-service';
import {UserProfile} from 'app/user/state/user.reducer';
import {TaskStoreService} from 'app/task/state/task.store-service';


@Injectable()
export class TaskHelperService implements OnDestroy {

  private tasksSubject: BehaviorSubject<Array<TaskWithDataEntries>> = new BehaviorSubject<Array<TaskWithDataEntries>>([]);
  private sortSubscription: Subscription;
  private currentProfileSubscription: Subscription;
  private taskSubscription: Subscription;
  private currentProfile: UserProfile;

  constructor(
    private taskService: TaskService,
    private taskStore: TaskStoreService,
    private userStore: UserStoreService
  ) {

    this.currentProfileSubscription = this.userStore.currentUserProfile$().subscribe(profile => {
      this.currentProfile = profile;
    });
  }

  ngOnDestroy() {
    this.sortSubscription.unsubscribe();
    this.currentProfileSubscription.unsubscribe();
    this.currentProfileSubscription.unsubscribe();
  }

  claim(task: Task): void {
    console.log('Claiming task', task.id);
    this.taskService.claim({id: task.id, XCurrentUserID: this.currentProfile.userIdentifier}).subscribe(
      (response) => {
        // claim successful
        this.reload();
      },
      (error) => {
        console.log('Error claiming task', error);
      }
    );
  }

  unclaim(task: Task): void {
    if (!this.currentProfile) {
      return;
    }
    console.log('Un-claiming task', task.id);
    this.taskService.unclaim({id: task.id, XCurrentUserID: this.currentProfile.userIdentifier}).subscribe(
      (response) => {
        // claim successful
        this.reload();
      },
      (error) => {
        console.log('Error un-claiming task', error);
      }
    );
  }

  reload(): void {

    if (!this.currentProfile) {
      // load only if a real user is set.
      return;
    }

    if (this.taskSubscription) {
      this.taskSubscription.unsubscribe();
    }
  }
}
