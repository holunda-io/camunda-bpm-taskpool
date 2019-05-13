import {UserState} from "app/user/state/user.reducer";

interface StateWithUsers {
  user: UserState
}

export const availableUserIds = (state: StateWithUsers): string[] => state.user.availableUserIds;

export const currentUserProfile = (state: StateWithUsers) => state.user.currentUserProfile;
