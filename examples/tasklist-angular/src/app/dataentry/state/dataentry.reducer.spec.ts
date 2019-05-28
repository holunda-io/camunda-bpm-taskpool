import {LoadDataEntries, DataEntriesLoaded} from './dataentry.actions';
import {DataEntry, dataentryReducer, DataEntryState} from './dataentry.reducer';

describe('processReducer', () => {

  const initialState: DataEntryState = {
    dataEntries: []
  };

  it('updates available users', () => {
    // given:
    const dataEntries: DataEntry[] = [
      {name: 'foo', description: '', url: '', type: '', payload: {}},
      {name: 'bar', description: '', url: '', type: '', payload: {}}
    ];
    const action = new DataEntriesLoaded(dataEntries);

    // when:
    const newState = dataentryReducer(initialState, action);

    // then:
    expect(newState.dataEntries).toBe(dataEntries);
  });

  it('ignores other actions', () => {
    // given:
    const action = new LoadDataEntries();

    // when:
    const newState = dataentryReducer(initialState, action);

    // then:
    expect(newState).toBe(initialState);
  });
});
