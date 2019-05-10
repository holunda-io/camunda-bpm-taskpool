import {Component} from '@angular/core';
import {UserProfile} from 'tasklist/models';
import {BehaviorSubject} from 'rxjs';
import {Profile, ProfileHelperService} from 'app/services/profile.helper.service';

@Component({
  selector: 'tasks-root',
  templateUrl: './app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent {

  private userIds = [];
  private currentProfile: Profile = this.profileHelperService.noProfile;

  constructor(private profileHelperService: ProfileHelperService) {
    this.subscribe();
  }

  setCurrentUser(userIdentifier: string) {
    this.profileHelperService.setCurrentUser(userIdentifier);
  }

  private subscribe() {
    this.profileHelperService.currentProfile$.subscribe(
      (profile: Profile) => {
        this.currentProfile = profile;
        console.log('Loaded user profile for user', this.currentProfile.username);
      },
      (error) => {
        console.log('Error loading user profile', error);
      }
    );

    this.profileHelperService.allUserIds$.subscribe((userIds: Array<string>) => {
      this.userIds = userIds;
    });
  }
}

