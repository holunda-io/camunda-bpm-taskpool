import {dataEntries, StateWithDataEntries} from 'app/dataentry/state/dataentry.selectors';

describe('user selectors', () => {
  const state: StateWithDataEntries = {
    archive: {
      dataEntries: [
        {name: 'foo', description: '', url: '', type: '', payload: {}},
        {name: 'bar', description: '', url: '', type: '', payload: {}}
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
