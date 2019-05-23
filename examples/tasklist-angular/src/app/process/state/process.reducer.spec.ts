import {LoadStartableProcessDefinitions, StartableProcessDefinitionsLoaded} from './process.actions';
import {ProcessDefinition, processReducer, ProcessState} from './process.reducer';

describe('processReducer', () => {

  const initialState: ProcessState = {
    startableProcesses: []
  };

  it('updates available users', () => {
    // given:
    const procDefs: ProcessDefinition[] = [
      {name: 'foo', description: '', url: ''},
      {name: 'bar', description: '', url: ''}
    ];
    const action = new StartableProcessDefinitionsLoaded(procDefs);

    // when:
    const newState = processReducer(initialState, action);

    // then:
    expect(newState.startableProcesses).toBe(procDefs);
  });

  it('ignores other actions', () => {
    // given:
    const action = new LoadStartableProcessDefinitions();

    // when:
    const newState = processReducer(initialState, action);

    // then:
    expect(newState).toBe(initialState);
  });
});
