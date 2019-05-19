import {Injectable} from '@angular/core';
import {ProfileService} from 'tasklist/services';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {
  AvailableUsersLoadedAction,
  LoadUserProfileAction,
  SelectUserAction,
  UserActionTypes,
  UserProfileLoadedAction
} from './user.actions';
import {first, flatMap, map, tap} from 'rxjs/operators';
import {UserProfile as UserDto} from 'tasklist/models';
import {UserProfile} from './user.reducer';
import {TitleCasePipe} from '@angular/common';
import {UserStoreService} from 'app/user/state/user.store-service';

@Injectable()
export class UserEffects {

  public constructor(
    private profileService: ProfileService,
    private userStore: UserStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadAvailableUserIds$ = this.actions$.pipe(
    ofType(UserActionTypes.LoadAvailableUsers),
    flatMap(() => this.profileService.getUsers()),
    map((users) => new AvailableUsersLoadedAction(users))
  );

  @Effect()
  selectUser$ = this.actions$.pipe(
    ofType(UserActionTypes.SelectUser),
    map((action: SelectUserAction) => action.payload),
    map(userId => new LoadUserProfileAction(userId))
  );

  @Effect()
  loadUserProfile$ = this.actions$.pipe(
    ofType(UserActionTypes.LoadUserProfile),
    map((action: LoadUserProfileAction) => action.payload),
    flatMap((userId) => this.profileService.getProfile(userId).pipe(
      map((profile) => mapFromDto(profile, userId))
    )),
    map((profile) => new UserProfileLoadedAction(profile)),
  );

  // Must be defined below selectUser$ and following effects.
  @Effect({dispatch: false})
  loadInitialUserProfile$ = this.userStore.userId$().pipe(
    first(),
    tap((userId) => this.userStore.selectUser(userId))
  );
}

function mapFromDto(profile: UserDto, userId: string): UserProfile {
  return {
    userIdentifier: userId,
    username: profile.username,
    fullName: new TitleCasePipe().transform(profile.username)
  };
}
