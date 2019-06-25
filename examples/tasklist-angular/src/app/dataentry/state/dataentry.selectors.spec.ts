import {dataEntries, StateWithDataEntries} from 'app/dataentry/state/dataentry.selectors';

describe('user selectors', () => {
  const state: StateWithDataEntries = {
    archive: {
      dataEntries: [
        {name: 'foo', description: '', url: '', type: 'type', payload: {}, state: 'MY STATE', stateType: '', protocol: []},
        {name: 'bar', description: '', url: '', type: 'type2', payload: {}, state: 'MY STATE2', stateType: '', protocol: []}
      ]
    }
  };

  it('should select data entries processes', () => {
    // when:
    const dataEntriesState = dataEntries(state);

    // then:
    expect(dataEntriesState).toBe(state.archive.dataEntries);
  });
});
