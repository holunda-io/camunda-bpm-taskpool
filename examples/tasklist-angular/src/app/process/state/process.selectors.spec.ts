import {startableProcesses, StateWithProcesses} from 'app/process/state/process.selectors';

describe('user selectors', () => {
  const state: StateWithProcesses = {
    process: {
      startableProcesses: [
        {name: 'foo', description: '', url: ''},
        {name: 'bar', description: '', url: ''}
      ]
    }
  };

  it('should select startable processes', () => {
    // when:
    const processDefinitions = startableProcesses(state);

    // then:
    expect(processDefinitions).toBe(state.process.startableProcesses);
  });
});
