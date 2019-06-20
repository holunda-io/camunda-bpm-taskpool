import {DataentryEffects} from './dataentry.effects';
import {Action} from '@ngrx/store';
import {of} from 'rxjs';
import {Actions} from '@ngrx/effects';
import {ArchiveService} from 'tasklist/services';
import {UserStoreService} from 'app/user/state/user.store-service';
import {createStoreServiceMock} from '@ngxp/store-service/testing';
import {DataEntriesLoaded, LoadDataEntries} from 'app/dataentry/state/dataentry.actions';
import {DataEntry} from 'app/dataentry/state/dataentry.reducer';

describe('DataEntryEffects', () => {

  let archiveService: ArchiveService;
  let userStore: UserStoreService;

  beforeEach(() => {
    archiveService = new ArchiveService(null, null);
    // default user store to be overridden in test if needed.
    userStore = createStoreServiceMock(UserStoreService,
      {userId$: 'kermit'});
  });

  function effectsFor(action: Action): DataentryEffects {
    return new DataentryEffects(archiveService, userStore, new Actions(of(action)));
  }

  it('should load available users', (done) => {
    // given:
    const action = new LoadDataEntries();
    const dataEntriesDtos: Array<DataEntry> = [
      {name: 'foo', description: '', url: '', type: 'type', payload: {}},
      {name: 'bar', description: '', url: '', type: 'type2', payload: {}}
    ];
    const serviceSpy = spyOn(archiveService, 'getBosResponse').and.returnValue(of({body: dataEntriesDtos, headers: {}}));

    // when:
    effectsFor(action).loadDataEntries$.subscribe((newAction) => {
      expect(newAction).toEqual(new DataEntriesLoaded([
        {name: 'foo', description: '', url: '', type: 'type', payload: {}},
        {name: 'bar', description: '', url: '', type: 'type2', payload: {}}
      ]));
      done();
    });
  });
});
