import {Injectable} from '@angular/core';
import {ProfileService} from 'tasklist/services';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class ProfileHelperService {

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

  get currentProfile$() {
    return this.currentProfile.asObservable();
  }

  get allUserIds$() {
    return this.userIds.asObservable();
  }

  noProfile: Profile = {
    userIdentifier: 'NO_ID',
    username: 'none',
    fullName: 'Unknown'
  };

  private currentProfile: BehaviorSubject<Profile> = new BehaviorSubject<Profile>(this.noProfile);
  private userIds: BehaviorSubject<Array<String>> = new BehaviorSubject<Array<String>>([]);

  private static capitalize(value: string): string {
    if (value == null) {
      return null;
    }
    return value.charAt(0).toUpperCase() + value.substring(1, value.length);
  }

  setCurrentUser(userIdentifier: string) {
    this.loadProfile(userIdentifier);
  }

  loadProfile(userIdentifier: string) {
    this.profileService.getProfile(userIdentifier).subscribe(
      (userProfile) => {
        this.currentProfile.next({
          userIdentifier: userIdentifier,
          username: userProfile.username,
          fullName: ProfileHelperService.capitalize(userProfile.username)
        });
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );
  }

}

export class Profile {
  userIdentifier: string;
  username: string;
  fullName: string;
}

