import {startableProcesses, StateWithProcesses} from 'app/process/state/process.selectors';

describe('user selectors', () => {
  const state: StateWithProcesses = {
    process: {
      startableProcesses: [
        {key: 'foo'},
        {key: 'bar'}
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
