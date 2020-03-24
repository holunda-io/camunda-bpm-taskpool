import {LoadDataEntries, DataEntriesLoaded} from './dataentry.actions';
import {DataEntry, dataentryReducer, DataEntryState} from './dataentry.reducer';

describe('processReducer', () => {

  const initialState: DataEntryState = {
    dataEntries: []
  };

  it('updates available users', () => {
    // given:
    const dataEntries: DataEntry[] = [
      {name: 'foo', description: '', url: '', type: 'type', payload: {}, currentState: 'MY STATE', currentStateType: '', protocol: []},
      {name: 'bar', description: '', url: '', type: 'type2', payload: {}, currentState: 'MY STATE2', currentStateType: '', protocol: []}
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
