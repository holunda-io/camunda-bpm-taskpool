import { Component, OnInit } from '@angular/core';
import {Profile, ProfileHelperService} from "app/services/profile.helper.service";

@Component({
  selector: 'tasks-user-selection',
  templateUrl: './user-selection.component.html',
  styleUrls: ['./user-selection.component.scss']
})
export class UserSelectionComponent implements OnInit {

  private userIds = [];
  private currentProfile: Profile = this.profileHelperService.noProfile;

  constructor(private profileHelperService: ProfileHelperService) {
  }

  ngOnInit(): void {
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
