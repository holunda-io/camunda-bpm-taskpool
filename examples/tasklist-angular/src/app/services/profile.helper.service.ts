import {Injectable} from '@angular/core';
import {ProfileService} from 'tasklist/services';
import {UserProfile} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class ProfileHelperService {

  private noneUserIdentifier = 'NOT_A_USER_ID';
  private noneUserProfile: UserProfile = {username: 'none'};

  private currentUserIdentifier: BehaviorSubject<string> = new BehaviorSubject<string>(this.noneUserIdentifier);
  private currentUserProfile: BehaviorSubject<UserProfile> = new BehaviorSubject<UserProfile>(this.noneUserProfile);
  private userIds: BehaviorSubject<Array<String>> = new BehaviorSubject<Array<String>>([]);

  constructor(private profileService: ProfileService) {

    // TODO: check in local storage if the userIdentifier is set,
    // then load current user with loadProfile(userIdentifier)
    this.loadProfile(null);

    this.profileService.getUsers().subscribe((userIds) => {
      this.userIds.next(userIds);
    }, (error) => {
      console.log('Error loading users', error);
    });


  }

  get currentUserIdentifier$() {
    return this.currentUserIdentifier.asObservable();
  }

  get currentUserProfile$() {
    return this.currentUserProfile.asObservable();
  }

  get allUserIds$() {
    return this.userIds.asObservable();
  }


  none(): string {
    return this.noneUserIdentifier;
  }

  noneProfile(): UserProfile {
    return this.noneUserProfile;
  }

  setCurrentUser(userIdentifier: string) {
    this.loadProfile(userIdentifier);
  }

  loadProfile(userIdentifier: string) {
    this.profileService.getProfile(userIdentifier).subscribe(
      (userProfile) => {
        this.currentUserProfile.next(userProfile);
        this.currentUserIdentifier.next(userIdentifier);
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );
  }
}
