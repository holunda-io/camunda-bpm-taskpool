import {Injectable, OnDestroy} from '@angular/core';
import {ProcessService} from 'tasklist/services';
import {ProcessDefinition} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Subscription} from 'rxjs/internal/Subscription';
import {Profile} from 'app/services/profile.helper.service';
import {UserStoreService} from "app/user/state/user.store-service";


@Injectable()
export class ProcessHelperService implements OnDestroy {

  private processesSubject: BehaviorSubject<Array<ProcessDefinition>> = new BehaviorSubject<Array<ProcessDefinition>>([]);
  private profileSubscription: Subscription;
  private processSubscription: Subscription;
  private currentProfile: Profile;

  constructor(
    private processService: ProcessService,
    private userStore: UserStoreService
  ) {
    this.profileSubscription = this.userStore.currentUserProfile$().subscribe(profile => {
      this.currentProfile = profile;
      this.reload();
    });
  }

  get processes() {
    return this.processesSubject.asObservable();
  }

  reload(): void {

    if (!this.currentProfile) {
      // load only if a real user is set.
      return;
    }

    this.processSubscription = this.processService.getStartableProcesses(this.currentProfile.userIdentifier)
      .subscribe((response: Array<ProcessDefinition>) => {
      this.processesSubject.next(response);
    }, (error) => {
      console.log('Error loading processes', error);
    });
  }

  ngOnDestroy() {
    this.processSubscription.unsubscribe();
    this.profileSubscription.unsubscribe();
  }

}
