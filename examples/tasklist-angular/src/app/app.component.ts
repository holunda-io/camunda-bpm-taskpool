import {Component} from '@angular/core';
import { ProfileService } from 'tasklist/services';
import { UserProfile } from 'tasklist/models';
import { BehaviorSubject, Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: [ 'app.component.scss' ]
})
export class AppComponent {

  private userProfile: BehaviorSubject<UserProfile> = new BehaviorSubject<UserProfile>({username: ''});

  constructor(private profileService: ProfileService) {
    this.subscribe();
  }

  get user(): User {
    const username = this.userProfile.getValue().username;
    return {
      username: username,
      avatar: 'https://mdbootstrap.com/img/Photos/Avatars/avatar-13.jpg',
      fullname: this.capitalize(username)
    };
  }

  private subscribe() {
    this.profileService.getProfile().subscribe(
      (profile: UserProfile) => {
        this.userProfile.next(profile);
        console.log('Loaded user profile for user', profile.username);
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );
  }

  private capitalize(value: string): string {
    if (value == null) {
      return null;
    }
    return value.charAt(0).toUpperCase() + value.substring(1, value.length);
  }
}

export class User {
  fullname: string;
  avatar: string;
  username: string;
}
