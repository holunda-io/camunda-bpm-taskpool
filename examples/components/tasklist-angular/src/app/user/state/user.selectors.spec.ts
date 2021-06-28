import { availableUsers, currentUserId, currentUserProfile, StateWithUsers } from 'app/user/state/user.selectors';

describe('user selectors', () => {
  const state: StateWithUsers = {
    user: {
      currentUserId: 'kermit',
      currentUserProfile: {
        fullName: 'Kermit',
        userIdentifier: '1',
        username: 'kermit'
      },
      availableUsers: [{ id: '1', username: 'kermit' }, { id: '2', username: 'piggy' }]
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
    const availableUsersResult = availableUsers(state);

    // then:
    expect(availableUsersResult).toBe(state.user.availableUsers);
  });
});
