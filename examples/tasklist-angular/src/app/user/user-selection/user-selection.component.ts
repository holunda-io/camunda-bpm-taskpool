import {Component, OnInit} from '@angular/core';
import {UserStoreService} from '../state/user.store-service';
import {UserProfile} from '../state/user.reducer';
import {Observable} from 'rxjs';

@Component({
  selector: 'tasks-user-selection',
  templateUrl: './user-selection.component.html',
  styleUrls: ['./user-selection.component.scss']
})
export class UserSelectionComponent implements OnInit {

  private userIds$: Observable<string[]>;
  private currentProfile$: Observable<UserProfile>;

  constructor(private userStore: UserStoreService) {
  }

  ngOnInit(): void {
    this.userStore.loadAvailableUsers();

    this.userIds$ = this.userStore.availableUserIds$();
    this.currentProfile$ = this.userStore.currentUserProfile$();
  }

  setCurrentUser(userIdentifier: string) {
    this.userStore.selectUser(userIdentifier);
  }
}
