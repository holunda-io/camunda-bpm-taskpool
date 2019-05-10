import {Injectable, OnDestroy} from '@angular/core';
import {ProcessService} from 'tasklist/services';
import {ProcessDefinition, UserProfile} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Subscription} from 'rxjs/internal/Subscription';
import {ProfileHelperService} from 'app/services/profile.helper.service';


@Injectable()
export class ProcessHelperService implements OnDestroy {

  private processesSubject: BehaviorSubject<Array<ProcessDefinition>> = new BehaviorSubject<Array<ProcessDefinition>>([]);
  private userSubscription: Subscription;
  private processSubscription: Subscription;
  private currentUserIdentifier: string = this.profileHelperService.none();

  constructor(
    private processService: ProcessService,
    private profileHelperService: ProfileHelperService
  ) {
    this.userSubscription = this.profileHelperService.currentUserIdentifier$.subscribe(user => {
      this.currentUserIdentifier = user;
      this.reload();
    });
  }

  get processes() {
    return this.processesSubject.asObservable();
  }

  reload(): void {

    if (this.currentUserIdentifier === this.profileHelperService.none()) {
      // load only if a real user is set.
      return;
    }

    this.processSubscription = this.processService.getStartableProcesses(this.currentUserIdentifier)
      .subscribe((response: Array<ProcessDefinition>) => {
      this.processesSubject.next(response);
    }, (error) => {
      console.log('Error loading processes', error);
    });
  }


  ngOnDestroy() {
    this.processSubscription.unsubscribe();
    this.userSubscription.unsubscribe();
  }

}
