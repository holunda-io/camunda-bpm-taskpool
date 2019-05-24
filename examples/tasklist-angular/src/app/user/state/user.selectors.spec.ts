import {availableUserIds, currentUserId, currentUserProfile, StateWithUsers} from 'app/user/state/user.selectors';

describe('user selectors', () => {
  const state: StateWithUsers = {
    user: {
      currentUserId: 'kermit',
      currentUserProfile: {
        fullName: 'Kermit',
        userIdentifier: 'kermit',
        username: 'kermit'
      },
      availableUserIds: ['kermit', 'piggy']
    }
  };

  it('should select current userId', () => {
    // when:
    const userId = currentUserId(state);

    // then:
    expect(userId).toBe(state.user.currentUserId);
  });

  it('should select current userProfile', () => {
    // when:
    const userProfile = currentUserProfile(state);

    // then:
    expect(userProfile).toBe(state.user.currentUserProfile);
  });

  it('should select available users', () => {
    // when:
    const availableUsers = availableUserIds(state);

    // then:
    expect(availableUsers).toBe(state.user.availableUserIds);
  });
});
