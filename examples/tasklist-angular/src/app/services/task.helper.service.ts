import {Injectable, OnDestroy} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Task, TaskWithDataEntries, UserProfile} from 'tasklist/models';
import {StrictHttpResponse} from 'tasklist/strict-http-response';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Field, FilterService} from './filter.service';
import {Subscription} from 'rxjs';
import {ProfileHelperService} from 'app/services/profile.helper.service';


@Injectable()
export class TaskHelperService implements OnDestroy {

  private tasksSubject: BehaviorSubject<Array<TaskWithDataEntries>> = new BehaviorSubject<Array<TaskWithDataEntries>>([]);
  private sortSubscription: Subscription;
  private userSubscription: Subscription;
  private userProfileSubscription: Subscription;
  private taskSubscription: Subscription;
  private currentUserIdentifier: string = this.profileHelperService.none();
  private currentUserProfile: UserProfile = this.profileHelperService.noneProfile();

  constructor(
    private taskService: TaskService,
    private filterService: FilterService,
    private profileHelperService: ProfileHelperService
  ) {

    this.userSubscription = this.profileHelperService.currentUserIdentifier$.subscribe(userIdentifier => {
      this.currentUserIdentifier = userIdentifier;
      this.reload();
    });

    this.userProfileSubscription = this.profileHelperService.currentUserProfile$.subscribe(userProfile => {
      this.currentUserProfile = userProfile;
    });

    this.sortSubscription = this.filterService.columnSorted$.subscribe((fieldEvent: Field) => {
      this.reload();
    });


  }

  ngOnDestroy() {
    this.sortSubscription.unsubscribe();
    this.userSubscription.unsubscribe();
    this.userProfileSubscription.unsubscribe();
  }

  get tasks() {
    return this.tasksSubject.asObservable();
  }

  claim(task: Task): void {
    console.log('Claiming task', task.id);
    this.taskService.claim({id: task.id, XCurrentUserID: this.currentUserIdentifier}).subscribe(
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
    console.log('Un-claiming task', task.id);
    this.taskService.unclaim({id: task.id, XCurrentUserID: this.currentUserIdentifier}).subscribe(
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

    if (this.currentUserIdentifier === this.profileHelperService.none()) {
      // load only if a real user is set.
      return;
    }

    if (this.taskSubscription) {
      this.taskSubscription.unsubscribe();
    }
    this.taskSubscription = this.taskService.getTasksResponse({
      filter: this.filterService.filter,
      page: this.filterService.page,
      size: this.filterService.itemsPerPage,
      sort: this.filterService.getSort(),
      XCurrentUserID: this.currentUserIdentifier
    }).subscribe((response: StrictHttpResponse<Array<TaskWithDataEntries>>) => {
      this.tasksSubject.next(response.body);
      this.filterService.countUpdate(Number(response.headers.get('X-ElementCount')));
    }, (error) => {
      console.log('Error loading tasks', error);
    });

  }
}
