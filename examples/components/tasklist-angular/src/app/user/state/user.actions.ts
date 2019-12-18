import {Action} from '@ngrx/store';
import {UserProfile} from './user.reducer';

export enum UserActionTypes {
  LoadAvailableUsers = '[User] Load available ids',
  AvailableUsersLoaded = '[User] Available ids loaded',
  SelectUser = '[User] Select user',
  LoadUserProfile = '[User] Load profile',
  UserProfileLoaded = '[User] Profile loaded',
}

export class LoadAvailableUsersAction implements Action {
  readonly type = UserActionTypes.LoadAvailableUsers;

  constructor() {
  }
}

export class AvailableUsersLoadedAction implements Action {
  readonly type = UserActionTypes.AvailableUsersLoaded;

  constructor(public payload: {[key: string]: string}) {
  }
}

export class SelectUserAction implements Action {
  readonly type = UserActionTypes.SelectUser;

  constructor(public payload: string) {
  }
}

export class LoadUserProfileAction implements Action {
  readonly type = UserActionTypes.LoadUserProfile;

  constructor(public payload: string) {
  }
}

export class UserProfileLoadedAction implements Action {
  readonly type = UserActionTypes.UserProfileLoaded;

  constructor(public payload: UserProfile) {
  }
}

export type UserActions =
  | LoadAvailableUsersAction
  | AvailableUsersLoadedAction
  | SelectUserAction
  | LoadUserProfileAction
  | UserProfileLoadedAction;
