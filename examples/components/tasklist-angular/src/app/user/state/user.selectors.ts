import {UserProfile, UserState} from 'app/user/state/user.reducer';
import {createSelector} from '@ngrx/store';
import { UserInfo } from 'tasklist/models/user-info';

export interface StateWithUsers {
  user: UserState;
}

const selectFeature = (state: StateWithUsers) => state.user;

export const availableUsers = createSelector(
  selectFeature,
  (state: UserState): UserInfo[] => state.availableUsers
);

export const currentUserId = createSelector(
  selectFeature,
  (state: UserState): string => state.currentUserId
);

export const currentUserProfile = createSelector(
  selectFeature,
  (state: UserState): UserProfile => state.currentUserProfile
);
