import { Injectable, OnInit, OnDestroy } from '@angular/core';
import { ProfileService } from 'tasklist/services';
import { UserProfile } from 'tasklist/models';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

@Injectable()
export class ProfileHelperService {

  private userProfileSubject: BehaviorSubject<UserProfile> = new BehaviorSubject<UserProfile>(this.none());

  constructor(private profileService: ProfileService) {
    this.profileService.getProfile().subscribe(
      (userProfile) => {
        this.userProfileSubject.next(userProfile);
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );
  }

  get userProfile() {
    return this.userProfileSubject.asObservable();
  }


  none(): UserProfile {
    return { username: 'none' };
  }
}
