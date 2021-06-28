import { Component, OnInit } from '@angular/core';
import { UserStoreService } from '../state/user.store-service';
import { UserProfile } from '../state/user.reducer';
import { Observable } from 'rxjs';
import { UserInfo } from 'tasklist/models/user-info';

@Component({
  selector: 'tasks-user-selection',
  templateUrl: './user-selection.component.html',
  styleUrls: ['./user-selection.component.scss']
})
export class UserSelectionComponent implements OnInit {

  availableUsers$: Observable<UserInfo[]>;
  currentProfile$: Observable<UserProfile>;

  constructor(private userStore: UserStoreService) {
  }

  ngOnInit(): void {
    this.userStore.loadAvailableUsers();

    this.availableUsers$ = this.userStore.availableUsers$();
    this.currentProfile$ = this.userStore.currentUserProfile$();
  }

  setCurrentUser(userIdentifier: string) {
    this.userStore.selectUser(userIdentifier);
  }
}
