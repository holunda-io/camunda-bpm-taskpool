import { Injectable } from '@angular/core';
import { ProfileService } from 'tasklist/services';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { AvailableUsersLoadedAction, LoadUserProfileAction, SelectUserAction, UserActionTypes, UserProfileLoadedAction } from './user.actions';
import { filter, flatMap, map, withLatestFrom } from 'rxjs/operators';
import { UserInfo, UserProfile as UserDto } from 'tasklist/models';
import { UserProfile } from './user.reducer';
import { TitleCasePipe } from '@angular/common';
import { UserStoreService } from 'app/user/state/user.store-service';

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
    map((users: UserInfo[]) => new AvailableUsersLoadedAction(users))
  );

  @Effect()
  loadInitialUser$ = this.actions$.pipe(
    ofType<AvailableUsersLoadedAction>(UserActionTypes.AvailableUsersLoaded),
    withLatestFrom(this.userStore.userId$()),
    filter(([_, userId]) => !userId),
    map(([action, _]) => action.payload),
    map((users) => new SelectUserAction(Object.keys(users)[0]))
  );

  @Effect()
  selectUser$ = this.actions$.pipe(
    ofType(UserActionTypes.SelectUser),
    map((action: SelectUserAction) => action.payload),
    filter(userId => !!userId),
    map(userId => new LoadUserProfileAction(userId))
  );

  @Effect()
  loadUserProfile$ = this.actions$.pipe(
    ofType(UserActionTypes.LoadUserProfile),
    map((action: LoadUserProfileAction) => action.payload),
    flatMap((userId) => this.profileService.getProfile({ 'X-Current-User-ID': userId }).pipe(
      map((profile) => mapFromDto(profile, userId))
    )),
    map((profile) => new UserProfileLoadedAction(profile))
  );
}

function mapFromDto(profile: UserDto, userId: string): UserProfile {
  return {
    userIdentifier: userId,
    username: profile.username,
    fullName: new TitleCasePipe().transform(profile.username)
  };
}
