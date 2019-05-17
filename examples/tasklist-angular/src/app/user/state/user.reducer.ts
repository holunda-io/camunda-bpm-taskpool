import {UserActions, UserActionTypes} from './user.actions';

export interface UserProfile {
  userIdentifier: string;
  username: string;
  fullName: string;
}

export interface UserState {
  availableUserIds: string[];
  currentUserProfile: UserProfile;
}

const initialState: UserState = {
  availableUserIds: [],
  currentUserProfile: {
    userIdentifier: '',
    username: '',
    fullName: ''
  }
};

export function userReducer(state: UserState = initialState, action: UserActions): UserState {
  switch (action.type) {

    case UserActionTypes.AvailableUsersLoaded:
      return {
        ...state,
        availableUserIds: action.payload
      };

    case UserActionTypes.UserProfileLoaded:
      return {
        ...state,
        currentUserProfile: action.payload
      };

    default:
      return state;
  }
}
