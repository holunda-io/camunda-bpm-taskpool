import {Component} from '@angular/core';
import {UserProfile} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs';
import {ProfileHelperService} from 'app/services/profile.helper.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent {

  private currentUser: BehaviorSubject<User> = new BehaviorSubject<User>({fullName: ''});
  private userIds = [];

  constructor(private profileHelperService: ProfileHelperService) {
    this.subscribe();
  }

  get user(): User {
    return this.currentUser.getValue();
  }

  setCurrentUser(userIdentifier: string) {
    this.profileHelperService.setCurrentUser(userIdentifier);
  }

  private subscribe() {
    this.profileHelperService.currentUserProfile$.subscribe(
      (profile: UserProfile) => {
        const fullName = this.capitalize(profile.username);
        this.currentUser.next({fullName: fullName});
        console.log('Loaded user profile for user', fullName);
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );

    this.profileHelperService.allUserIds$.subscribe((userIds: Array<string>) => {
      this.userIds = userIds;
    });
  }

  private capitalize(value: string): string {
    if (value == null) {
      return null;
    }
    return value.charAt(0).toUpperCase() + value.substring(1, value.length);
  }
}

export class User {
  fullName: string;
}
