import {UserEffects} from './user.effects';
import {Action} from '@ngrx/store';
import {of} from 'rxjs';
import {Actions} from '@ngrx/effects';
import {ProfileService} from 'tasklist/services';
import {LoadAvailableUsersAction, LoadUserProfileAction, SelectUserAction} from 'app/user/state/user.actions';
import {UserProfile} from 'app/user/state/user.reducer';
import {UserStoreService} from 'app/user/state/user.store-service';
import {createStoreServiceMock} from '@ngxp/store-service/testing';
import { UserInfo } from 'tasklist/models';

describe('UserEffects', () => {

  let profileService: ProfileService;
  let userStore: UserStoreService;

  beforeEach(() => {
    profileService = new ProfileService(null, null);
    // default user store to be overridden in test if needed.
    userStore = createStoreServiceMock(UserStoreService);
  });

  function effectsFor(action: Action): UserEffects {
    return new UserEffects(profileService, userStore, new Actions(of(action)));
  }

  it('should load available users', (done) => {
    // given:
    const action = new LoadAvailableUsersAction();
    const usersList = [{ id: '1', username: 'foo'}, { id: '2', username: 'bar'}];
    const serviceSpy = spyOn(profileService, 'getUsers').and.returnValue(of(usersList));

    // when:
    effectsFor(action).loadAvailableUserIds$.subscribe((newAction) => {
      expect(serviceSpy).toHaveBeenCalled();
      expect(newAction.payload).toEqual(usersList);
      done();
    });
  });

  it('should trigger a user load on user selection', (done) => {
    // given:
    const user = 'foo';
    const action = new SelectUserAction(user);

    // when:
    effectsFor(action).selectUser$.subscribe(newAction => {
      expect(newAction).toEqual(new LoadUserProfileAction(user));
      done();
    });
  });

  it('should load a user profile', (done) => {
    // given:
    const userId = 'foo';
    const user: UserProfile = {
      username: 'foo',
      userIdentifier: 'foo',
      fullName: 'Foo'
    };
    const action = new LoadUserProfileAction(userId);
    spyOn(profileService, 'getProfile').and.returnValue(of(user));

    // when:
    effectsFor(action).loadUserProfile$.subscribe((newAction) => {
      expect(newAction.payload).toEqual(user);
      done();
    });
  });
});
