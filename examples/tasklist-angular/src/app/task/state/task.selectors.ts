import {UserProfile, UserState} from 'app/user/state/user.reducer';
import {createSelector} from '@ngrx/store';

export interface StateWithUsers {
  user: UserState;
}

const selectFeature = (state: StateWithUsers) => state.user;

export const availableUserIds = createSelector(
  selectFeature,
  (state: UserState): string[] => state.availableUserIds
);

export const currentUserId = createSelector(
  selectFeature,
  (state: UserState): string => state.currentUserId
);

export const currentUserProfile = createSelector(
  selectFeature,
  (state: UserState): UserProfile => state.currentUserProfile
);
