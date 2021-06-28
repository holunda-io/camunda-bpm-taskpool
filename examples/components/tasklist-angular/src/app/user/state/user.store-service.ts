import {Injectable} from '@angular/core';
import {Dispatch, Select, StoreService} from '@ngxp/store-service';
import {UserProfile, UserState} from './user.reducer';
import {availableUsers, currentUserId, currentUserProfile} from './user.selectors';
import {Observable} from 'rxjs';
import {LoadAvailableUsersAction, SelectUserAction} from './user.actions';
import {first} from 'rxjs/operators';
import { UserInfo } from 'tasklist/models/user-info';

@Injectable()
export class UserStoreService extends StoreService<UserState> {

  @Select(availableUsers)
  availableUsers$: () => Observable<UserInfo[]>;

  @Select(currentUserId)
  userId$: () => Observable<string>;

  @Select(currentUserProfile)
  currentUserProfile$: () => Observable<UserProfile>;

  @Dispatch(LoadAvailableUsersAction)
  loadAvailableUsers: () => void;

  @Dispatch(SelectUserAction)
  selectUser: (string) => void;

  loadInitialUser(): void {
    this.userId$().pipe(first()).subscribe(id => this.selectUser(id));
  }
}
